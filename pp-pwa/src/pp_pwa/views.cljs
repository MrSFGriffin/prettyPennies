(ns pp-pwa.views
  (:require
   ["semantic-ui-react" :as ui]
   [cljs.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent :refer [atom]]
   [pp-pwa.budget :as budget]
   [pp-pwa.styles :as styles]
   [pp-pwa.events :as events]
   [pp-pwa.routes :as routes]
   [pp-pwa.specs :as specs]
   [pp-pwa.subs :as subs]))

(def budget-item-border-colour-css (styles/colour :apple :system-teal :css))

(def budget-item-border-style
  (str "solid 1px " (styles/colour :apple :system-teal :css)))

(def button-style {:background-color (styles/colour :web :pink :css)
                   :padding-left "1.4em"
                   :padding-right "1.4em"
                   :color (styles/colour :web :white :css)})

(defn currency-str
  [amount]
  (.toLocaleString amount "sk-SK" #js {:style "currency" :currency "EUR"}))

(defn pink-button
  ([label] (pink-button label #()))
  ([label on-click] (pink-button label on-click nil))
  ([label on-click disabled]
   [:> ui/Button
    {:style button-style
     :disabled disabled
     :onClick on-click}
    label]))

(defn update-item-name
  "Updates an item name."
  [event set-item-name-key set-item-name-error-key]
  (let [value (-> event .-target .-value)]
    ; todo: move most of this to the set event
    (if (s/valid? ::specs/budget-item-name value)
      (do
        (re-frame/dispatch [set-item-name-key value])
        (re-frame/dispatch [set-item-name-error-key nil]))
      (re-frame/dispatch [set-item-name-error-key "Name required."]))))

(defn update-amount
  [event set-amount-key set-error-key]
  (let [value (* 100 (-> event .-target .-valueAsNumber))]
    (if (and ; todo: move most of this to the set event
         (-> event .-target .-validity .-valid)
         (s/valid? ::specs/amount value))
      (do
        (re-frame/dispatch [set-amount-key value])
        (re-frame/dispatch [set-error-key nil]))
      (re-frame/dispatch [set-error-key "0 or greater required."]))))

(defn delete-panel
  [delete-item-id]
  (let [view-mode @(re-frame/subscribe [::subs/view-mode])]
    [:div
     [:h3 "Delete?"]
     [pink-button "Yes"
      #(do
         (re-frame/dispatch [::events/cancel-editing])
         (re-frame/dispatch [::events/cancel-deleting])
         (re-frame/dispatch [::events/deselect-all])
         (re-frame/dispatch [::events/delete delete-item-id view-mode]))]
     [pink-button "No" #(re-frame/dispatch [::events/cancel-deleting])]]))

(defn edit-panel
  [item]
  (let [name-error @(re-frame/subscribe [::subs/edit-item-name-error])
        amount-error @(re-frame/subscribe [::subs/edit-item-amount-error])
        delete-item-id @(re-frame/subscribe [::subs/delete-item-id])
        new-binding 0
        view-mode @(re-frame/subscribe [::subs/view-mode])]
    [:div
     {:style {:padding-bottom "0.3em"
              :text-align "left"}}
     [:div
      (if delete-item-id
        (delete-panel delete-item-id)
        [pink-button "Delete" #(re-frame/dispatch [::events/deleting item])])]
     [:div
      [:> ui/Input
       {:label "Name"
        :auto-focus true
        :on-change #(update-item-name
                     %
                     ::events/set-edit-item-name
                     ::events/set-edit-item-name-error)
        :error (some? name-error)
        :default-value (:budget-item-name item)
        :style {:margin-top "1em"
                :margin-bottom "1em"
                :margin-left "0.1em"
                :width "11.5em"}}]
      (when (some? name-error)
        [:div
         {:style {:margin-bottom "1em"
                  :margin-left "0.2em"}}
         [:> ui/Label name-error]])
      [:> ui/Input
       {:label "Limit"
        :on-change #(update-amount
                     %
                     ::events/set-edit-item-amount
                     ::events/set-edit-item-amount-error)
        :error (some? amount-error)
        :step 0.01
        :type "number"
        :min new-binding
        :default-value (/ (get-in item [:limit :amount]) 100)
        :style {:margin-bottom "1em"
                :margin-left "0.1em"
                :width "12em"}}]
      (when (some? amount-error)
        [:div
         {:style {:margin-bottom "1em"}}
         [:> ui/Label amount-error]])]
     [pink-button "Cancel"
      #(do
         (re-frame/dispatch [::events/cancel-editing])
         (re-frame/dispatch [::events/cancel-deleting]))]
     [pink-button "Save"
      #(re-frame/dispatch [::events/edit item view-mode])
      (or (some? amount-error) (some? name-error))]]))

(defn update-spending-amount
  "Updates the new item limit amount."
  [event]
  (update-amount
   event
   ::events/set-spending-amount
   ::events/set-spending-amount-error))

(defn spend-panel
  [item]
  (let [amount-error @(re-frame/subscribe [::subs/spending-amount-error])]
    [:div
     {:style {:padding "0.5em"
              :text-align "left"}}
     [:> ui/Input
      {:label (get-in item [:spent :currency-code])
       :id "spend-amount-input"
       :step 0.01
       :auto-focus true
       :type "number"
       :on-change #(update-spending-amount %)
       :error (some? amount-error)
       :min 0
       :style {:padding-bottom "0.5em"}}]
     (when amount-error
       [:div
        [:> ui/Label
         amount-error]])
     [:div
      {:style {:padding-bottom "0.3em"
               :margin-top "0.5em"}}
      [pink-button "Cancel" #(re-frame/dispatch [::events/cancel-spending])]
      [pink-button "Spend" #(re-frame/dispatch [::events/spend])]]]))

(defn reset-panel
  [item]
  [:div
   {:style {:padding-bottom "0.5em"}}
   [:h4 "Reset spending?"]
   [pink-button "No" #(re-frame/dispatch [::events/toggle-reset-item])]
   [pink-button "Yes" #(re-frame/dispatch [::events/reset-item item])]])

(defn item-controls
  [item colour spending editing resetting]
  (let [view-mode @(re-frame/subscribe [::subs/view-mode])
        allow-spend (= view-mode :budget)
        reset-possible (> (get-in item [:spent :amount]) 0)]
    [:> ui/Grid.Row
     {:style {:margin-top "-0.1em"}}
     [:> ui/Grid.Column
      {:width 13
       :style {:margin-left "3em"
               :padding-left "0.3em"
               :padding-top "0.2em"
               :border-left (str "3px solid " (colour :css))}}
      (cond
        spending (spend-panel item)
        editing (edit-panel item)
        resetting (reset-panel item)
        :else [:div
               {:style {:padding-bottom "0.3em"}}
               [pink-button "Edit" #(re-frame/dispatch [::events/editing item])]
               (when allow-spend
                 [pink-button "Spend"
                  #(re-frame/dispatch [::events/spending item])])
               (when (and allow-spend reset-possible)
                 [pink-button "Reset"
                  #(re-frame/dispatch [::events/toggle-reset-item])])])]]))

(defn budget-item-row
  [item selected-item-id spending-item-id edit-item-id reset-item]
  (let [item-id (:budget-item-id item)
        spending (= item-id spending-item-id)
        editing (= item-id edit-item-id)
        resetting reset-item
        selected (= item-id selected-item-id)
        limit (-> item :limit :amount)
        spent (-> item :spent :amount)
        left (/ (- limit spent) 100)
        limit (/ limit 100)
        spent (/ spent 100)
        negative (< left 0)
        label (cond
                (not negative) ""
                negative "-"
                :else "")
        colour (item :colour)
        view-mode @(re-frame/subscribe [::subs/view-mode])
        planning (= :plan view-mode)]
    [:> ui/Grid.Row
     {:style {:padding-bottom 0
              :min-width "25em"
              :margin-bottom "-14px"}}
     [:> ui/Grid.Column
      {:width 1}]
     [:> ui/Grid.Column
      {:width 5
       :style {:padding-right 0}}
      [:div                             ; left of item row
       {:on-click (if (and selected (or spending editing)) #()
                      #(re-frame/dispatch [::events/select-item item]))
        :id (str "budget-item-" item-id)
        :style {:border-top budget-item-border-style
                :border-left (str "3px solid " (colour :css))
                :padding "0.4em"}}
       [:> ui/Grid
        [:> ui/Grid.Row
         [:> ui/Grid.Column
          {:text-align "left"
           :style {:font-size "1.2em"}}
          (:budget-item-name item)]]
        [:> ui/Grid.Row
         {:style {:padding 0}}
         [:> ui/Grid.Column
          {:text-align "left"
           :style (if planning
                    {:min-height "1em"}
                    {})}
          (when (not planning)
            [:> ui/Progress
             {:size "tiny"
              :total limit
              :value (min spent limit)
              :color (colour :name)}])]]]]]
     [:> ui/Grid.Column ; right of item row (but goes to far right, so background on chlidren)
      {:width 8
       :on-click (if (and selected (or spending editing))
                   #() #(re-frame/dispatch [::events/select-item item]))
       :style {:padding-left 0}
       :text-align "right"}
      [:> ui/Grid.Row
       {:style {:border-top budget-item-border-style
                :padding "0.4em"}}
       [:div
        {:style {:color (if (not negative) "black" "red")
                 :font-size "1.3em"}}
        (str label " "
             (-> item :spent :currency-code)
             (currency-str (Math/abs left)))]]
      (when (not planning)
        [:> ui/Grid.Row
         {:style {:padding-bottom "0.3em"
                  :padding-right "0.5em"}}
         [:div
          (str (-> item :limit :currency-code) (currency-str limit))]])]
     (when selected
       [item-controls item colour spending editing resetting])]))

(defn update-new-item-amount
  "Updates the new item limit amount."
  [event]
  (update-amount
   event
   ::events/set-new-item-amount
   ::events/set-new-item-amount-error))

(defn add-item-panel
  []
  (let [name-error @(re-frame/subscribe [::subs/new-item-name-error])
        amount-error @(re-frame/subscribe [::subs/new-item-amount-error])
        view-mode @(re-frame/subscribe [::subs/view-mode])]
    [:> ui/Grid.Row
     {:align "left"
      :style {:margin-top "0.7em"
              :padding-left "0.1em"}}
     [:> ui/Grid.Column
      [:div
       [:> ui/Input
        {:label "Name"
         :auto-focus true
         :id "item-name-input"
         :error (some? name-error)
         :name "add-item-name"
         :default-value ""
         :on-change #(update-item-name
                      %
                      ::events/set-new-item-name
                      ::events/set-new-item-name-error)
         :style {:max-width "80%"
                 :margin-top "1em"}}]
       (when name-error
         [:> ui/Label
          {:style {:margin-top "1em"}}
          name-error])]]
     [:> ui/Grid.Column
      {:style {:margin-top "1em"}}
      [:div
       [:> ui/Input
        {:label "Limit"
         :name "add-item-amount"
         :error (some? amount-error)
         :step 0.01
         :type "number"
         :min 0
         :on-change #(update-new-item-amount %)
         :style {:max-width "80%"}}]]
      (when amount-error
        [:> ui/Label
         {:style {:margin-top "0.5em"}}
         amount-error])]
     [:> ui/Grid.Column
      {:style {:margin-top "1em"}}
      (pink-button
       "Cancel" #(re-frame/dispatch [::events/toggle-adding-item]))
      (pink-button
       "Add"
       #(re-frame/dispatch [::events/add-item view-mode])
       (or name-error amount-error))]]))

(defn reset-all-panel
  []
  [:div
   {:style {:margin-top "2em"}}
   [:h4 "Reset spending on all items?"]
   [pink-button "No" #(re-frame/dispatch [::events/toggle-resetting-all])]
   [pink-button "Yes" #(re-frame/dispatch [::events/reset-all-items])]])

(defn budget-control-panel [budget]
  (let [adding-item @(re-frame/subscribe [::subs/adding-item])
        resetting-all @(re-frame/subscribe [::subs/resetting-all])
        any-spending (some #(> (get-in % [:spent :amount]) 0) budget)]
    [:div
     {:style {:margin-bottom "1em"
              :margin-top "1em"
              :padding "1em 1em 1em 0.1em"}}
     [:> ui/Grid
      (when (not (or adding-item resetting-all))
        [:div
         [:> ui/Grid.Column
          {:width 5
           :style {:margin-top "1em"
                   :margin-right "0.7em"}}
          (pink-button "Add"
                       #(re-frame/dispatch
                         [::events/toggle-adding-item]))
          (when (and (> (count budget) 0) any-spending)
            [:span
             {:style {:margin-left "1em"}}
             (pink-button
              "Reset all"
              #(re-frame/dispatch [::events/toggle-resetting-all]))])]])]
     (cond
       adding-item [add-item-panel]
       resetting-all [reset-all-panel])]))

(defn budget-list-panel [budget]
  (let [selected-item-id @(re-frame/subscribe [::subs/selected-item-id])
        spending-item-id @(re-frame/subscribe [::subs/spending-item-id])
        edit-item-id @(re-frame/subscribe [::subs/edit-item-id])
        reset-item @(re-frame/subscribe [::subs/reset-item])]
  [:> ui/Grid
   (map
    #(budget-item-row %
                      selected-item-id
                      spending-item-id
                      edit-item-id
                      reset-item)
    budget)
   ]))

(defn budget-panel
  [budget]
  (let [view-mode @(re-frame/subscribe [::subs/view-mode])
        total (budget/sum-limits budget)
        spend (budget/sum-spents budget)
        over-spend (if (< total spend)
                     (.abs js/Math (- total spend))
                     0)
        over-spent (> over-spend 0)
        over-spend (/ over-spend 100)
        total (/ total 100)]
    [:> ui/Grid
     (when (= view-mode :budget)
       [:> ui/Grid.Row
        {:centered true}
        [:> ui/Grid.Column
         {:width 14}
         [:> ui/Card
          [:> ui/Grid
           [:> ui/Grid.Row
            {:centered true}
            [:> ui/Grid.Column {:width 2}]
            [:> ui/Grid.Column
             {:width 7}
             [:h5 (currency-str total) [:p "Total"]]]
            [:> ui/Grid.Column
             {:width 7
              :style (if over-spent {:color "red"} {})}
             [:h5 (currency-str over-spend) [:p "Overspend"]]]]]]]])
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      [:div
       {:style {:height "17em"
                :max-height "17em"
                :overflow-x "hidden"
                :overflow-y "scroll"}}
       (budget-list-panel budget)]]
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      [:> ui/Grid.Column
       {:width 1}]
      [:> ui/Grid.Column
       {:width 13}
       (budget-control-panel budget)]]]))

(defn adjust-income-panel
  [income]
  (let [income-error @(re-frame/subscribe [::subs/income-error])]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:centered true}
      [:> ui/Grid.Column
       [:> ui/Input
        {:label "Income"
         :id "income-input"
         :on-change #(update-amount
                      %
                      ::events/set-income
                      ::events/set-income-error)
         :error (some? income-error)
         :step 0.01
         :type "number"
         :min 0
         :default-value income
         :style {:margin-bottom "1em"
                 :margin-left "1em"
                 :max-width "11em"}}]
       [:div
        {:style {:margin-bottom "1em"
                 :margin-left "1em"}}
        [pink-button "Save" #(re-frame/dispatch [::events/adjust-income])]
        [pink-button "Cancel" #(re-frame/dispatch [::events/stop-adjusting-income])]]]]]))

(defn plan-panel
  [plan]
  (let [income (/ (:income plan) 100)
        outgoing (/ (budget/sum-limits (:budget plan)) 100)
        balance (- income outgoing)
        minus (< balance 0)
        adjusting-income @(re-frame/subscribe [::subs/adjusting-income])]
    [:div
     [:> ui/Grid
      [:> ui/Grid.Row
       {:centered true
        :on-click
        (if adjusting-income
          #()
          #(re-frame/dispatch [::events/start-adjusting-income income]))}
       [:> ui/Grid.Column
        {:width 14}
        [:> ui/Card
         [:> ui/Grid
          [:> ui/Grid.Row
           {:centered true}
           [:> ui/Grid.Column
            {:width 5}
            [:h5 (currency-str income) [:p "Income"]]]
           [:> ui/Grid.Column
            {:width 5}
            [:h5 (currency-str outgoing) [:p "Outgoing"]]]
           [:> ui/Grid.Column
            {:width 5
             :text-align "left"}
            [:h5
             {:style {:color (if minus "red" "#737588")}}
             (currency-str balance) [:p "Balance"]]]]
          (when adjusting-income
            [:> ui/Grid.Row
             [:> ui/Grid.Column
              {:width 16}
              [adjust-income-panel income]]])]]]]]
     [budget-panel (:budget plan)]]))

(defn money-panel [budget plan view-mode]
  {:pre [(s/valid? ::specs/budget budget)]}
  (let [name (re-frame/subscribe [::subs/name])]
    [:> ui/Card
     {:centered true
      :style {:height "35.5em"}}
     [:h1 {:style {:margin "0.3em 0 0.5em 0"}} @name]
     [:> ui/Grid
      [:> ui/Grid.Row
       {:centered true}
       [:> ui/Grid.Column
        {:width 14}
        [:> ui/Tab
         {:panes [{:menuItem "Budget"}
                  {:menuItem "Bills"}]
          :defaultActiveIndex (cond
                                (= view-mode :budget) 0
                                (= view-mode :plan) 1)
          :onTabChange #(let [index (.-activeIndex %2)]
                          (cond
                            (= index 0) (re-frame/dispatch [::events/set-view-mode :budget])
                            (= index 1) (re-frame/dispatch [::events/set-view-mode :plan])))}]]]
      [:> ui/Grid.Row
       {:style {:margin-top "-1em"}}
       [:> ui/Grid.Column
        (cond
          (= view-mode :budget) [budget-panel budget]
          (= view-mode :plan) [plan-panel plan])]]]]))

(defn home-panel []
  (let [loading @(re-frame/subscribe [::subs/loading])
        budget @(re-frame/subscribe [::subs/coloured-budget])
        plan @(re-frame/subscribe [::subs/coloured-plan])
        view-mode @(re-frame/subscribe [::subs/view-mode])]
    (assert ::specs/budget budget)
    [:div
     {:class "outer-div"
      :style {:max-width "94%"
              :padding-left "6%"}}
     [:> ui/Grid
      {:class (styles/full-height)
       :text-align "center"
       :vertical-align "middle"}
      [:> ui/Grid.Column
       {:style (if loading
                 {:background-color "white"
                  :min-height "20%"
                  :max-width "40%"}
                 {:max-height "80%"
                  :overflow-y "auto"
                  :overflow-x "hidden"})}
       (if loading
         [:> ui/Loader
          {:active true
           :size "massive"}
          [:h1 "Loading"]]
         (money-panel budget plan view-mode))]]]))

(defmethod routes/panels :home-panel [] [home-panel])

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]
   [:div
    [:a {:on-click #(re-frame/dispatch [::events/navigate :home])}
     "go to Home Page"]]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    (routes/panels @active-panel)))

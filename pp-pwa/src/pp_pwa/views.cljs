(ns pp-pwa.views
  (:require
   ["semantic-ui-react" :as ui]
   ["chartist" :as c]
   [cljs.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent :refer [atom]]
   [pp-pwa.budget :as budget]
   [pp-pwa.styles :as styles]
   [pp-pwa.events :as events]
   [pp-pwa.routes :as routes]
   [pp-pwa.specs :as specs]
   [pp-pwa.subs :as subs]))

;(def budget-item-border-colour-css (styles/colour :apple :system-teal :css))

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

(defn budget-item-delete-panel
  []
  (let [item-id @(re-frame/subscribe [::subs/delete-item-id])
        view-mode @(re-frame/subscribe [::subs/view-mode])]
    [:> ui/Grid
     [:> ui/Grid.Row
      [:> ui/Grid.Column
       [:h3 "Delete?"]]]
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      [:> ui/Grid.Column
       {:width 6}
       [pink-button "No" #(re-frame/dispatch [::events/cancel-deleting])]]
      [:> ui/Grid.Column
       {:width 6}
       [pink-button "Yes"
        #(re-frame/dispatch [::events/delete item-id view-mode])]]]]))

(defn input-panel
  [options]
  {:pre [(if (s/valid? ::specs/input-panel-options options)
           true
           (do
             (js/console.log
              (expound.alpha/expound-str ::specs/input-panel-options options))
             nil))]}
  (let [error @(re-frame/subscribe [(:error-sub options)])]
    [:> ui/Grid
     [:> ui/Grid.Row
      [:> ui/Grid.Column
       {:width 12}
       [:> ui/Input
        (merge
         {
          :error (some? error)
          :style {:width "100%"}}
         (or (:input-options options) {}))]]]
     (when error
       [:> ui/Grid.Row
        {:style {:padding-top 0}
         :text-align "left"}
        [:> ui/Grid.Column
         [:> ui/Label error]]])]))

(defn budget-item-edit-panel
  [item]
  (let [item-id (:budget-item-id item)
        deleting (= item-id @(re-frame/subscribe [::subs/delete-item-id]))
        view-mode @(re-frame/subscribe [::subs/view-mode])
        name-error @(re-frame/subscribe [::subs/edit-item-name-error])
        amount-error @(re-frame/subscribe [::subs/edit-item-amount-error])]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      [:> ui/Grid.Column
       {:text-align "left"
        :width 12}
       (cond
         deleting [budget-item-delete-panel]
         :else [:a
                {:on-click #(re-frame/dispatch [::events/deleting item])}
                "Delete"])]]
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      [:> ui/Grid.Column
       {:width 12}
       [input-panel {:error-sub ::subs/edit-item-name-error
                     :input-options
                     {:auto-focus true
                      :default-value (:budget-item-name item)
                      :label "Name"
                      :on-change #(update-item-name
                                   %
                                   ::events/set-edit-item-name
                                   ::events/set-edit-item-name-error)}}]]]
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      [:> ui/Grid.Column
       {:width 12}
       [input-panel {:error-sub ::subs/edit-item-amount-error
                     :input-options
                     {:default-value (/ (get-in item [:limit :amount]) 100)
                      :label "Amount"
                      :min 0
                      :on-change #(update-amount
                                   %
                                   ::events/set-edit-item-amount
                                   ::events/set-edit-item-amount-error)
                      :step 0.01
                      :type "number"}}]]]
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      [:> ui/Grid.Column
       {:width 6}
       [pink-button "Cancel"
        #(re-frame/dispatch [::events/cancel-editing])]]
      [:> ui/Grid.Column
       {:width 6}
       [pink-button "Save"
        #(re-frame/dispatch [::events/edit item view-mode])
        (or (some? amount-error) (some? name-error))]]]]))

(defn update-spending-amount
  "Updates the new item limit amount."
  [event]
  (update-amount
   event
   ::events/set-spending-amount
   ::events/set-spending-amount-error))

(defn budget-item-reset-panel
  [item]
  [:> ui/Grid
   [:> ui/Grid.Row
    {:text-align "left"}
    [:> ui/Grid.Column
     [:h4 "Reset Spending?"]]]
   [:> ui/Grid.Row
    {:style {:padding-top 0}}
    [:> ui/Grid.Column
     {:width 5}
     [pink-button "No" #(re-frame/dispatch [::events/toggle-reset-item])]]
    [:> ui/Grid.Column
     {:width 5}
     [pink-button "Yes" #(re-frame/dispatch [::events/reset-item item])]]]])

(defn budget-item-spend-panel
  [item]
  (let [amount-error @(re-frame/subscribe [::subs/spending-amount-error])]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:style {:padding-bottom 0}}
      [:> ui/Grid.Column
       [:> ui/Input
        {:label (get-in item [:spent :currency-code])
         :id "spend-amount-input"
         :step 0.01
         :auto-focus true
         :placeholder "amount spent"
         :type "number"
         :on-change #(update-spending-amount %)
         :error (some? amount-error)
         :min 0
         :style {:padding-bottom "0.5em"}}]]]
     (when amount-error
       [:> ui/Grid.Row
        {:text-align "left"}
        [:> ui/Grid.Column
         [:> ui/Label amount-error]]])
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      [:> ui/Grid.Column
       {:width 6}
       [pink-button "Cancel" #(re-frame/dispatch [::events/cancel-spending])]]
      [:> ui/Grid.Column
       {:width 6}
       [pink-button "Spend" #(re-frame/dispatch [::events/spend])]]]]))

(defn budget-item-button-panel
  [item]
  (let [planning (= :plan @(re-frame/subscribe [::subs/view-mode]))
        reset-possible (and (> (get-in item [:spent :amount]) 0) (not planning))]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      (when (not planning)
        [:> ui/Grid.Column
         {:style {:padding-left "0.3em"}
          :width 6}
         [pink-button "Spend" #(re-frame/dispatch [::events/spending item])]])
      (when (not planning)
        [:> ui/Grid.Column {:width 1}])
      [:> ui/Grid.Column
       {:style (if planning {:padding-left "0.3em"} {})
        :vertical-align (if planning "top" "bottom")
        :width 4}
       (if planning
         [pink-button "Edit" #(re-frame/dispatch [::events/editing item])]
         [:a {:on-click #(re-frame/dispatch [::events/editing item])} "Edit"])]
      (when reset-possible
        [:> ui/Grid.Column
         {:vertical-align (if planning "top" "bottom")
          :width 3}
         [:a
          {:on-click #(re-frame/dispatch [::events/toggle-reset-item])}
          "Reset"]])]]))

(defn budget-item-amount-panel
  [item]
  (let [raw-limit (-> item :limit :amount)
        limit (/ raw-limit 100)
        raw-spent (-> item :spent :amount)
        left (/ (- raw-limit raw-spent) 100)
        negative (< left 0)]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:style {:padding-bottom "0.3em"}}
      [:> ui/Grid.Column
       {:style {:font-size "1.3em"
                :color (if negative "red" "black")}
        :text-align "right"}
        (currency-str left)]]
     [:> ui/Grid.Row
      {:style {:padding-top 0}}
      [:> ui/Grid.Column
       {:text-align "right"}
       (currency-str limit)]]]))

(defn budget-item-name-panel
  [item planning]
  (let [raw-limit (-> item :limit :amount)
        limit (/ raw-limit 100)
        raw-spent (-> item :spent :amount)
        spent (/ raw-spent 100)
        colour (item :colour)]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:style {:padding-bottom 0}}
      [:> ui/Grid.Column
       {:style {:font-size "1.2em"}}
       (:budget-item-name item)]]
     [:> ui/Grid.Row
      {:style {:height "1em"
               :padding-top "0.9em"
               :padding-bottom "1.5em"}}
      [:> ui/Grid.Column
       (when (not planning)
         [:> ui/Progress
          {:size "tiny"
           :total limit
           :value (min spent limit)
           :color (colour :name)}])]]]))

(defn budget-item-panel
  [item]
  (let [selected-item-id @(re-frame/subscribe [::subs/selected-item-id])
        spending-item-id @(re-frame/subscribe [::subs/spending-item-id])
        edit-item-id @(re-frame/subscribe [::subs/edit-item-id])
        reset-item @(re-frame/subscribe [::subs/reset-item])
        item-id (:budget-item-id item)
        spending (= item-id spending-item-id)
        editing (= item-id edit-item-id)
        resetting reset-item
        selected (= item-id selected-item-id)
        colour (item :colour)
        item-border (str "3px solid " (colour :css colour))
        planning (= :plan @(re-frame/subscribe [::subs/view-mode]))]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:id (str "budget-item-" item-id)
       :style {:padding-bottom 0}}
      [:> ui/Grid.Column {:style (merge {:border-right item-border}
                                        (if selected {} {:height "79%"}))
                          :width 2}]
      [:> ui/Grid.Column
       {:on-click #(re-frame/dispatch [::events/select-item item])
        :width 5
        :text-align "left"
        :style {:border-top budget-item-border-style
                :padding "0.4em"
                :padding-bottom "1.5em"}}
       [budget-item-name-panel item planning]]
      [:> ui/Grid.Column
       {:on-click #(re-frame/dispatch [::events/select-item item])
        :width 7
        :style {:border-top budget-item-border-style
                :padding "0.4em"}}
       [budget-item-amount-panel item]]]
     (when selected
       [:> ui/Grid.Row
        {:style {:padding-top 0
                 :padding-bottom 0}}
        [:> ui/Grid.Column
         {:style {:border-right item-border
                  :min-height "1em"}
          :width 2}]
        [:> ui/Grid.Column {:width 12}]])
     (when selected
       [:> ui/Grid.Row
        {:style {:padding-top 0}}
        [:> ui/Grid.Column
         {:style {:border-right item-border}
          :width 2}]
        [:> ui/Grid.Column
         {:style {:padding-bottom "0.5em"}
          :width 12}
         (cond
           spending [budget-item-spend-panel item]
           resetting [budget-item-reset-panel item]
           editing [budget-item-edit-panel item]
           :else [budget-item-button-panel item])]])]))

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
    [:> ui/Grid
     [:> ui/Grid.Row
      {:centered true
       :style {:padding-top 0}}
      [:> ui/Grid.Column
       {:width 14}
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
         :style {:max-width "80%"}}]]]
     (when name-error
       [:> ui/Grid.Row
        {:centered true
         :style {:padding 0}
         :text-align "left"}
        [:> ui/Grid.Column
         {:width 14}
         [:> ui/Label name-error]]])
     [:> ui/Grid.Row
      {:centered true
       :style {:padding-top 0}}
      [:> ui/Grid.Column
       {:width 14}
       [:> ui/Input
        {:label "Limit"
         :name "add-item-amount"
         :error (some? amount-error)
         :step 0.01
         :type "number"
         :min 0
         :on-change #(update-new-item-amount %)
         :style {:max-width "80%"}}]]]
     (when amount-error
       [:> ui/Grid.Row
        {:centered true
         :style {:padding-top 0}
         :text-align "left"}
        [:> ui/Grid.Column
         {:width 14}
         [:> ui/Label amount-error]]])
     [:> ui/Grid.Row
      {:centered true
       :style {:padding 0}
       :text-align "left"}
      [:> ui/Grid.Column
       {:width 14}
       [:> ui/Grid
        [:> ui/Grid.Row
         [:> ui/Grid.Column
          {:width 6}
          [pink-button
           "Cancel" #(re-frame/dispatch [::events/toggle-adding-item])]]
         [:> ui/Grid.Column
          {:width 6}
          [pink-button
           "Add"
           #(re-frame/dispatch [::events/add-item view-mode])
           (or name-error amount-error)]]]]]]]))

(defn reset-all-panel
  []
  [:> ui/Grid
   [:> ui/Grid.Row
    {:style {:padding-top 0
             :padding-bottom 0}}
    [:> ui/Grid.Column {:width 1}]
    [:> ui/Grid.Column
     {:width 14}
     [:h4 "Reset all spending?"]]]
   [:> ui/Grid.Row
    [:> ui/Grid.Column {:width 1}]
    [:> ui/Grid.Column
     {:width 4}
     [pink-button "No" #(re-frame/dispatch [::events/toggle-resetting-all])]]
    [:> ui/Grid.Column
     {:width 4}
     [pink-button "Yes" #(re-frame/dispatch [::events/reset-all-items])]]]])

(defn budget-list-panel [budget]
  [:> ui/Grid
   (map (fn [i]
          [:> ui/Grid.Row
           {:style {:padding-top 0
                    :padding-bottom 0}}
           [:> ui/Grid.Column
            [budget-item-panel i]]]) budget)])

(defn budget-data-options-panel
  [options]
  [:div
   (for [[name value] (partition 2 options)]
     [:a
      {:on-click #(re-frame/dispatch [::events/set-budget-data-view value])}
      name])])

(defn budget-data-table
  "Table of budget data. Outermost element should is a ui/Grid.Column"
  [options]
  (let [budget @(re-frame/subscribe [::subs/coloured-budget])
        total (budget/sum-limits budget)
        spend (budget/sum-spents budget)
        over-spend (if (< total spend)
                     (.abs js/Math (- total spend))
                     0)
        total (/ total 100)
        over-spent (> over-spend 0)
        over-spend (/ over-spend 100)]
    [:> ui/Card
     {:centered true
      :style {:margin-bottom "-2em"
              :padding-top "1em"
              :padding-left "1em"
              :padding-bottom "2em"}}
     [:> ui/Grid
      [:> ui/Grid.Row
       {:centered true
        :style {:padding-bottom 0}}
       [:> ui/Grid.Column
        {:width 6}
        [:> ui/Grid
         [:> ui/Grid.Row
          {:style {:padding-bottom 0}}
          [:> ui/Grid.Column
           [:h5 (currency-str total)]]]
         [:> ui/Grid.Row
          {:style {:padding-top 0}}
          [:> ui/GridColumn
           [:h5 "Total"]]]]]
       [:> ui/Grid.Column
        {:width 6}
        [:> ui/Grid
         [:> ui/Grid.Row
          {:style {:padding-bottom 0}}
          [:> ui/Grid.Column
           [:h5
            {:style (if over-spent {:color "red"} {})}
            (currency-str over-spend)]]]
         [:> ui/Grid.Row
          {:style {:padding-top 0}}
          [:> ui/Grid.Column [:h5 "Over-spent"]]]]]]
      (comment
        [:> ui/Grid.Row
         [:> ui/Grid.Column
          [budget-data-options-panel options]]])]]))

(defn budget-data-panel
  [budget]
  (let [budget-data-view @(re-frame/subscribe [::subs/budget-data-view])]
    (cond
      (= budget-data-view :table) [budget-data-table budget ["Chart" :table]]
      (= budget-data-view :pie) [budget-data-table budget ["Table" :pie]])))

(defn adjust-income-panel
  []
  (let [income (-> @(re-frame/subscribe [::subs/coloured-plan])
                   :income
                   (/ 100))
        income-error @(re-frame/subscribe [::subs/income-error])]
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

(defn plan-data-table
  []
  (let [plan @(re-frame/subscribe [::subs/coloured-plan])
        income (/ (:income plan) 100)
        outgoing (/ (budget/sum-limits (:budget plan)) 100)
        balance (- income outgoing)
        minus (< balance 0)
        balance-style {:color (if minus "red" "#737588")}
        adjusting-income @(re-frame/subscribe [::subs/adjusting-income])]
    [:> ui/Card
     {:centered true
      :style {:padding-top "1em"
              :padding-left "1em"
              :padding-bottom "1em"}}
     [:> ui/Grid
      [:> ui/Grid.Row
       {:centered true
        :on-click
        (if adjusting-income
          #()
          #(re-frame/dispatch [::events/start-adjusting-income income]))}
       [:> ui/Grid.Column
        {:width 5}
        [:> ui/Grid
         [:> ui/Grid.Row
          {:style {:padding-bottom 0}}
          [:> ui/Grid.Column
           [:h5 (currency-str income)]]]
         [:> ui/Grid.Row
          {:style {:padding-top 0}}
          [:> ui/Grid.Column
           [:h5 "Income"]]]]]
       [:> ui/Grid.Column
        {:width 5}
        [:> ui/Grid
         [:> ui/Grid.Row
          {:style {:padding-bottom 0}}
          [:> ui/Grid.Column
           [:h5 (currency-str outgoing)]]]
         [:> ui/Grid.Row
          {:style {:padding-top 0}}
          [:> ui/Grid.Column
           [:h5 "Outgoing"]]]]]
       [:> ui/Grid.Column
        {:width 5}
        [:> ui/Grid
         [:> ui/Grid.Row
          {:style {:padding-bottom 0}}
          [:> ui/Grid.Column
           [:h5
            {:style balance-style}
            (currency-str balance)]]]
         [:> ui/Grid.Row
          {:style {:padding-top 0}}
          [:> ui/Grid.Column
           [:h5
            {:style balance-style}
            "Balance"]]]]]]]]))

(defn money-panel []
  (let [planning (= :plan @(re-frame/subscribe [::subs/view-mode]))
        data-key (if planning ::subs/coloured-plan ::subs/coloured-budget)
        data @(re-frame/subscribe [data-key])
        budget (if planning (:budget data) data)
        adding-item @(re-frame/subscribe [::subs/adding-item])
        resetting-all @(re-frame/subscribe [::subs/resetting-all])
        adjusting-income @(re-frame/subscribe [::subs/adjusting-income])
        editing (or adding-item resetting-all adjusting-income)]
    (assert ::specs/budget budget)
    [:> ui/Card
     {:centered true
      :style {:height "100%"
              :overflow "hidden scroll"}}
     [:> ui/Grid
      [:> ui/Grid.Row]
      [:> ui/Grid.Row
       {:centered true}
       [:> ui/Grid.Column
        {:width 15
         :style {:padding-bottom 0}}
        (if planning [plan-data-table] [budget-data-table])]]
      [:> ui/Grid.Row]
      (when (not editing)
        [:> ui/Grid.Row
         {:centered true
          :style {:padding-top 0}}
         [:> ui/Grid.Column
          {:width 14}
          [:> ui/Tab
           {:panes [{:menuItem "Budget"}
                    {:menuItem "Bills"}]
            :defaultActiveIndex (if planning 1 0)
            :onTabChange
            #(let [index (.-activeIndex %2)]
               (cond
                 (= index 0)
                 (re-frame/dispatch [::events/set-view-mode :budget])
                 (= index 1)
                 (re-frame/dispatch [::events/set-view-mode :plan])))}]]])
      [:> ui/Grid.Row
       {:centered true}
       [:> ui/Grid.Column
        (cond
          adding-item [add-item-panel]
          resetting-all [reset-all-panel]
          adjusting-income [adjust-income-panel]
          :else [budget-list-panel budget])]]
      [:> ui/Grid.Row
       [:> ui/Grid.Column]]]]))

(defn budget-bottom-menu-panel
  []
  (let [adding-item @(re-frame/subscribe [::subs/adding-item])
        resetting-all @(re-frame/subscribe [::subs/resetting-all])
        planning (= :plan @(re-frame/subscribe [::subs/view-mode]))]
    [:> ui/Card
     {:centered true}
     [:> ui/Menu
      {:icon "labeled"
       :widths 3}
      [:> ui/Menu.Item
       {:on-click #(re-frame/dispatch [::events/toggle-adding-item])
        :disabled adding-item}
       [:> ui/Icon {:name "plus"}]
       "Add"]
      [:> ui/Menu.Item
       [:> ui/Icon
        {:disabled (or resetting-all planning)
         :on-click #(re-frame/dispatch [::events/toggle-resetting-all])
         :name "undo"}]
       "Reset All"]
      [:> ui/Menu.Item
       {:disabled true}
       [:> ui/Icon
        {:name "numbered list"}]
       "Transactions"]]]))

(defn home-panel []
  (let [loading @(re-frame/subscribe [::subs/loading])
        planning (= :plan @(re-frame/subscribe [::subs/view-mode]))]
    [:> ui/Grid
     {:container true
      :style {:margin-top "-0.4em"
              :height "70%"}
      :text-align "center"
      :vertical-align "middle"}
     (comment  (if (not loading)
                 [:> ui/Grid.Row
                  [:> ui/Grid.Column
                   {:width 15}
                   (if planning [plan-data-table] [budget-data-table])]]))
     [:> ui/Grid.Row]
     [:> ui/Grid.Row
      {:style {:height "100%"
               :padding-top 0}}
      [:> ui/Grid.Column
       {:style (if loading
                 {:background-color "white"
                  :min-height "20%"
                  :height "60%"
                  :width "80%"}
                 {:height "100%"
                  :overflow "hidden hidden"})} ; overflow hidden hidden
       (if loading
         [:> ui/Loader
          {:active true
           :size "massive"}
          [:h1 "Loading"]]
         [money-panel])]]
     (when (not loading)
       [:> ui/Grid.Row
        {:centered true
         :style {:padding 0}}
        [:> ui/Grid.Column
         [budget-bottom-menu-panel]]])]))

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

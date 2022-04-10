(ns pp-pwa.views
  (:require
   ["semantic-ui-react" :as ui]
   ["chartist" :as c]
   [cljs.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent :refer [atom]]
   [pp-pwa.budget :as budget]
   [pp-pwa.datetime :as dt]
   [pp-pwa.events :as events]
   [pp-pwa.main-menu :as main-menu]
   [pp-pwa.routes :as routes]
   [pp-pwa.styles :as styles]
   [pp-pwa.specs :as specs]
   [pp-pwa.subs :as subs]
   [pp-pwa.utility :as u]))

;(def budget-item-border-colour-css (styles/colour :apple :system-teal :css))

(def budget-item-border-style
  (str "solid 1px " (styles/colour :apple :system-teal :css)))

(def button-style {:background-color (styles/colour :web :pink :css)
                   :padding-left "1.4em"
                   :padding-right "1.4em"
                   :color (styles/colour :web :white :css)})

(defn currency-str
  [amount]
  (u/currency-str amount))

(defn pink-button
  ([label] (pink-button label #()))
  ([label on-click] (pink-button label on-click nil))
  ([label on-click disabled]
   [:> ui/Button
    {:style button-style
     :disabled disabled
     :onClick on-click}
    label]))


(defn update-text-property
  "Updates a text property."
  [event set-property-key set-property-error-key]
  (let [value (-> event .-target .-value)]
    ; todo: move most of this to the set event
    (if (s/valid? ::specs/budget-item-name value)
      (do
        (re-frame/dispatch [set-property-key value])
        (re-frame/dispatch [set-property-error-key nil]))
      (re-frame/dispatch [set-property-error-key "Required."]))))

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
                      :on-change #(update-text-property
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
       {:width 7}
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
  (let [amount-error @(re-frame/subscribe [::subs/spending-amount-error])
        note-error @(re-frame/subscribe [::subs/spending-note-error])
        msg @(re-frame/subscribe [::subs/spend-msg])]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:style {:padding-bottom 0}}
      [:> ui/Grid.Column
       [:> ui/Input
        {:auto-focus true
         :error (some? amount-error)
         :fluid true
         :id "spend-amount-input"
         :on-change #(update-spending-amount %)
         :label (get-in item [:spent :currency-code])
         :min 0
         :placeholder "amount spent"
         :step 0.01
         :style {:padding-bottom "0.5em"}
         :type "number"}]]]
     (when amount-error
       [:> ui/Grid.Row
        {:text-align "left"}
        [:> ui/Grid.Column
         [:> ui/Label amount-error]]])
     [:> ui/Grid.Row
      {:centered true}
      [:> ui/Grid.Column
       [:> ui/Input
        {:id "spend-note-input"
         :fluid true
         :on-blur #(update-text-property
                    %
                    ::events/set-spending-note
                    ::events/set-spending-note-error)
         :label "Note"}]]]
     [:> ui/Grid.Row
      [:> ui/Grid.Column
       [pink-button [:span [:> ui/Icon {:name "payment"}] "Submit"]
        #(re-frame/dispatch [::events/spend])
        (not @(re-frame/subscribe [::subs/spend-is-valid]))]]]
     (when msg
       [:> ui/Grid.Row
        [:> ui/Grid.Column
         [:> ui/Message
          {:success true}
          msg]]])]))

(defn budget-item-selector
  [options]
  (let [budget-drop-down-items @(re-frame/subscribe
                                 [::subs/budget-drop-down-items])]
    [:> ui/Dropdown
     (merge {:placeholder "Category"
             :fluid true
             :selection true
             :options budget-drop-down-items}
            options)]))

(defn budget-spend-panel
  []
  (let [item-id @(re-frame/subscribe [::subs/spending-item-id])]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:centered true}
      [:> ui/Grid.Column {:width 1}]
      [:> ui/Grid.Column
       {:width 14}
       [:h1
        {:id "budget-spending-panel-header"}
        "Spend"]]]
     [:> ui/Grid.Row
      [:> ui/Grid.Column {:width 1}]
      [:> ui/Grid.Column
       {:width 13}
       [budget-item-selector
        {:id "spending-category-selector"
         :on-change (fn [_e, data]
                      (let [value (.-value data)]
                        (re-frame/dispatch [::events/spending
                                            {:budget-item-id value}])))
         :value item-id}]]
      [:> ui/Grid.Column {:width 1}]]
     [:> ui/Grid.Row
      {:centered true}
      [:> ui/Grid.Column
       {:width 13}
       [budget-item-spend-panel]]]]))

(defn budget-item-button-panel
  [item]
  (let [planning (= :plan @(re-frame/subscribe [::subs/view-mode]))
        reset-possible (and (> (get-in item [:spent :amount]) 0) (not planning))
        writeable (-> item :read-only not)]
    (when writeable
      [:> ui/Grid
       [:> ui/Grid.Row
        {:centered reset-possible
         :style {:padding-top 0}}
        [:> ui/Grid.Column
         {:style {:padding-left (if reset-possible 0 "0.5em")
                  :padding-right 0}
          :vertical-align (if planning "top" "bottom")
          :width 8}
         [pink-button [:span [:> ui/Icon {:name "edit"}] "Edit"]
          #(re-frame/dispatch [::events/editing item])]]
        (when reset-possible
          [:> ui/Grid.Column
           {:style {:padding-left 0
                    :padding-right 0}
            :vertical-align (if planning "top" "bottom")
            :width 8}
           [pink-button [:span [:> ui/Icon {:name "undo"}] "Reset"]
            #(re-frame/dispatch [::events/toggle-reset-item])]])]])))

(defn budget-item-amount-panel
  [item planning]
  (let [raw-limit (-> item :limit :amount)
        limit (/ raw-limit 100)
        raw-spent (-> item :spent :amount)
        spent (/ raw-spent 100)
        negative (> spent limit)]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:style {:padding-bottom "0.3em"}}
      [:> ui/Grid.Column
       {:style {:font-size "1.3em"
                :color (if negative "red" "black")}
        :text-align "right"}
       (currency-str (if planning limit spent))]]
     (when (not planning)
       [:> ui/Grid.Row
        {:style {:padding-top 0}}
        [:> ui/Grid.Column
         {:text-align "right"}
         (currency-str limit)]])]))

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
        edit-item-id @(re-frame/subscribe [::subs/edit-item-id])
        reset-item @(re-frame/subscribe [::subs/reset-item])
        item-id (:budget-item-id item)
        editing (= item-id edit-item-id)
        resetting reset-item
        selected (= item-id selected-item-id)
        colour (item :colour)
        item-border (str "3px solid " (colour :css colour))
        planning (= :plan @(re-frame/subscribe [::subs/view-mode]))
        writeable (-> item :read-only not)]
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
       [budget-item-amount-panel item planning]]]
     (when (and selected writeable)
       [:> ui/Grid.Row
        {:style {:padding-top 0
                 :padding-bottom 0}}
        [:> ui/Grid.Column
         {:style {:border-right item-border
                  :min-height "1em"}
          :width 2}]
        [:> ui/Grid.Column {:width 12}]])
     (when (and selected writeable)
       [:> ui/Grid.Row
        {:style {:padding-top 0}}
        [:> ui/Grid.Column
         {:style {:border-right item-border}
          :width 2}]
        [:> ui/Grid.Column
         {:style {:padding-bottom "0.5em"}
          :width 12}
         (cond
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
        view-mode @(re-frame/subscribe [::subs/view-mode])
        msg @(re-frame/subscribe [::subs/add-item-msg])]
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
         :on-change #(update-text-property
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
          {:width 9}
          [pink-button
           [:span [:> ui/Icon {:name "cancel"}] "Cancel"]
           #(re-frame/dispatch [::events/toggle-adding-item])]]
         [:> ui/Grid.Column
          {:style {:padding-left 0
                   :padding-right 0}
           :width 7}
          [pink-button
           [:span [:> ui/Icon {:name "plus"}] "Add"]
           #(re-frame/dispatch [::events/add-item view-mode])
           (or name-error amount-error)]]]
        (when msg
          [:> ui/Grid.Row
           {:centered true}
           [:> ui/Message
            {:success true
             :onDimiss #()}
            msg]])]]]]))

(defn reset-all-panel
  []
  [:> ui/Grid
   [:> ui/Grid.Row
    {:style {:padding-bottom 0}}
    [:> ui/Grid.Column {:width 1}]
    [:> ui/Grid.Column
     {:text-align "left"
      :width 14}
     [:h4 "Reset all spending?"]]]
   [:> ui/Grid.Row
    [:> ui/Grid.Column {:width 1}]
    [:> ui/Grid.Column
     {:style {:padding-right 0}
      :width 6}
     [pink-button [:span [:> ui/Icon {:name "cancel"}] "No"]
      #(re-frame/dispatch [::events/toggle-resetting-all])]]
    [:> ui/Grid.Column
     {:style {:padding-left 0}
      :width 6}
     [pink-button [:span [:> ui/Icon {:name "undo"}] "Yes"]
      #(re-frame/dispatch [::events/reset-all-items])]]]])

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
          [:> ui/Grid.Column [:h5 "Over"]]]]]]
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
        [pink-button [:span [:> ui/Icon {:name "cancel"}] "Cancel"]
         #(re-frame/dispatch [::events/stop-adjusting-income])]
        [pink-button [:span [:> ui/Icon {:name "save"}] "Save"]
         #(re-frame/dispatch [::events/adjust-income])]]]]]))

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
              :padding-left "0em"
              :padding-bottom "1em"}}
     [:> ui/Grid
      [:> ui/Grid.Row
       {:centered true}
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

(defn budget-panel []
  (let [planning (= :plan @(re-frame/subscribe [::subs/view-mode]))
        spending @(re-frame/subscribe [::subs/spending])
        data-key (if planning ::subs/coloured-plan ::subs/coloured-budget)
        data @(re-frame/subscribe [data-key])
        budget (if planning (:budget data) data)
        adding-item @(re-frame/subscribe [::subs/adding-item])
        resetting-all @(re-frame/subscribe [::subs/resetting-all])
        adjusting-income @(re-frame/subscribe [::subs/adjusting-income])
        editing (or adding-item resetting-all adjusting-income spending)
        plan @(re-frame/subscribe [::subs/coloured-plan])
        income (/ (:income plan) 100)]
    (assert ::specs/budget budget)
    [:> ui/Grid
     [:> ui/Grid.Row]
     (when (not editing)
       [:> ui/Grid.Row
        {:centered true
         :style {:padding-bottom "1em"
                 :padding-top 0}}
        [:> ui/Grid.Column
         {:width 14}
         [:> ui/Tab
          {:panes [{:menuItem "Budget"}
                   {:menuItem "Bills"}
                   {:menuItem {:name "Debt" :disabled true}}]
           :defaultActiveIndex (if planning 1 0)
           :onTabChange
           #(let [index (.-activeIndex %2)]
              (cond
                (= index 0)
                (re-frame/dispatch [::events/set-view-mode :budget])
                (= index 1)
                (re-frame/dispatch [::events/set-view-mode :plan])))}]]])
     (when
         (and planning
              (not (or editing spending resetting-all adding-item adjusting-income)))
       [:> ui/Grid.Row
        {:centered true
         :style {:padding-bottom "1em"}}
        [:> ui/Grid.Column
         {:width 14}
         [plan-data-table]]])
     [:> ui/Grid.Row
      {:centered true}
      [:> ui/Grid.Column
       (cond
         adding-item [add-item-panel]
         resetting-all [reset-all-panel]
         adjusting-income [adjust-income-panel]
         spending [budget-spend-panel]
         :else [budget-list-panel budget])]]
     (when (not editing)
       [:> ui/Grid.Row
        {:style {:margin-left "1em"
                 :margin-top "2em"}
         :text-align "left"}
        (when planning
          [:> ui/Grid.Column
           {:width 14}
           [pink-button [:span [:> ui/Icon {:name "money"}] "Change income"]
            #(re-frame/dispatch [::events/start-adjusting-income income])]])
        [:> ui/Grid.Row
         {:style {:margin-left "1em"
                  :margin-top "2em"}
          :text-align "left"}
         [:> ui/Grid.Column
          {:width 10}
          [pink-button [:span [:> ui/Icon {:name "plus"}] "Add Category"]
           #(re-frame/dispatch [::events/toggle-adding-item])]]
         (when (not planning)
           [:> ui/Grid.Column
            {:style {:margin-top "2em"}
             :width 10}
            [pink-button [:span [:> ui/Icon {:name "undo"}] "Reset All"]
             #(re-frame/dispatch [::events/toggle-resetting-all])]])]])]))

(defn transaction-item-delete-panel
  [transaction]
  (let [deleting (= transaction
                    @(re-frame/subscribe [::subs/deleting-transaction]))
        year-kw (-> @(re-frame/subscribe [::subs/selected-transaction-year])
                    str
                    keyword)
        month-kw (-> @(re-frame/subscribe [::subs/selected-transaction-month])
                     dt/month-number
                     str
                     keyword)]
    (if deleting
      [:> ui/Grid
       [:> ui/Grid.Row
        {:text-align "left"}
        [:> ui/Grid.Column
         [:h4 "Delete transaction?"]]]
       [:> ui/Grid.Row
        {:style {:padding-top 0}}
        [:> ui/Grid.Column
         {:width 8}
         [pink-button
          [:span [:> ui/Icon {:name "cancel"}] "No"]
          #(re-frame/dispatch [::events/cancel-deleting-transaction])]]
        [:> ui/Grid.Column
         {:width 8}
         [pink-button
          [:span [:> ui/Icon {:name "trash"}] "Yes"]
          #(re-frame/dispatch [::events/delete-transaction
                               year-kw month-kw])]]]]
      [pink-button
       [:span [:> ui/Icon {:name "trash"}] "Delete"]
       #(re-frame/dispatch [::events/deleting-transaction transaction])])))

(defn transaction-item-panel [transaction]
  [:> ui/Grid
   [:> ui/Grid.Row
    {:centered true}
    [:> ui/Grid.Column
     {:width 13}
     [:> ui/Card
      {:style {:padding-bottom "0.5em"
               :padding-left "1em"
               :padding-right "1em"
               :padding-top "0.5em"}}
      [:> ui/Grid
       [:> ui/Grid.Row
        {:style {:padding-bottom 0}}
        [:> ui/Grid.Column
         {:text-align "left"
          :width 8}
         [:h4 (:budget-item-name transaction)]]
        [:> ui/Grid.Column
         {:text-align "right"
          :width 8}
         [:h5 (currency-str (-> transaction :spent :amount (/ 100)))]]]
       [:> ui/Grid.Row
        {:style {:padding-top 0
                 :padding-bottom 0}
         :text-align "left"}
        [:> ui/Grid.Column
         {:width 16}
         [:p (or (:note transaction) "")]]]
       [:> ui/Grid.Row
        {:text-align "right"}
        [:> ui/Grid.Column
         (-> transaction :datetime-info :datetime)]]
       [:> ui/Grid.Row
        {:text-align "right"}
        [:> ui/Grid.Column
         [transaction-item-delete-panel transaction]]]]]]]])

(defn transaction-list-panel []
  (let [year @(re-frame/subscribe [::subs/selected-transaction-year])
        years @(re-frame/subscribe [::subs/transaction-years])
        year-options (map (fn [y]
                            {:key y
                             :text y
                             :value y
                             :active (= y year)})
                          years)
        month @(re-frame/subscribe [::subs/selected-transaction-month])
        months @(re-frame/subscribe [::subs/transaction-months])
        month-options (map (fn [m]
                             {:key m
                              :text m
                              :value m
                              :active (= m month)})
                           months)
        transactions @(re-frame/subscribe [::subs/selected-transactions])]
    [:> ui/Grid
     [:> ui/Grid.Row
      {:centered true
       :style {:padding-bottom 0}}
      [:> ui/Grid.Column
       {:width (if (empty? transactions) 8 7)}
       [:> ui/Dropdown
        {:fluid true
         :on-change #(do (re-frame/dispatch
                          [::events/set-selected-transaction-month
                           (-> % .-target .-innerText)]))
         :options month-options
         :placeholder "Month"
         :selection true
         :default-value month
         }]]
      [:> ui/Grid.Column
       {:width 6}
       [:> ui/Dropdown
        {:fluid true
         :on-change #(re-frame/dispatch
                      [::events/set-selected-transaction-year
                       (-> % .-target .-innerText)])
         :options year-options
         :placeholder "Year"
         :selection true
         :value year}]]]
     [:> ui/Grid.Row
      {:style {:padding-bottom 0
               :padding-top 0}}
      [:> ui/Grid.Column]]
     (map (fn [i]
            [:> ui/Grid.Row
             {:style {:padding-bottom 0
                      :padding-top "1em"}}
             [:> ui/Grid.Column
              [transaction-item-panel i]]])
          transactions)]))

(defn settings-panel
  []
  [:div
   [:h1 "Settings"]])

(defn transaction-panel []
  [:> ui/Grid
   [:> ui/Grid.Row]
   [:> ui/Grid.Row
    [:> ui/Grid.Column
     [:h1 "Transactions"]]]
   [:> ui/Grid.Row
    {:centered true}
    [transaction-list-panel]]])

(defn history-panel []
  [:> ui/Grid
   [:> ui/Grid.Row]
   [:> ui/Grid.Row
    [:> ui/Grid.Column
     [:h1 "History"]]]])

(defn money-panel []
  (let [view @(re-frame/subscribe [::subs/view])]
    [:> ui/Card
     {:centered true
      :style {:height "100%"
              :opacity "85%"
              :overflow "hidden scroll"}}
     (cond
       (= view :settings) [settings-panel]
       (= view :transaction) [transaction-panel]
       (= view :history) [history-panel]
       :else [budget-panel])]))

(defn home-panel []
  (let [loading @(re-frame/subscribe [::subs/loading])]
    [:> ui/Grid
     {:container true
      :style {:margin-top "-0.4em"
              :height "90%"}
      :text-align "center"
      :vertical-align "middle"}
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
     [:> ui/Grid.Row
      [:> ui/Grid.Column [main-menu/main-menu]]]]))

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

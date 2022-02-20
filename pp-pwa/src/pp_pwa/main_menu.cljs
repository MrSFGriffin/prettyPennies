(ns pp-pwa.main-menu
  (:require
   ["semantic-ui-react" :as ui]
   [pp-pwa.events :as events]
   [pp-pwa.subs :as subs]
   [re-frame.core :as re-frame]))

(defn spending-menu-data
  []
  {:menu-options {:widths 2}
   :items
   [{:menu-item-options
     {:on-click #(do
                   (re-frame/dispatch [::events/set-main-menu-mode :budget])
                   (re-frame/dispatch [::events/cancel-spending]))}
     :icon-options {:name "clipboard outline"}
     :label "Budget"}
    {:menu-item-options
     {:on-click #(do
                   (re-frame/dispatch [::events/cancel-spending])
                   (re-frame/dispatch [::events/set-main-menu-mode :transaction])
                   (re-frame/dispatch [::events/set-view :transaction]))}
     :icon-options {:name "numbered-list"}
     :label "Transactions"}]})

(defn transactions-menu-data
  []
  {:menu-options {:widths 2}
   :items
   [;; {:menu-item-options
    ;;  {:disabled true
    ;;   :on-click #(re-frame/dispatch [::events/set-view :settings])}
    ;;  :icon-options {:name "setting"}
    ;;  :label "Configure"}
    {:menu-item-options
     {:on-click #(do
                   (re-frame/dispatch [::events/set-main-menu-mode :budget])
                   (re-frame/dispatch [::events/set-view :money]))}
     :icon-options {:name "clipboard outline"}
     :label "Budget"}
    {:menu-item-options
     {:on-click #(do
                   (re-frame/dispatch [::events/set-main-menu-mode :spending])
                   (re-frame/dispatch [::events/set-view :money])
                   (re-frame/dispatch [::events/spending]))
      :disabled (not @(re-frame/subscribe [::subs/any-item]))}
     :icon-options {:name "payment"}
     :label "Spend"}]})

(defn settings-menu-data
  []
  {:menu-options {:widths 2}
   :items
   [{:menu-item-options
     {:disabled true
      :on-click #(do
                   (re-frame/dispatch
                    [::events/set-main-menu-mode :transaction])
                   (re-frame/dispatch [::events/set-view :transaction]))}
     :icon-options {:name "numbered-list"}
     :label "Transactions"}
    {:menu-item-options
     {:on-click #(re-frame/dispatch [::events/set-view :money])}
     :icon-options {:name "clipboard outline"}
     :label "Budget"}]})

(defn budget-menu-data
  []
  (let [any-item @(re-frame/subscribe [::subs/any-item])
        any-transactions @(re-frame/subscribe [::subs/any-transactions])]
    {:menu-options {:widths 2}
     :items
     [{:menu-item-options
       {:on-click #(do
                     (re-frame/dispatch
                      [::events/set-main-menu-mode :transaction])
                     (re-frame/dispatch [::events/set-view :transaction]))
        :disabled (not any-transactions)}
       :icon-options {:name "numbered list"}
       :label "Transactions"}
      ;; {:menu-item-options
      ;;  {:disabled true
      ;;   :on-click #(do
      ;;                (re-frame/dispatch [::events/set-main-menu-mode :settings])
      ;;                (re-frame/dispatch [::events/set-view :settings]))}
      ;;  :icon-options {:name "setting"}
      ;;  :label "Configure"}
      {:menu-item-options
       {:on-click #(do
                     (re-frame/dispatch [::events/set-main-menu-mode :spending])
                     (re-frame/dispatch [::events/spending]))
        :disabled (not any-item)}
       :icon-options {:name "payment"}
       :label "Spend"}]}))

(defn main-menu-data
  []
  (let [menu-mode @(re-frame/subscribe [::subs/main-menu-mode])]
    (cond
      (= menu-mode :spending) (spending-menu-data)
      (= menu-mode :transaction) (transactions-menu-data)
      (= menu-mode :settings) (settings-menu-data)
      :else (budget-menu-data))))

(defn main-menu-item
  [item]
  [:> ui/Menu.Item
   (:menu-item-options item)
   [:> ui/Icon (:icon-options item)]
   (:label item)])

(defn main-menu
  []
  (let [data (main-menu-data)]
    [:> ui/Menu
     (merge (:menu-options data)
            {:fixed "bottom"})
     (map main-menu-item (:items data))]))

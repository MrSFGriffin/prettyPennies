(ns pp-pwa.main-menu
  (:require
   ["semantic-ui-react" :as ui]
   [pp-pwa.events :as events]
   [pp-pwa.subs :as subs]
   [re-frame.core :as re-frame]))

(defn history-menu-item
  []
  {:menu-item-options
   {:on-click #(do
                 (re-frame/dispatch
                  [::events/set-main-menu-mode :history])
                 (re-frame/dispatch [::events/set-view :history]))}
   :icon-options {:name "history"}
   :label "History"})

(defn budget-menu-item
  []
  {:menu-item-options
   {:on-click #(do
                 (re-frame/dispatch [::events/cancel-spending])
                 (re-frame/dispatch [::events/set-main-menu-mode :budget])
                 (re-frame/dispatch [::events/set-view :money]))}
   :icon-options {:name "clipboard outline"}
   :label "Budget"})

(defn spend-menu-item
  []
  {:menu-item-options
   {:on-click #(do
                 (re-frame/dispatch [::events/set-main-menu-mode :spending])
                 (re-frame/dispatch [::events/set-view :money])
                 (re-frame/dispatch [::events/spending]))
    :disabled (not @(re-frame/subscribe [::subs/any-item]))}
   :icon-options {:name "payment"}
   :label "Spend"})

(defn transaction-menu-item
  []
  (let [any-transactions @(re-frame/subscribe [::subs/any-transactions])]
    {:menu-item-options
     {:on-click #(do
                   (re-frame/dispatch
                    [::events/set-main-menu-mode :transaction])
                   (re-frame/dispatch [::events/set-view :transaction]))
      :disabled (not any-transactions)}
     :icon-options {:name "numbered list"}
     :label "Transactions"}))

(defn main-menu-data
  []
  (let [menu-mode @(re-frame/subscribe [::subs/main-menu-mode])
        menu-item-data [{:excluded-menu-mode :budget
                         :item (budget-menu-item)}
                        {:excluded-menu-mode :transaction
                         :item (transaction-menu-item)}
                        ;; {:excluded-menu-mode :history
                        ;;  :item (history-menu-item)}
                        {:excluded-menu-mode :spending
                         :item (spend-menu-item)}]
        menu-items (remove #(= (:excluded-menu-mode %) menu-mode)
                           menu-item-data)]
    {:menu-options {:widths (count menu-items)}
     :items (map :item menu-items)}))

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

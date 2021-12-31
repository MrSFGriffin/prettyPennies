(ns pp-pwa.subs
  (:require
   [orchestra.core :refer-macros [defn-spec]]
   [re-frame.core :as re-frame]
   [pp-pwa.specs :as specs]
   [pp-pwa.styles :as styles]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::budget
 (fn [db]
   (:budget db)))

(defn-spec budget-item-colours ::specs/budget-item-colours
  []
  (map #(apply hash-map
               [:name (styles/colour %1 %2 :name)
                :css (styles/colour %1 %2 :css)])
       (repeat :semantic)
       [:purple
        :orange
        :green
        :blue
        :violet
        :red
        :olive
        :pink
        :teal
        :yellow]))

(defn-spec colour-budget ::specs/budget
  "Adds colours to the items of a budget."
  [budget ::specs/budget]
  (js/console.log budget)
  (map #(assoc %1 :colour %2) budget (cycle (budget-item-colours))))

(re-frame/reg-sub
 ::coloured-budget
 (fn [_ _] (re-frame/subscribe [::budget]))
 (fn [budget _]
   (colour-budget budget)))

(re-frame/reg-sub
 ::adding-item
 (fn [db]
   (:adding-item db)))

(re-frame/reg-sub
 ::resetting-all
 (fn [db]
   (:resetting-all db)))

(re-frame/reg-sub
 ::reset-item
 (fn [db]
   (:reset-item db)))

(re-frame/reg-sub
 ::new-item-name-error
 (fn [db]
   (get-in db [:new-item :name-error])))

(re-frame/reg-sub
 ::new-item-amount-error
 (fn [db]
   (get-in db [:new-item :amount-error])))

(re-frame/reg-sub
 ::selected-item-id
 (fn [db]
   (get db :selected-item-id)))

(re-frame/reg-sub
 ::spending-item-id
 (fn [db]
   (get-in db [:spending :item-id])))

(re-frame/reg-sub
 ::spending-amount-error
 (fn [db]
   (get-in db [:spending :amount-error])))

(re-frame/reg-sub
 ::edit-item-id
 (fn [db]
   (get-in db [:edit-item :item-id])))

(re-frame/reg-sub
 ::edit-item-name-error
 (fn [db]
   (get-in db [:edit-item :name-error])))

(re-frame/reg-sub
 ::edit-item-amount-error
 (fn [db]
   (get-in db [:edit-item :amount-error])))

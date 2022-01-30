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
 ::view-mode
 (fn [db _]
   (:view-mode db)))

(re-frame/reg-sub
 ::budget-data-view
 (fn [db _]
   (:budget-data-view db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::loading
 (fn [db _]
   (:loading db)))

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
  (map #(assoc %1 :colour %2) budget (cycle (budget-item-colours))))

(re-frame/reg-sub
 ::coloured-budget
 (fn [_ _] (re-frame/subscribe [::budget]))
 (fn [budget _]
   (colour-budget budget)))

(re-frame/reg-sub
 ::plan
 (fn [db]
   (let [plan (:plan db)
         budget (:budget plan)
         sorted-budget (sort-by #(get-in % [:limit :amount]) > budget)]
     (assoc plan :budget sorted-budget))))

(re-frame/reg-sub
 ::coloured-plan
 (fn [_ _] (re-frame/subscribe [::plan]))
 (fn [plan _]
   (let [budget (:budget plan)
         coloured-budget (colour-budget budget)]
     (assoc plan :budget coloured-budget))))

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

(re-frame/reg-sub
 ::delete-item-id
 (fn [db]
   (get-in db [:delete-item :item-id])))

(re-frame/reg-sub
 ::adjusting-income
 (fn [db]
   (-> db :income-adjustment)))

(re-frame/reg-sub
 ::income-error
 (fn [db]
   (-> db :income-adjustment :income-error)))

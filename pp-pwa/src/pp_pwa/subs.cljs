(ns pp-pwa.subs
  (:require
   [orchestra.core :refer-macros [defn-spec]]
   [re-frame.core :as re-frame]
   [pp-pwa.budget :as budget]
   [pp-pwa.datetime :as dt]
   [pp-pwa.specs :as specs]
   [pp-pwa.styles :as styles]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::view
 (fn [db _]
   (:view db)))

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

(defn add-total-item
  [budget]
  (let [limit-sum (budget/sum-limits budget)
        spent-sum (budget/sum-spents budget)
        currency-code (-> budget first :limit :currency-code) ; assume they all have the same currency, probably true...
        total-item {:budget-item-id 0
                    :budget-item-name "Total"
                    :limit {:amount limit-sum :currency-code currency-code}
                    :spent {:amount spent-sum :currency-code currency-code}
                    :read-only true
                    }]
    (cons total-item budget)))

(re-frame/reg-sub
 ::coloured-budget
 (fn [_ _] (re-frame/subscribe [::budget]))
 (fn [budget _]
   (colour-budget (add-total-item budget))))

(re-frame/reg-sub
 ::budget-drop-down-items
 (fn [db]
   (let [budget (:budget db)
         item-id (get-in db [:spending :item-id])]
     (map (fn [i]
            {:key (:budget-item-id i)
             :text (:budget-item-name i)
             :value (:budget-item-id i)
             :active (= (:budget-item-id i) item-id)}) budget))))

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
 ::spending
 (fn [db]
   (get db :spending)))

(re-frame/reg-sub
 ::spending-item-id
 (fn [db]
   (get-in db [:spending :item-id])))

(re-frame/reg-sub
 ::spending-amount-error
 (fn [db]
   (get-in db [:spending :amount-error])))

(re-frame/reg-sub
 ::spending-note-error
 (fn [db]
   (get-in db [:spending :note-error])))

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

(defn selected-transaction-year [db]
  (or (get-in db [:transaction-view :year])
      (dt/current-year)))

(defn selected-transaction-month [db]
  (or (get-in db [:transaction-view :month])
      (dt/current-month)))

(defn selected-transactions [db]
  (or
   (get-in db [:transactions
               (-> db selected-transaction-year str keyword)
               (-> db selected-transaction-month str keyword)])
   []))

(defn year-keyword->year
  [year-keyword]
  (-> year-keyword str (subs 1) js/parseInt))

(defn month-keyword->month-name
  [month-keyword]
  (-> month-keyword str (subs 1) js/parseInt dt/month-name))

(re-frame/reg-sub
 ::transaction-years
 (fn [db]
   (distinct
    (cons (-> (dt/current-year) str keyword year-keyword->year)
          (->> db
               :transactions
               keys
               (filter #(not= % :id))
               (filter #(not= % :next-id))
               (mapv year-keyword->year))))))

(re-frame/reg-sub
 ::transaction-months
 (fn [db]
   (distinct
    (cons (-> (dt/current-month) str keyword month-keyword->month-name)
          (let [year (selected-transaction-year db)
                month-kws (keys
                           (get-in db [:transactions (-> year str keyword)]))]
            (mapv month-keyword->month-name month-kws))))))

(re-frame/reg-sub
 ::selected-transaction-year
 (fn [db]
   (selected-transaction-year db)))

(re-frame/reg-sub
 ::selected-transaction-month
 (fn [db]
   (dt/month-name (selected-transaction-month db))))

(re-frame/reg-sub
 ::selected-transactions
 (fn [db]
   (selected-transactions db)))

(re-frame/reg-sub
 ::any-transactions
 (fn [db]
   (not-empty (selected-transactions db))))

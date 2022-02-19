(ns pp-pwa.transactions
  (:require
   [cljs.spec.alpha :as s]
   [pp-pwa.datetime :as dt]
   [pp-pwa.specs :as specs]))

;; (defn next-id
;;   [transactions]
;;   (:next-id transactions))


(defn add-transaction
  [transactions budget-item currency-value note]
  (let [new-tran {:budget-item-name (:budget-item-name  budget-item)
                  :budget-item-uuid (:id budget-item)
                  :spent currency-value
                  :note note
                  :datetime-info (dt/current-datetime-info)}
        {year :year
         month :month} (dt/current-year-and-month)
        year-kw  (-> year str keyword)
        month-kw (-> month str keyword)
        trans-list (get-in transactions [year-kw month-kw])
        new-trans-list (cons new-tran trans-list)]
    (s/assert ::specs/transaction-list new-trans-list)
    {:transactions (-> transactions
                       (assoc-in [year-kw :id] year-kw)
                       (assoc-in [year-kw month-kw] new-trans-list)
                       (update :years #(distinct (cons year-kw %))))
     :year-kw year-kw}))

(defn delete-transaction
  [transactions transaction]
  (filter #(not= % transaction) transactions))

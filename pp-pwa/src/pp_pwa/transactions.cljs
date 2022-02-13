(ns pp-pwa.transactions
  (:require
   [cljs.spec.alpha :as s]
   [pp-pwa.datetime :as dt]
   [pp-pwa.specs :as specs]))

;; (defn next-id
;;   [transactions]
;;   (:next-id transactions))

(defn add-transaction
  [transactions budget-item-name currency-value note]
  (let [;;id (next-id transactions)
        new-tran {;;:id id
                  :budget-item-name budget-item-name
                  :spent currency-value
                  :note note
                  :datetime-info {:datetime (dt/current-datetime)
                                  :locale (dt/locale)
                                  :timezone (dt/time-zone)}}
        year-kw (-> (dt/current-year) str keyword)
        month-kw (-> (dt/current-month) str keyword)
        trans-list (get-in transactions [year-kw month-kw])
        new-trans-list (cons new-tran trans-list)]
        ;;updated (assoc transactions :next-id (inc id))]
    (s/assert ::specs/transaction-list new-trans-list)
    (assoc-in transactions [year-kw month-kw] new-trans-list)))

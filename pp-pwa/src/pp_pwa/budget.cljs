(ns pp-pwa.budget
  (:require
   [pp-pwa.specs :as specs]))

(defn next-item-id
  "Gets the next item id to use for an new item"
  [budget]
  (let [id (->> budget (map :budget-item-id) (apply max))]
    (if (nil? id)
      1
      (inc id))))

(defn add-item
  "Adds a new item to a budget."
  [budget name currency amount]
  (conj (if (nil? budget)
          []
          budget)
        {:budget-item-id (next-item-id budget)
         :budget-item-name name
         :spent {:amount 0 :currency-code currency}
         :limit {:amount amount :currency-code currency}}))

(defn remove-item
  "Removes an item from a budget."
  [budget item-id]
  (remove #{= (:budget-item-id budget) item-id} budget))

(defn spend
  "Increase the spend on an item of a budget."
  [budget item-id amount]
  (for [item budget :when (= (:budget-item-id item) item-id)]
    (update-in item [:spent :amount] + amount)))

(defn reset-item
  "Resets the spend of an item."
  [budget item-id]
  (for [item budget :when (= (:budget-item-id item) item-id)]
    (assoc-in item [:spent :amount] 0)))

(defn set-limit-amount
  "Sets the limit amount of a budget-item of a budget."
  [budget item-id amount]
  (for [item budget :when (= (:budget-item-id item) item-id)]
    (assoc-in item [:limit :amount] amount)))

(defn set-limit-currency
  "Sets the limit currency of a budget-item of a budget."
  [budget item-id currency-code]
  (for [item budget :when (= (:budget-item-id item) item-id)]
    (assoc-in item [:limit :currency] currency-code))
  ;; spent and limit need to be in the same currency
  (for [item budget :when (= (:budget-item-id item) item-id)]
    (assoc-in item [:spent :currency] currency-code)))

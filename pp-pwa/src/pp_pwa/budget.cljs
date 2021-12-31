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
  [budget item]
  (let [item-id (:budget-item-id item)]
    (remove #(= (:budget-item-id %) item-id) budget)))

(defn for-items
  "Applies fn to either all items or, if pred, those for which pred is true."
  ([budget fn] (for-items budget fn #(identity true)))
  ([budget fn pred]
   (vec (map #(if (pred %) (fn %) %) budget))))

(defn assoc-in-items
  "assoc-in value to kws of each item in budget. If pred, then only on items for which pred is true."
  ([budget kws v] (assoc-in-items budget kws v #(identity true)))
  ([budget kws v pred]
   (for-items budget
              #(assoc-in % kws v)
              pred)))

(defn is-item?
  "Checks if item has the supplied item-id"
  [item-id item]
  (= (:budget-item-id item) item-id))

(defn spend
  "Increase the spend on an item of a budget."
  [budget item amount]
  (let [item-id (:budget-item-id item)]
    (for-items budget
               #(update-in % [:spent :amount] + amount)
               #(is-item? item-id %))))

(defn reset-all-items
  "Resets spending on all items of a budget."
  [budget]
  (assoc-in-items budget [:spent :amount] 0))

(defn reset-item
  "Resets spending on an item."
  [budget item]
  (let [item-id (:budget-item-id item)]
    (assoc-in-items budget [:spent :amount] 0 #(is-item? item-id %))))

(defn set-item-name
  "Sets the name of a budget-item of a budget."
  [budget item name]
  (let [item-id (:budget-item-id item)]
    (assoc-in-items budget [:budget-item-name] name #(is-item? item-id %))))

(defn set-limit-amount
  "Sets the limit amount of a budget-item of a budget."
  [budget item amount]
  (let [item-id (:budget-item-id item)]
    (assoc-in-items budget [:limit :amount] amount #(is-item? item-id %))))

(defn set-limit-currency
  "Sets the limit currency of a budget-item of a budget."
  [budget item currency-code]
  (let [item-id (:budget-item-id item)]
    (assoc-in-items
     budget [:limit :currency] currency-code #(is-item? item-id %))
    ;; spent and limit need to be in the same currency
    (assoc-in-items
     budget [:spent :currency] currency-code #(is-item? item-id %))))

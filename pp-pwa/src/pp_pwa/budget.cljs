(ns pp-pwa.budget
  (:require
   [pp-pwa.datetime :as dt]
   [pp-pwa.specs :as specs]
   [pp-pwa.utility :as utility]))

(defn next-item-id
  "Gets the next item id to use for an new item"
  [budget]
  (let [id (->> budget (map :budget-item-id) (apply max))]
    (if (nil? id)
      1
      (inc id))))

(defn create-item
  [budget name amount currency]
  (utility/ensure-identity
   {:budget-item-id (next-item-id budget)
    :budget-item-name name
    :spent {:amount 0 :currency-code currency}
    :limit {:amount amount :currency-code currency}}))


(defn add-item
  "Adds a new item to a budget."
  [budget name currency amount]
  (let [item (create-item budget name amount currency)]
    (assert ::specs/budget-item item)
    (conj item
          (if (nil? budget)
            []
            budget))))

(defn remove-item
  "Removes an item from a budget."
  [budget item]
  (let [item-id (:budget-item-id item)]
    (remove #(= (:budget-item-id %) item-id) budget)))

(defn for-items
  "Applies fn to either all items or, if pred, those for which pred is true."
  ([budget fn] (for-items budget fn #(identity true)))
  ([budget fn pred]
   (mapv #(if (pred %) (fn %) %) budget)))

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

(defn delete-transaction
  [budget transaction]
  (let [id (:budget-item-uuid transaction)
        amount (get-in transaction [:spent :amount])]
    (js/console.log "transaction = " (clj->js transaction))
    (js/console.log "get-in transaction [:datetime-info :ticks]"
                    (get-in transaction [:datetime-info :ticks]))
    (for-items budget
               #(update-in % [:spent :amount] - amount)
               #(and (= id (:id %))
                     (or (not (contains? % :most-recent-reset))
                         (>=
                          (get-in transaction [:datetime-info :ticks])
                          (get-in % [:most-recent-reset :ticks])))))))

(defn spend
  "Increase the spend on an item of a budget."
  [budget item amount]
  (let [item-id (:budget-item-id item)]
    (for-items budget
               #(update-in % [:spent :amount] + amount)
               #(is-item? item-id %))))

(defn reset-items
  [budget pred]
  (-> budget
      (assoc-in-items [:spent :amount] 0 pred)
      (assoc-in-items [:most-recent-reset] (dt/current-datetime-info) pred)))

(defn reset-all-items
  "Resets spending on all items of a budget."
  [budget]
  (reset-items budget (fn [_] true)))

(defn reset-item
  "Resets spending on an item."
  [budget item]
  (let [item-id (:budget-item-id item)]
    (reset-items budget #(is-item? item-id %))))

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

(defn get-item
  [budget item-id]
  (first (filter #(= item-id (:budget-item-id %)) budget)))

(defn sum-limits
  "Sums the amounts of the limits of the items of a budget."
  [budget]
  (reduce + (map #(-> % :limit :amount) budget)))

(defn sum-spents
  "Sums the amounts of the limits of the items of a budget."
  [budget]
  (reduce + (map #(-> % :spent :amount) budget)))

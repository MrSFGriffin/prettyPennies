(ns pp-pwa.specs
  (:require
   [clojure.string :as str]
   [cljs.spec.alpha :as s]
   [cljs.spec.gen.alpha :as gen]))

(def currency-codes ["€" "$"])

(defn currency-code?
  "true iff `cc` is a recognised currency code."
  [cc]
  (some #(= cc %) currency-codes))

(defn natural?
  "true iff x is a natural number."
  [x]
  (and
   (number? x)
   (<= 0 x)))

(defn distinct-budget-item-names?
  "true iff all budget-item-names of all budget-items of budget are distinct."
  [budget]
  (if (empty? budget)
    true
    (apply distinct? (mapv :budget-item-name budget))))


(s/def ::colour-name
  (s/with-gen
    string?
    #(gen/elements ["yellow" "red" "blue" "orange" "pink"])))
(s/def ::css
  (s/with-gen
    string?
    #(gen/elements ["rgb(123, 123, 123)", "rgb(223, 223, 233)"])))
(s/def ::budget-item-colour (s/keys :req-un [::colour-name ::css]))
(s/def ::budget-item-colours (s/coll-of ::budget-item-colour :count 2))
(s/def ::budget-item-id pos-int?)
(s/def ::budget-item-name
  (s/with-gen
    #(and (not (str/blank? %)) (string? %))
    #(gen/elements ["Food" "Gas" "Petrol" "Kids" "Trips"])))
(s/def ::amount
  (s/with-gen
    natural?
    #(s/gen pos-int?)))
(s/def ::currency-code
  (s/with-gen
    currency-code?
    #(gen/elements currency-codes)))
(s/def ::currency-value (s/keys :req-un (::amount ::currency-code)))
(s/def ::spent ::currency-value)
(s/def ::limit ::currency-value)
(s/def ::budget-item (s/keys :req-un [::budget-item-id
                                      ::budget-item-name
                                      ::spent
                                      ::limit]
                          :opt-un [::budget-item-colours]))
(s/def ::budget (s/and (s/coll-of ::budget-item)
                       distinct-budget-item-names?))

(defn humanise-errors
  "Gives human friendly error messages for budget spec failures."
  [budget]
  (if (= (-> (s/explain-data ::budget budget)
             ::cljs.spec.alpha/problems
             first
             :pred
             str)
         "pp-pwa.specs/distinct-budget-item-names?")
    "Budget item names must be unique."
    "Invalid budget"))


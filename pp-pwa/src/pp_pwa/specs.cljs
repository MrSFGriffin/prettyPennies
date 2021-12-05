(ns pp-pwa.specs
  (:require
   [cljs.spec.alpha :as s]))

(defn currency-code?
  "Determines whether `cc` is a currency code."
  [cc]
  (cond
    (= cc "€") true
    (= cc "$") true
    :else false))

(s/def ::name string?)
(s/def ::css string?)
(s/def ::budget-item-colour (s/keys :req-un [::name ::css]))
(s/def ::budget-item-colours (s/coll-of ::budget-item-colour))
(s/def ::budget-item-id int?)
(s/def ::amount int?)
(s/def ::currency-code currency-code?)
(s/def ::spent (s/keys :req-un (::amount ::currency-code)))
(s/def ::budget-item (s/keys :req-un [::budget-item-id
                                   ::name
                                   ::spent
                                   ::limit]
                          :opt-un [::budget-item-colors]))
(s/def ::budget (s/coll-of ::budget-item))

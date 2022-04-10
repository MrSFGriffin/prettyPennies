(ns pp-pwa.utility
  (:require))

(defn ensure-identity
  [x]
  (assoc x :id (or (:id x) (-> (random-uuid) str))))


(defn currency-str
  [amount]
  (.toLocaleString
   (or amount 0) "sk-SK" #js {:style "currency"
                              :currency "EUR"
                              :minimumFractionDigits 0}))

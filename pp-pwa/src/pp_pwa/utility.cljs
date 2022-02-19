(ns pp-pwa.utility
  (:require))

(defn ensure-identity
  [x]
  (assoc x :id (or (:id x) (-> (random-uuid) str))))

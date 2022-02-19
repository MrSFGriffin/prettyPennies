(ns pp-pwa.core
  (:require
   [cljs.spec.alpha :as s]
   [orchestra.core :refer-macros [defn-spec]]
   [orchestra-cljs.spec.test :as st]
   [expound.alpha :as expound]
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [pp-pwa.events :as events]
   [pp-pwa.routes :as routes]
   [pp-pwa.storage :as storage]
   [pp-pwa.views :as views]
   [pp-pwa.config :as config]))

(defn-spec instrument-test int?
  [b boolean?]
  (if b 1 :nah))

(defn instrument
  []
  (st/instrument)
  (set! s/*explain-out* expound/printer))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn load-transactions
  []
  (storage/get-transaction-years
   (fn [years]
     (re-frame/dispatch [::events/set-transaction-years years]))))

(defn load-budget
  "Load budget from persistent storage"
  []
  (storage/get-budget-items
   (fn [b]
     (re-frame/dispatch [::events/set-budget b])
     (storage/get-plan-income
      (fn [i]
        (re-frame/dispatch [::events/set-plan-income i])
        (storage/get-plan-items
         (fn [items]
           (re-frame/dispatch [::events/set-plan-items items]))))))))

(defn load-data
  []
  ;; to do a loading indictator: increment and decrement a counter
  (storage/get-budget-items
   (fn [b]
     (re-frame/dispatch [::events/set-budget b])))
  (storage/get-plan-income
   (fn [i]
     (re-frame/dispatch [::events/set-plan-income i])))
  (storage/get-plan-items
   (fn [items]
     (re-frame/dispatch [::events/set-plan-items items])))
  (storage/get-transaction-years
   (fn [years]
     (re-frame/dispatch [::events/set-transaction-years years]))))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (load-data)
  (dev-setup)
  (mount-root))

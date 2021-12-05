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

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))

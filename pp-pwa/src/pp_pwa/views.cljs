(ns pp-pwa.views
  (:require
   ["semantic-ui-react" :as ui]
   [re-frame.core :as re-frame]
   [pp-pwa.styles :as styles]
   [pp-pwa.events :as events]
   [pp-pwa.routes :as routes]
   [pp-pwa.subs :as subs]
   ))

(defn categories-panel []
  [:p1 "Categories here"])

(defn money-panel []
  [:> ui/Card
   {:centered true}
   [:> ui/Grid.Row
    [:h1 "MONEY"]]
   (categories-panel)])

(defn home-panel []
  [:div
   [:> ui/Grid
    {:equal true
     :class (styles/full-height)
     :text-align "center"
     :vertical-align "middle"}
    [:> ui/Grid.Column
     (money-panel)]]])

(defmethod routes/panels :home-panel [] [home-panel])

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:on-click #(re-frame/dispatch [::events/navigate :home])}
     "go to Home Page"]]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    (routes/panels @active-panel)))

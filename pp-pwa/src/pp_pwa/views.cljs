(ns pp-pwa.views
  (:require
   ["semantic-ui-react" :as ui]
   [cljs.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [pp-pwa.styles :as styles]
   [pp-pwa.events :as events]
   [pp-pwa.routes :as routes]
   [pp-pwa.specs :as specs]
   [pp-pwa.subs :as subs]))

(def budget-item-border-colour-css (styles/colour :apple :system-teal :css))

(def budget-item-border-style
  (str "solid 1px " (styles/colour :apple :system-teal :css)))

(def button-style {:background-color (styles/colour :web :pink :css)
                   :color (styles/colour :web :white :css)})

(defn pink-button
  ([label] (pink-button label #()))
  ([label on-click]
   [:> ui/Button
    {:style button-style
     :onClick on-click}
    label]))

;; (defn budget []
;;   (colour-budget [{:budget-item-id 1
;;                    :name "Food"
;;                    :spent {:amount 0 :currency-code "€"}
;;                    :limit {:amount 200 :currency-code "€"}}
;;                   {:budget-item-id 2
;;                    :name "Petrol"
;;                    :spent {:amount 120:currency-code "€"}
;;                    :limit {:amount 200 :currency-code "€"}}
;;                   {:budget-item-id 3
;;                    :name "House"
;;                    :spent {:amount 201 :currency-code "€"}
;;                    :limit {:amount 200 :currency-code "€"}}
;;                   {:budget-item-id 3
;;                    :name "Children"
;;                    :spent {:amount 200 :currency-code "€"}
;;                    :limit {:amount 200 :currency-code "€"}}]))

(defn budget-item-row [budget-item]
  (let [limit (-> budget-item :limit :amount)
        spent (-> budget-item :spent :amount)
        left (- limit spent)
        negative (< left 0)
        label (cond
                (not negative) "left"
                negative "minus"
                :else "")
        colour (budget-item :colour)]
    [:> ui/Grid.Row
     {:style {:padding-bottom 0
              :margin-bottom "-14px"}}
     [:> ui/Grid.Column
      {:width 1}]
     [:> ui/Grid.Column
      {:width 7
       :style {:padding-right 0}}
      [:div
       {:style {:border-top budget-item-border-style
                :border-left (str "3px solid " (colour :css))
                :padding "0.4em"}}
       [:> ui/Grid
        [:> ui/Grid.Row
         [:> ui/Grid.Column
          {:text-align "left"
           :style {:font-size "1.2em"}}
          (:name budget-item)]]
        [:> ui/Grid.Row
         {:style {:padding 0}}
         [:> ui/Grid.Column
          {:text-align "left"}
          [:> ui/Progress
           {:size "tiny"
            :total limit
            :value (min spent limit)
            :color (colour :name)}]]]]]]
     [:> ui/Grid.Column
      {:width 6
       :style {:padding-left 0}
       :text-align "right"}
      [:> ui/Grid.Row
       {:style {:border-top budget-item-border-style
                :padding "0.4em"}}
       [:div
        {:style {:color (if (not negative) "black" "red")
                 :font-weight "bold"
                 :font-size "1.3em"}}
        (str label " "
             (-> budget-item :spent :currency-code)
             (Math/abs left))]]
      [:> ui/Grid.Row
       [:div
        (str (-> budget-item :limit :currency-code)
             (-> budget-item :limit :amount))]]]]))

(defn spend-control-panel [budget]
  [:> ui/Grid.Row
   [:> ui/Grid
    [:> ui/Grid.Column
     {:width 1}]
    [:> ui/Grid.Column
     {:width 4
      :style {:margin "0 1em 0.7em 0"}}
     (pink-button "Spend")]
      [:> ui/Grid.Column
       {:width 4}
       (pink-button "Reset")]]])

(defn adding-item-panel []
 [:> ui/Grid.Row
  {:align "left"
   :style {:margin-top "0.7em"
           :padding-left "0.1em"}}
  [:> ui/Grid.Column
   [:p "Adding item"]]
  [:> ui/Grid.Column
   {:style {:margin-top "0.5em"}}
   (pink-button
    "Cancel" #(re-frame/dispatch [::events/toggle-adding-item]))
   (pink-button "Add")]])

(defn budget-control-panel [budget]
  (let [adding-item @(re-frame/subscribe [::subs/adding-item])]
    [:div
     {:style {:margin-bottom "1em"
              :margin-top "1em"
              :padding "1em 1em 1em 0.1em"
              :border-top budget-item-border-style}}
     [:> ui/Grid
      [:> ui/Grid.Row
       {:style {:padding "0.5em 0 0 0"
                :margin-bottom "0.5em"}}
       [:> ui/Grid.Column
        {:align "left"}
        [:h3
         "Edit Items"]]]
      (when (not adding-item)
        [:div
         [:> ui/Grid.Column
          {:width 5
           :style {:margin-right "0.7em"}}
          (pink-button "Add" #(re-frame/dispatch [::events/toggle-adding-item]))]
         [:> ui/Grid.Column
          {:width 5}
          (when (> 0 (count budget))
            (pink-button "Delete"))]])]
     (when adding-item
       (adding-item-panel))]))

(defn budget-panel [budget]
  [:> ui/Grid
   (map #(budget-item-row %) budget)
   [:> ui/Grid.Row
    {:style {:padding-top 0}}
    [:> ui/Grid.Column
     {:width 1}]
    [:> ui/Grid.Column
     {:width 13}
     (budget-control-panel budget)]]])

(defn money-panel [budget]
  {:pre [(s/valid? ::specs/budget budget)]}
  (let [name (re-frame/subscribe [::subs/name])]
    [:> ui/Card
     {:centered true}
     [:> ui/Grid.Row
      [:h1 {:style {:margin"0.3em 0 0.3em 0"}} @name]]
     (when (> 0 (count budget))
       [:> ui/Grid.Row
        (spend-control-panel budget)])
     [:> ui/Grid.Row
      (budget-panel budget)]]))

(defn home-panel []
  (let [budget @(re-frame/subscribe [::subs/coloured-budget])]
    [:div
     [:> ui/Grid
      {:class (styles/full-height)
       :text-align "center"
       :vertical-align "middle"}
      [:> ui/Grid.Column
       (money-panel budget)]]]))

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

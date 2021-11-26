(ns pp-pwa.views
  (:require
   ["semantic-ui-react" :as ui]
   [re-frame.core :as re-frame]
   [pp-pwa.styles :as styles]
   [pp-pwa.events :as events]
   [pp-pwa.routes :as routes]
   [pp-pwa.subs :as subs]
   ))

(defn spend-control-panel []
  [:> ui/Grid.Row
   [:> ui/Grid
    [:> ui/Grid.Column
     {:width 1}]
    [:> ui/Grid.Column
     {:width 4
      :style {:margin "0 1em 0.7em 0"}}
     [:> ui/Button
      {:color "pink"}
      "Spend"]]
    [:> ui/Grid.Column
     {:width 4}
     [:> ui/Button
      {:color "pink"}
      "Reset"]]]])

(defn category-control-panel []
  [:div
   {:style {:margin-bottom "1em"
            :margin-top "1em"
            :padding "1em 1em 1em 1em"
            :border-top "1px solid skyblue"
            :border-bottom "1px solid skyblue"
            :border-left "3px solid magenta"}}
   [:> ui/Grid
    [:> ui/Grid.Row
     {:style {:padding "0.5em 0 0 0"
              :margin 0}}
     [:> ui/Grid.Column
      {:align "left"}
      [:h3
       "Edit Categories"]]]
    [:> ui/Grid.Column
     {:width 5
      :style {:margin-right "0.7em"}}
     [:> ui/Button
      {:color "pink"}
      "Add"]]
    [:> ui/Grid.Column
     {:width 5}
     [:> ui/Button
      {:color "pink"}
      "Delete"]]]])

(defn category-row [category color]
  (let [left (- (-> category :limit :amount)
                (-> category :spent :amount))
        positive (> left 0)
        label (if positive "left" "minus")]
    [:> ui/Grid.Row
     {:style {:padding-bottom 0
              :margin-bottom "-14px"}}
     [:> ui/Grid.Column
      {:width 1}]
     [:> ui/Grid.Column
      {:width 7
       :style {:padding-right 0}}
      [:div
       {:style {:border-top "1px solid skyblue"
                :border-left (str "3px solid " color)
                :padding "0.4em"}}
       [:> ui/Grid
        [:> ui/Grid.Row
         [:> ui/Grid.Column
          {:text-align "left"
           :style {:font-size "1.2em"}}
          (:name category)]]
        [:> ui/Grid.Row
         {:style {:padding 0}}
         [:> ui/Grid.Column
          {:text-align "left"}
          [:> ui/Progress
           {:size "tiny"
            :color color
            :style {:background-color (str "light" color)}}]]]]]]
     [:> ui/Grid.Column
      {:width 6
       :style {:padding-left 0}
       :text-align "right"}
      [:> ui/Grid.Row
       {:style {:border-top "1px solid skyblue"
                :padding "0.4em"}}
       [:div
        {:style {:font-weight "bold"
                 :font-color (if positive "black" "red")
                 :font-size "1.3em"}}
        (str label " "
             (-> category :spent :currency-code)
             (Math/abs left))]]
      [:> ui/Grid.Row
       [:div
        (str (-> category :limit :currency-code)
             (-> category :limit :amount))]]]]))

(defn category-panel []
  [:> ui/Grid
   (category-row {:category-id 1
                  :name "Food"
                  :spent {:amount 201 :currency-code "€"}
                  :limit {:amount 200 :currency-code "€"}}
                 "green")
   (category-row {:category-id 1
                  :name "Petrol"
                  :spent {:amount 201 :currency-code "€"}
                  :limit {:amount 200 :currency-code "€"}}
                 "pink")
   (category-row {:category-id 1
                  :name "House"
                  :spent {:amount 201 :currency-code "€"}
                  :limit {:amount 200 :currency-code "€"}}
                 "blue")
   [:> ui/Grid.Row
    {:style {:padding-top 0}}
    [:> ui/Grid.Column
     {:width 1}]
    [:> ui/Grid.Column
     {:width 13}
     (category-control-panel)]]])

(defn money-panel []
  [:> ui/Card
   {:centered true}
   [:> ui/Grid.Row
    [:h1 {:style {:margin"0.3em 0 0.3em 0"}} "MONEY"]]
   [:> ui/Grid.Row
    (spend-control-panel)]
   [:> ui/Grid.Row
    (category-panel)]])

(defn home-panel []
  [:div
   [:> ui/Grid
    {:class (styles/full-height)
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

(ns pp-pwa.styles
  (:require-macros
    [garden.def :refer [defcssfn]])
  (:require
    [spade.core   :refer [defglobal defclass]]
    [garden.units :refer [deg px]]
    [garden.color :refer [rgba]]))

(defcssfn linear-gradient
 ([c1 p1 c2 p2]
  [[c1 p1] [c2 p2]])
 ([dir c1 p1 c2 p2]
  [dir [c1 p1] [c2 p2]]))

(defglobal defaults
  [:body
   {:color               :red
    :background-color    :white
    :background-image "url('/img/flower_small.webp') !important"
    :background-size "cover !important"
    ;; :background-image    [(linear-gradient :white (px 2) :transparent (px 2))
    ;;                       (linear-gradient (deg 90) :white (px 2) :transparent (px 2))
    ;;                       (linear-gradient (rgba 255 255 255 0.3) (px 1) :transparent (px 1))
    ;;                       (linear-gradient (deg 90) (rgba 255 255 255 0.3) (px 1) :transparent (px 1))]
    ;; :background-size     [[(px 100) (px 100)] [(px 100) (px 100)] [(px 20) (px 20)] [(px 20) (px 20)]]
    }])

(defclass full-height
  []
  {:height "100vh"})


(defclass level1
  []
  {:color :green})

(ns pp-pwa.styles
  (:require-macros
   [garden.def :refer [defcssfn]])
  (:require
   [spade.core   :refer [defglobal defclass]]
   [garden.color :refer [rgb]]))

(defcssfn linear-gradient
 ([c1 p1 c2 p2]
  [[c1 p1] [c2 p2]])
 ([dir c1 p1 c2 p2]
  [dir [c1 p1] [c2 p2]]))

(defglobal defaults
  [:body
   {:color               :red
    :background-color    "#34dbeb"
    :background-image "url('/img/flower_small.webp') !important"
    :background-size "cover !important"
    :scroll-boundary-behavior "none"}
   :bar ])

(defclass full-height [] {:height "100vh"})

(defclass level1 [] {:color :green})

(def colours
  "Palette of colours."
  {:apple
   {:system-pink {:r 255 :g 55:b 95:a 0}
    :system-blue {:r 10 :g 132 :b 255 :a 0}
    :system-teal {:r 64 :g 200 :b 224 :a 0}}
   :material
   {:yellow {:r 255 :g 235 :b 59}
    :yellow-shade200 {:r 255 :g 245 :b 157 :a 0}
    :green {:r 76 :g 175 :b 80 :a 0}
    :green-shade200 {:r 165 :g 214 :b 167 :a 0}
    :pink {:r 233 :g 30 :b 99 :a 0}
    :pink-shade200 {:r 244 :g 143 :b 177 :a 0}
    :blue {:r 33 :g 150 :b 243 :a 0}
    :blue-shade200 {:r 144 :g 202 :b 249 :a 0}
    :orange {:r 255 :g 152 :b 0 :a 0}
    :orange-shade200 {:r 255 :g 204 :b 128 :a 0}}
   :semantic
   {:red {:r 219 :g 40 :b 40}
    :orange {:r 242 :g 113 :b 28}
    :yellow {:r 251 :g 189 :b 8}
    :olive {:r 181 :g 204 :b 24 }
    :green {:r 33 :g 186 :b 69}
    :teal {:r 0 :g 181 :b 173}
    :blue {:r 33 :g 133 :b 208}
    :violet {:r 100 :g 53 :b 201}
    :purple {:r 163 :g 51 :b 200}
    :pink {:r 224 :g 57 :b 151}
    :brown {:r 165 :g 103 :b 63}
    :grey {:r 118 :g 188 :b 188 }
    :black {:r 27 :g 28 :b 29}}
   :web
   {:pink {:r 224 :g 57 :b 151}
    :white {:r 255 :g 255 :b 255}}})

(defn css
  "Colour as css"
  [colour]
  (str "rgb(" (:r colour) ", " (:g colour) ", " (:b colour) "" ")"))

(defn garden
  "Colour as garden rgb"
  [colour]
  (rgb (:r colour) (:g colour) (:b colour)))

(defn colour
  "Formats a styles/colours as :css or :garden."
  [palette colour-name format]
  (let [colour (get-in colours [palette colour-name])]
    (cond
      (= format :name) (name colour-name)
      (= format :css) (css colour)
      :else (garden colour))))

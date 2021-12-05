[cljs.spec.alpha :as s]
[orchestra.core :refer-macros [defn-spec]]

(def app-palettes-specced
  "Palette of colours."
  [{:name "Apple"
    :colours [{:name "SystemPink" :rgba {:r 255 :g 45 :b 85 :a 0}}
              {:name "SystemBlue" :rgba {:r 94 :g 132 :b 255 :a 0}}
              {:name "SystemTeal" :rgba {:r 64 :g 200 :b 224 :a 0}}]}
   {:name "Flutter"
    :colours [{:name "yellow" :rgba {:r 255 :g 235 :b 59 :a 0}}
              {:name "yellow-shade200" :rgba {:r 255 :g 245 :b 157 :a 0}}
              {:name "green" :rgba {:r 76 :g 175 :b 80 :a 0}}
              {:name "green-shade200" :rgba {:r 165 :g 214 :b 167 :a 0}}
              {:name "pink" :rgba {:r 233 :g 30 :b 99 :a 0}}
              {:name "pink-200" :rgba {:r 244 :g 143 :b 177 :a 0}}
              {:name "blue" :rgba {:r 33 :g 150 :b 243 :a 0}}
              {:name "blue-shade200" :rgba {:r 144 :g 202 :b 249 :a 0}}
              {:name "orange" :rgba {:r 255 :g 152 :b 0 :a 0}}
              {:name "orange-shade200" :rgba {:r 255 :g 204 :b 128 :a 0}}]}])

(s/def ::a (s/and number? #(<= 0 % 1)))
(s/def ::b (s/int-in 0 256))
(s/def ::g (s/int-in 0 256))
(s/def ::r (s/int-in 0 256))
(s/def ::rgba (s/keys :req-un [::r ::g ::b ::a]))
(s/def ::name string?)
(s/def ::colour (s/keys :req-un [::name ::rgba]))
(s/def ::colours (s/coll-of ::colour))
(s/def ::palette (s/keys :req-un [::name ::colours]))
(s/def ::palettes (s/coll-of ::palette))

(defn-spec colour-style number?
  "Converts a colour into a style."
  [colour ::colour]
  nil)


;; spec notes:
;; at REPL, require:
;; [clojure.test.check :as stc]
;; [cljs.spec.alpha :as s]
;; [cljs.spec.gen.alpha :as gen]

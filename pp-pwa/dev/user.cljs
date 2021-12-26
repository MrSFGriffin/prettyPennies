(ns cljs.user
  "Commonly used symbols for easy access in the ClojureScript REPL during
  development."
  (:require
    [cljs.repl :refer (Error->map apropos dir doc error->str ex-str ex-triage
                       find-doc print-doc pst source)]
    [clojure.pprint :refer (pprint)]
    [clojure.string :as str]
    [clojure.test.check :as stc]
    [cljs.spec.alpha :as s]
    [cljs.spec.gen.alpha :as gen]
    [pp-pwa.specs :as specs]))

(comment
  (pprint (str/trim "This line suppresses some clj-kondo warnings.")))


(defn generate-budget
  "Generates a budget."
  []
  (gen/generate (s/gen ::specs/budget)))

(defn eight-bit-int?
  "true iff value is between 0 and 255."
  [value]
  (<= 0 value 255))

(s/fdef eight-bit-int?
  :args (s/cat :value int?)
  :ret int?
  :fn #(<= 0 % 255))

(def eight-bit-err
  {:error/fn '(fn [{:keys [value]} _]
                (str "8-bit error: " value " is not between 0 and 255"))}
  )

(defn eight-bit-int?-schema []
  [:fn
   {:error/fn '(fn [{:keys [value]} _]
                 (str "8-bit error: " value " is not between 0 and 255"))}
   eight-bit-int?])

(def Colour-Map ; Malli spec
  "Colour-map schema."
  [:map {:closed true}
   [:name string?]
   [:colours [:vector
              [:map {:closed true}
               [:name string?]
               [:rgb [:map {:closed true}
                      [:r eight-bit-int?-schema]
                      [:g eight-bit-int?-schema]
                      [:b eight-bit-int?-schema]]]]]]])

(ns pp-pwa.storage)

(defn get-indexeddb
 []
 (or
  (. js/indexedDB)
  (. js/mozIndexedDB)
  (. js/webkitIndexedDB)
  (. js/msIndexedDB)))

;; (defn open-db
;;  [name]
;;  (let [indexedDB (get-indexeddb)]
;;    (when indexedDB
;;      (. indexedDB (open "PrettyPennies")))))

(defn error [e]
  (.error js/console "IndexedDB error: " e))

(defn open
  [name]
  (let [db (atom nil)
        request (.open (get-indexeddb) "pretty-pennies")]
    (set! (.-onupgradeneeded request
                             (fn [e]
                               (reset! db (.. e -target -result))
                               (set! (.. e -target -transaction -onerror) error)
                               (.createObjectStore @db "budget" #js {:keyPath "id"}))))))

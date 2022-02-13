(ns pp-pwa.storage
  (:require
   [cljs.spec.alpha :as s]
   [pp-pwa.specs :as specs]))

(defn get-indexeddb
  []
  (or
   (.-indexedDB js/window)
   (.-mozIndexedDB js/window)
   (.-webkitIndexedDB js/window)
   (.-msIndexedDB js/window)))

(defn error
  [e]
  (.error js/console "IndexedDB error: " e))

(defn set-error-handler
  "Sets up error handling on a request."
  [request]
  (set! (.-onerror request) error))

(def db-name "pretty-order")
(def db-version 20)

(def budget-store-name "budget")
(def transaction-store-name "transaction")
(def plan-income-store-name "plan-income")
(def plan-items-store-name "plan-items")
(def budget-view-store-name "budget-view")
(def income-store-name "income")

(def store-names
  [budget-store-name
   plan-income-store-name
   plan-items-store-name
   transaction-store-name])

(def defunct-store-names
  [budget-view-store-name
   income-store-name])

(defn contains-store
  "Nil unless db contains an object store named store-name."
  [db store-name]
  (let [names (js->clj (.-objectStoreNames db))]
    (some #{store-name} names)))

(defn ensure-store-nonexistent
  "Creates an objectStore iff it doesn't already exist."
  [db store-name]
  (when (contains-store db store-name)
    (.deleteObjectStore db store-name)))

(defn ensure-store-exists
  "Creates an objectStore iff it doesn't already exist."
  [db store-name]
  (when (not (contains-store db store-name))
    (.createObjectStore db store-name (clj->js {:keyPath "id"}))))

(defn call-with-db
  "Calls a fn with indexeddb and error handler as arguments. The success-fn has the option of calling the error handler. It takes a request as its only argument."
  [success-fn]
  (let [request (.open (get-indexeddb) db-name db-version)]
    (set! (.-onerror request) error)
    (set! (.-onupgradeneeded request)
          (fn [e]
            (let [db (.. e -target -result)]
              (doall (map #(ensure-store-nonexistent db %) defunct-store-names))
              (doall (map #(ensure-store-exists db %) store-names)))))
    (set! (.-onsuccess request)
          (fn [e]
            (success-fn (.. e -target -result))))))

(defn save-map
  "Saves a map to persistent storage. It must have an :id key with a value."
  [item success-fn store-name]
  (call-with-db
   (fn
     [db]
     (let [transaction (.transaction db #js [store-name] "readwrite")
           store (.objectStore transaction store-name)
           request (.put store (clj->js item))]
       (set-error-handler request)
       (set! (.-onsuccess request)
             (fn [_e] (success-fn)))))))

(defn save-transaction-map
  ([trans-map success-fn]
   (save-transaction-map trans-map success-fn transaction-store-name))
  ([trans-map success-fn store-name]
   (save-map trans-map success-fn store-name)))

(defn save-budget-item
  "Saves a budget-item from persistent storage."
  ([item success-fn] (save-budget-item item success-fn budget-store-name))
  ([item success-fn store-name]
   (let [item (assoc item :id (:budget-item-id item))]
     (when (not (:read-only item))
       (save-map item success-fn store-name)))))

(defn save-plan-income
  "Saves an income to persistent storage."
  [income success-fn]
  (save-map {:id 1 :income income} success-fn plan-income-store-name))

(defn save-plan-item
  "Saves a budget-view-item"
  [item success-fn] (save-budget-item item success-fn plan-items-store-name))

(defn delete-map
  "Deletes a map from persistent storage. It must have an :id key with a value."
  [item success-fn store-name]
  (call-with-db
   (fn
     [db]
     (let [transaction (.transaction db #js [store-name] "readwrite")
           store (.objectStore transaction store-name)
           request (.delete store (:id item))]
       (set-error-handler request)
       (set! (.-onsuccess request)
             (fn [e] (success-fn)))))))

(defn delete-budget-item
  "Deletes a budget-item from persistent storage."
  ([item success-fn] (delete-budget-item item success-fn budget-store-name))
  ([item success-fn store-name]
   (let [item (assoc item :id (:budget-item-id item))]
     (delete-map item success-fn store-name))))

(defn delete-plan-item
  "Deletes a budget-view-item"
  [item success-fn] (delete-budget-item item success-fn plan-items-store-name))

(defn get-maps
  "Retrieves maps from persistent storage and passes them to success-fn."
  [success-fn store-name]
  (call-with-db
   (fn
     [db]
     (let [transaction (.transaction db #js [store-name] "readonly")
           store (.objectStore transaction store-name)
           request (.getAll store)]
       (set-error-handler request)
       (set! (.-onsuccess request)
             (fn
               [e]
               (success-fn
                (js->clj (.-result request) :keywordize-keys true))))))))

(defn get-transaction-map
  ([success-fn] (get-transaction-map success-fn transaction-store-name))
  ([success-fn store-name]
   (let [wrapped-success-fn (fn [transaction-map]
                              (success-fn (or
                                           (first transaction-map)
                                           {:id 1})))]
     (get-maps wrapped-success-fn store-name))))

(defn get-budget-items
  "Retrieves budget items from persistent storage and passes them to success-fn."
  ([success-fn] (get-budget-items success-fn budget-store-name))
  ([success-fn store-name] (get-maps success-fn store-name)))

(defn get-plan-income
  "Retrieves a budget view income from persistent storage."
  [success-fn]
  (let [wrapped-success-fn (fn [income-maps]
                             (let [income-map (first income-maps) ; 1 at most
                                   income (:income income-map)
                                   income (if (nil? income) 0 income)]
                               (success-fn income)))]
    (get-maps wrapped-success-fn plan-income-store-name)))

(defn get-plan-items
  [success-fn]
  (get-maps success-fn plan-items-store-name))

(ns pp-pwa.storage
  (:require))

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

(def budget-store-name "budget")

(defn call-with-db
  "Calls a fn with indexeddb and error handler as arguments. The success-fn has the option of calling the error handler. It takes a request as its only argument."
  [success-fn]
  (let [request (.open (get-indexeddb) db-name)]
    (set! (.-onerror request) error)
    (set! (.-onupgradeneeded request)
          (fn [e]
            (let [db (.. e -target -result)]
              (.createObjectStore db budget-store-name (clj->js {:keyPath "id"})))))
    (set! (.-onsuccess request)
          (fn [e]
            (success-fn (.. e -target -result))))))

(defn save-budget-item
  "Deletes a budget-item from persistent storage."
  [item success-fn]
  (call-with-db
   (fn
     [db]
     (let [transaction (.transaction db #js [budget-store-name] "readwrite")
           store (.objectStore transaction budget-store-name)
           item (assoc item :id (:budget-item-id item))
           request (.put store (clj->js item))]
       (set-error-handler request)
       (set! (.-onsuccess request)
             (fn [e] (success-fn)))))))

(defn delete-budget-item
  "Saves a budget-item to persistent storage."
  [item success-fn]
  (call-with-db
   (fn
     [db]
     (let [transaction (.transaction db #js [budget-store-name] "readwrite")
           store (.objectStore transaction budget-store-name)
           request (.delete store (:budget-item-id item))]
       (set-error-handler request)
       (set! (.-onsuccess request)
             (fn [e] (success-fn)))))))

(defn get-budget-items
  "Retrieves budget items from persistent storage and passes them to success-fn."
  [success-fn]
  (call-with-db
   (fn
     [db]
     (let [transaction (.transaction db #js [budget-store-name] "readonly")
           store (.objectStore transaction budget-store-name)
           request (.getAll store)]
       (set-error-handler request)
       (set! (.-onsuccess request)
             (fn
               [e]
               (success-fn
                (js->clj (.-result request) :keywordize-keys true))))))))

;; (defn save-budget-items
;;   [store request items]
;;   (when (seq items)
;;     (let [item (rest items)
;;           put-fn #(.put store (clj->js item))]
;;       (set! (.-onerror request)
;;             (save-budget-items store (put-fn) (rest items))
;;             (set! (.-onsuccess request)
;;                   (save-budget-items store (put-fn) (rest items)))))))

;; (defn save-budget
;;   "Saves a budget to persistent storage. complete-fn is called when the save is completed."
;;   [budget complete-fn]
;;   (call-with-db
;;    (fn
;;      [db]
;;      (let [transaction (.transaction db #js [budget-store-name] "readwrite")
;;            store (.objectStore transaction budget-store-name)
;;            put-fn #(.put store (clj->js %))
;;            request (put-fn (first budget))]))))

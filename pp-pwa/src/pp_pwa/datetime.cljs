(ns pp-pwa.datetime
  (:require))

(defn current-year []
  (.. (js/Date.) getFullYear))

(defn current-month []
  (+ 1 (.. (js/Date.) getMonth)))


(defn current-datetime []
  (.. (js/Date.) toLocaleString))

(defn date-time-format []
  (.. js/Intl DateTimeFormat resolvedOptions))

(defn locale []
  (.. (date-time-format) -locale))

(defn time-zone []
  (.. (date-time-format) -timeZone))

(defn month-name [month-number]
  (nth ["January" "February" "March"
        "April" "May" "June"
        "July" "August" "September"
        "October" "November" "December"]
       (dec month-number)))

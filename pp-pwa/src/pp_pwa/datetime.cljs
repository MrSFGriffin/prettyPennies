(ns pp-pwa.datetime
  (:require))

(defn current-year-and-month []
  (let [date (js/Date.)]
    {:year (.. date getFullYear)
     :month (.. date getMonth)}))

(defn current-year []
  (.. (js/Date.) getFullYear))

(defn current-month []
  (.. (js/Date.) getMonth))

(defn date-time-format []
  (.. js/Intl DateTimeFormat resolvedOptions))

(defn locale []
  (.. (date-time-format) -locale))

(defn time-zone []
  (.. (date-time-format) -timeZone))

(defn current-datetime-info []
  (let [now (js/Date.)
        formatted (.. now toLocaleString)
        ticks (.. now getTime)]
    {:datetime formatted
     :locale (locale)
     :ticks ticks
     :timezone (time-zone)}))

(defn month-name [month-number]
  (nth ["January" "February" "March"
        "April" "May" "June"
        "July" "August" "September"
        "October" "November" "December"]
       month-number))

(defn month-number [month-name]
  (get {"January" 0
        "February" 1
        "March" 2
        "April" 3
        "May" 4
        "June" 5
        "July" 6
        "August" 7
        "September" 8
        "October" 9
        "November" 10
        "December" 11}
       month-name))

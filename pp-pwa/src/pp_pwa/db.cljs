(ns pp-pwa.db)

(def default-db
  {:name "Money"
   :budget []
   :sync-status {:indexeddb :not-synced
                 :pp-service :not-synced}})

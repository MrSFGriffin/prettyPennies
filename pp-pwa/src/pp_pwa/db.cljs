(ns pp-pwa.db)

(def default-db
  {:name "Pretty Pennies"
   :budget []
   :sync-status {:indexeddb :not-synced
                 :pp-service :not-synced}})

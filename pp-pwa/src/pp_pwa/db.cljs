(ns pp-pwa.db)

(def default-db
  {:name "Money"
   :budget []
   :transactions {:years []}
   :view :money
   :view-mode :budget
   :main-menu-mode :budget
   :budget-data-view :table
   :budget-view {:income 0 :budget []}
   :sync-status {:indexeddb :not-synced
                 :pp-service :not-synced}})

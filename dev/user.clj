(ns user
  (:require [coffee-table.config :as config]
            [coffee-table.migrations :as migrations]
            [coffee-table.system :refer [new-system]]
            [reloaded.repl :refer [system init start stop go reset reset-all]]))

(reloaded.repl/set-init! #(new-system :dev))

(defn migrate []
  (migrations/migrate :dev))

(defn rollback []
  (migrations/rollback :dev))

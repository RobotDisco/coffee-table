(ns user
  (:require [coffee-table.component.database :as db]
            [coffee-table.system :refer [new-system]]
            [reloaded.repl :refer [system init start stop go reset reset-all]]))

(reloaded.repl/set-init! #(new-system :dev))

(defn migrate []
  (db/migrate (:db system)))

(defn rollback []
  (db/rollback (:db system)))

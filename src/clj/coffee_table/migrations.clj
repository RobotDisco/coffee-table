(ns coffee-table.migrations
  (:require [coffee-table.config :as config]
            [migratus.core :as migrations]))

(defn migrate
  "Roll the database forward to the latest migration."
  [profile]
  (let [cfg (config/config profile)]
    (migrations/migrate (config/migratus cfg))))

(defn rollback
  "Roll back the latest migration from the DB"
  [profile]
  (let [cfg (config/config profile)]
    (migrations/rollback (config/migratus cfg))))

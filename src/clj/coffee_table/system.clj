(ns coffee-table.system
  "Components and their dependency relationships"
  (:require [coffee-table.component.database :refer [new-database]]
            [coffee-table.config :refer [config]]
            [com.stuartsierra.component :refer [system-map system-using]]
            [taoensso.timbre :as timbre]
            [coffee-table.config :as config]))

(timbre/refer-timbre)

(defn new-system-map
  "Create the system. See https://github.com/stuartsierra/component"
  [config]
  (system-map
   :database (new-database (config/database-spec config))))

(defn new-system
  "Construct a new system, configured with the given profile"
  [profile]
  (let [config (config profile)]
    (new-system-map config)))

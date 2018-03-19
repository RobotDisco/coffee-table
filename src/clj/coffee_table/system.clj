(ns coffee-table.system
  "Components and their dependency relationships"
  (:require [coffee-table.component.database :refer [new-database]]
            [coffee-table.component.visits :refer [new-visits]]
            [coffee-table.component.web-server :refer [new-web-server]]
            [coffee-table.config :as ctcfg]
            [com.stuartsierra.component :refer [system-map system-using]]
            [taoensso.timbre :as timbre]
            [coffee-table.config :as config]))

(timbre/refer-timbre)

(defn new-system-map
  "Create the system. See https://github.com/stuartsierra/component"
  [config]
  (system-map
   :db (new-database {:spec (ctcfg/database-spec config)
                      :migratus (ctcfg/migratus config)})
   :visits (new-visits)
   :web (new-web-server {:host (ctcfg/webserver-host config)
                         :port (ctcfg/webserver-port config)})))

(defn new-dependency-map
  "Declare the dependency relationship between components. See https://github.com/stuartsierra/component"
  []
  {:visits [:db]
   :web [:visits]})

(defn new-system
  "Construct a new system, configured with the given profile"
  [profile]
  (let [config (ctcfg/config profile)]
    (system-using
     (new-system-map config)
     (new-dependency-map))))

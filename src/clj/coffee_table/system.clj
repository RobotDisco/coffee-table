(ns coffee-table.system
  "Components and their dependency relationships"
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [com.stuartsierra.component :refer [system-map system-using]]))

(defn config
  "Read EDN config, with the given profile. See Aero docs at
  https://github.com/juxt/aero for details."
  [profile]
  (aero/read-config (io/resource "config.edn") {:profile profile}))

(defrecord DoNothing []
  com.stuartsierra.component/Lifecycle
  (start [component]
    (log/info component "started"))
  (stop [component]
    (log/info component "stopped")))

(defn new-do-nothing []
  (map->DoNothing {}))

(defn new-system-map
  "Create the system. See https://github.com/stuartsierra/component"
  [config]
  (system-map
   :do-nothing (new-do-nothing)))

(defn new-system
  "Construct a new system, configured with the given profile"
  [profile]
  (let [config (config profile)]
    (new-system-map config)))

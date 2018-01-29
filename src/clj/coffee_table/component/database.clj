(ns coffee-table.component.database
  (:require [com.stuartsierra.component :as component]
            [clojure.java.jdbc :as jdbc]))

(defrecord Database []
  component/Lifecycle
  (start [this])
  (stop [this]))

(defn new-database
 []
 (map->Database {}))

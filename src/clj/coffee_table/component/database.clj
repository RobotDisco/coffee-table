(ns coffee-table.component.database
  (:require [com.stuartsierra.component :as component]
            [clojure.java.jdbc :as jdbc]))

(defrecord Database [db-spec]
  component/Lifecycle
  (start [this]
    (let [conn (jdbc/get-connection (:db-spec this))]
      (assoc this :connection conn)))
  (stop [this]
    (when-let [conn (:connection this)]
      (.close conn))
    (dissoc this :connection)))

(defn new-database
 [db-spec]
 (map->Database {:db-spec db-spec}))

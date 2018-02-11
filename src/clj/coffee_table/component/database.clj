(ns coffee-table.component.database
  (:require [coffee-table.db.visits :as dbv]
            [coffee-table.model :as m]
            [com.stuartsierra.component :as component]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [migratus.core :as migrations]
            [taoensso.timbre :as timbre]
            [schema.core :as s])
  (:import [java.sql PreparedStatement]))

(timbre/refer-timbre)

(defn to-date [sql-date]
  (-> sql-date (.getTime) (java.util.Date.)))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [value metadata index]
    (to-date value)))

(extend-type java.util.Date
  jdbc/ISQLParameter
  (set-parameter [value ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (java.sql.Timestamp. (.getTime value)))))

(s/defrecord Database [spec migratus connection]
  component/Lifecycle
  (start [this]
    (info ::starting)
    (let [conn (or connection (jdbc/get-connection spec))]
      (assoc this :connection conn)))
  (stop [this]
    (info ::stopping)
    (when-let [conn (:connection this)]
      (.close conn))
    (assoc this :connection nil)))

(s/defn new-database :- Database
  [m]
  (map->Database m))

(s/defn insert-visit! :- s/Int
  "Insert a visit record into the DB. return the newly created record's ID"
  [component :- Database
   visit :- m/Visit]
  (let [{:keys [id]} (dbv/insert-visit! (:spec component) visit)]
    id))

(s/defn get-visit :- m/Visit
  "Fetch a visit from the DB with the provided ID"
  [component :- Database
   id :- s/Int]
  (dbv/get-visit (:spec component) id))



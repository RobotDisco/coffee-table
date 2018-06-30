(ns coffee-table.component.database
  (:require [coffee-table.db.users :as dbu]
            [coffee-table.db.visits :as dbv]
            [coffee-table.model :as m]
            [com.stuartsierra.component :as component]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [migratus.core :as migrations]
            [taoensso.timbre :as timbre]
            [schema.core :as s]
            [java-time]
            [buddy.hashers :as bhash])
  (:import [org.postgresql.util PGobject]))

(timbre/refer-timbre)

(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "citext" (str value)
        :else value))))

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

(s/defn migrate
  "Roll the database forward to the latest migration."
  [component :- Database]
  (migrations/migrate (:migratus component)))

(s/defn rollback
  "Roll back the latest migration from the DB"
  [component :- Database]
  (migrations/rollback (:migratus component)))

(s/defn insert-visit! :- s/Int
  "Insert a visit record into the DB. return the newly created record's ID"
  [component :- Database
   visit :- m/Visit]
  (let [{:keys [id]} (first (dbv/insert-visit! (:spec component) visit))]
    id))

(s/defn list-visit-summaries :- [m/Summary]
  "Get a summary of all visits currently recorded"
  [component :- Database]
  (mapv #(update % :visit_date java-time/local-date)
        (dbv/list-visit-summaries (:spec component))))

(s/defn get-visit :- (s/maybe m/Visit)
  "Fetch a visit from the DB with the provided ID"
  [component :- Database
   id :- s/Int]
  (let [sql-visit (dbv/get-visit (:spec component) {:id id})]
    (when-not (nil? sql-visit)
      (update sql-visit :visit_date java-time/local-date))))

(s/defn delete-visit-by-id! :- s/Int
  "Delete a visit from the DB with the provided ID. Return rows deleted"
  [component :- Database
   id :- s/Int]
  (dbv/delete-visit-by-id! (:spec component) {:id id}))

(s/defn update-visit! :- s/Int
  "Update a visit from the DB. Return rows updated"
  [component :- Database
   id :- s/Int
   visit :- m/Visit]
  (dbv/update-visit-by-id! (:spec component) (merge visit {:id id})))

(s/defn exec-sql
  "Perform an arbitrary HugSQL function"
  [db :- Database
   f
   params]
  (f (:spec db) params))

(s/defn add-user! :- s/Int
  "Add a user into the DB"
  [component :- Database
   user :- m/PrivateUser]
  (-> (exec-sql component dbu/insert-user! user)
      first
      :id))

(s/defn get-private-user :- (s/maybe m/PrivateUser)
  "Get user w/ password. Be very careful with this"
  [component :- Database
   username :- s/Str]
  (exec-sql component dbu/private-user-by-username {:username username}))

(s/defn get-public-user :- (s/maybe m/PublicUser)
  "Get user w/o password"
  [component :- Database
   username :- s/Str]
  (exec-sql component dbu/public-user-by-username {:username username}))

(s/defn verify :- s/Bool
  "Verify that supplied username/password combo is valid"
  [component :- Database
   username :- s/Str
   password :- s/Str]
  (let [user (get-private-user component username)]
    (when user
      (bhash/check password (:password user)))))

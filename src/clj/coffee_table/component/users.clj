(ns coffee-table.component.users
  (:require [buddy.hashers :as bhash]
            [clojure.java.jdbc :as jdbc]
            [coffee-table.component.database :as dbc]
            [coffee-table.db.users :as dbu]
            [taoensso.timbre :as timbre]
            [schema.core :as s]
            [com.stuartsierra.component :as component])
  (:import [coffee_table.component.database Database]
           [org.postgresql.util PGobject]))

(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "citext" (str value)
        :else value))))

(timbre/refer-timbre)

(s/defschema PublicUser
  "Coffee Table Users (password removed for security reasons)"
  {(s/optional-key :id) s/Int
   :username s/Str
   :is_admin s/Bool})

(s/defschema PrivateUser
  "Coffee Table User accounts"
  (merge PublicUser
         {:password s/Str}))

(s/defn make-user :- PrivateUser
  [username :- String
   password :- String]
  {:username username
   :password (bhash/derive password)
   :is_admin false})

(s/defrecord Users [db :- (s/maybe Database)]
  component/Lifecycle
  (start [this]
    (info ::starting)
    this)
  (stop [this]
    (info ::stopping)
    this))

(s/defn new-users :- Users
  []
  (map->Users {}))

(s/defn add-user! :- s/Int
  "Add a user into the DB"
  [users :- Users
   user :- PrivateUser]
  (-> (dbc/exec-sql (:db users) dbu/insert-user! user)
      first
      :id))

(s/defn get-private-user :- (s/maybe PrivateUser)
  "Get user w/ password. Be very careful with this"
  [users :- Users
   username :- s/Str]
  (dbc/exec-sql (:db users) dbu/private-user-by-username {:username username}))

(s/defn get-public-user :- (s/maybe PublicUser)
  "Get user w/o password"
  [users :- Users
   username :- s/Str]
  (dbc/exec-sql (:db users) dbu/public-user-by-username {:username username}))

(s/defn verify :- s/Bool
  "Verify that supplied username/password combo is valid"
  [component :- Users
   username :- s/Str
   password :- s/Str]
  (let [user (get-private-user component username)]
    (when user
      (bhash/check password (:password user)))))

(ns coffee-table.database
  (:require [coffee-table.db.users :as dbu]
            [coffee-table.db.visits :as dbv]
            [coffee-table.model :as m]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [integrant.core :as ig]
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

(defmethod ig/init-key :coffee-table/database
  [_ {:coffee-table.database/keys [spec migration] :as config}]
  (let [#_ conn #_ (jdbc/get-connection spec)]
    (infof "Started database connection to %s:%d/%s"
          (:host spec)
          (:port spec)
          (:dbname spec))
    ;; get-connection isn't right, I'll have to use something like
    ;; hikari or c3po (worse?) to do this properly. Hacking a plain
    ;; spec for now.
    {:connection #_ conn spec
     :migration migration}))

(defmethod ig/halt-key! :coffee-table/database
  [_ {:coffee-table.database/keys [connection]}]
  (when-let [_ connection]
    (info "Stopping database connection")
    ;; Will need to do this when using connection pool
    #_ (.close connection)))

(s/defn migrate
  "Roll the database forward to the latest migration."
  [{migration :migration}]
  (migrations/migrate migration))

(s/defn rollback
  "Roll back the latest migration from the DB"
  [{migration :migration}]
  (migrations/rollback migration))

(s/defn insert-visit! :- s/Int
  "Insert a visit record into the DB. return the newly created record's ID"
  [{connection :connection}
   visit :- m/Visit]
  (let [{:keys [id]} (first (dbv/insert-visit! connection visit))]
    id))

(s/defn list-visit-summaries :- [m/Summary]
  "Get a summary of all visits currently recorded"
  [{connection :connection}]
  (mapv #(update % :visit_date java-time/local-date)
        (dbv/list-visit-summaries connection)))

(s/defn get-visit :- (s/maybe m/Visit)
  "Fetch a visit from the DB with the provided ID"
  [{connection :connection}
   id :- s/Int]
  (let [sql-visit (dbv/get-visit connection {:id id})]
    (when-not (nil? sql-visit)
      (update sql-visit :visit_date java-time/local-date))))


(s/defn delete-visit-by-id! :- s/Int
  "Delete a visit from the DB with the provided ID. Return rows deleted"
  [{connection :connection}
   id :- s/Int]
  (dbv/delete-visit-by-id! connection {:id id}))

(s/defn update-visit! :- s/Int
  "Update a visit from the DB. Return rows updated"
  [{connection :connection}
   id :- s/Int
   visit :- m/Visit]
  (dbv/update-visit-by-id! connection (merge visit {:id id})))

(s/defn add-user! :- s/Int
  "Add a user into the DB"
  [{connection :connection}
   user :- m/PrivateUser]
   (-> (dbu/insert-user! connection user)
       first
       :id))

(s/defn get-private-user :- (s/maybe m/PrivateUser)
  "Get user w/ password. Be very careful with this"
  [{connection :connection}
   username :- s/Str]
  (dbu/private-user-by-username connection {:username username}))

(s/defn get-public-user :- (s/maybe m/PublicUser)
  "Get user w/o password"
  [{connection :connection}
   username :- s/Str]
  (dbu/public-user-by-username connection {:username username}))

(s/defn verify :- s/Bool
  "Verify that supplied username/password combo is valid"
  [db
   username :- s/Str
   password :- s/Str]
  (let [user (get-private-user db username)]
    (when user
      (bhash/check password (:password user)))))

(ns coffee-table.database-mock
  (:require [coffee-table.model :as m]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [schema.core :as s]
            [java-time]
            [buddy.hashers :as bhash]))

(timbre/refer-timbre)

(defmethod ig/init-key :coffee-table/database-mock
  [_ _]
  (infof "Started mock database connection")
  {:users (atom [])
   :visits (atom [])})

(s/defn insert-visit! :- s/Int
  "Insert a visit record into the DB. return the newly created record's ID"
  [{:keys [visits]}
   visit :- m/Visit]
  (let [nextpos (count @visits)]
    (swap! visits conj (merge visit {:id nextpos}))
    nextpos))

(s/defn list-visit-summaries :- [m/Summary]
  "Get a summary of all visits currently recorded"
  [{:keys [visits]}]
  (map @visits #(select-keys % [:id :cafe_name :visit_date :beverage_rating])))

(s/defn get-visit :- (s/maybe m/Visit)
  "Fetch a visit from the DB with the provided ID"
  [{:keys [visits]}
   id :- s/Int]
  (get @visits id))

(s/defn delete-visit-by-id! :- s/Int
  "Delete a visit from the DB with the provided ID. Return rows deleted"
  [{:keys [visits] :as component}
   id :- s/Int]
  (if (nil? (get-visit component id))
    0
    (do
      (swap! visits assoc id nil)
      1)))

(s/defn update-visit! :- s/Int
  "Update a visit from the DB. Return rows updated"
  [{:keys [visits]}
   id :- s/Int
   visit :- m/Visit]
  (swap! visits assoc id visit)
  1)

(s/defn add-user! :- s/Int
  "Add a user into the DB"
  [{:keys [users]}
   user :- m/PrivateUser]
  (let [nextpos (count @users)]
    (swap! users conj (merge user {:id nextpos}))
    nextpos))

(s/defn get-private-user :- (s/maybe m/PrivateUser)
  "Get user w/ password. Be very careful with this"
  [{:keys [users]}
   username :- s/Str]
  (first (filterv #(= (:username %) username) @users)))

(s/defn get-public-user :- (s/maybe m/PublicUser)
  "Get user w/o password"
  [component
   username :- s/Str]
  (let [privateuser (get-private-user component username)]
    (when privateuser
      (dissoc privateuser :password))))

(s/defn verify :- s/Bool
  "Verify that supplied username/password combo is valid"
  [component
   username :- s/Str
   password :- s/Str]
  (let [user (get-private-user component username)]
    (when user
      (bhash/check password (:password user)))))

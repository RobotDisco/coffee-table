(ns user
  (:require [coffee-table.database :as db]
            [coffee-table.system :refer [new-system]]
            [integrant.repl :refer [clear halt prep init reset reset-all]]
            [schema.core :as s]
            [buddy.hashers :as bhash]))

(s/set-fn-validation! true)

(defn go []
  (let [res (integrant.repl/go)]
    res))

(integrant.repl/set-prep! #(new-system :dev))

(defn migrate! []
  (-> integrant.repl.state/system
      :coffee-table/database
      db/migrate))

(defn rollback! []
  (-> integrant.repl.state/system
      :coffee-table/database
      db/migrate))

(defn new-user!
  ([username password]
   (new-user! username password false))
  ([username password admin?]
   (let [{:coffee-table/keys [database]} integrant.repl.state/system]
     (db/add-user!
      database
      {:username username
       :password (bhash/derive password)
       :is_admin admin?}))))

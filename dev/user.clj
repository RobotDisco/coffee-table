(ns user
  (:require [coffee-table.component.database :as db]
            [coffee-table.component.users :as uc]
            [coffee-table.system :refer [new-system]]
            [reloaded.repl :refer [system init start stop go reset reset-all]]
            [schema.core :as s]
            [buddy.hashers :as bhash]))

(s/set-fn-validation! true)

(reloaded.repl/set-init! #(new-system :dev))

(defn migrate! []
  (db/migrate (:db system)))

(defn rollback! []
  (db/rollback (:db system)))

(defn new-user!
  ([username password]
   (new-user! username password false))
  ([username password admin?]
   (uc/add-user!
    (:users system)
    {:username username
     :password (bhash/derive password)
     :is_admin admin?})))

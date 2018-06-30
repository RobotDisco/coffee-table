(ns coffee-table.users-test
  (:require [buddy.hashers :as bhash]
            [clojure.test :as t :refer [deftest is]]
            [coffee-table.config :as ctcfg]
            [com.stuartsierra.component :as component]
            [coffee-table.component.database :as dbc]
            [coffee-table.test.system :as cts]
            [environ.core :refer [env]]
            [schema.core :as s]
            [schema.test]
            [coffee-table.model :as m]))

(def config (ctcfg/config (keyword (env :clj-profile))))

(defn test-system
  "Create minimal system to test user DB logic"
  []
  (component/system-using
   (component/system-map
    :db (dbc/new-database {:spec (ctcfg/database-spec config)
                           :migratus (ctcfg/migratus config)}))
   {}))

(t/use-fixtures :once
  schema.test/validate-schemas
  (cts/with-system-fixture test-system)
  (cts/with-transaction-fixture [:db :spec]))


(def example-user {:username "testuser"
                   :password (bhash/derive "testpass")
                   :is_admin false})

(deftest add-user
  (let [db (:db cts/*system*)
        user-id (dbc/add-user! db example-user)
        username (:username example-user)
        test-private-user (merge example-user
                                 {:id user-id})
        test-public-user (dissoc test-private-user :password)]
    (is (= test-private-user (dbc/get-private-user db username)))
    (is (= test-public-user (dbc/get-public-user db username)))))

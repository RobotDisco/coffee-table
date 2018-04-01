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
            [coffee-table.model :as m]
            [coffee-table.component.database :as db]
            [coffee-table.component.users :as users]))

(def config (ctcfg/config (keyword (env :clj-profile))))

(defn test-system
  "Create minimal system to test user DB logic"
  []
  (component/system-using
   (component/system-map
    :db (dbc/new-database {:spec (ctcfg/database-spec config)
                           :migratus (ctcfg/migratus config)})
    :users (users/new-users))
   {:users [:db]}))

(t/use-fixtures :once
  schema.test/validate-schemas
  (cts/with-system-fixture test-system)
  (cts/with-transaction-fixture [:db :spec]))


(def example-user {:username "testuser"
                   :password (bhash/derive "testpass")
                   :is_admin false})

(deftest add-user
  (let [users (:users cts/*system*)
        user-id (users/add-user! users example-user)
        username (:username example-user)
        test-private-user (merge example-user
                                 {:id user-id})
        test-public-user (dissoc test-private-user :password)]
    (is (= test-private-user (users/get-private-user users username)))
    (is (= test-public-user (users/get-public-user users username)))))

(ns coffee-table.users-test
  (:require [buddy.hashers :as bhash]
            [clojure.test :as t :refer [deftest is]]
            [coffee-table.database-mock :as dbc]
            [coffee-table.test.system :as cts]
            [schema.core :as s]
            [schema.test]
            [coffee-table.model :as m]))

(defn test-system
  "Create minimal system to test user DB logic"
  []
   {:coffee-table/database-mock {}})

(t/use-fixtures :once
  schema.test/validate-schemas
  (cts/with-system-fixture test-system))


(def example-user {:username "testuser"
                   :password (bhash/derive "testpass")
                   :is_admin false})

(deftest add-user
  (let [db (:coffee-table/database-mock cts/*system*)
        user-id (dbc/add-user! db example-user)
        username (:username example-user)
        test-private-user (merge example-user
                                 {:id user-id})
        test-public-user (dissoc test-private-user :password)]
    (is (= test-private-user (dbc/get-private-user db username)))
    (is (= test-public-user (dbc/get-public-user db username)))))

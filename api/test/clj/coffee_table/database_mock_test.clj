(ns coffee-table.database-mock-test
  (:require [java-time]
            [clojure.test :as t :refer [deftest is]]
            [coffee-table.database-mock :as sut]
            [coffee-table.model :as m]
            [coffee-table.test.system :as cts]
            [schema.core :as s]
            [schema.test]))

(defn test-system
  "Create minimal system to test DB logic functionality"
  []
  {:coffee-table/database-mock {}})

(t/use-fixtures :once
  schema.test/validate-schemas
  (cts/with-system-fixture test-system))

(deftest test-create-visit
  (let [db (:coffee-table/database-mock cts/*system*)
        visit-params (m/make-visit "Test Cafe" (java-time/local-date) "Espresso" 5)
        visit-id (sut/insert-visit! db visit-params)]
    (is (= (assoc visit-params :id visit-id)
           (sut/get-visit db visit-id)))))

(deftest test-remove-visit
  (let [db (:coffee-table/database-mock cts/*system*)
        visit-params (m/make-visit "Test Cafe" (java-time/local-date) "Espresso" 5)
        visit-id (sut/insert-visit! db visit-params)
        rows-removed (sut/delete-visit-by-id! db visit-id)]
    (is (nil? (sut/get-visit db visit-id)))))

(deftest test-update-visit
  (let [db (:coffee-table/database-mock cts/*system*)
        visit-params (m/make-visit "Test Cafe" (java-time/local-date) "Espresso" 5)
        visit-id (sut/insert-visit! db visit-params)
        old-visit (assoc visit-params :id visit-id)
        new-visit (assoc old-visit :ambience_rating 3)
        rows-updated (sut/update-visit! db visit-id new-visit)]
    (is (= new-visit (sut/get-visit db visit-id)))))

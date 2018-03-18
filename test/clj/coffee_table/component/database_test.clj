(ns coffee-table.component.database-test
  (:require [java-time]
            [clojure.test :as t :refer [deftest is]]
            [coffee-table.component.database :as sut]
            [coffee-table.config :as ctcfg]
            [coffee-table.model :as m]
            [coffee-table.test.system :as cts]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [schema.core :as s]
            [schema.test]))

(def config (ctcfg/config (keyword (env :clj-profile))))

(defn test-system
  "Create minimal system to test DB logic functionality"
  []
  (component/system-using
   (component/system-map
    :db (sut/new-database {:spec (ctcfg/database-spec config)
                           :migratus (ctcfg/migratus config)}))
   {}))

(t/use-fixtures :once
  schema.test/validate-schemas
  (cts/with-system-fixture test-system)
  (cts/with-transaction-fixture [:db :spec]))

(deftest test-create-visit
  (let [db (:db cts/*system*)
        visit-params (m/make-visit "Test Cafe" (java-time/local-date) "Espresso" 5)
        visit-id (sut/insert-visit! db visit-params)]
    (is (= (assoc visit-params :id visit-id)
           (sut/get-visit db visit-id)))))

(deftest test-remove-visit
  (let [db (:db cts/*system*)
        visit-params (m/make-visit "Test Cafe" (java-time/local-date) "Espresso" 5)
        visit-id (sut/insert-visit! db visit-params)
        rows-removed (sut/delete-visit-by-id! db visit-id)]
    (is (nil? (sut/get-visit db visit-id)))))

(deftest test-update-visit
  (let [db (:db cts/*system*)
        visit-params (m/make-visit "Test Cafe" (java-time/local-date) "Espresso" 5)
        visit-id (sut/insert-visit! db visit-params)
        old-visit (assoc visit-params :id visit-id)
        new-visit (assoc old-visit :ambience_rating 3)
        rows-updated (sut/update-visit! db new-visit)]
    (is (= new-visit (sut/get-visit db visit-id)))))

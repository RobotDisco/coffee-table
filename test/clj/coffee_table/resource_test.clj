(ns coffee-table.resource-test
  (:require [coffee-table.resource :as sut]
            [clojure.test :as t :refer [deftest testing is]]
            [environ.core :refer [env]]
            [schema.core :as s]
            [schema.test]
            [yada.yada :as yada]
            [coffee-table.test.system :as cts]
            [coffee-table.config :as ctcfg]
            [com.stuartsierra.component :as component]
            [coffee-table.component.database :as dbc]
            [coffee-table.model :as m]
            [java-time]
            [ring.mock.request :as mock]
            [cheshire.core :refer [parse-string]]
            [byte-streams :as bs :refer [convert]]))

(def config (ctcfg/config (keyword (env :clj-profile))))

(defn test-system
  "Create minimal system to test Yada resource logic functionality"
  []
  (component/system-using
   (component/system-map
    :db (dbc/new-database {:spec (ctcfg/database-spec config)
                           :migratus (ctcfg/migratus config)}))
   {}))

(t/use-fixtures :once schema.test/validate-schemas)
(t/use-fixtures :each (cts/with-system-fixture test-system) cts/with-transaction-fixture)

(s/def example-visit :- m/Visit
  "Minimally-defined visit for our testing purposes"
  (m/make-visit "Test Café" (java-time/local-date) "Espresso" 3))

(deftest create-visits-valid-data
  (testing "POST /visits (valid data)"
    (let [db (:db cts/*system*)
          handler (yada/handler (sut/new-visit-index-resource db))
          data example-visit
          request (mock/json-body (mock/request :post "/") data)
          response @(handler request)]
      (is (= 201 (:status response)))
      (is (contains? (:headers response) "location"))
      (is (not (nil? (re-matches #"/visits/(\d+)"
                                 (get-in response [:headers "location"]))))))))

(deftest create-visits-invalid-data
  (testing "POST /visits (invalid data)"
    (let [db (:db cts/*system*)
          handler (yada/handler (sut/new-visit-index-resource db))
          request (mock/json-body (mock/request :post "/") {})
          response @(handler request)]
      (is (= 400 (:status response))))))

(deftest list-visits-no-entries-yet
  (testing "GET /visits (no entries yet)"
    (let [db (:db cts/*system*)
          handler (yada/handler (sut/new-visit-index-resource db))
          request (mock/json-body (mock/request :get "/") {})
          response @(handler request)]
      (is (= 200 (:status response)))
      (is (= [] (parse-string (bs/to-string (:body response))))))))

(deftest list-visits-entries-exist
  (testing "Get /visits (a couple of entries)"
    (let [db (:db cts/*system*)
          handler (yada/handler (sut/new-visit-index-resource db))
          numtimes 2
          _ (dotimes [_ numtimes]
              @(handler (mock/json-body (mock/request :post "/") example-visit)))
          list-request (mock/request :get "/")
          list-response @(handler list-request)
          list-body (parse-string (bs/to-string (:body list-response)) keyword)]
      (is (= 200 (:status list-response)))
      (is (= numtimes (count list-body)))
      (doseq [visit list-body]
        (is (not (nil? (re-matches #"/visits/(\d+)" (:uri visit)))))))))

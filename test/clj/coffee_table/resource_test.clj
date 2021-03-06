(ns coffee-table.resource-test
  (:require [bidi.vhosts :refer [make-handler vhosts-model]]
            [coffee-table.resource :as sut]
            [clojure.test :as t :refer [deftest testing is]]
            [schema.core :as s]
            [schema.test]
            [yada.yada :as yada]
            [coffee-table.test.system :as cts]
            [coffee-table.database-mock :as dbc]
            [coffee-table.model :as m]
            [java-time]
            [ring.mock.request :as mock]
            [cheshire.core :refer [parse-string]]
            [byte-streams :as bs :refer [convert]]
            [clojure.data.codec.base64 :as base64]))

(def ^:dynamic *handler*)

(defn test-system
  "Create minimal system to test Yada resource logic functionality"
  []
  {:coffee-table/database-mock {}})

(defn include-handler [f]
  (let [db (:coffee-table/database-mock cts/*system*)
        handler (make-handler (vhosts-model [:* (sut/visit-routes db)]))]
    (binding [*handler* handler]
      (f))))

(defn add-regular-user [f]
  (let [db (:coffee-table/database-mock cts/*system*)]
    (dbc/add-user! db (m/make-user "testuser" "password"))))

(t/use-fixtures :once
  schema.test/validate-schemas)

(t/use-fixtures :each
  (cts/with-system-fixture test-system)
  add-regular-user
  include-handler)

(s/defn auth-via-test-user
  [request]
  (mock/header request
               "Authorization"
               (str "Basic "
                    (base64/encode (str "testuser" ":" "password")))))

(s/def example-visit :- m/Visit
  "Minimally-defined visit for our testing purposes"
  (m/make-visit "Test Café" (java-time/local-date) "Espresso" 3))

(deftest create-visits-valid-data
  (testing "POST /visits (valid data)"
    (let [data example-visit
          request (auth-via-test-user (mock/json-body (mock/request :post "/visits") data))
          response @(*handler* request)]
      (is (= 201 (:status response)))
      (is (contains? (:headers response) "location"))
      (is (not (nil? (re-matches #"/visits/(\d+)"
                                 (get-in response [:headers "location"]))))))))

(deftest create-visits-invalid-data
  (testing "POST /visits (invalid data)"
    (let [request (auth-via-test-user (mock/json-body (mock/request :post "/visits") {}))
          response @(*handler* request)]
      (is (= 400 (:status response))))))

(deftest list-visits-no-entries-yet
  (testing "GET /visits (no entries yet)"
    (let [request (auth-via-test-user (mock/json-body (mock/request :get "/visits") {}))
          response @(*handler* request)]
      (is (= 200 (:status response)))
      (is (= {:data []} (parse-string (bs/to-string (:body response)) keyword))))))

(deftest list-visits-entries-exist
  (testing "Get /visits (a couple of entries)"
    (let [numtimes 2
          _ (dotimes [_ numtimes]
              @(*handler* (auth-via-test-user (mock/json-body (mock/request :post "/visits") example-visit))))
          list-request (mock/request :get "/visits")
          list-response @(*handler* list-request)
          list-body (:data (parse-string (bs/to-string (:body list-response)) keyword))]
      (is (= 200 (:status list-response)))
      (is (= numtimes (count list-body)))
      (doseq [visit list-body]
        (is (not (nil? (re-matches #"/visits/(\d+)" (get-in visit [:links :self])))))
        (is (s/validate m/Summary (m/json->summary (get visit :data))))))))

(deftest get-visit-id-does-not-exist
  (testing "GET /visits/<someid> (nonexistant entity)"
    (let [request (auth-via-test-user (mock/request :get "/visits/9999"))
          response @(*handler* request)]
      (is (= 404 (:status response))))))

(deftest get-visits-id-exists
  (testing "GET /visits/<someid> (existing entry)"
    (let [data example-visit
          create-request (auth-via-test-user (mock/json-body (mock/request :post "/visits") data))
          create-response @(*handler* create-request)
          get-request (auth-via-test-user (mock/request :get (get-in create-response [:headers "location"])))
          get-response (-> get-request
                           *handler*
                           deref
                           :body
                           bs/to-string
                           (parse-string keyword))]
      (is (= example-visit (dissoc (m/json->visit (:data get-response)) :id)))
      (is (not (nil? (re-matches #"/visits/(\d+)" (get-in get-response [:links :self]))))))))

(deftest update-visits-entry-exists
  (testing "PUT /visits/<id> (<id> existed already)"
    (let [create-request (auth-via-test-user (mock/json-body (mock/request :post "/visits") example-visit))
          location (get-in @(*handler* create-request) [:headers "location"])
          put-body (assoc example-visit :cafe_name "Updated Café")
          put-request (auth-via-test-user (mock/json-body (mock/request :put location) put-body))
          put-response @(*handler* put-request)]
      (is (= 204 (:status put-response))))))

(deftest update-visits-entry-does-not-exist
  (testing "PUT /visits/<id> (<id> doesn't exist)"
    (let [put-body (-> example-visit (assoc :id 0) (assoc :cafe_name "Updated Café"))
          put-request (auth-via-test-user (mock/json-body (mock/request :put "/visits/0") put-body))
          put-response @(*handler* put-request)]
      (is (= 404 (:status put-response))))))

(deftest update-visits-invalid-data
  (testing "PUT /visits/<id> (wrong field name)"
    (let [visits (:visits cts/*system*)
          visit-routes (vhosts-model [:* (sut/visit-routes visits)])
          create-request (auth-via-test-user (mock/json-body (mock/request :post "/visits") example-visit))
          location (get-in @(*handler* create-request) [:headers "location"])
          put-body (dissoc example-visit :cafe_name)
          put-request (mock/json-body (mock/request :put location) put-body)
          put-response @(*handler* put-request)]
      (is (= 400 (:status put-response))))))

(deftest delete-visits-id-exists
    (testing "DELETE /visits/<someid> (existing entry)"
      (let [create-request (auth-via-test-user (mock/json-body (mock/request :post "/visits") example-visit))
            create-response @(*handler* create-request)
            location (get-in create-response [:headers "location"])
            delete-request (auth-via-test-user (mock/request :delete location))
            delete-response @(*handler* delete-request)
            get-request (auth-via-test-user (mock/request :get location))
            get-response @(*handler* get-request)]
        (is (= 204 (:status delete-response)))
        (is (= 404 (:status get-response))))))

(deftest delete-visits-id-does-not-exist
  (testing "DELETE /visits/<id> (<id> doesn't exist)"
    (let [delete-request (auth-via-test-user (mock/request :delete "/visits/0"))
          delete-response @(*handler* delete-request)]
      (is (= 204 (:status delete-response))))))

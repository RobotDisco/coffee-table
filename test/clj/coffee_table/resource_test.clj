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
            [coffee-table.component.database :as dbc]))

(def config (ctcfg/config (keyword (env :clj-profile))))

(defn test-system
  "Create minimal system to test Yada resource logic functionality"
  []
  (component/system-using
   (component/system-map
    :db (dbc/new-database {:spec (ctcfg/database-spec config)
                           :migratus (ctcfg/migratus config)}))
   {}))

(t/use-fixtures :once schema.test/validate-schemas (cts/with-system-fixture test-system) cts/with-transaction-fixture)

(deftest create-visits-valid-data
  (testing "POST /visits (valid data)"
    (let [db (:db cts/*system*)
          handler (yada/handler coffee-table.resource/new-visit-index-resource db)])))

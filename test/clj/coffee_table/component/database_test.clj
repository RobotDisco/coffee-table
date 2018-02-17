(ns coffee-table.component.database-test
  (:require [java-time]
            [clojure.test :as t :refer [deftest is]]
            [coffee-table.component.database :as sut]
            [coffee-table.config :as ctcfg]
            [coffee-table.test.system :as cts]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [schema.core :as s]
            [schema.test]))

(def config (ctcfg/config (keyword (env :clj-profile))))

(defn test-system
  "Create minimal system to test Visit logic functionality"
  []
  (component/system-using
   (component/system-map
    :db (sut/new-database {:spec (ctcfg/database-spec config)
                           :migratus (ctcfg/migratus config)}))
   {}))

(t/use-fixtures :once schema.test/validate-schemas (cts/with-system-fixture test-system) cts/with-transaction-fixture)

(deftest test-create-visit
  (let [db (:db cts/*system*)
        visit-params {:cafe_name "Test Café"
                      :visit_date (java-time/sql-date)
                      :beverage_ordered "Espresso"
                      :beverage_rating 3}
        visit-id (sut/insert-visit! db visit-params)]
    (is (= {:id visit-id
            :cafe_name (:cafe_name visit-params)
            :visit_date (:visit_date visit-params)
            :beverage_ordered (:beverage_ordered visit-params)
            :beverage_rating (:beverage_rating visit-params)
            :machine nil
            :grinder nil
            :roast nil
            :beverage_notes nil
            :service_rating nil
            :service_notes nil
            :ambience_rating nil
            :ambience_notes nil
            :other_notes nil}
           (sut/get-visit db visit-id)))))

(ns coffee-table.component.database-test
  (:require  [clojure.test :as t :refer [deftest is]]
             [coffee-table.component.database :as sut]
             [coffee-table.test.system :as cts]
             [com.stuartsierra.component :as component]
             [schema.core :as s]
             [schema.test]))

(defn test-system
  "Create minimal system to test Visit logic functionality"
  []
  (component/system-using
   (component/system-map)
   {}))

(t/use-fixtures :once schema.test/validate-schemas (cts/with-system-fixture test-system))

(deftest test-create-visit
  (is (= 1 1)))

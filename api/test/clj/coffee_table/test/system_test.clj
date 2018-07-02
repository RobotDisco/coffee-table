(ns coffee-table.test.system-test
  (:require [coffee-table.test.system :as sut :refer [*system* with-system-fixture]]
            [integrant.core :as ig]
            [clojure.test :as t :refer [is]]
            [schema.test :refer [deftest]]))

(defn new-system
  "Define a minimal system which is just enough for the tests in this namespace to run"
  []
  {})

(t/use-fixtures :once (with-system-fixture new-system))

(deftest system-fixture-test
  ;; Does our testing scaffolding produce a valid system?
  (is *system*)
  (is (= {} *system*)))

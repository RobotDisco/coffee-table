(ns coffee-table.test.system-test
  (:require [coffee-table.test.system :as sut :refer [*system* with-system-fixture]]
            [com.stuartsierra.component :refer [system-using system-map]]
            [clojure.test :as t :refer [is]]
            [schema.test :refer [deftest]])
  (:import [com.stuartsierra.component SystemMap]))

(defn new-system
  "Define a minimal system which is just enough for the tests in this namespace to run"
  []
  (system-using
   (system-map)
   {}))

(t/use-fixtures :once (with-system-fixture new-system))

(deftest system-fixture-test
  ;; Does our testing scaffolding produce a valid system?
  (is *system*)
  (is (instance? SystemMap *system*)))

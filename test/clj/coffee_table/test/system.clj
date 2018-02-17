(ns coffee-table.test.system
  (:require [coffee-table.component.database :as dbc]
            [com.stuartsierra.component :as component]
            [clojure.java.jdbc :as sql]))

(def ^:dynamic *system* nil)

(defmacro with-system
  [system & body]
  `(let [s# (component/start ~system)]
     (try
       (binding [*system* s#] ~@body)
       (finally
         (component/stop s#)))))

(defn with-system-fixture
  [system]
  (fn [f]
    (with-system (system)
      (f))))

(defn with-transaction-fixture
  [f]
  (let [db (:db *system*)
        spec (:spec db)]
    (dbc/migrate db)
    (sql/with-db-transaction [spec spec]
      (sql/db-set-rollback-only! spec)
      (binding [*system* (assoc-in *system* [:db :spec] spec)]
        (f)))))


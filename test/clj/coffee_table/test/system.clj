(ns coffee-table.test.system
  (:require [com.stuartsierra.component :as component]
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
  (let [dbc (:db *system*)
        spec (:spec dbc)
        conn (:connection dbc)]
    (sql/with-db-transaction [spec spec]
      (sql/db-set-rollback-only! spec)
      (binding [*system* (assoc-in *system* [:db :spec] spec)]
        (f)))))


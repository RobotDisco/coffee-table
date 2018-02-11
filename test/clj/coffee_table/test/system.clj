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
    (sql/with-db-transaction [t-conn spec]
      (sql/db-set-rollback-only! t-conn)
      (try 
        (assoc dbc :connection t-conn)
        (f)
        (finally
          (assoc dbc :connection conn))))))


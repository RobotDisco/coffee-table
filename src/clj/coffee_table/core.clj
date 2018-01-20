(ns coffee-table.core
  "Entrypoint for production Uberjars"
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [coffee-table.system :refer [new-system]]
            [clojure.tools.logging :as log]))

(def system nil)

(defn shut-down-app [system]
  (log/info ::shutting-down-app)
  (component/stop system)
  (log/info ::shut-down-app))

(defn -main
  [& args]
  (let [system (new-system :prod)]
    ;; Shut down system if we terminate
    (.addShutdownHook (Runtime/getRuntime) (Thread. (partial shut-down-app system)))
    (log/info ::starting-app)
    (component/start system)
    (log/info ::started-app))
  ;; All threads are daemon, so block forever:
  @(promise))


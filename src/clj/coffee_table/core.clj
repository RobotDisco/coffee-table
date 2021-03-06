(ns coffee-table.core
  "Entrypoint for production Uberjars"
  (:gen-class)
  (:require [integrant.core :as ig]
            [coffee-table.system :refer [new-system]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn add-shutdown-hook! [^Runnable f]
  (.addShutdownHook (Runtime/getRuntime) (Thread. f)))

(defn logged-shutdown [system]
  (info ::shutting-down-app)
  (ig/halt! system)
  (info ::shut-down-app))

(defn -main
  [& args]
  (let [system (new-system :prod)]
    ;; Shut down system if we terminate
    (add-shutdown-hook! (partial logged-shutdown system))
    (info ::starting-app)
    (ig/init system)
    (info ::started-app))
  ;; All threads are daemon, so block forever:
  @(promise))

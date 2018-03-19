(ns coffee-table.component.web-server
  (:require [bidi.vhosts :refer [vhosts-model]]
            [schema.core :as s]
            [coffee-table.component.visits :refer [visit-routes]]
            [com.stuartsierra.component :as component]
            [yada.yada :as yada]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(s/defn routes
  "Create the URI route structure for our application."
  [visits]
  [""
   [
    ["/hello" (yada/handler "Hello World")]
    ["/api" [(visit-routes visits)]]
    [true (yada/handler nil)]]])

(s/defrecord WebServer [host port visits listener]
  component/Lifecycle
  (start [component]
    (if listener
      component ; idempotence
      (let [vhosts-model (vhosts-model [{:scheme :http :host host} (routes visits)])
            listener (yada/listener vhosts-model {:port port})]
        (info ::started "using port %s, host %s" (:port listener) host)
        (assoc component :listener listener))))

  (stop [component]
    (when-let [close (get-in component [:listener :close])]
      (close))
    (assoc component :listener nil)))

(defn new-web-server [m]
  (component/using
   (map->WebServer m)
   [:visits]))

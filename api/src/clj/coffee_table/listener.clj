(ns coffee-table.listener
  (:require [bidi.bidi :refer [tag]]
            [bidi.vhosts :refer [vhosts-model]]
            [schema.core :as s]
            [coffee-table.resource :refer [visit-routes]]
            [yada.yada :as yada]
            [yada.swagger :refer [swaggered]]
            [yada.resources.webjar-resource :refer [new-webjar-resource]]
            [taoensso.timbre :as timbre]
            [integrant.core :as ig]))

(timbre/refer-timbre)

(s/defn routes
  "Create the URI route structure for our application."
  [config]
  [""
   [
    ["/api" (-> (visit-routes config)
                (yada/swaggered
                 {:info {:title "Coffee Table API"
                         :version "1.0"
                         :description "A REST API"}
                  :basePath "/api"})
                (tag :coffee-table.resources/api))]
    ;; Swagger UI
    ["/swagger" (tag
                 (new-webjar-resource "/swagger-ui" {:index-files ["index.html"]})
                 :coffee-table.resources/swagger)]
    [true (yada/handler nil)]]])

(defmethod ig/init-key :coffee-table/listener
  [_ {:coffee-table.listener/keys [host port] :as config}]
  (let [vhosts-model (vhosts-model [{:scheme :http :host host} (routes config)])
        listener (yada/listener vhosts-model {:port port})]
    (info "Started http server on port %s" (:port listener))
    {:listener listener
     ;; host is used for announcement in dev
     :host host}))

(defmethod ig/halt-key! :coffee-table/listener [_ {:keys [listener]}]
  (when-let [close (:close listener)]
    (info "Shutting down http server")
    (close)))

(ns coffee-table.component.visits
  (:require [yada.yada :as yada]
            [coffee-table.model :as m]
            [coffee-table.component.database :as dbc]
            [taoensso.timbre :as timbre]
            [schema.core :as s]
            [com.stuartsierra.component :as component])
  (:import [java.net URI]
           [coffee_table.component.database Database]))

(timbre/refer-timbre)

(s/defrecord Visits [db :- (s/maybe Database)]
  component/Lifecycle
  (start [this]
    (info ::starting)
    this)
  (stop [this]
    (info ::stopping)
    this))

(s/defn new-visits :- Visits
  []
  (map->Visits {}))

(s/defn new-visit-index-resource :- yada.schema/Resource
  "Resource for visit collection (create, list)"
  [db :- Database]
  (yada/resource
   {#_ :access-control #_ {#_ :allow-origin #_ "http://localhost:3449"
                           :allow-methods [:options :head :get :post]
                           :allow-headers ["Content-Type" "Authorization"]
                           #_ :scheme #_ :jwt
                           #_ :authorization #_ {:methods {:get :user
                                                           :post :user}}}
    :id :visits/index
    :summary "Café Visit index"
    :logger #(info %)
    :description "Café Visit index"
    :consumes #{"application/json"}
    :produces #{"application/json"}
    :methods {:get {:response (fn [ctx]
                                {:data (mapv (fn [x] {:links {:self (yada/path-for ctx :visits/entry {:route-params {:id (:id x)}})}
                                                      :data x})
                                             (dbc/list-visit-summaries db))})}
              :post {:parameters {:body m/Visit}
                     :response (fn [ctx]
                                 (let [id (dbc/insert-visit! db (get-in ctx [:parameters :body]))]
                                   (URI. (yada/path-for ctx :visits/entry {:route-params {:id id}}))))}}}))

(s/defn new-visit-node-resource :- yada.schema/Resource
  "Resource for visit items (get, update, delete)"
  [db :- Database]
  (yada/resource
   {#_ :access-control #_ {:allow-origin "http://localhost:3449"
                           :allow-methods [:options :head :get :put :delete]
                           :allow-headers ["Content-Type" "Authorization"]
                           :scheme :jwt
                           :authorization {:methods {:get :user
                                                     :put :user
                                                     :delete :user}}}
    :id :visits/entry
    :logger #(info %)
    :description "Café Visit entries"
    :consumes #{"application/json"}
    :produces #{"application/json"}
    :parameters {:path {:id Long}}
    :properties (fn [ctx]
                  (let [id (get-in ctx [:parameters :path :id])]
                    {:exists? (not (nil? (dbc/get-visit db id)))}))
    :methods {#_ :delete #_ {:response (fn [ctx]
                                         (let [id (get-in ctx [:parameters :path :id])]
                                           (dbc/delete-visit db id)))}
              :get {:response (fn [ctx]
                                (let [id (get-in ctx [:parameters :path :id])
                                      uri (yada/path-for ctx :visits/entry {:route-params {:id id}})]
                                  {:links {:self uri}
                                   :data (dbc/get-visit db id)}))}
              #_ :put #_ {:parameters {:body Visit}
                          :response (fn [ctx]
                                      (let [id (get-in ctx [:parameters :path :id])
                                            updated-visit (get-in ctx [:parameters :body])
                                            updated-visit1 (assoc updated-visit :id id)
                                            res (dbc/update-visit db updated-visit1)]
                                        (if-not (nil? res)
                                          nil
                                          (assoc-in ctx [:response :status] 404))))}}}))

(s/defn visit-routes :- bidi.schema/RoutePair
  "Define the API route for visit entities"
  [component :- Visits]
  (let [db (:db component)
        routes ["/visits"
                [;; Visit actions w/o requiring visit id
                 ["" (new-visit-index-resource db)]
                 ;; Visit actions requiring visit id
                 [["/" :id] (new-visit-node-resource db)]]]]
    ["" [routes]]))

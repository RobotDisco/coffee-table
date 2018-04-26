(ns coffee-table.component.visits
  (:require [yada.yada :as yada]
            [coffee-table.model :as m]
            [coffee-table.component.database :as dbc]
            [coffee-table.component.users :refer [verify]]
            [taoensso.timbre :as timbre]
            [schema.core :as s]
            [com.stuartsierra.component :as component])
  (:import [java.net URI]
           [coffee_table.component.database Database]
           [coffee_table.component.users Users]))

(timbre/refer-timbre)

(s/defrecord Visits [db :- (s/maybe Database)
                     users :- (s/maybe Users)]
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
  [db :- Database
   users :- Users]
  (yada/resource
   {:access-control {:allow-methods [:options :head :get :post]
                     :allow-headers ["Content-Type" "Authorization"]
                     :scheme "Basic"
                     :verify (fn [[username password]]
                               (when (verify users username password)
                                 {:user username
                                  :roles #{:user}}))
                     :authorization {:methods {:get :user
                                               :post :user}}}
    :id :visits/index
    :summary "Café Visit index"
    #_ :logger #_ #(info %)
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
  [db :- Database
   users :- Users]
  (yada/resource
   {#_ :access-control #_ {:allow-origin "http://localhost:3449"
                           :allow-methods [:options :head :get :put :delete]
                           :allow-headers ["Content-Type" "Authorization"]
                           :scheme :jwt
                           :authorization {:methods {:get :user
                                                     :put :user
                                                     :delete :user}}}
    :id :visits/entry
    #_ :logger #_ #(info %)
    :description "Café Visit entries"
    :consumes #{"application/json"}
    :produces #{"application/json"}
    :parameters {:path {:id s/Int}}
    :properties (fn [ctx]
                  (let [id (get-in ctx [:parameters :path :id])]
                    {:exists? (not (nil? (dbc/get-visit db id)))}))
    :methods {:delete {:response (fn [ctx]
                                   (let [id (get-in ctx [:parameters :path :id])
                                         _ (dbc/delete-visit-by-id! db id)]
                                     nil))}
              :get {:response (fn [ctx]
                                (let [id (get-in ctx [:parameters :path :id])
                                      uri (yada/path-for ctx :visits/entry {:route-params {:id id}})]
                                  {:links {:self uri}
                                   :data (dbc/get-visit db id)}))}
              :put {:parameters {:body m/Visit}
                    :response (fn [ctx]
                                (let [id (get-in ctx [:parameters :path :id])
                                      updated-visit (get-in ctx [:parameters :body])
                                      res (dbc/update-visit! db id updated-visit)]
                                  (if (> res 0)
                                    nil
                                    (assoc-in ctx [:response :status] 404))))}}}))

(s/defn visit-routes :- bidi.schema/RoutePair
  "Define the API route for visit entities"
  [component :- Visits]
  (let [db (:db component)
        users (:users component)
        routes ["/visits"
                [;; Visit actions w/o requiring visit id
                 ["" (new-visit-index-resource db users)]
                 ;; Visit actions requiring visit id
                 [["/" :id] (new-visit-node-resource db users)]]]]
    [""
     [routes]]))

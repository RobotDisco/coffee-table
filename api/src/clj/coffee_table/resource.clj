(ns coffee-table.resource
  (:require [yada.yada :as yada]
            [bidi.bidi :as bidi]
            [coffee-table.model :as m]
            [coffee-table.database :as dbc :refer [verify]]
            [taoensso.timbre :as timbre]
            [schema.core :as s]
            [selmer.parser :as selmer])
  (:import [java.net URI]))

(timbre/refer-timbre)

(s/defn new-index-resource :- yada.schema/Resource
  "Resource for visit collection (create, list)"
  [db]
  (yada/resource
   {:access-control {:allow-methods [:options :head :get :post]
                     :allow-headers ["Content-Type" "Authorization"]
                     :scheme "Basic"
                     :verify (fn [[username password]]
                               (when (verify db username password)
                                 {:user username
                                  :roles #{:user}}))
                     :authorization {:methods {:get :user
                                               :post :user}}}
    :id :coffee-table.resources.visits/index
    :summary "Café Visit index"
    #_ :logger #_ #(info %)
    :description "Café Visit index"
    :consumes #{"application/json"}
    :produces [{:media-type #{"text/html" "application/edn;q=0.9" "application/json;q=0.8" "application/transit+json;q=0.7"}
                :charset "UTF-8"}]
    :methods {:get {:swagger/tags ["default" "getters"]
                    :response (fn [ctx]
                                (let [entries (mapv (fn [x] {:links {:self (yada/path-for ctx :coffee-table.resources.visits/entry {:route-params {:id (:id x)}})}
                                                             :data x})
                                                    (dbc/list-visit-summaries db))]
                                  (case (yada/content-type ctx)
                                    "text/html" (selmer/render-file
                                                 "coffee-table.html"
                                                 {:title "Coffee Table"
                                                  :ctx ctx
                                                  :entries entries})
                                    {:data entries})))}
              :post {:consumes #{"application/x-www-form-urlencoded"}
                     :parameters {:form m/Visit}
                     :response (fn [ctx]
                                 (let [id (dbc/insert-visit! db (merge
                                                                 {:ambience_rating nil
                                                                  :service_rating nil}
                                                                 (get-in ctx [:parameters :form])))]
                                   (URI. (yada/path-for ctx :visits/entry {:route-params {:id id}}))))}}}))

(s/defn new-node-resource :- yada.schema/Resource
  "Resource for visit items (get, update, delete)"
  [db]
  (yada/resource
   {:access-control {:allow-methods [:options :head :get :put :delete]
                     :allow-headers ["Content-Type" "Authorization"]
                     :scheme "Basic"
                     :verify (fn [[username password]]
                               (when (dbc/verify db username password)
                                 {:user username
                                  :roles #{:user}}))
                     :authorization {:methods {:get :user
                                               :put :user
                                               :delete :user}}}
    :id :coffee-table.resources.visits/entry
    #_ :logger #_ #(info %)
    :description "Café Visit entries"
    :consumes #{"application/json"}
    :produces [{:media-type #{"text/html" "application/edn;q=0.9" "application/json;q=0.8" "application/transit+json;q=0.7"}
                :charset "UTF-8"}]
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
                                      uri (yada/path-for ctx :visits/entry {:route-params {:id id}})
                                      data (dbc/get-visit db id)]
                                  (case (yada/content-type ctx)
                                    "text/html" (selmer/render-file
                                                 "visit-entry.html"
                                                 {:title "Visit"
                                                  :ctx ctx
                                                  :entry data})
                                    {:links {:self uri}
                                     :data (dbc/get-visit db id)})))}
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
  [{:coffee-table/keys [database]}]
  (let [routes ["/visits"
                [;; Visit actions w/o requiring visit id
                 ["" (new-index-resource database)]
                 ;; Visit actions requiring visit id
                 [["/" :id] (new-node-resource database)]]]]
    [""
     [routes]]))

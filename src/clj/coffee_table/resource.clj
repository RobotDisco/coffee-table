(ns coffee-table.resource
  (:require [yada.yada :as yada]))

(defn new-visit-index-resource [db]
  #_ (yada/resource
   {:access-control {:allow-origin "http://localhost:3449"
                     :allow-methods [:options :head :get :post]
                     :allow-headers ["Content-Type" "Authorization"]
                     :scheme :jwt
                     :authorization {:methods {:get :user
                                               :post :user}}}
    :logger stupid-logger
    :description "Café Visit index"
    :consumes #{"application/json"}
    :produces #{"application/json"}
    :methods {:get {:response (fn [ctx]
                                (dbc/visits db))}
              :post {:parameters {:body Visit}
                     :response (fn [ctx]
                                 (let [visit (dbc/add-visit db (get-in ctx [:parameters :body]))
                                       id (:id visit)]
                                   (URI. (str "/visits/" id))))}}}))

(defn new-visit-node-resource [db]
  #_ (yada/resource
   {:access-control {:allow-origin "http://localhost:3449"
                     :allow-methods [:options :head :get :put :delete]
                     :allow-headers ["Content-Type" "Authorization"]
                     :scheme :jwt
                     :authorization {:methods {:get :user
                                               :put :user
                                               :delete :user}}}
    :logger stupid-logger
    :description "Café Visit entries"
    :consumes #{"application/json"}
    :produces #{"application/json"}
    :parameters {:path {:id Long}}
    :properties (fn [ctx]
                  (let [id (get-in ctx [:parameters :path :id])]
                    {:exists? (not (nil? (dbc/visit db id)))}))
    :methods {:delete {:response (fn [ctx]
                                   (let [id (get-in ctx [:parameters :path :id])]
                                     (dbc/delete-visit db id)))}
              :get {:response (fn [ctx]
                                (let [id (get-in ctx [:parameters :path :id])]
                                  (dbc/visit db id)))}
              :put {:parameters {:body Visit}
                    :response (fn [ctx]
                                (let [id (get-in ctx [:parameters :path :id])
                                      updated-visit (get-in ctx [:parameters :body])
                                      updated-visit1 (assoc updated-visit :id id)
                                      res (dbc/update-visit db updated-visit1)]
                                  (if-not (nil? res)
                                    nil
                                    (assoc-in ctx [:response :status] 404))))}}}))

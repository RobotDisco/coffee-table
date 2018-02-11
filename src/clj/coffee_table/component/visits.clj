(ns coffee-table.component.visits
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]))

(s/defrecord Visits
    ;; "Component for handling the business logic of Visits"
    []
    component/Lifecycle
    (start [this])
    (stop [this]))

(s/defn new-visit
  []
  (map->Visits {}))

(ns coffee-table.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]))

(defn config
  "Read EDN config, with the given profile. See Aero docs at
  https://github.com/juxt/aero for details."
  [profile]
  (aero/read-config (io/resource "config.edn") {:profile profile}))

(defn migratus
  "Return the migratus subset of the given config"
  [config]
  (get config :migratus))

(defn database-spec
  "Return the spec dictionary, or URL, of the app's database"
  [config]
  (get-in config [:database :spec]))


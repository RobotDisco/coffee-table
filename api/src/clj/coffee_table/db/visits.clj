(ns coffee-table.db.visits
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/visits.sql")
(hugsql/def-sqlvec-fns "sql/visits.sql")


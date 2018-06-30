(ns coffee-table.db.users
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/users.sql")
(hugsql/def-sqlvec-fns "sql/users.sql")

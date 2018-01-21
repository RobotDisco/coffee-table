(ns user
  (:require [coffee-table.system :refer [new-system]]
            [reloaded :refer [system init start stop go reset reset-all]]))

(reloaded.repl/set-init! #(new-system))


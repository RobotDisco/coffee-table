(defproject net.robot-disco/coffee-table "0.0.0-SNAPSHOT"
  :description "A site for Gaelan's café reviews"
  :url "http://github.com/RobotDisco/coffee-table"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :min-lein-version "2.0.0"
  :dependencies [;; Configuration management
                 [aero "1.1.2"]

                 ;; Component management
                 [com.stuartsierra/component "0.3.2"]]
  :plugins [[com.roomkey/lein-v "6.2.0"]]
  :source-paths ["src/clj"]
  :profiles {:uberjar {:omit-source true
                       :aot :all
                       :uberjar-name "coffee_table.jar"}}
  :main ^:skip-aot coffee-table.core
  :prep-tasks [["v" "cache" "src"]]
  :release-tasks [["vcs" "assert-committed"]
                  ["v" "update"] ;; compute new version and tag it
                  ["vcs" push]
                  ["deploy"]])

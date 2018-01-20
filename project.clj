(defproject coffee-table "0.0.0-SNAPSHOT"
  :description "A site for Gaelan's café reviews"
  :url "http://github.com/RobotDisco/coffee-table"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  
  :dependencies [;; Configuration management
                 [aero "1.1.2"]
                 
                 ;; Component management
                 [com.stuartsierra/component "0.3.2"]
                 
                 ;; Logging
                 [org.clojure/tools.logging "0.4.0"]]

  :min-lein-version "2.0.0"
  
  :source-paths ["src/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"

  :main ^:skip-aot coffee-table.core
  
  :plugins [[com.roomkey/lein-v "6.2.0"]]
  
  :profiles {:uberjar {:omit-source true
                       :aot :all
                       :uberjar-name "coffee_table.jar"}}
  
  :prep-tasks [["v" "cache" "src"]]
  :release-tasks [["vcs" "assert-committed"]
                  ["v" "update"] ;; compute new version and tag it
                  ["vcs" push]
                  ["deploy"]])

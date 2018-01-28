(defproject coffee-table "0.0.3-SNAPSHOT"
  :description "A site for Gaelan's café reviews"
  :url "http://github.com/RobotDisco/coffee-table"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  
  :dependencies [;; Core language runtime
                 [org.clojure/clojure "1.9.0"] 
                 
                 ;; Configuration management
                 [aero "1.1.2"]
                 
                 ;; Component management
                 [com.stuartsierra/component "0.3.2"]

                 ;; Databases; SQL, migrations
                 [migratus "1.0.3"]
                 [org.postgresql/postgresql "42.2.0"]
                 
                 ;; Logging
                 [com.taoensso/timbre "4.10.0"]]

  :env {:squiggly {checkers [:eastwood :kibit]}}
  
  :min-lein-version "2.0.0"
  
  :source-paths ["src/clj"]
  :repl-options {:init-ns user}
  :resource-paths ["resources"]
  :target-path "target/%s/"

  :main ^:skip-aot coffee-table.core
  
  :plugins [[com.roomkey/lein-v "6.2.0"]
            [lein-environ "1.1.0"]]
  
  :profiles {:uberjar {:omit-source true
                       :aot :all
                       :uberjar-name "coffee_table.jar"}
             :dev {:dependencies [[reloaded.repl "0.2.4"]]
                   :source-paths ["dev"]}}
  
  #_ :prep-tasks #_ [["v" "cache" "src/clj"]]
  #_ :release-tasks #_ [["vcs" "assert-committed"]
                  ["v" "update"] ;; compute new version and tag it
                  ["vcs" push]
                        ["deploy"]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version"
                   "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])

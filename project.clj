(defproject coffee-table "0.0.4-SNAPSHOT"
  :description "A site for Gaelan's café reviews"
  :url "http://github.com/RobotDisco/coffee-table"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :dependencies [;; Core language runtime
                 [org.clojure/clojure "1.8.0"]

                 ;; Configuration management
                 [aero "1.1.2"]
                 [environ "1.1.0"]

                 ;; Time/Date libraries
                 [clojure.java-time "0.3.1"]

                 ;; Component management
                 [com.stuartsierra/component "0.3.2"]

                 ;; Databases; SQL, migrations
                 [com.layerware/hugsql "0.4.8"]
                 [migratus "1.0.3"]
                 [org.postgresql/postgresql "42.2.1"]

                 ;; HTTP resources, routing, serving
                 [yada "1.2.11"]

                 ;; Logging
                 [com.taoensso/timbre "4.10.0"]

                 ;; Mocks
                 [ring/ring-mock "0.3.2"]
                 [byte-streams "0.2.3"] ; This is how mock requests return bodies

                 ;; schema (data model) validation, coercion
                 [prismatic/schema "1.1.7"]]

  :env {:squiggly {checkers [:eastwood :kibit]}}

  :min-lein-version "2.0.0"

  :source-paths #{"src/clj" "src/cljc"}
  :test-paths ["test/clj"]
  :repl-options {:init-ns user}
  :resource-paths ["resources"]
  :target-path "target/%s/"

  :main ^:skip-aot coffee-table.core

  :plugins [[jonase/eastwood "0.2.5"]
            [com.roomkey/lein-v "6.2.0"]
            [lein-environ "1.1.0"]
            [lein-kibit "0.1.5"]]

  :profiles {:uberjar {:omit-source true
                       :aot :all
                       :uberjar-name "coffee_table.jar"}
             :dev {:dependencies [[reloaded.repl "0.2.4"]]
                   :env {:clj-profile "dev"}
                   :source-paths ["dev"]}
             :test {:env {:clj-profile "test"}}}

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

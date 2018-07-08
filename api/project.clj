(defproject coffee-table "0.0.9-SNAPSHOT"
  :description "A site for Gaelan's café reviews"
  :url "http://github.com/RobotDisco/coffee-table"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :dependencies [;; Core language runtime
                 [org.clojure/clojure "1.9.0"]

                 ;; Configuration management
                 [aero "1.1.3"]
                 [environ "1.1.0"]

                 ;; Time/Date libraries
                 [clojure.java-time "0.3.2"]

                 ;; Component management
                 [integrant "0.6.3"]

                 ;; Databases; SQL, migrations, connection pooling
                 [com.layerware/hugsql "0.4.9"]
                 [migratus "1.0.6"]
                 [org.postgresql/postgresql "42.2.2"]

                 ;; HTTP resources, routing, serving
                 [bidi "2.1.3"]
                 [yada "1.2.13"]

                 ;; Logging
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.8"]

                 ;; Base64
                 [org.clojure/data.codec "0.1.1"]

                 ;; Mocks
                 [ring/ring-mock "0.3.2"]
                 [byte-streams "0.2.4"] ; This is how mock requests return bodies

                 ;; Password management
                 [buddy/buddy-hashers "1.3.0"]

                 ;; schema (data model) validation, coercion
                 [prismatic/schema "1.1.9"]]

  :env {:squiggly {checkers [:eastwood :kibit]}}

  :min-lein-version "2.0.0"

  :source-paths #{"src/clj" "src/cljc"}
  :test-paths ["test/clj"]
  :repl-options {:init-ns user}
  :resource-paths ["resources"]
  :target-path "target/%s/"

  :main ^:skip-aot coffee-table.core

  :plugins [[jonase/eastwood "0.2.5"]
            [lein-environ "1.1.0"]
            [lein-kibit "0.1.5"]
            [migratus-lein "0.5.7"]]

  :profiles {:uberjar {:omit-source true
                       :aot :all}
             :dev {:env {:clj-profile "dev"}
                   :source-paths ["dev"]
                   :dependencies [[integrant/repl "0.3.1"]]}
             :test {:env {:clj-profile "test"}}}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version"
                   "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])

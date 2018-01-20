(defproject net.robot-disco/coffee-table :lein-v
  :description "A site for Gaelan's café reviews"
  :url "http://github.com/RobotDisco/coffee-table"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :min-lein-version "2.0.0"
  :plugins [[com.roomkey/lein-v "6.2.0l"]]
  :prep-tasks [["v" "cache" "src/clj"]]
  :release-tasks [["vcs" "assert-committed"]
                  ["v" "update"] ;; compute new version and tag it
                  ["vcs" push]
                  ["deploy"]])


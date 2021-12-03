(defproject job-queue-manager "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main job-queue-manager.app
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [failjure "1.3.0"]
                 [org.clojure/data.json "0.2.6"]
                 [metosin/scjsv "0.4.1"]
                 [ring "1.6.3"]
                 [metosin/compojure-api "1.1.11"]
                 [ring/ring-mock "0.3.2"]]
  :source-paths ["src/clj"])

(defproject weather-app "0.1.0-SNAPSHOT"
  :description "Weather app"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-time "0.11.0"] ; required due to bug in `lein-ring uberwar`
                 [metosin/compojure-api "1.1.1"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [org.clojure/data.json "0.1.2"]
                 [clj-http "0.2.1"]]
  :ring {:handler weather-app.routes.handler/app :port 3399 :reload-paths ["src/"]}
  :source-paths ["src/"]
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]
                   :plugins [[lein-ring "0.9.7"]]}}
  :main weather-app.core)


(ns weather-app.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [weather-app.routes.handler :refer [app]]))


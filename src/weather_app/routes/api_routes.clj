(ns weather-app.routes.api-routes
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [clj-http.client :as http])
  (:use [clojure.data.json :only [read-json pprint-json]]
        [clojure.string :only [join split]]))

(def search-url "http://autocomplete.wunderground.com/aq?query=")
(def base-url "http://api.wunderground.com/api/")
(def my-key "e2216f14d51a0427")
(def feature ["conditions" "astronomy"])

;; initial location
(def ^:private city (atom "London"))
(def ^:private country (atom "UK"))

(defn create-search-url [cityLocation]
  "Create a URL to get names for cities and countries"
  (str search-url (clojure.string/replace cityLocation #" " "%20")))

(defn findCity
  "Get names of a cities and countries from search"
  [cityLocation]
  (-> (create-search-url cityLocation)
      (http/get {:headers {"Accept-Encoding" ""}})
      :body
      read-json))

(defn create-url [api-key features location]
  "Create URL to get json data"
  (str base-url api-key "/" (if (coll? features)
                              (join "/" (map name features))
                              (name features))
       "/q/" (.replace (if (string? location)
                         location
                         (join "/" (reverse location)))
                       " " "_") ".json"))

(defn api-call []
  "Read json data from the URL"
  (-> (create-url my-key feature [@city @country])
      (http/get {:headers {"Accept-Encoding" ""}})
      :body
      read-json))

;; initial data - using atoms to store information and easily change it for a new city (starts with London)
(def ^:private data (atom (api-call)))
(def ^:private temperature (atom (str (get (get @data :current_observation) :temp_c) "°C")))
(def ^:private date (atom (apply str (drop-last 15 (get (get @data :current_observation) :local_time_rfc822)))))
(def ^:private condition (atom (get (get @data :current_observation) :weather)))
(def ^:private wind (atom (str (get (get @data :current_observation) :wind_kph) "km/h")))
(def ^:private feels-like (atom (str (get (get @data :current_observation) :feelslike_c) "°C")))
(def ^:private humidity (atom (get (get @data :current_observation) :relative_humidity)))
(def ^:private sunrise (atom (str (get (get (get @data :sun_phase) :sunrise) :hour) ":" (get (get (get @data :sun_phase) :sunrise) :minute))))
(def ^:private sunset (atom (str (get (get (get @data :sun_phase) :sunset) :hour) ":" (get (get (get @data :sun_phase) :sunset) :minute))))
(def ^:private imgIcon (atom (str "http://icons.wxug.com/i/c/v4/" (get (get @data :current_observation) :icon) ".svg")))
(def ^:private id (atom [1]))
(def ^:private weatherData (atom [{:city @city :country @country :wind @wind :feels @feels-like
                                   :humidity @humidity :id @id :temperature @temperature :date @date
                                   :condition @condition :sunrise @sunrise :sunset @sunset :imgIcon @imgIcon}]))

(defn reset-data [cityLocation]
  "Get results from search: if the city is not found, don't change previous data; if city is found
  but and API doesn't return any data - show 'not found' (the case for Beijing, Shanghai and others)"
  (if (false? (empty? (get (findCity (:city cityLocation)) :RESULTS)))
  (do
    (reset! city (first (split (get (first (get (findCity (:city cityLocation)) :RESULTS)) :name) #",")))
    (reset! country (second (split (get (first (get (findCity (:city cityLocation)) :RESULTS)) :name) #",")))
    (reset! data (api-call))
    (if (false? (nil? (get (get @data :current_observation) :local_time_rfc822)))
      (do
        (reset! temperature (str (get (get @data :current_observation) :temp_c) "°C"))
        (reset! date (apply str (drop-last 15 (get (get @data :current_observation) :local_time_rfc822))))
        (reset! condition (get (get @data :current_observation) :weather))
        (reset! wind (str (get (get @data :current_observation) :wind_kph) "km/h"))
        (reset! feels-like (str (get (get @data :current_observation) :feelslike_c) "°C"))
        (reset! humidity (get (get @data :current_observation) :relative_humidity))
        (reset! sunrise (str (get (get (get @data :sun_phase) :sunrise) :hour) ":" (get (get (get @data :sun_phase) :sunrise) :minute)))
        (reset! sunset (str (get (get (get @data :sun_phase) :sunset) :hour) ":" (get (get (get @data :sun_phase) :sunset) :minute)))
        (reset! imgIcon (str "http://icons.wxug.com/i/c/v4/" (get (get @data :current_observation) :icon) ".svg"))
        (reset! id (System/currentTimeMillis))
        (reset! weatherData [{:city @city :country @country :wind @wind :feels @feels-like
                        :humidity @humidity :id @id :temperature @temperature :date @date
                        :condition @condition :sunrise @sunrise :sunset @sunset :imgIcon @imgIcon}]))
      (do
        (reset! city "Nothing found!")
        (reset! country " ")
        (reset! temperature " ")
        (reset! condition " ")
        (reset! wind " ")
        (reset! feels-like " ")
        (reset! humidity " ")
        (reset! sunrise " ")
        (reset! sunset " ")
        (reset! imgIcon "http://www.webiners.com/static/img/commons/no_results2.png")
        (reset! id (System/currentTimeMillis))
        (reset! weatherData [{:city @city :country @country :wind @wind :feels @feels-like
                        :humidity @humidity :id @id :temperature @temperature :date @date
                        :condition @condition :sunrise @sunrise :sunset @sunset :imgIcon @imgIcon}]))))
    @weatherData))


(defroutes api-routes
  "Send and receive data to/from the frontend"
  (context "/api" []
    (GET "/weatherData" []
      (ok @weatherData))

    (POST "/weatherData" {cityLocation :params}
      (reset! weatherData (reset-data cityLocation))
      (ok @weatherData)
      )))




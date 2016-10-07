(ns weather-app.routes.api-routes
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [clj-http.client :as http])
  (:use [clojure.data.json :only [read-json pprint-json]]
        [clojure.string :only [join split]]))


(def search-url "http://autocomplete.wunderground.com/aq?query=")
(def base-url "http://api.wunderground.com/api/")
(def my-key "XXXXXXXXXXXXXXXX")
(def feature ["conditions" "astronomy"])

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

(defn fetch-data [city-location]
  (when-let [city (first (split (get (first (get (findCity (:city city-location)) :RESULTS)) :name) #","))]
    (let [country (second (split (get (first (get (findCity (:city city-location)) :RESULTS)) :name) #","))
          data (api-call)]
      [city country data])))

(defn get-data [[city country data]]
  (if-let [temperature (str (get-in data [:current_observation :temp_c]) "°C")]
    (let [date (apply str (drop-last 15 (get-in data [:current_observation :local_time_rfc822])))
          condition (get-in data [:current_observation :weather])
          wind (str (get-in data [:current_observation :wind_kph]) "km/h")
          feels-like (str (get-in data [:current_observation :feelslike_c]) "°C")
          humidity (get-in data [:current_observation :relative_humidity])
          sunrise (str (get-in data [:sun_phase :sunrise :hour]) ":" (get-in data [:sun_phase :sunrise :minute]))
          sunset (str (get-in data [:sun_phase :sunset :hour]) ":" (get-in data [:sun_phase :sunset :minute]))
          imgIcon (str "http://icons.wxug.com/i/c/v4/" (get-in data [:current_observation :icon]) ".svg")
          id (System/currentTimeMillis)]
      [{:city city :country country :wind wind :feels feels-like
                        :humidity humidity :id id :temperature temperature :date date
                        :condition condition :sunrise sunrise :sunset sunset :imgIcon imgIcon}])))


#_(defn reset-data [cityLocation]
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
      (ok (get-data(fetch-data {:city "London"}))))

    (POST "/weatherData" {cityLocation :params}
      (ok (get-data (fetch-data cityLocation)))
      )))




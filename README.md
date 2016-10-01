# Weather App

Simple application to fetch weather data from https://www.wunderground.com/. Written in Clojure with React front-end.

Finds the city and shows it's weather data:

![london](https://cloud.githubusercontent.com/assets/11088666/19014645/8923a9c2-87ea-11e6-9afc-67f33f45c99e.png)
![glasgow](https://cloud.githubusercontent.com/assets/11088666/19014646/8bd1bf1a-87ea-11e6-9641-67897e6f1497.png)

## Usage

Get your own key from https://www.wunderground.com/weather/api, add it at to weather-app/src/weather_app/routes/api_routes.clj variable "my-key". Save a file, run in terminal:

`lein ring server`

## License

Copyright © 2016 

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

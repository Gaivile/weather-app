// render LeftAlign container to show the data
var LeftAlign = React.createClass({
  render: function() {
    // map the array of data to return each element
    var cityCountry = this.props.data.map(function(cityData) {
      return (
        <CityData city={cityData.city} key={cityData.id}
          country = {cityData.country}
          wind = {cityData.wind}
          feels = {cityData.feels}
          humidity = {cityData.humidity}
          sunrise = {cityData.sunrise}
          sunset = {cityData.sunset}>
        </CityData>
      );
    });
    return (
      <div className="LeftAlign">
        {cityCountry}
      </div>
    );
  }
});

// render the data inside of LeftAlign element
var CityData = React.createClass({
  render: function() {
    return (
      // show the elements accordingly
      <div className="cityData">
        <h2 className="city">
          {this.props.city}
        </h2>
        <h3 className = "country"> {this.props.country} </h3>
        <h4 className="feature">Wind speed: </h4> <h4 className="value"> {this.props.wind} </h4>
        <h4 className="feature">Feels like: </h4> <h4 className="value"> {this.props.feels} </h4>
        <h4 className="feature">Humidity: </h4> <h4 className="value"> {this.props.humidity} </h4>
        <h4 className="feature">Sunrise: </h4> <h4 className="value"> {this.props.sunrise} </h4>
        <h4 className="feature">Sunset: </h4> <h4 className="value"> {this.props.sunset} </h4>
      </div>
    );
  }
});

// render RightAlign container to show the data
var RightAlign = React.createClass({
  render: function() {
    // map the array of data to return each element
    var cityCountry = this.props.data.map(function(cityData) {
      return (
        <WeatherTemperature city={cityData.city} key={cityData.id}
          condition = {cityData.condition}
          temperature = {cityData.temperature}
          date = {cityData.date}
          imgIcon = {cityData.imgIcon}>
        </WeatherTemperature>
      );
    });
    return (
      <div className="RightAlign">
        {cityCountry}
      </div>
    );
  }
});

// render the data inside of RightAlign element
var WeatherTemperature = React.createClass({
  render: function() {
    return (
      // show the elements accordingly
      <div className="cityData">
        <img src={this.props.imgIcon}/>
        <h4 className = "date"> {this.props.date}</h4>
        <h3 className = "weather">{this.props.condition}</h3>
        <h4 className = "temp">{this.props.temperature}</h4>
      </div>
    );
  }
});

// create a form to search for a city
var ChangeLocationForm = React.createClass({
    getInitialState: function() {
    return {city: ''};
  },
  // get the input from the user
  handleCityChange: function(e) {
    this.setState({city: e.target.value});
  },
  // assign "city" variable to user's input and clear the inpu't field
  handleSubmit: function(e) {
    e.preventDefault();
    var city = this.state.city.trim();
    if (!city) {
      return;
    }
    this.props.onCityData({city: city});
    this.setState({city: ''});
  },

  // render a form to search for a city
  render: function() {
    return (
      <form className="ChangeLocationForm" onSubmit={this.handleSubmit}>
        <input
          type="text"
          placeholder="City"
          value={this.state.city}
          onChange={this.handleCityChange}
        />
      <input type="submit" value="Search" />
    </form>
    );
  }
});

// create the main container
var WeatherToday = React.createClass({
  //get json data and update state
  loadWeatherData: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },

  // handle city search and submit user's input
  handleSearch: function(cityData) {
      $.ajax({
        url: this.props.url,
        dataType: 'json',
        type: 'POST',
        data: cityData,
        success: function(data) {
          this.setState({data: data});
        }.bind(this),
        error: function(xhr, status, err) {
          console.error(this.props.url, status, err.toString());
        }.bind(this)
      });
  },

  // get data for UI
  getInitialState: function() {
    return {data: []};
  },
  // update data
  componentDidMount: function() {
    this.loadWeatherData();
  },

  // render DOM elements
  render: function() {
    return (
      <div className="weatherToday">
        <LeftAlign data = {this.state.data}/>
        <RightAlign data = {this.state.data}/>
        <ChangeLocationForm onCityData={this.handleSearch} />
      </div>
    );
  }
});

// connect to backend
ReactDOM.render(
  <WeatherToday url="/api/weatherData"/>,
  document.getElementById('content')
);

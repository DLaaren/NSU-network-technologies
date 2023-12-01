package ru.nsu.vinter.lab3.async.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherWind (
    @JsonProperty("speed") double windSpeed,
    @JsonProperty("deg") double windDegree,
    @JsonProperty("gust") double windGust
) {

    @Override
    public double windSpeed() {
        return windSpeed;
    }

    @Override
    public double windDegree() {
        return windDegree;
    }

    @Override
    public double windGust() {
        return windGust;
    }
}


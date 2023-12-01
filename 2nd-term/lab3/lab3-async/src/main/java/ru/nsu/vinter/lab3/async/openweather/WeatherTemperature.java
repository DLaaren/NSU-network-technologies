package ru.nsu.vinter.lab3.async.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherTemperature(
        @JsonProperty("temp") double temperature,
        @JsonProperty("feels_like") double temperatureFeelsLike,
        @JsonProperty("pressure") double pressure
) {

    @Override
    public double temperature() {
        return temperature;
    }

    @Override
    public double temperatureFeelsLike() {
        return temperatureFeelsLike;
    }

    @Override
    public double pressure() {
        return pressure;
    }
}

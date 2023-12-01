package ru.nsu.vinter.lab3.async.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherClouds (
        @JsonProperty("all") double cloudiness
) {

    @Override
    public double cloudiness() {
        return cloudiness;
    }
}

package ru.nsu.vinter.lab3.async.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record OpenWeatherResponse (
        @JsonProperty("weather") WeatherDesc[] weatherDesc,
        @JsonProperty("main") WeatherTemperature weatherTemperature,
        @JsonProperty("visibility") double weatherVisibility,
        @JsonProperty("snow") WeatherSnow weatherSnow,
        @JsonProperty("wind") WeatherWind weatherWind,
        @JsonProperty("rain") WeatherRain weatherRain,
        @JsonProperty("clouds") WeatherClouds weatherClouds
) {

    @Override
    public WeatherDesc[] weatherDesc() {
        return weatherDesc;
    }

    @Override
    public WeatherTemperature weatherTemperature() {
        return weatherTemperature;
    }

    @Override
    public double weatherVisibility() {
        return weatherVisibility;
    }

    @Override
    public WeatherSnow weatherSnow() {
        return weatherSnow;
    }

    @Override
    public WeatherWind weatherWind() {
        return weatherWind;
    }

    @Override
    public WeatherRain weatherRain() {
        return weatherRain;
    }

    @Override
    public WeatherClouds weatherClouds() {
        return weatherClouds;
    }
}
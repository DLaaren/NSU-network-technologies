package ru.nsu.vinter.lab3.async.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherRain (
       @JsonProperty("3h") int rainVolume
) {

    @Override
    public int rainVolume() {
        return rainVolume;
    }
}

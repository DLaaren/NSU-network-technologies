package ru.nsu.vinter.lab3.async.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherSnow (
        @JsonProperty("3h") int snowVolume
) {

    @Override
    public int snowVolume() {
        return snowVolume;
    }
}

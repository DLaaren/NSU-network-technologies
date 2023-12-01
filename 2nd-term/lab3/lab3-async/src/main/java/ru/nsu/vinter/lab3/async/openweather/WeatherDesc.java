package ru.nsu.vinter.lab3.async.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherDesc (
    @JsonProperty("icon") String iconId,
    @JsonProperty("main") String weatherType
) {

    @Override
    public String iconId() {
        return iconId;
    }

    @Override
    public String weatherType() {
        return weatherType;
    }
}

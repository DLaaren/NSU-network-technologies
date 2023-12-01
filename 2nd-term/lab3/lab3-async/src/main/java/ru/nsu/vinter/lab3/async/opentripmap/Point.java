package ru.nsu.vinter.lab3.async.opentripmap;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Point (
        @JsonProperty("lat") double latitude,
        @JsonProperty("lon") double longitude
) {}
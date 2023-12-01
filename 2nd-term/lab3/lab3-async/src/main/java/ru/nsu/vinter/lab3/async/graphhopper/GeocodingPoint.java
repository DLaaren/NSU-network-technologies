package ru.nsu.vinter.lab3.async.graphhopper;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeocodingPoint (
    @JsonProperty("lat") double latitude,
    @JsonProperty("lng") double longitude
) {}

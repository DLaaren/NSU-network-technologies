package ru.nsu.vinter.lab3.async.graphhopper;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.util.Pair;

public record GeocodingPoint (
    @JsonProperty("lat") double latitude,
    @JsonProperty("lng") double longitude
) {

    @Override
    public double latitude() {
        return latitude;
    }

    @Override
    public double longitude() {
        return longitude;
    }
}

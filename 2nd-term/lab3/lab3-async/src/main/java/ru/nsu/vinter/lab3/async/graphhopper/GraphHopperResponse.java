package ru.nsu.vinter.lab3.async.graphhopper;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GraphHopperResponse (
        @JsonProperty("message") String message,
        @JsonProperty("hits") GeocodingLocation[] hits,
        @JsonProperty("took") long took
) {

}

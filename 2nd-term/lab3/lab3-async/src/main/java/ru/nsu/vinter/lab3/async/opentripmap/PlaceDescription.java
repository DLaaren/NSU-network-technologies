package ru.nsu.vinter.lab3.async.opentripmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.nsu.vinter.lab3.async.graphhopper.GeocodingPoint;

public record PlaceDescription (
        @JsonProperty("name") String name,
        @JsonProperty("rate") String rate,
        @JsonProperty("point") Point geocodingPoint,
        @JsonProperty("info") Info description
) {}

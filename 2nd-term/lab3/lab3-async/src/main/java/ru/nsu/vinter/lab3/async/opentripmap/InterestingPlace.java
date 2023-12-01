package ru.nsu.vinter.lab3.async.opentripmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.nsu.vinter.lab3.async.graphhopper.GeocodingPoint;

public record InterestingPlace(
        @JsonProperty("xid") String xid,
        @JsonProperty("name") String name,
        @JsonProperty("kinds") String kinds,
        @JsonProperty("dist") double distance,
        @JsonProperty("point") Point geocodingPoint
) {}
package ru.nsu.vinter.lab3.async.graphhopper;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.util.Pair;
import ru.nsu.vinter.lab3.async.graphhopper.GeocodingPoint;

public record GeocodingLocation (
        @JsonProperty("point") GeocodingPoint geocodingPoint,
//        @JsonProperty("osm_id") String osm_id,
//        @JsonProperty("osm_type") String psm_type,
//        @JsonProperty("osm_key") String osm_key,
        @JsonProperty("name") String name,
        @JsonProperty("country") String country,
        @JsonProperty("city") String city,
        @JsonProperty("state") String state,
        @JsonProperty("street") String street,
        @JsonProperty("housenumber") String housenumber,
        @JsonProperty("postcode") String postcode
) {

    @Override
    public String name() {
        return name;
    }

    @Override
    public String country() {
        return country;
    }

    @Override
    public String city() {
        return city;
    }

    @Override
    public String state() {
        return state;
    }

    @Override
    public String street() {
        return street;
    }

    @Override
    public String housenumber() {
        return housenumber;
    }

    @Override
    public String postcode() {
        return postcode;
    }

    @Override
    public GeocodingPoint geocodingPoint() {
        return geocodingPoint;
    }
}

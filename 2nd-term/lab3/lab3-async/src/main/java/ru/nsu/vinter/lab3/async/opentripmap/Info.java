package ru.nsu.vinter.lab3.async.opentripmap;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Info (
    @JsonProperty("descr") String description
) {}

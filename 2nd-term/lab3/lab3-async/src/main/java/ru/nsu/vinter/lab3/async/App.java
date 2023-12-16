package ru.nsu.vinter.lab3.async;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.nsu.vinter.lab3.async.graphhopper.GeocodingLocation;
import ru.nsu.vinter.lab3.async.graphhopper.GeocodingPoint;
import ru.nsu.vinter.lab3.async.graphhopper.GraphHopperResponse;
import ru.nsu.vinter.lab3.async.opentripmap.InterestingPlace;
import ru.nsu.vinter.lab3.async.opentripmap.PlaceDescription;
import ru.nsu.vinter.lab3.async.openweather.OpenWeatherResponse;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class App {
    private String requestedPlace;
    private HashMap<String, String> api_keys;
    private GeocodingLocation[] places;
    private OpenWeatherResponse weather;
    private InterestingPlace[] interestingPlaces;
    private PlaceDescription placeDescription;

    public App(String requestedPlace) {
        this.requestedPlace = requestedPlace;

        this.api_keys = new HashMap<>();
        api_keys.put("graphhopper", "545e4668-9866-4fb0-b97e-1d1cd49bee76");
        api_keys.put("openweathermap", "22f6baa853beaae97866e356b9bd4a4a");
        api_keys.put("opentripmap", "5ae2e3f221c38a28845f05b6b32443833816842e4ada3c2cbd955612");

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                getPlace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void getPlace() throws IOException {
        String q = requestedPlace;
        String locale = "en";
        String limit = "10";
        URL url =  new URL("https://graphhopper.com/api/1/geocode?q=" + q.replaceAll(" ", "%20") + "&locale=" + locale + "&limit=" + limit + "&key=" + api_keys.get("graphhopper"));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        GraphHopperResponse response = objectMapper.readValue(url, GraphHopperResponse.class);

        if (response.message() != null) {
            System.out.println(response.message());
        }

         places = response.hits();
    }

    public GeocodingLocation[] getPlaces() {
        return places;
    }

    public void findInfoAboutPlace(GeocodingLocation location) {
        CompletableFuture<Void> weatherFutureTask = CompletableFuture.runAsync(() -> {
            try {
                getWeather(location.geocodingPoint());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        CompletableFuture<Void> interestingPlacesFutureTask = CompletableFuture.runAsync(() -> {
            try {
                getInterestingPlaces(location.geocodingPoint());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<Void> combined = CompletableFuture.allOf(weatherFutureTask, interestingPlacesFutureTask);
        try {
            combined.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void findPlaceDescription(InterestingPlace interestingPlaces) {
        CompletableFuture<Void> placeDescriptionFutureTask = CompletableFuture.runAsync(() -> {
            try {
                getPlaceDescription(interestingPlaces);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            placeDescriptionFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void getWeather(GeocodingPoint point) throws IOException {
        String lat = String.valueOf(point.latitude());
        String lon = String.valueOf(point.longitude());
        URL url =  new URL("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + api_keys.get("openweathermap"));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.weather = objectMapper.readValue(url, OpenWeatherResponse.class);
    }

    private void getInterestingPlaces(GeocodingPoint point) throws IOException {
        String radius = "300"; //im meters
        String lat = String.valueOf(point.latitude());
        String lon = String.valueOf(point.longitude());
        String limit = "10";
        URL url =  new URL("https://api.opentripmap.com/0.1/en/places/radius?radius=" + radius + "&lon=" + lon + "&lat=" + lat + "&kinds=interesting_places&format=json&limit="+ limit + "&apikey=" + api_keys.get("opentripmap"));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.interestingPlaces = objectMapper.readValue(url, InterestingPlace[].class);
    }

    public void getPlaceDescription(InterestingPlace interestingPlace) throws IOException {
        String xid = interestingPlace.xid();
        URL url =  new URL("https://api.opentripmap.com/0.1/en/places/xid/" + xid + "?apikey=" + api_keys.get("opentripmap"));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.placeDescription = objectMapper.readValue(url, PlaceDescription.class);
    }

    public String getRequestedPlace() {
        return requestedPlace;
    }

    public OpenWeatherResponse getWeather() {
        return weather;
    }

    public InterestingPlace[] getInterestingPlaces() {
        return interestingPlaces;
    }

    public PlaceDescription getPlaceDescription() {
        return placeDescription;
    }
}

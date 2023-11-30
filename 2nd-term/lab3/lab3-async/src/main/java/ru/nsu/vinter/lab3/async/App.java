package ru.nsu.vinter.lab3.async;

import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class App {
    String requestedPlace;
    HashMap<String, String> api_keys;
    HashMap<String, Pair<Double, Double>> places;

    public App(String requestedPlace) {
        this.requestedPlace = requestedPlace;

        this.api_keys = new HashMap<>();
        api_keys.put("findPlace", "545e4668-9866-4fb0-b97e-1d1cd49bee76");
        api_keys.put("weather", "");
        api_keys.put("interestingPlaces", "");
        api_keys.put("placeDescription", "");

        this.places = new HashMap<>();

        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            try {
                getPlace(requestedPlace, places);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            completableFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void getPlace(String requestedPlace, HashMap<String, Pair<Double, Double>> places) throws IOException {
        String q = requestedPlace;
        String locale = "en";
        String limit = "10";

        URLConnection connection =
                new URL("https://graphhopper.com/api/1/geocode?q=" + q + "&locale=" + locale + "&limit=" + limit + "&key=" + api_keys.get("findPlace")).openConnection();

        
        // set places with result
    }

    public HashMap<String, Pair<Double, Double>> getPlaces() {
        return places;
    }

    private void getWeather() {

    }

    private void getInterestingPlaces() {

    }

    private void getPlaceDescription() {

    }
}

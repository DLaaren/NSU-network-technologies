package ru.nsu.vinter.lab3.async;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ru.nsu.vinter.lab3.async.graphhopper.GeocodingLocation;
import ru.nsu.vinter.lab3.async.opentripmap.InterestingPlace;
import ru.nsu.vinter.lab3.async.opentripmap.PlaceDescription;
import ru.nsu.vinter.lab3.async.openweather.OpenWeatherResponse;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.Math.round;

public class Controller {
    private App app;

    private HashMap<String, GeocodingLocation> geocodingLocationStringHashMap;
    private HashMap<String, InterestingPlace> interestingPlaceInfoStringHashMap;

    @FXML
    TabPane background;
    @FXML
    TextField userSearchField;
    @FXML
    Button findLocationButton;
    @FXML
    VBox placesList;
    @FXML
    ImageView weatherIcon;
    @FXML
    Label temperatureInfo;
    @FXML
    Label visibilityInfo;
    @FXML
    Label cloudsInfo;
    @FXML
    Label rainInfo;
    @FXML
    Label snowInfo;
    @FXML
    Label windSpeedInfo;
    @FXML
    Label windDegreeInfo;
    @FXML
    Label windGustInfo;
    @FXML
    ScrollPane placeDescriptionText;
    @FXML
    VBox interestingPlacesList;

    @FXML
    public void findLocationButtonPressed() {
        geocodingLocationStringHashMap = new HashMap<>();
        placesList.getChildren().clear();

        String requestedPlace = userSearchField.getCharacters().toString();
        if (!requestedPlace.isEmpty()) {
            app = new App(requestedPlace);
        }

        GeocodingLocation[] places = app.getPlaces();
        while (places.length == 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            places = app.getPlaces();
        }
        
        drawPlacesList(places);
    }

    public void drawPlacesList(GeocodingLocation[] places) {
        ListView<String> list = new ListView<>();
        list.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item);
                    setFont(Font.font(15));
                }
            }
        });

        for (GeocodingLocation place : places) {
            String placeInfoString = getPlaceStringInfo(place);
            list.getItems().add(placeInfoString);
            geocodingLocationStringHashMap.put(placeInfoString, place);
        }

        VBox.setVgrow(list, Priority.ALWAYS);
        placesList.getChildren().add(list);
        list.setOnMouseClicked(event -> {
            app.findInfoAboutPlace(geocodingLocationStringHashMap.get(
                                list.getSelectionModel().getSelectedItem()));
            CompletableFuture<Void> weatherFutureTask = CompletableFuture.runAsync(() -> {
                OpenWeatherResponse weather = app.getWeather();
                while (weather == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    weather = app.getWeather();
                }

                OpenWeatherResponse finalWeather = weather;
                Platform.runLater(() -> {
                    try {
                        drawWeather(finalWeather);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            CompletableFuture<Void> interestingPlacesFutureTask = CompletableFuture.runAsync(() -> {
                InterestingPlace[] interestingPlace = app.getInterestingPlaces();
                while (interestingPlace == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    interestingPlace = app.getInterestingPlaces();
                }
                InterestingPlace[] finalInterestingPlace = interestingPlace;
                Platform.runLater(() -> {
                    drawInterestingPlaces(finalInterestingPlace);
                });
            });

            CompletableFuture<Void> combined = CompletableFuture.allOf(weatherFutureTask, interestingPlacesFutureTask);
            try {
                combined.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String getPlaceStringInfo(GeocodingLocation place) {
        String placeInfoString = "Name: " + place.name() + "\n" +
                                 "Latitude: " + place.geocodingPoint().latitude() + "\n" +
                                 "Longitude: " + place.geocodingPoint().longitude() + "\n";
        if (place.country() != null) {
            placeInfoString = placeInfoString.concat("Country: " + place.country() + "\n");
        }
        if (place.city() != null) {
            placeInfoString = placeInfoString.concat("City: " + place.city() + "\n");
        }
        if (place.state() != null) {
            placeInfoString = placeInfoString.concat("State: " + place.state() + "\n");
        }
        if (place.street() != null) {
            placeInfoString = placeInfoString.concat("Street: " + place.street() + "\n");
        }
        if (place.housenumber() != null) {
            placeInfoString = placeInfoString.concat("House number: " + place.housenumber() + "\n");
        }
        if (place.postcode() != null) {
            placeInfoString = placeInfoString.concat("Postcode: " + place.postcode() + "\n");
        }
        return placeInfoString;
    }

    public void drawWeather(OpenWeatherResponse weather) throws MalformedURLException {
        String iconId = String.valueOf((weather.weatherDesc())[0].iconId());
        String url = "https://openweathermap.org/img/wn/" + iconId + "@2x.png";
        this.weatherIcon.setImage(new Image(url));

        String temperature = String.valueOf(round(weather.weatherTemperature().temperature() - 273));
        String visibility = String.valueOf(weather.weatherVisibility());
        String clouds = "0";
        String rain = "0";
        String snow = "0";
        String windSpeed = "0";
        String windDegree = "0";
        String windGust ="0";

        if (weather.weatherClouds() != null) {
            clouds = String.valueOf(weather.weatherClouds().cloudiness());
        }
        else if (weather.weatherRain() != null) {
            rain = String.valueOf(weather.weatherRain().rainVolume());
        }
        else if (weather.weatherSnow() != null) {
            snow = String.valueOf(weather.weatherSnow().snowVolume());
        }
        else if (weather.weatherWind() != null) {
            windSpeed = String.valueOf(weather.weatherWind().windSpeed());
            windDegree = String.valueOf(weather.weatherWind().windDegree());
            windGust = String.valueOf(weather.weatherWind().windGust());
        }

        temperatureInfo.setText(temperature);
        visibilityInfo.setText(visibility);
        cloudsInfo.setText(clouds);
        rainInfo.setText(rain);
        snowInfo.setText(snow);
        windSpeedInfo.setText(windSpeed);
        windDegreeInfo.setText(windDegree);
        windGustInfo.setText(windGust);
    }

    public void drawInterestingPlaces(InterestingPlace[] interestingPlaces) {
        interestingPlacesList.getChildren().clear();
        interestingPlaceInfoStringHashMap = new HashMap<>();
        ListView<String> list = new ListView<>();
        list.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item);
                    setFont(Font.font(15));
                }
            }
        });

        for (InterestingPlace interestingPlace : interestingPlaces) {
            String interestingPlaceInfoString = getInterestingPlaceStringInfo(interestingPlace);
            list.getItems().add(interestingPlaceInfoString);
            interestingPlaceInfoStringHashMap.put(interestingPlaceInfoString, interestingPlace);
        }

        VBox.setVgrow(list, Priority.ALWAYS);
        interestingPlacesList.getChildren().add(list);
        list.setOnMouseClicked(event -> {
            app.findPlaceDescription(interestingPlaceInfoStringHashMap.get(
                    list.getSelectionModel().getSelectedItem()));
            CompletableFuture<Void> descriptionFutureTask = CompletableFuture.runAsync(() -> {
                PlaceDescription placeDescription = app.getPlaceDescription();
                while (placeDescription == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    placeDescription = app.getPlaceDescription();
                }
                PlaceDescription finalPlaceDescription = placeDescription;
                Platform.runLater(() -> {
                    drawPlaceDescription(finalPlaceDescription);
                });

            });

            try {
                descriptionFutureTask.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String getInterestingPlaceStringInfo(InterestingPlace interestingPlace) {
        return "Name: " + interestingPlace.name() + "\n" +
                "Latitude: " + interestingPlace.geocodingPoint().latitude() + "\n" +
                "Longitude: " + interestingPlace.geocodingPoint().longitude() + "\n" +
                "Kinds: " + interestingPlace.kinds() + "\n" +
                "Distance: " + interestingPlace.distance() + "\n";
    }

    public void drawPlaceDescription(PlaceDescription placeDescription) {
        String info = "Name: " + placeDescription.name() + "\n" +
                "Rate: " + placeDescription.rate() + "\n" +
                "Latitude: " + placeDescription.geocodingPoint().latitude() + "\n" +
                "Longitude: " + placeDescription.geocodingPoint().longitude() + "\n";
        if (placeDescription.description() != null && placeDescription.description().description() != null) {
            info = info.concat("Description: " + placeDescription.description().description());
            info = info.replaceAll("</p>", "");
            info = info.replaceAll("<p>", "");
            info = info.replaceAll("<span>", "");
            info = info.replaceAll("</span>", "");
            info = info.replaceAll("<&nbsp;>", "");
        }
        Label label = new Label();
        label.setText(info);
        label.setFont(Font.font(25));
        label.setTextAlignment(TextAlignment.LEFT);
        label.setContentDisplay(ContentDisplay.CENTER);
        label.setWrapText(true);
        label.setStyle("-fx-max-width:1000");
        placeDescriptionText.setContent(label);
    }
}

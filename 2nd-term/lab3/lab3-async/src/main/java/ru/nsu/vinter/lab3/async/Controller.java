package ru.nsu.vinter.lab3.async;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.Objects;

public class Controller {
    private App app;

    @FXML
    TextField userSearchField;

    @FXML
    Button findLocationButton;

    @FXML
    public void findLocationButtonPressed() {
        String requestedPlace = userSearchField.getCharacters().toString();
        if (!requestedPlace.equals("")) {
            app = new App(requestedPlace);
        }
    }
}
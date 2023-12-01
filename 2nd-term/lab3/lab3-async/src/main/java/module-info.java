module ru.nsu.vinter.lab3.async {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    opens ru.nsu.vinter.lab3.async to javafx.fxml;
    exports ru.nsu.vinter.lab3.async;
    exports ru.nsu.vinter.lab3.async.graphhopper;
    exports ru.nsu.vinter.lab3.async.openweather;
    exports ru.nsu.vinter.lab3.async.opentripmap;
    opens ru.nsu.vinter.lab3.async.graphhopper to javafx.fxml;
}
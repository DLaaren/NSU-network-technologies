module ru.nsu.vinter.lab3.async {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens ru.nsu.vinter.lab3.async to javafx.fxml;
    exports ru.nsu.vinter.lab3.async;
}
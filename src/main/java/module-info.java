module com.example.terminalapplication {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.terminalapplication to javafx.fxml;
    exports com.example.terminalapplication;
}
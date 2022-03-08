module com.example.smatp2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.sma to javafx.fxml;
    exports com.sma;
}
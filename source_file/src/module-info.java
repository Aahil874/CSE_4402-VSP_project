module vsp_project {
    requires javafx.controls;
    requires javafx.fxml; // <-- MAKE SURE THIS LINE IS HERE

    opens application to javafx.fxml; // <-- ALLOWS FXML TO READ YOUR CONTROLLER
    exports application;
}
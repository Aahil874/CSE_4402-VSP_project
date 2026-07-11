module YourProjectName {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop; // <-- ADD THIS LINE HERE
    
    opens application to javafx.graphics, javafx.fxml;
}
module vsp_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires javafx.graphics;

    requires com.almasb.fxgl.all;

    opens application to javafx.graphics, javafx.fxml;
}
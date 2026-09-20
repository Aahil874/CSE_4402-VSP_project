package application;

import javafx.application.Application;
import javafx.stage.Stage;

public final class GameApplication extends Application {
    @Override
    public void start(Stage stage) {
        SceneManager.initialize(stage);
        SceneManager.showDatabaseLogin();
    }

    @Override
    public void stop() {
        DatabaseConnection.clear();
    }
}

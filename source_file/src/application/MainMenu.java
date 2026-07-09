package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenu extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
        	Parent root = FXMLLoader.load(getClass().getResource("/application/MainMenu.fxml"));
        	Scene scene = new Scene(root, 800, 600);
        	scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
            
            primaryStage.setTitle("Baseball Game - Main Menu");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args); // This is likely line 29!
    }
}
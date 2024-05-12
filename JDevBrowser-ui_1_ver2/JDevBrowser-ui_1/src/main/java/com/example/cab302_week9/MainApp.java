package com.example.cab302_week9;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BrowserUI.fxml")); // Make sure the path is correct
        Parent root = loader.load();

        BrowserController controller = loader.getController();  // This is the correct way to get the controller
        controller.updateTheme();  // Call the method after the scene is shown

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.example.cab302_week9;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("BrowserUI.fxml")); // Load main UI
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        showTimerWindow(); // Optionally, open the timer window on start
    }

    public void showTimerWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TimerWindow.fxml"));
        Parent timerRoot = loader.load();
        Stage timerStage = new Stage();
        timerStage.setScene(new Scene(timerRoot));
        timerStage.setTitle("Focus Timer");
        timerStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

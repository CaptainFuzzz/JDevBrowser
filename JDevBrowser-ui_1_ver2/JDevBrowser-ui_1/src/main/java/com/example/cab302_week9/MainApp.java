package com.example.cab302_week9;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import javafx.scene.text.Font;
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFonts(getClass().getResourceAsStream("/Jersey20Charted-Regular.ttf"), 20);

        Parent root = FXMLLoader.load(getClass().getResource("BrowserUI.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        primaryStage.setTitle("Mindful Browser");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

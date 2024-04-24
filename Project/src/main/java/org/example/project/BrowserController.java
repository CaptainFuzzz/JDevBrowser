package org.example.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BrowserController {

    public Button themeToggleButton;
    public Button parentalModeButton;
//    public Button minimalistModeButton;
    @FXML private VBox root;
    @FXML
    private Label clockLabel; // Make sure the FXML Label has this fx:id

    public void initialize() {
        startClockAndReminder();
    }

    private void startClockAndReminder() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            clockLabel.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        Timeline reminder = new Timeline(new KeyFrame(Duration.minutes(40), e -> showReminder()));
        reminder.setCycleCount(Timeline.INDEFINITE);
        reminder.play();
    }

    private void showReminder() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exercise Reminder");
        alert.setHeaderText("Time to stand up and stretch!");
        alert.showAndWait();
    }

    public void toggleTheme() {
        if (root.getStylesheets().contains("dark-theme.css")) {
            root.getStylesheets().remove("dark-theme.css");
            root.getStylesheets().add("light-theme.css");
        } else {
            root.getStylesheets().remove("light-theme.css");
            root.getStylesheets().add("dark-theme.css");
        }
    }

    private boolean parentalModeEnabled = false;
    @FXML
    private void toggleParentalMode() {
        parentalModeEnabled = !parentalModeEnabled;
    }


//    @FXML
//    public void toggleMinimalistMode(ActionEvent actionEvent) {
//            otherUIElement.setVisible(!otherUIElement.isVisible());
//        }


}

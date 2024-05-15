package com.example.cab302_week9;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class TimerController {

    @FXML
    private VBox root;
    @FXML
    private ComboBox<String> themeSelector;
    @FXML
    private TextField timeInput;
    @FXML
    private Text countdownText;
    @FXML
    private Button startPauseButton;
    @FXML
    private Button settingsButton;
    private boolean isDarkTheme = true;

    private Timeline timeline;
    private int timeInSeconds;
    private SettingsManager settingsManager;

    public void initialize() {
        settingsManager = new SettingsManager();
        // Load settings
        String lastTime = settingsManager.getProperty("lastTime");
        String theme = settingsManager.getProperty("theme");

        if (lastTime != null) {
            timeInput.setText(lastTime);
            setTime();  // Set time based on the last saved time
        }
        resetTimer();
    }

    private void resetTimer() {
        timeInSeconds = (timeInput.getText().isEmpty() ? 0 : Integer.parseInt(timeInput.getText())) * 60;
        setupTimer();
    }


    @FXML
    private void setTime() {
        try {
            int minutes = Integer.parseInt(timeInput.getText());
            timeInSeconds = minutes * 60;
            countdownText.setText(formatTime(timeInSeconds));
            setupTimer();
            settingsManager.setProperty("lastTime", String.valueOf(minutes));  // Save the last used time
        } catch (NumberFormatException e) {
            countdownText.setText("Invalid Input");
        }
    }

    private void setupTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeInSeconds--;
            countdownText.setText(formatTime(timeInSeconds));
            if (timeInSeconds <= 0) {
                timeline.stop();
                countdownText.setText("Time's up!");
                startPauseButton.setText("Start");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    @FXML
    private void toggleTimer() {
        if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
            timeline.pause();
            startPauseButton.setText("Start");
        } else {
            timeline.play();
            startPauseButton.setText("Pause");
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @FXML
    private void openSettings() {
        Scene scene = root.getScene();
        if (isDarkTheme) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
            isDarkTheme = false;
        } else {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
            isDarkTheme = true;
        }
    }
}



package com.example.cab302_week9;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.scene.layout.VBox;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import javafx.util.Duration;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

public class BrowserController {

    @FXML
    public TabPane browserTabPane;
    @FXML
    private TextField urlTextField;
    @FXML
    public VBox expandableContent;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button themeToggleButton;
    @FXML private Text welcomeText;
    @FXML boolean isDarkTheme = true;
    @FXML private CheckBox simpleModeToggle;
    @FXML Timeline reminderTimeline;

    public void updateTheme() {
        Scene scene = urlTextField.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();
            String cssFile = isDarkTheme ? "dark-theme.css" : "light-theme.css";
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssFile)).toExternalForm());
        }
    }
    @FXML
    public void initialize() {
        setupReminder();
        updateTheme();
    }
    @FXML
    private void toggleSimpleMode(ActionEvent event) {
        simpleModeToggle.setSelected(!simpleModeToggle.isSelected());
        setupReminder();
        updateTheme();
    }
    @FXML
    public void toggleExpansion() {
        boolean isVisible = expandableContent.isVisible();
        expandableContent.setVisible(!isVisible);
        expandableContent.setManaged(!isVisible);
    }
    // Method to display a welcome message to the user
    private void showWelcomeMessage(String username) {
        welcomeText.setText("Welcome, " + username + "!");
        welcomeText.setVisible(true); // Make the welcome text visible
    }

    @FXML
    private void setupReminder() {
        if (simpleModeToggle.isSelected()) {
            if (reminderTimeline != null) {
                reminderTimeline.stop();
            }
            return;
        }
        KeyFrame reminderFrame = new KeyFrame(Duration.hours(1), event -> showReminder());
        reminderTimeline = new Timeline(reminderFrame);
        reminderTimeline.setCycleCount(Timeline.INDEFINITE);
        reminderTimeline.play();
    }
    @FXML
    private void showReminder() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Take a break!");
        alert.setHeaderText("Reminder");
        alert.showAndWait();
    }


    @FXML
    private void toggleTheme(ActionEvent event) {
        if (simpleModeToggle.isSelected()) return; // Do nothing if simple mode is enabled
        isDarkTheme = !isDarkTheme;
        updateTheme();
    }
    @FXML
    private void openHistory() {
        ObservableList<String> history = getHistory();
        ListView<String> historyView = new ListView<>(history);
        Label titleLabel = new Label("JDev Browser"); // Title label
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox historyLayout = new VBox(titleLabel, historyView);
        historyLayout.setSpacing(10);
        historyLayout.setPadding(new Insets(10));

        Tab historyTab = new Tab("Browsing History", historyLayout);
        browserTabPane.getTabs().add(historyTab);
        browserTabPane.getSelectionModel().select(historyTab);
    }
    @FXML
    public void openLoginTab() {
        VBox loginContent = new VBox(20);
        loginContent.setAlignment(Pos.CENTER);
        loginContent.setMaxWidth(300);
        loginContent.setStyle("-fx-padding: 20; -fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 5;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #337ab7; -fx-text-fill: white;");

        Label errorMessageLabel = new Label();
        errorMessageLabel.setTextFill(Color.RED);
        errorMessageLabel.setVisible(false);

        loginButton.setOnAction(event -> {
            if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                errorMessageLabel.setText("Username and password are required.");
                errorMessageLabel.setVisible(true);
            } else {
                MongoDBUtil mongoDBUtil = new MongoDBUtil();
                boolean isValidUser = mongoDBUtil.validateUser(usernameField.getText(), passwordField.getText());

                if (isValidUser) {
                    System.out.println("Login successful for: " + usernameField.getText());
                    closeLoginTab("Login"); // Close the login tab
                    updateUIPostLogin(usernameField.getText()); // Update UI to show logged in state
                } else {
                    errorMessageLabel.setText("Invalid username or password.");
                    errorMessageLabel.setVisible(true);
                }
            }
        });

        Label loginLabel = new Label("Login");
        loginContent.getChildren().addAll(loginLabel, usernameField, passwordField, loginButton, errorMessageLabel);

        StackPane centeredContent = new StackPane(loginContent);
        centeredContent.setAlignment(Pos.CENTER);

        Tab loginTab = new Tab("Login", centeredContent);
        browserTabPane.getTabs().add(loginTab);
        browserTabPane.getSelectionModel().select(loginTab);
    }
    private void updateUIPostLogin(String username) {
        welcomeText.setText("Welcome, " + username + "!");
        welcomeText.setVisible(true);
        loginButton.setVisible(false); // Hide the login button
        registerButton.setVisible(false); // Hide the register button
    }
    // Method to close the login tab
    private void closeLoginTab(String tabTitle) {
        browserTabPane.getTabs().stream()
                .filter(tab -> tab.getText().equals(tabTitle))
                .findFirst()
                .ifPresent(tab -> {
                    browserTabPane.getTabs().remove(tab);
                    browserTabPane.getSelectionModel().select(0); // Select the first tab
                });
    }




    @FXML
    private void openRegisterTab() {
        VBox registerContent = new VBox(20);
        registerContent.setAlignment(Pos.CENTER);
        registerContent.setMaxWidth(300);
        registerContent.setStyle("-fx-padding: 20; -fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 5;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");

        registerButton.setOnAction(event -> {
            String errorMessage = validateInput(usernameField, passwordField, emailField);
            if (errorMessage.isEmpty()) {
                // Hash the password here before sending to MongoDBUtil
                String hashedPassword = hashPassword(passwordField.getText());
                MongoDBUtil mongoDBUtil = new MongoDBUtil();
                mongoDBUtil.registerUser(usernameField.getText(), emailField.getText(), hashedPassword);

                System.out.println("User registered successfully");
                errorLabel.setText(""); // Clear error message on successful registration
                // Optional: Clear fields or redirect user
                usernameField.clear();
                passwordField.clear();
                emailField.clear();
            } else {
                errorLabel.setText(errorMessage);
                System.out.println("Invalid input. Please check your details.");
            }
        });

        registerContent.getChildren().addAll(new Label("Register"), usernameField, passwordField, emailField, registerButton, errorLabel);

        StackPane centeredContent = new StackPane(registerContent);
        centeredContent.setAlignment(Pos.CENTER);

        Tab registerTab = new Tab("Register", centeredContent);
        browserTabPane.getTabs().add(registerTab);
        browserTabPane.getSelectionModel().select(registerTab);
    }

    private String hashPassword(String plainTextPassword) {
        // Use a secure password hashing method, such as BCrypt
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public String validateInput(TextField username, PasswordField password, TextField email) {
        String errorMessage = "";
        if (username.getText().trim().isEmpty()) {
            errorMessage += "Username is required.\n";
        }
        if (password.getText().trim().isEmpty()) {
            errorMessage += "Password is required.\n";
        } else if (password.getText().length() < 8) {
            errorMessage += "Password must be at least 8 characters long.\n";
        }
        if (email.getText().trim().isEmpty()) {
            errorMessage += "Email is required.\n";
        } else if (!email.getText().matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            errorMessage += "Invalid email format.\n";
        }
        return errorMessage;
    }

    private ObservableList<String> getHistory() {
        // mock data first
        return FXCollections.observableArrayList(
                "http://example.com",
                "http://google.com",
                "http://openai.com"
        );
    }
    @FXML
    private void loadPage() {
        String url = urlTextField.getText().trim();
        if (!url.isEmpty()) { // Check if the URL is not empty
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url; // Append "http://" if not present
            }
            WebView webView = new WebView();
            webView.getEngine().load(url);

            Tab newTab = new Tab("New Tab"); // Create a new tab
            newTab.setContent(webView);
            browserTabPane.getTabs().add(newTab);
            browserTabPane.getSelectionModel().select(newTab); // Select the newly added tab
        } else {
            showAlert("Error", "Please enter a URL to load.");
        }
    }

    // Helper method to show alerts
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAddTab() {
        // Only add a new tab if the "+" tab is selected
        if (browserTabPane.getSelectionModel().getSelectedItem().getText().equals("+")) {
            addNewTab("New Tab", "http://example.com");
            browserTabPane.getSelectionModel().select(browserTabPane.getTabs().size() - 2);
        }
    }

    private void addNewTab(String title, String url) {
        WebView webView = new WebView();
        webView.getEngine().load(url);

        Tab newTab = new Tab(title);
        newTab.setContent(webView);
        browserTabPane.getTabs().add(browserTabPane.getTabs().size() - 1, newTab);
    }
}

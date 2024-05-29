package com.example.cab302_week9;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import webmaster.Searchengine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BrowserController {

    @FXML
    TabPane browserTabPane;
    @FXML
    TextField urlTextField;
    @FXML
    private ListView<String> historyListView; // ListView for displaying history
    @FXML
    private TextField usernameField; // TextField for username input
    @FXML
    private PasswordField passwordField; // PasswordField for password input
    @FXML
    private Label clockLabel;
    @FXML
    private StackPane rootPane;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML
    private ToggleButton toggleFilterButton;
    @FXML
    public Text welcomeText;
    @FXML
    private ToggleButton themeToggle;
    @FXML
    private CheckBox simpleModeCheck;

    boolean userLoggedIn = false;
    private String currentUsername;
    private final String mongoConnectionString = "mongodb+srv://alexludford3:RunydXriJx97r0fj@jdevbrowser.zxlsuec.mongodb.net";

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>();

    static {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                BrowserController.class.getResourceAsStream("/sensitiveword.txt")))) {
            SENSITIVE_WORDS.addAll(reader.lines().map(String::trim).collect(Collectors.toSet()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openHistory() {
        ObservableList<String> history = FXCollections.observableArrayList(getHistory());
        ListView<String> historyView = new ListView<>(history);
        Label titleLabel = new Label("Browsing History");
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
        TextField passwordField = new TextField();
        TextField usernameField = new TextField();
        usernameField.setId("usernameField");
        usernameField.setPromptText("Username");
        passwordField.setId("passwordField");
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.setId("login");
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
                    userLoggedIn = true;
                    currentUsername = usernameField.getText();
                    System.out.println("Login successful for: " + currentUsername);
                    closeLoginTab("Login"); // Close the login tab
                    updateUIPostLogin(currentUsername); // Update UI to show logged in state
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
    void openRegisterTab() {
        VBox registerContent = new VBox(20);
        registerContent.setAlignment(Pos.CENTER);
        registerContent.setMaxWidth(300);
        registerContent.setStyle("-fx-padding: 20; -fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 5;");

        TextField usernameField = new TextField();
        usernameField.setId("usernameField");
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setId("passwordField");
        passwordField.setPromptText("Password");
        TextField emailField = new TextField();
        emailField.setId("emailField");
        emailField.setPromptText("Email");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        Button registerButton = new Button("Register");
        registerButton.setId("registerButton");
        registerButton.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");

        registerButton.setOnAction(event -> {
            String errorMessage = validateInput(usernameField, passwordField, emailField);
            if (errorMessage.isEmpty()) {
                // Hash the password here before sending to MongoDBUtil
                String hashedPassword = hashPassword(passwordField.getText());
                MongoDBUtil mongoDBUtil = new MongoDBUtil();
                mongoDBUtil.registerUser(usernameField.getText(), emailField.getText(), hashedPassword);

                System.out.println("User registered successfully");
                errorLabel.setText("");
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

    String hashPassword(String plainTextPassword) {
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
        if (userLoggedIn) {
            return FXCollections.observableArrayList(MongoDBUtil.fetchHistory(currentUsername));
        } else {
            return FXCollections.observableArrayList("No history available or not logged in.");
        }
    }

    @FXML
    private void initialize() {
        startClock();
        startReminder();
    }
    @FXML
    private ToggleButton parentalModeToggle; // Ensure this is declared at the top with other FXML elements
    public class Settings {
        public static boolean parentalModeEnabled = false;  // Default to off
    }
    @FXML
    public void onParentalModeToggle() {
        Settings.parentalModeEnabled = parentalModeToggle.isSelected();
        showAlert("Parental Mode", "Parental Mode is now " + (Settings.parentalModeEnabled ? "enabled" : "disabled") + ".");
    }

    private boolean containsSensitiveWord(String input) {
        String[] words = input.toLowerCase().split("\\W+");
        for (String word : words) {
            if (SENSITIVE_WORDS.contains(word)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    void loadPage() {
        String url = urlTextField.getText().trim();
        if (Settings.parentalModeEnabled && containsSensitiveWord(url)) {
            showAlert("Mindful Alert", "The URL contains sensitive words and cannot be loaded in parental mode.");
            return;
        }

        Searchengine searchengine = new Searchengine(mongoConnectionString);

        if (!url.isEmpty()) {

            WebView webView = new WebView();

            if (searchengine.urltest(url)){
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }

                webView.getEngine().load(url);

                Tab newTab = createTabWithCloseButton("New Tab", webView);
                browserTabPane.getTabs().add(newTab);
                browserTabPane.getSelectionModel().select(newTab);
            }
            else{
                ObservableList<String> SearchResults = FXCollections.observableArrayList(searchengine.Search(url));
                if (SearchResults.isEmpty()) {
                    showAlert("Error", "No search results found.");
                    return;
                }
                ListView<String> SearchView = new ListView<>(SearchResults);
                SearchView.setOnMouseClicked(event -> {
                    String selectedItem = SearchView.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        urlTextField.setText(selectedItem);
                        loadPage();
                    }
                });
                Label titleLabel = new Label("Search Results");
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                VBox searchLayout = new VBox(titleLabel, SearchView);
                searchLayout.setSpacing(10);
                searchLayout.setPadding(new Insets(10));

                Tab SearchResultTab = createTabWithCloseButton("Search Results", searchLayout);
                browserTabPane.getTabs().add(SearchResultTab);
                browserTabPane.getSelectionModel().select(SearchResultTab);
            }

            if (userLoggedIn) {
                MongoDBUtil.storeHistory(currentUsername, url);
            }
        } else {
            showAlert("Error", "Please enter a URL to load.");
        }
    }


    private Tab createTabWithCloseButton(String title, Object content) {
        Tab tab = new Tab(title);
        if (content instanceof WebView) {
            tab.setContent((WebView) content);
        } else if (content instanceof VBox) {
            tab.setContent((VBox) content);
        }

        // Create a close button
        Button closeButton = new Button("x");
        closeButton.setOnAction(event -> browserTabPane.getTabs().remove(tab));


        HBox tabHeader = new HBox(new Label(title), closeButton);
        tabHeader.setSpacing(5);
        tabHeader.setAlignment(Pos.CENTER_LEFT);


        tab.setGraphic(tabHeader);
        tab.setText(null);

        // Add hover effect for close button
        closeButton.setStyle("-fx-background-color: transparent;");
        closeButton.setOnMouseEntered(event -> closeButton.setStyle("-fx-background-color: #ff0000;"));
        closeButton.setOnMouseExited(event -> closeButton.setStyle("-fx-background-color: transparent;"));

        return tab;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleAddTab() {
        // Only add a new tab if the "+" tab is selected
        if (browserTabPane.getSelectionModel().getSelectedItem().getText().equals("+")) {
            addNewTab("New Tab", "http://example.com");
            browserTabPane.getSelectionModel().select(browserTabPane.getTabs().size() - 2);
        }
    }

    private void addNewTab(String title, String url) {
        WebView webView = new WebView();
        webView.getEngine().load(url);


        VBox container = new VBox(webView);
        VBox.setVgrow(webView, Priority.ALWAYS);

        container.setFillWidth(true);

        Tab newTab = createTabWithCloseButton(title, container);
        browserTabPane.getTabs().add(browserTabPane.getTabs().size() - 1, newTab);
        browserTabPane.getSelectionModel().select(newTab);
    }


    private void updateHistory(String url) {
        historyListView.getItems().add(url);
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            clockLabel.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void startReminder() {
        Timeline reminder = new Timeline(new KeyFrame(Duration.hours(1), e -> showReminder()));
        reminder.setCycleCount(Timeline.INDEFINITE);
        reminder.play();
    }

    private void showReminder() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exercise Reminder");
        alert.setHeaderText("Time to exercise!");
        alert.setContentText("Take a moment to stretch and move around.");
        alert.showAndWait();
    }

    @FXML
    private void handleThemeToggle() {
        Scene scene = themeToggle.getScene();
        if (themeToggle.isSelected()) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
            themeToggle.setText("Switch to Light Theme");
        } else {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
            themeToggle.setText("Switch to Dark Theme");
        }
    }

    @FXML
    private void handleSimpleModeToggle() {
        boolean isSimpleMode = simpleModeCheck.isSelected();
        clockLabel.setVisible(!isSimpleMode);
        themeToggle.setVisible(!isSimpleMode);
        parentalModeToggle.setVisible((!isSimpleMode));
    }
}

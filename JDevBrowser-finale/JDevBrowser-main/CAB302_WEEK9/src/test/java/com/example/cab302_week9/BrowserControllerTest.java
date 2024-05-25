package com.example.cab302_week9;
import static org.mockito.Mockito.*;
import com.example.cab302_week9.BrowserController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static org.testfx.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.Callable;

public class BrowserControllerTest extends ApplicationTest {

    private BrowserController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302_week9/BrowserUI.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
        stage.show();
        stage.toFront();
    }

    @Test
    public void testHashPassword() {
        String plainTextPassword = "password";
        String hashedPassword = controller.hashPassword(plainTextPassword);
        assertNotNull(hashedPassword);
        assertNotEquals(plainTextPassword, hashedPassword);
    }

    @Test
    public void testAddNewTab() {
        interact(() -> {

            controller.handleAddTab();
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, controller.browserTabPane.getTabs().size(), "New tab should be added");
    }
    @Test
    public void testOpenHistory() {
        interact(() -> controller.openHistory());
        WaitForAsyncUtils.waitForFxEvents();
        TabPane tabPane = lookup("#browserTabPane").query();
        assertThat(tabPane.getTabs()).anyMatch(tab -> "Browsing History".equals(tab.getText()));
        // Check if the tab is selected
        Tab historyTab = tabPane.getSelectionModel().getSelectedItem();
        assertThat(historyTab.getText()).isEqualTo("Browsing History");
        // Verify the contents of the tab
        VBox historyLayout = (VBox) historyTab.getContent();
        assertThat(historyLayout.getChildren()).hasAtLeastOneElementOfType(ListView.class);
        ListView<String> historyView = (ListView<String>) historyLayout.getChildren().get(1);
        // Check for correct label styling and text
        Label titleLabel = (Label) historyLayout.getChildren().get(0);
        assertThat(titleLabel.getText()).isEqualTo("Browsing History");
        assertThat(titleLabel.getStyle()).contains("-fx-font-size: 16px");

    }

    @Test
    public void testSuccessfulLogin() {
        interact(() -> {
            controller.openLoginTab();
        });
        clickOn("#usernameField").write("test3");
        clickOn("#passwordField").write("12345678");
        clickOn("#login");
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(controller.userLoggedIn, "User should be logged in after valid credentials");
    }
    @Test
    public void testSuccessfulRegistration() {
        // Open the registration tab
        interact(() -> controller.openRegisterTab());

        // Input valid registration data
        clickOn("#usernameField").write("newUser");
        clickOn("#passwordField").write("newPassword123");
        clickOn("#emailField").write("newUser@example.com");

        // Click the register button
        clickOn("#registerButton");

        // Wait for any asynchronous operations or UI updates to complete
        WaitForAsyncUtils.waitForFxEvents();

        // Optionally, check if the fields are cleared after registration
        TextField usernameField = lookup("#usernameField").query();
        TextField passwordField = lookup("#passwordField").query();
        TextField emailField = lookup("#emailField").query();
        assertThat(usernameField.getText()).isEmpty();
        assertThat(passwordField.getText()).isEmpty();
        assertThat(emailField.getText()).isEmpty();
    }
    @Test
    public void testLoadingGoogle() {
        interact(() -> {
            controller.urlTextField.setText("http://google.com/");
            controller.loadPage();  // This will trigger the page loading mechanism
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Assertions to check if a new Tab with WebView is added
        assertThat(controller.browserTabPane.getTabs()).isNotEmpty();
        Tab loadedTab = controller.browserTabPane.getTabs().stream()
                .filter(tab -> tab.getContent() instanceof WebView)
                .findFirst()
                .orElse(null);

        assertNotNull(loadedTab, "A new tab with a WebView should be opened.");
        WebView webView = (WebView) loadedTab.getContent();
        // Check that the WebView's location is correct
        String location = webView.getEngine().getLocation();
        assertTrue(location.contains("google.com/"), "The WebView should load Google.");

        if (!controller.userLoggedIn) {
            assertThat(location).isEqualTo("http://google.com/");
        }
    }
    @Test
    public void testHandleAddTab() throws Exception {
        // Setup: Ensure that the TabPane is initialized and the "+" tab is ready
        interact(() -> {
            Tab plusTab = new Tab("+");
            controller.browserTabPane.getTabs().add(plusTab);
            controller.browserTabPane.getSelectionModel().select(plusTab);
        });

        // Act: Trigger the method that handles adding new tabs
        interact(() -> controller.handleAddTab());

        // Assert: Check that a new tab has been added and is not the '+' tab itself
        assertThat(controller.browserTabPane.getTabs()).hasSizeGreaterThan(1);
        Tab newTab = controller.browserTabPane.getTabs().get(controller.browserTabPane.getTabs().size() - 2); // The new tab should be just before the '+' tab
        assertEquals("New Tab", newTab.getText());

        // Assert that the newly added tab is selected
        assertEquals(newTab, controller.browserTabPane.getSelectionModel().getSelectedItem(), "The new tab should be selected.");

    }

}

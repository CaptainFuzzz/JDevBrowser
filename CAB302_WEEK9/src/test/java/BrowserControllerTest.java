import com.example.cab302_week9.BrowserController;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(org.testfx.framework.junit5.ApplicationExtension.class)
class BrowserControllerTest {

    private BrowserController controller;

    @BeforeEach
    void setUp(javafx.stage.Stage stage) throws Exception {
        // Initialize JavaFX stuff necessary for tests
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/path/to/BrowserController.fxml"));
        javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
        controller = loader.getController();
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testToggleExpansion() {
        // Initial state is visible
        assertTrue(controller.expandableContent.isVisible());
        controller.toggleExpansion();
        // Check if the visibility toggles
        assertFalse(controller.expandableContent.isVisible());
    }

    @Test
    void testOpenLoginTab() {
        // Simulate opening the login tab
        controller.openLoginTab();
        // Check if the correct tab is opened and selected
        assertEquals("Login", controller.browserTabPane.getSelectionModel().getSelectedItem().getText());
    }

    @Test
    void validateInputTest() {
        // Perform input validation using correct PasswordField usage
        TextField username = new TextField("");
        PasswordField password = new PasswordField();
        password.setText("12345678");
        TextField email = new TextField("user@example.com");

        String result = controller.validateInput(username, password, email);
        assertTrue(result.contains("Username is required"), "Validation should indicate missing username");
    }
}

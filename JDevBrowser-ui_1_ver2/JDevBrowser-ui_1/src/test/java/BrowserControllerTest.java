import com.example.cab302_week9.BrowserController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.testfx.framework.junit5.ApplicationTest;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BrowserControllerTest extends ApplicationTest {

    private BrowserController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BrowserUI.fxml"));
        Scene scene = new Scene(loader.load());
        controller = loader.getController();
        stage.setScene(scene);
        stage.show();
    }



    @Test
    public void testLoginFunctionality() {
        clickOn("#loginButton");
        clickOn("#usernameField").write("test4");
        clickOn("#passwordField").write("12345678");
        clickOn("#Login");
        assertTrue(controller.welcomeText.isVisible());
    }
}

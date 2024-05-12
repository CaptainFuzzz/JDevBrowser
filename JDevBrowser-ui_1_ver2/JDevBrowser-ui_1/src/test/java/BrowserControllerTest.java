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


}


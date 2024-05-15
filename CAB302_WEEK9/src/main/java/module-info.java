module com.example.cab302_week9 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.kordamp.bootstrapfx.core;
    requires java.logging;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires java.desktop;
    
    requires jbcrypt;
    requires eu.iamgio.animated;


    opens com.example.cab302_week9 to javafx.fxml;
    exports com.example.cab302_week9;
}

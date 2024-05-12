package com.example.cab302_week9;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import org.mindrot.jbcrypt.BCrypt;

public class MongoDBUtil {
    private final String connectionString = "mongodb+srv://JDev:12345678qwe@jdevbrowser.zxlsuec.mongodb.net/";

    public void registerUser(String username, String email, String hashedPassword) {
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("JDevBrowser");
            MongoCollection<Document> usersCollection = database.getCollection("Users");

            Document newUser = new Document("username", username)
                    .append("email", email)
                    .append("password", hashedPassword);

            usersCollection.insertOne(newUser);
            System.out.println("New user registered successfully.");
        } catch (MongoException e) {
            System.err.println("Error registering new user: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public boolean validateUser(String username, String inputPassword) {
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("JDevBrowser");
            MongoCollection<Document> usersCollection = database.getCollection("Users");

            Document userDoc = usersCollection.find(Filters.eq("username", username)).first();
            if (userDoc != null) {
                String storedHashedPassword = userDoc.getString("password");
                // BCrypt to check the hashed password against the stored hashed password
                return BCrypt.checkpw(inputPassword, storedHashedPassword);
            }
            return false;
        } catch (MongoException e) {
            System.err.println("Error accessing the database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

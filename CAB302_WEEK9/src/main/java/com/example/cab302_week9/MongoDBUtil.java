package com.example.cab302_week9;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoDBUtil {
    private static final String connectionString = "mongodb+srv://JDev:12345678qwe@jdevbrowser.zxlsuec.mongodb.net/";

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

    public static void storeHistory(String username, String url) {
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("JDevBrowser");
            MongoCollection<Document> historyCollection = database.getCollection("History");

            Document historyDoc = new Document("username", username)
                    .append("url", url)
                    .append("visitedAt", new Date());
            historyCollection.insertOne(historyDoc);
            System.out.println("History saved for user: " + username);
        } catch (Exception e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }
    public static List<String> fetchHistory(String username) {
        List<String> history = new ArrayList<>();
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("JDevBrowser");
            MongoCollection<Document> historyCollection = database.getCollection("History");

            FindIterable<Document> documents = historyCollection.find(Filters.eq("username", username));
            for (Document document : documents) {
                history.add(document.getString("url") + " - Visited on: " + document.getDate("visitedAt"));
            }
        } catch (Exception e) {
            System.err.println("Error fetching history: " + e.getMessage());
        }
        return history;
    }
    public static boolean validateUser(String username, String inputPassword) {
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

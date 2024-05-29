package webmaster;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.json.JsonObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Pattern;

public class Searchengine {
    private String connectionstring;

    public static Set<String> STOP_WORDS = Set.of("a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it",
            "no", "not", "of", "on", "or", "s", "such",
            "t", "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with");

    public Searchengine(String connectionstring) {
        this.connectionstring = connectionstring;
    }

    public List<String> Search(String SearchInput) {
        String processedInput = processInput(SearchInput);
        if (processedInput == null) {
            return Collections.emptyList();
        }
        return query(processedInput);
    }

    public boolean urltest(String SearchInput) {
        Pattern pattern = Pattern.compile("^(http|https)://");
        if (!pattern.matcher(SearchInput).find()) {
            SearchInput = "https://" + SearchInput;
        }

        try {
            URL url = new URL(SearchInput);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println(getFullResponse(connection));
            connection.disconnect();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String processInput(String SearchInput) {
        String Searchtext = "text=" + SearchInput.replace(" ", "%20");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://twinword-lemmatizer1.p.rapidapi.com/extract/"))
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("X-RapidAPI-Key", "f305bca7b2msh51c590cf1d24024p1bb2abjsn511d89b14154")
                    .header("X-RapidAPI-Host", "twinword-lemmatizer1.p.rapidapi.com")
                    .method("POST", HttpRequest.BodyPublishers.ofString(Searchtext))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jobject = new JsonObject(response.body());
            BsonDocument bsonDocument = jobject.toBsonDocument();

            if (bsonDocument.containsKey("lemma")) {
                return bsonDocument.get("lemma").toString()
                        .replaceAll("[{}]", "").replaceAll(": \\d", "")
                        .replaceAll(",", "").replaceAll("\"", "");
            } else {
                System.out.println("Lemma not found in the response");
                return null;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    private List<String> query(String searchQuery) {
        List<String> SearchResults = new ArrayList<>();
        try (MongoClient mongoClient = MongoClients.create(connectionstring)) {
            MongoDatabase database = mongoClient.getDatabase("JDevBrowser");
            MongoCollection<Document> collection = database.getCollection("JDevSites");

            Document match = new Document("$match", new Document("Token", new Document("$in", Arrays.asList(searchQuery.split(" ")))));

            Document group = new Document("$group", new Document("_id", 0L).append("sets", new Document("$push", "$URLS")).append("initialSet", new Document("$first", "$URLS")));

            Document project = new Document("$project", new Document("commonSets", new Document("$reduce", new Document("input", "$sets")
                    .append("initialValue", "$initialSet").append("in", new Document("$setIntersection", Arrays.asList("$$value", "$$this"))))));

            Iterator<Document> result = collection.aggregate(Arrays.asList(match, group, project)).iterator();

            if (result.hasNext()) {
                SearchResults.addAll(Arrays.asList(result.next().get("commonSets").toString()
                        .replaceAll("[\\[\\]]", "")
                        .replaceAll(",", "").split(" ")));
            }

            return SearchResults;
        } catch (MongoException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean invalidToken(String token) {
        return STOP_WORDS.contains(token) || token.matches("[\\p{Punct}\\d]+");
    }

    private static String getFullResponse(HttpURLConnection connection) throws IOException {
        StringBuilder fullResponseBuilder = new StringBuilder();
        return fullResponseBuilder.append(connection.getResponseCode()).append(" ")
                .append(connection.getResponseMessage()).append("\n").toString();
    }
}

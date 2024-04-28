package webmaster;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.in;

import com.mongodb.client.model.Filters;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.Reader;
import java.lang.String;
import java.util.*;
import java.util.regex.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SearchEngine {
    private final String connectionstring = "mongodb+srv://JDev:zvljXDeneK5D9Rm9@jdevbrowser.zxlsuec.mongodb.net/?retryWrites=true&w=majority&appName=JDevBrowser";
    private final StanfordCoreNLP pipeline;
    static Logger logger = Logger.getLogger(SearchEngine.class.getName());
    SearchEngine(){
        this.pipeline = new StanfordCoreNLP();
        logger.log(Level.INFO, "Search Engine created.");
    }

    public List<String> search(String SearchInput){

        if (SearchInput.isBlank()){
            logger.log(Level.INFO, "Search Input is null.");
            return null;
        }

        if (urltest(SearchInput)){
            return null;
        }

        return query(tokenizeSearch(SearchInput));
    }

    private boolean urltest(String SearchInput){

        Pattern pattern = Pattern.compile("^(http|https)://");
        if (!pattern.matcher(SearchInput).find()){
            SearchInput = "https://" + SearchInput;
        }

        try {
            logger.log(Level.INFO, "Testing input string as url");
            URL url = new URL(SearchInput);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            logger.log(Level.INFO, getFullResponse(connection));

            connection.disconnect();

            return true;
        }
        catch (IOException e){
            logger.log(Level.INFO, e.toString());
            return false;
        }
    }

    public static String getFullResponse(HttpURLConnection connection) throws IOException {
        StringBuilder fullResponseBuilder = new StringBuilder();

        return fullResponseBuilder.append(connection.getResponseCode()).append(" ")
                .append(connection.getResponseMessage()).append("\n").toString();
    }

    private String tokenizeSearch(String SearchInput){
        HashSet<String> tokenSet = new HashSet<String>(); // keeps strings unique.
        List<String> tokenized = new ArrayList<>();

        Annotation annotation = new Annotation(SearchInput.toLowerCase());
        pipeline.annotate(annotation);

        List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);

        // Adding lemmatized tokens to list, while keeping every element unique.
        for (CoreLabel token : tokens){
            String tk = token.get(CoreAnnotations.LemmaAnnotation.class);

            if (!tokenSet.contains(tk)){
                tokenized.add(tk);
                tokenSet.add(tk);
            }
        }

        // Remove stopwords and numbers from corpus
        tokenized = tokenized.stream().filter(token -> !invalidToken(token)).collect(Collectors.toList());

        String tokenizedString = "";
        for (int i = 0; i < tokenized.size(); i++){
            if (i == tokenized.size() - 1){
                tokenizedString = tokenizedString + tokenized.get(i);
            }
            else {
                tokenizedString = tokenizedString + tokenized.get(i) + " ";
            }
        }

        return tokenizedString;
    }

    private boolean invalidToken(String token){
        return Indexer.STOP_WORDS.contains(token) || token.matches("[\\p{Punct}\\d]+");
    }

    private List<String> query(String querystring){
        try (MongoClient mongoClient  = MongoClients.create(connectionstring)){


            MongoDatabase database = mongoClient.getDatabase("JDevBrowser");
            MongoCollection<Document> collection = database.getCollection("JDevSites");


            Iterator<Document> result = collection.aggregate(Arrays.asList(new Document("$match",
                            new Document("Token",
                                    new Document("$in", Arrays.asList(querystring.split(" "))))),
                    new Document("$group",
                            new Document("_id", 0L)
                                    .append("sets",
                                            new Document("$push", "$URLS"))
                                    .append("initialSet",
                                            new Document("$first", "$URLS"))),
                    new Document("$project",
                            new Document("commonSets",
                                    new Document("$reduce",
                                            new Document("input", "$sets")
                                                    .append("initialValue", "$initialSet")
                                                    .append("in",
                                                            new Document("$setIntersection", Arrays.asList("$$value", "$$this")))))))).iterator();

            return Arrays.asList(result.next().get("commonSets").toString()
                    .replaceAll("[\\[\\]]", "").replaceAll(",", "")
                    .split(" "));

        }
        catch (MongoException e){
            e.printStackTrace();

            return null;
        }


    }
}

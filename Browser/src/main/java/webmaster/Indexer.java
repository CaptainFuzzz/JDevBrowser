package webmaster;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.lang.*;
import java.util.*;


import java.util.logging.Logger;

import java.util.stream.Collectors;

import com.mongodb.*;

import com.mongodb.MongoException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.model.Filters.*;
import org.bson.conversions.Bson;
import lombok.extern.slf4j.Slf4j;

import static com.mongodb.client.model.Filters.*;


public class Indexer{
    public static Set<String> STOP_WORDS = Set.of("a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it",
            "no", "not", "of", "on", "or", "s", "such",
            "t", "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with");

    private final String connectionString = "mongodb+srv://alexludford3:m30zNpl8vvsC1Biw@jdevbrowser.zxlsuec.mongodb.net/?retryWrites=true&w=majority&appName=JDevBrowser";
    private final JDevDocument Doc;
    private final StanfordCoreNLP pipeline;

    static Logger logger = Logger.getLogger(Indexer.class.getName());

    public Indexer(JDevDocument Doc){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        this.Doc = Doc;
        this.pipeline = new StanfordCoreNLP(props);
        index();
    }

    // Doc structer:
    // Token: "someword"
    // Urls: ["https://url1.com, ..., https://urln.com]
    public void index(){
        List<String> tokens = tokenize(Doc.getBody());

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            try {

                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("JDevBrowser");
                MongoCollection<Document> mongocollection = database.getCollection("JDevSites");

                for (String token : tokens){
                    Document document = mongocollection.find(eq("Token", token)).first();

                    if(document != null){
                        Bson filter = Filters.eq("Token", token);
                        Bson update = Updates.push("URLS", Doc.getUrl());

                        mongocollection.findOneAndUpdate(filter, update);
                    }
                    else {
                        document = new Document("Token", token);
                        BasicDBList urls = new BasicDBList();
                        urls.add(Doc.getUrl());
                        document.append("URLS", urls);

                        mongocollection.insertOne(document);
                    }
                }
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> tokenize(String corpus){
        HashSet<String> tokenSet = new HashSet<String>(); // keeps strings unique.
        List<String> tokenized = new ArrayList<>();

        Annotation annotation = new Annotation(corpus.toLowerCase());
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
        return tokenized.stream().filter(token -> !invalidToken(token)).collect(Collectors.toList());
    }

    private boolean invalidToken(String token){
        return STOP_WORDS.contains(token) || token.matches("[\\p{Punct}\\d]+");
    }

}

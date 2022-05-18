package database;
// MongoDb imports
import com.mongodb.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Field;
import org.bson.Document;
import com.mongodb.DBObject;
import com.mongodb.MongoClientSettings;

// java imports
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
public class MongoDB {
    MongoClient client;
    MongoDatabase database;

    MongoCollection<Document> URLTitles;
    MongoCollection<Document> IndexedCollection;

    public MongoDB(String databaseName){
        try{
            ConnectionString connectS = new ConnectionString("mongodb://localhost:27017");
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectS).retryWrites(true).build();
            client = MongoClients.create(settings);
            database = client.getDatabase(databaseName);
        }catch(Exception e){
            URLTitles = database.getCollection("URLTitle");
            IndexedCollection = database.getCollection("Indexer");
        }
    }

    // adding to the data base function
    // please put the purpose of the addition and your name
    // over each function


    // MARK
    // what are the urls that are connected to this word
    // just URLS

    public void WordUrls(String word , Set<String> URLS){
        org.bson.Document URLWORDINDEX = new Document("Word" , word).append("URLS",URLS);
        IndexedCollection.insertOne(URLWORDINDEX);
    }

    // MARK ADD URL Sentence
    // for every word in a URL add a sentence that contain this word.
    // so I will store the indexes of the word in every url.
    // Mark add the positions of the word in the URl.
    public void WordPlaces(String word , String currentURL , ArrayList<Integer> indexes){
        org.bson.Document WORDINDEXES = new Document("Word" , word);
        BasicDBObject whereINURL = new BasicDBObject();
        whereINURL.put("URL" , currentURL);
        whereINURL.put("Indexes" , indexes);
        WORDINDEXES.append("URL_indexes" , whereINURL);
        IndexedCollection.insertOne(WORDINDEXES);
    }

    //MARK
    // ADD a title for every URL so that in interface what to call this link
    // aka the blue/purple text in google

    public void URLtitle( String currentURL , String linkTitle){
        org.bson.Document LINKTITLE = new Document("URL", currentURL).append("Title" , linkTitle);
        URLTitles.insertOne(LINKTITLE);
    }



}

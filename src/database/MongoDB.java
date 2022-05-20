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
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.*;

public class MongoDB {
    MongoClient client;
    MongoDatabase database;

    MongoCollection<Document> URLTitles ;
    MongoCollection<Document> IndexedCollection;

    public MongoDB(String databaseName){
        try{
            //mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&directConnection=true&ssl=false
            ConnectionString connectS = new ConnectionString("mongodb+srv://mohammedzaki:zaki@cluster0.qjawu.mongodb.net/?retryWrites=true&w=majority");
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectS).retryWrites(true).build();
            client = MongoClients.create(settings);
            database = client.getDatabase(databaseName);
            URLTitles = database.getCollection("URLTitle");
            IndexedCollection = database.getCollection("indexer");
        }catch(Exception e){
            URLTitles = database.getCollection("URLTitle");
            IndexedCollection = database.getCollection("indexer");
        }
    }

    // adding to the data base function
    // please put the purpose of the addition and your name
    // over each function


    // MARK
    // what are the urls that are connected to this word
    // just URLS

    public void WordUrls(String word , Set<String> URLS , HashMap<String , ArrayList<Integer>> stringMap , HashMap<String , Double> DOCSIZE){
        HashMap<String , Double> NTF = new HashMap<String , Double>();
        URLS.forEach((i) -> {
            NTF.put(i , Double.valueOf(stringMap.get(i).size()/DOCSIZE.get(i)) );
        });
        Set<String> keySet = NTF.keySet();
        ArrayList<String> listOfKeys = new ArrayList<String>(keySet);
        Collection<Double> values = NTF.values();
        ArrayList<Double> listOfValues = new ArrayList<Double>(values);
        org.bson.Document URLWORDINDEX = new Document("Word" , word).append("URLS", listOfKeys);
        URLWORDINDEX.put("NTF" , listOfValues);
        URLWORDINDEX.append("DF", Double.valueOf(URLS.size()));
        Double URLsize = Double.valueOf(URLS.size());
        Double max = Double.valueOf(5000);
        URLWORDINDEX.append("IDF", Double.valueOf(URLsize/max));
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
        System.out.println(currentURL);
        URLTitles.insertOne(LINKTITLE);
    }



}

package indexer;
//jsoup
import com.mongodb.client.MongoClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
// port stemmer
import ca.rmen.porterstemmer.PorterStemmer;
// mongoDB
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import database.MongoDB;
import org.jsoup.select.Evaluator;
// java utils
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream;
import java.util.*;
public class Indexer {
    //max limit of files
    public static final int Number = 5000;
    // database entries
    public static final String DBName = "sekoseko";
    public  static final String DBHost = "localhost";
    public static final int DBPort = 27017;
    MongoDB ourDataBase = new MongoDB(DBName);
    MongoClient mngoClient;
    MongoDatabase database;
    // URL links array to store them
    ArrayList<String> URLS;

    public HashMap<String , HashMap<String , ArrayList<Integer>>> WordMap;
    public HashMap<String , Double> DOCSIZE;
    public void DBWrite(){

        DBObject WordIndex = new BasicDBObject("word", "mytenomak");
        for(Map.Entry mapElement : WordMap.entrySet()){
            String word = (String)mapElement.getKey();
            HashMap<String, ArrayList<Integer>> urls = (HashMap<String , ArrayList<Integer>>)(mapElement.getValue());

            Set<String> wordUrls = urls.keySet();
            HashMap<String , ArrayList<Integer>> stringMap = WordMap.get(word);

            ourDataBase.WordUrls(word,wordUrls ,stringMap , DOCSIZE);
        }
    }

    public Indexer(){
        WordMap = new HashMap<String , HashMap<String,ArrayList<Integer>>>();
        URLS = new ArrayList<String>(Number);
        DOCSIZE = new HashMap<String , Double>();
        Scanner fileReader = null;
        File linksVisited = new File("visited.txt");
        try{
            fileReader = new Scanner(linksVisited);
            while(fileReader.hasNextLine()){
                String link = fileReader.nextLine();
                URLS.add(link);
            }
        }
        catch (FileNotFoundException e){ System.out.println("File visitedd not found");}
    }

    public String[] indexHTMLdoc(String file , int ID) throws IOException{

        String HTMLSTRING = Files.readString(Paths.get(file));
        Document HTML = Jsoup.parse(HTMLSTRING);
         System.out.println(ID);
         System.out.println(file);

        // Start of Preprocceing
        // STEP-1 Lower case the text
        String HTMLCONTENT = (HTML.title()+" "+HTML.body().text()).toLowerCase(Locale.ROOT);
        //System.out.println(URLS.get(5));

        ourDataBase.URLtitle(URLS.get(ID-1) , HTML.title());
        //Step-2
        // remove any numbers
        // used regex [^a-zA-Z]
        HTMLCONTENT = HTMLCONTENT.replaceAll("[^a-zA-Z]", " ");
        String[]  NonCleanedWords = HTMLCONTENT.split(" ");

        // then remove all the stop words
        //create aa list of words

        ArrayList<String> CleanedWordList = new ArrayList<String>();

        HashSet<String> stopWords = new HashSet<String>();
        File file2= new File("stopping.txt");
        Scanner fileReader2 = new Scanner(file2);

        while(fileReader2.hasNextLine()){
            String sWord = fileReader2.nextLine();
            stopWords.add(sWord);
        }
        fileReader2.close();

        for(String dirtyWord : NonCleanedWords){
            if(!stopWords.contains(dirtyWord)){
                CleanedWordList.add(dirtyWord);
            }
        }

        // then stemm all the no stop words
        ArrayList<String> FinalWordList = new ArrayList<String>();
        PorterStemmer stemmer = new PorterStemmer();
        for( String cleanWord : CleanedWordList){
            if(!cleanWord.isBlank())
                FinalWordList.add(stemmer.stemWord(cleanWord));
        }
        // need to be atring array to be able to insert in mongo db
        String [] output = new String[FinalWordList.size()];

        for( int s = 0 ; s<FinalWordList.size(); s++)
            output[s] = FinalWordList.get(s);

        return output;

    }

    public void retAllHtml(){
        Path file = Paths.get("HTMLDOCS/");
        File[] files = new File("HTMLDOCS/").listFiles();
        // read directory stream from stackoverflow
        try(DirectoryStream<Path> allfiles = Files.newDirectoryStream(file)){
            int fileID;
            for(File singleFile : files){
                    String name = singleFile.getName();
                    System.out.println(name);
                    // html and dot are 5 chars so sunstring from 0 till lengh -5
                    // get the id of file
                    fileID = Integer.parseInt(name.substring(0,name.length()-5));
                    System.out.println("HTMLDOCS/"+singleFile.getName().toString());
                    String[] htmlwords = indexHTMLdoc("HTMLDOCS/"+singleFile.getName(), fileID);
                    Double docSize = Double.valueOf(htmlwords.length);
                    DOCSIZE.putIfAbsent(URLS.get(fileID-1) , docSize);
                    ArrayIndexed(htmlwords,URLS.get(fileID-1), docSize);
            }
        }catch (IOException e){
            System.out.println("Error retriving html DOCSSSSSSSS");
        }
    }

    void ArrayIndexed(String[] words , String URL, Double docsize){
        int start = 1;
        for(String word : words){
            WordIndexed(word , URL , start++ , docsize);
        }
    }

    void WordIndexed( String word , String URL , int place  , Double docsize){
        WordMap.putIfAbsent(word,new HashMap<String , ArrayList<Integer>>());
        HashMap<String , ArrayList<Integer>> stringMap = WordMap.get(word);
        stringMap.putIfAbsent(URL , new ArrayList<Integer>());
        ArrayList<Integer> where = stringMap.get(URL);
        where.add(place);
    }

}

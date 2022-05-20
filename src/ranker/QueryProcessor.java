package ranker;

import java.io.IOException;

import ranker.Ranker;
import ranker.Stemmer;
import ranker.Page;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.PriorityQueue;


import java.util.Comparator;
import java.util.LinkedHashMap;
//import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import org.json.JSONException;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.*;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;



import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

import com.mongodb.DBCollection;



public class QueryProcessor {

	public static void main(String[] args)throws IOException
	{
		/////Receive search query
		String searchQuery = "The Content is signing the tools ";

		/////Apply stemming and remove the stop words
		Stemmer stemmer=new Stemmer();
		String processedSearchQuery=stemmer.StemAndRemove(searchQuery);
		System.out.println("Processed Search Query: "+processedSearchQuery);

		/////Split string to words(String[])
		String[] queryWords = processedSearchQuery.split(" ");

		System.out.println("Processed Search Query: ");

		for(int i=1;i<queryWords.length;i++)
		{
			System.out.print(queryWords[i]+" ");
		}
		System.out.println();






		/////Establish Connection
		MongoClient client = MongoClients.create("mongodb+srv://mohammedzaki:zaki@cluster0.qjawu.mongodb.net/?retryWrites=true&w=majority");

		MongoDatabase searchIndexDb = client.getDatabase("SearchDB");

		MongoCollection collec = searchIndexDb.getCollection("searchIndex");


		//////Receive relevant documents from the database
		//Initialize an array of mongoDB documents, to receive each entry in the inverted index
		Document[] docs=new Document[queryWords.length-1];
		int j=0;//Iterator for the documents array

		/////Loop on the processed query to retrieve the documents for ranking
		//i starts from 1 because the first element is " " 
		for(int i=1;i<queryWords.length;i++)
		{
			docs[j]=(Document) collec.find(eq("word", queryWords[i])).first();	
			j++;
		}

	
		
		/////Map the received mongoDB documents to the SeDocument array
		SeDocument[] mappedDocuments=new SeDocument[docs.length];


		//Temporary variables to hold the values in each field for each mongo DB ndocument
		int tempId;
		String tempWord;
		Page[] tempPages;
		double tempIdf;
		int tempUrlId;
		double tempNtf;
		ArrayList<Document> pagesRetrieved=new ArrayList<Document>();   //Pages retrieved = pages retrieved from the DB 
		ArrayList<String> tagsRetrieved=new ArrayList<String>();     //Tags retrieved = tags retrieved each retrieved page 


		for(int i=0;i<mappedDocuments.length;i++)
		{	
			//Allocating the temporary variables
			tempId=(int) docs[i].get("_id");
			tempWord=(String) docs[i].get("word");
			tempIdf=(double) docs[i].get("idf");

			//Allocating Pages retrieved and mapping to temp pages
			pagesRetrieved = (ArrayList<Document>) docs[i].get("pages");
			
			//Initialize the temporary pages container with the length of the received pages
			tempPages=new Page[pagesRetrieved.size()];
			
			//Iterate on the pages received to map the pagesReceived to tempPages
			j=0;
			for(Document pageRetrieved: pagesRetrieved)
			{
				//Allocate urlId and Tf of each page to the temporary variables
				tempUrlId= (Integer) pageRetrieved.get("urlID");
				tempNtf = (double) pageRetrieved.get("ntf");
				tagsRetrieved = (ArrayList<String>) pageRetrieved.get("tags");
				
				//mapping to the pages tempor
				tempPages[j]=new Page(tempNtf,tempUrlId,tagsRetrieved);	
				
				j++;
			}
			
			//Initialize the mapped documents
			mappedDocuments[i]=new SeDocument(tempId,tempIdf,tempWord,tempPages);

			
			//Pages retrieved = pages retrieved from the DB 
			//Tags retrieved = tags retrieved each retrieved page 
			//tempPages = pages retrieved mapped to the Page class
			//Empty the 3 containers
			pagesRetrieved.clear();
			tagsRetrieved.clear();
			tempPages=null;

			System.out.println("The received Word is "+	mappedDocuments[i].word );
		}


		
		/////Rank
		Ranker ranker=new Ranker();
		ranker.Relevance(mappedDocuments);


		
		
		
		/////Send sorted documents
		
		
		
		
		
		
		


	}
}

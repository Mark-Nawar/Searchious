package ranker;


import ranker.SeDocument;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import java.util.LinkedHashMap;
import java.util.Set;

/*
 Document doc = (Document) collec.find(eq("word", word)).first();
        				//String pageJS = doc.toJson();

        	            ArrayList<Document> pagesRetrieved = (ArrayList<Document>) doc.get("pages");

        	            for(Document pageRetrieved: pagesRetrieved) 
        	            {
        	            	int urlRetrieved = (Integer) pageRetrieved.get("urlID");
            	            int tfRetrieved = (Integer) pageRetrieved.get("tf");
            	            ArrayList<String> tagsRetrieved = (ArrayList<String>) pageRetrieved.get("tags");

            	            System.out.println("word is: "+ word +" url is: "+ urlRetrieved + " tf is: "+tfRetrieved + " tags: " +tagsRetrieved );
        	            }

 */



public class Ranker {

	public  void Relevance(SeDocument SeDocumentsArray[])throws IOException
	{


		//Initialize array map to store the urlId and it's equivalent TF-IDF value
		Map<Integer, Double> urlPairs = new LinkedHashMap<Integer,Double>();

		//Temporary storage fore each TF value
		double tf;

		//Nested Loop to allocate the hash map
		for(int i=0;i< SeDocumentsArray.length;i++)
		{
			for(int j=0;j<SeDocumentsArray[i].pages.length;j++)
			{
				//Allocate tf
				tf=SeDocumentsArray[i].pages[j].ntf;

				//Set the TF-IDF value for each page(Document)
				SeDocumentsArray[i].pages[j].setTFIDF(SeDocumentsArray[i].idf*tf);
				System.out.println(	SeDocumentsArray[i].pages[j].tfidf);
				//Check if the url was already allocated in the hash map
				Double tfidf = urlPairs.get(SeDocumentsArray[i].pages[j].urlId);
				if (tfidf == null) {
					//If the value wasn't allocated, Allocate it with it's urlId(key) and TF-IDF(value)
					urlPairs.put(SeDocumentsArray[i].pages[j].urlId,SeDocumentsArray[i].pages[j].tfidf);
				} else {
					//If it was allocated, sum the two values
					urlPairs.put(SeDocumentsArray[i].pages[j].urlId,SeDocumentsArray[i].pages[j].tfidf+tfidf);
				}
			}
		}


		//System.out.println(urlPairs);

		//Sort the pairs based on the TF-IDF values
		Map<Integer,Double> urlPairsSorted = new LinkedHashMap<Integer,Double>();
		urlPairs.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		.forEachOrdered(x ->urlPairsSorted.put(x.getKey(), x.getValue()));


		Set<Integer> keys = urlPairsSorted.keySet();
		for (Integer key : keys) {
			System.out.println("Url ID:"+ key+" IF-IDF value: " + urlPairsSorted.get(key));
		}
		//System.out.println(urlPairsSorted);


	}



	public static void main(String[] args)throws IOException
	{
		///////////////////////////

		//Hash map of urlId and urls that point to the url
		Map<String,HashSet<String>> graphMap = new HashMap<String,HashSet<String>>();

		//Hash map of the url and number of outbound links
		Map<String,Integer> outboundMap=new HashMap<String,Integer>();


		//Hash map to store the popularities
		Map<String,Double> prevPopularity=new HashMap<String,Double>();
		Map<String,Double> newPopularity=new HashMap<String,Double>();
		


		double dnum = 0.2;  

		prevPopularity.put("A",dnum);
		prevPopularity.put("B",dnum);
		prevPopularity.put("C",dnum);
		prevPopularity.put("D", dnum);
		prevPopularity.put("E", dnum);

		
		newPopularity.put("A",0.0);
		newPopularity.put("B",0.0);
		newPopularity.put("C",0.0);
		newPopularity.put("D", 0.0);
		newPopularity.put("E", 0.0);
		
		System.out.println(dnum);


		///////
		outboundMap.put("A",1);
		outboundMap.put("B",1);
		outboundMap.put("C",4);
		outboundMap.put("D",2);
		outboundMap.put("E",1);
		//		for (Map.Entry<String,Integer> entry : outboundMap.entrySet()) 
		//			System.out.println("Key = " + entry.getKey() +
		//					", Value = " + entry.getValue());



		////////////////
		//Hard coding
		HashSet<String> hashSetA=new HashSet<String>();
		hashSetA.add("C");
		HashSet<String> hashSetB=new HashSet<String>();
		hashSetB.add("A");
		hashSetB.add("C");

		HashSet<String> hashSetC=new HashSet<String>();
		hashSetC.add("D");
		HashSet<String> hashSetD=new HashSet<String>();
		hashSetD.add("C");
		hashSetD.add("E");
		HashSet<String> hashSetE=new HashSet<String>();
		hashSetE.add("B");
		hashSetE.add("C");
		hashSetE.add("D");
		
		graphMap.put("A",hashSetA);
		graphMap.put("B", hashSetB);
		graphMap.put("C", hashSetC);
		graphMap.put("D", hashSetD);
		graphMap.put("E", hashSetE);
		///////////////////////


		//Main loop

		// Iterating HashMap through for loop
		//		for (Map.Entry<String,HashSet<String>> entry : graphMap.entrySet()) 
		//			System.out.println("Key = " + entry.getKey() +
		//					", Value = " + entry.getValue());




		//Alogrithm's main loop

		//Initialize hash map iterator

		HashSet<String> tempHashSet;

		Iterator hmIterator=graphMap.entrySet().iterator();

		double tempPopularity=0;
	
		
		double dF=0.85;

		int i=0;
		while(i<2)
		{
			while (hmIterator.hasNext()) 
			{
				Map.Entry mapElement= (Map.Entry)hmIterator.next();
				String urlId = (String)mapElement.getKey();
				tempHashSet=( HashSet<String>)mapElement.getValue();
				//System.out.println("Destination"+urlId);
				//Loop over the hash set
				for (String j : tempHashSet) 
				{
					//System.out.println("String"+j);
					tempPopularity=tempPopularity+(prevPopularity.get(j)/outboundMap.get(j));
				} 
				tempPopularity=((1-dF)/graphMap.size())+(dF*(tempPopularity));
				newPopularity.put(urlId, tempPopularity);
				tempPopularity=0;
			}
			
			prevPopularity=newPopularity;
			newPopularity=new HashMap<String,Double>();
			hmIterator =graphMap.entrySet().iterator();
		    
			i++;
			
		}
        
		System.out.println(prevPopularity);	
	}
}



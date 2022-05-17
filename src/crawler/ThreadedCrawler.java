package crawler;
//MARK

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.sql.*;
import java.util.*;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ThreadedCrawler extends Thread{

    public final int MAX = 5000;
    // reserve crawling data structures
    public static HashSet<String> links;
    public static HashSet<String> disallow;
    public static HashSet<String> hosts;
    public static Map<String, List<String>> blocked = new HashMap<String , List<String>>();
    public static LinkedList<String> queue;

    public static FileWriter fWriterA , fWriterB;
    public static BufferedWriter WriterA , WriterB;
    public static File fileA , fileB;

    public static void init() throws IOException {
        // initialize the data structures
        queue = new LinkedList<String>();
        links = new HashSet<String>();
        disallow = new HashSet<String>();
        hosts = new HashSet<String>();

        // create the files needed ( visited )
        // will reload this anyways even after interuption
        // load the visitied from other thrads
        File vis = new File("visited.txt");
        Scanner fileReader = new Scanner(vis);
        //read the file
        while(fileReader.hasNextLine()){
            String linkTmp = fileReader.nextLine();
            links.add(linkTmp);
        }
        fileReader.close();
        // this part is to restore interupted threads store the state
        // of the last queue and start from it not from scratch
        // the links in the queue should
        // load other thrads' queue if exits
        File que = new File("queue.txt");
        Scanner fileReaderB = new Scanner(que);
        while(fileReaderB.hasNextLine()){
            String linkTmp = fileReaderB.nextLine();
            // if link in the queue is not visitied before add it
            if(!links.contains(linkTmp))
                queue.add(linkTmp);
        }
        fileReaderB.close();

        // if this is the first crawl then create the files first
        // first chunk of threads has the responsibility of creating this initial files
        fileA = new File("visited.txt");
        fileB = new File("queue.txt");
        if(!fileA.exists())
            fileA.createNewFile();
        if(!fileB.exists())
            fileB.createNewFile();

        // initialize the file
        fWriterA = new FileWriter(fileA.getName(),true);
        fWriterB = new FileWriter(fileB.getName(),true);
        // initialize the buffer writer
        WriterA = new BufferedWriter(fWriterA);
        WriterB = new BufferedWriter(fWriterB);

    }

    @Override
    public void run(){
        try{
            while(links.size() < MAX){

                // if a thead approached a queue that is empty wait
                // till other thread wakes it up
                synchronized (queue){
                    while(queue.isEmpty())
                        queue.wait();
                }

                String currentURL = queue.poll();
                int numberOfNewLinks = 0;
                Boolean takeURL = true;

                // the links is a common resource that I want only
                // single thread to alter
                synchronized (links){

                    if(!links.contains(currentURL) && !disallow.contains(currentURL)){
                        Boolean rejected = false;
                        try{
                            GetRobotFile(currentURL);
                            rejected = CheckIfInRobot(currentURL);
                            if(rejected){
                                disallow.add(currentURL);
                                continue;
                            }

                        }
                        catch (Exception e){System.out.println("Checking against robot.txt failed");}

                        // add the link to the links hash
                        links.add(currentURL);
                        try{
                            WriterA.write(currentURL+"\n");
                            WriterA.flush();
                        }catch(IOException e){System.out.println("Writing in visited.txt failed");}
                        numberOfNewLinks = links.size();
                    }
                    else
                        takeURL = false;

                }
                if(takeURL){
                    try{
                        // used a stackoverflow for this
                        // https://stackoverflow.com/questions/30408174/jsoup-how-to-get-href
                        // get the links in the page if added (not in robot or repeated)
                        Document document = Jsoup.connect(currentURL).get();
                        Elements hrefs = document.select("a[href]");

                        for(Element page : hrefs) {
                            String newlyAdded = page.attr("abs:href");

                            // not all threads can add a new link at the same time
                            // so synchronize
                            synchronized (queue){
                                queue.add(newlyAdded);
                                try{
                                    WriterB.write(newlyAdded+"\n");
                                    WriterB.flush();
                                }
                                catch (IOException e){System.out.println("Writing in Queue.txt failed"); }
                                queue.notifyAll();
                            }
                        }
                    }catch (Exception e){System.out.println("fetching Hrefs failed");}

                    SAVEHTMLDOCS(currentURL , numberOfNewLinks);
                }
            }
        }catch (Exception e)
        {
            System.out.println("Threading Error in run function");
        }
    }

}

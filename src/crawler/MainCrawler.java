package crawler;

import java.util.Scanner;
import java.sql.*;

public class MainCrawler {

    static int numberOfThreads;
    public static void main(String[] args) throws Exception
    {
        System.out.println("Thread started for crawling");

        ThreadedCrawler.init();

        //get the number from the user
        System.out.println("How many threads do you want :");
        Scanner scanner = new Scanner(System.in);
        numberOfThreads = scanner.nextInt();
        scanner.close();

        ThreadedCrawler [] threads = new ThreadedCrawler[numberOfThreads];

        // start threads for the size the user gave
        for(int i = 0 ; i<numberOfThreads ; i++){
            threads[i] = new ThreadedCrawler();
            threads[i].start();
        }
        // MARK
        // wait for the other threaads to finish
        // this needs to be more optimized to get thr thread that finished
        // not waiting till the end

        for( int i = 0 ; i<numberOfThreads ; i++){
            threads[i].join();
            System.out.println("Thread :"+i+" is done");
        }


        // end the threaded object

        ThreadedCrawler.end();



    }
}

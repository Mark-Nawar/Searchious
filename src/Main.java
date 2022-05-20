import crawler.MainCrawler;
import indexer.Indexer;
import java.util.ArrayList;
public class Main {
    public static void main(String [] args){
        try {
           MainCrawler.main(args);
            Indexer sercho = new Indexer();
            sercho.retAllHtml();
            sercho.DBWrite();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
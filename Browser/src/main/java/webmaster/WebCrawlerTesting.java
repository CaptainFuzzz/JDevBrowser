package webmaster;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;


public class WebCrawlerTesting {
    public static void main(String[] args){
        ArrayList<String> url_list = new ArrayList<>();

        try {
            File topmill = new File("JDevBrowser/src/main/resources/top-1m.txt");
            Scanner myReader = new Scanner(topmill);

            while (myReader.hasNextLine()){
                String data = myReader.nextLine();
                url_list.add(data);
            }
        }
        catch (FileNotFoundException e){
            System.out.println("An error with reading the file occurred.");
            e.printStackTrace();
        }


        ArrayList<Webcrawler> bots = new ArrayList<Webcrawler>();
        bots.add(new Webcrawler(1, url_list));
        bots.add(new Webcrawler(2, url_list));
        bots.add(new Webcrawler(3, url_list));
        bots.add(new Webcrawler(4, url_list));

        for (Webcrawler w : bots){
            try {
                w.getThread().join();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}

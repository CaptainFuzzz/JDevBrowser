package webmaster;


import java.lang.*;

import java.io.IOException;

import java.util.ArrayList;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.Level;


import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Webcrawler implements Runnable{
    private final int Crawler_ID;
    private ArrayList<String> url_list;
    private final Thread thread;
    private final ReentrantLock mutex = new ReentrantLock();
    static Logger logger = Logger.getLogger(Webcrawler.class.getName());

    public Webcrawler(int Crawler_ID, ArrayList<String> url_list){
        this.url_list = url_list;
        this.Crawler_ID = Crawler_ID;

        thread = new Thread(this);
        thread.start();
        logger.log(Level.INFO, "Thread: " + thread.getId() + " Crawler " + Crawler_ID + " Created.");
    }

    @Override
    public void run(){crawl();}

    private void crawl() {
        String url;
        while (!url_list.isEmpty()) {
            try {
                mutex.lock();
                url = "https://" + url_list.get(0);
                url_list.remove(0);
            } finally {
                mutex.unlock();
            }

            logger.log(Level.INFO, "Crawler " + Crawler_ID + ": Fetching page from " + url);

            HtmlPage page = request(url);

            if (page != null)
            {
                JDevDocument doc = buildDocument(page);
                Indexer indexer = new Indexer(doc);
            }
        }
    }

    private HtmlPage request(String url) {
        HtmlPage htmlPage = null;

        // fetch the website content
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            htmlPage = webClient.getPage(url);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Crawler " + Crawler_ID + ": Unable to fetch the page from " + url);
        }

        return htmlPage;
    }

    private JDevDocument buildDocument(HtmlPage htmlPage){
        return new JDevDocument(UUID.randomUUID().toString(), htmlPage);
    }

    public Thread getThread() {
        return thread;
    }
}

package webmaster;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class JDevDocument {
    private String ID;
    private String Url;
    private String Title;
    private String Body;

    public JDevDocument(String id, HtmlPage htmlpage){
        ID = id;
        Title = htmlpage.getTitleText();
        Url = htmlpage.getBaseURI();
        Body = htmlpage.getBody().asNormalizedText();
    }

    public String getID() {
        return ID;
    }

    public String getUrl() {
        return Url;
    }

    public String getTitle() {
        return Title;
    }

    public String getBody() {
        return Body;
    }
}

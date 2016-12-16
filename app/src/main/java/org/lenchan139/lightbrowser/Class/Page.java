package org.lenchan139.lightbrowser.Class;

/**
 * Created by len on 10/16/16.
 */

public class Page {
    private String url,title;
    public Page(String url1, String title1){
        url = url1;
        title = title1;
    }
    public String getUrl(){return url;};
    public String getTitle(){return title;};
}

package org.lenchan139.lightbrowser.History;

/**
 * Created by len on 14/4/2017.
 */

public class HistoryItem {
    private String title;
    private String url;
    private String date;
    public HistoryItem(String title, String url, String date){
        this.title = title;
        this.url = url;
        this.date = date;
    }
    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "title=" + title + ",url=" + url + ",date=" + date;
    }
}

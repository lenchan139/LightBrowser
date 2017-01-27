package org.lenchan139.lightbrowser.Settings;

import android.view.View;

/**
 * Created by len on 27/1/2017.
 */

public class SettingsViewItem {
    private String title;
    private String content;
    private View.OnClickListener clickHandler;

    public SettingsViewItem(String title, String content, View.OnClickListener clickHandler){
        this.title = title;
        this.content = content;
        this.clickHandler = clickHandler;
    }

    public void setTitle(String in ){
        title = in;
    }
    public void setContent(String in ){
        content = in;
    }

    public void setClickHandler(View.OnClickListener in ){
        clickHandler = in;
    }


    public String getTitle(){
        return title;
    }
    public String getContent(){
        return content;
    }
    public View.OnClickListener getClickHandler(){
        return clickHandler;
    }
}

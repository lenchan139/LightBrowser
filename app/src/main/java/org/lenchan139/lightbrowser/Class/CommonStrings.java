package org.lenchan139.lightbrowser.Class;

import android.net.Uri;

/**
 * Created by len on 10/15/16.
 */

public class CommonStrings {
    public String searchHeader(){
        return "https://duckduckgo.com/?q=";
    }
    public String mobilizeHeader(){
        return "https://googleweblight.com/?lite_url=";
    }
    public String homePage(){
        return "https://ddg.gg";
    }
    public String TAG_setting(){return "org.lenchan139.lightbrowser.settings";}
    public String TAG_pref_home(){return "org.lenchan139.lightbrowser.Home";}
    public String TAG_pref_fab(){return "org.lenchan139.lightbrowser.Fab";}
    public String[] ARRAY_pref_fab(){return new String[]{"Disable", "Home", "Refresh", "Share"};}
    public  String STR_INTENT_MainToSetting (){return "org.lenchan139.lightbrowser.STR_INTENT_MainToSetting";}
    public  String TAG_pref_oc_bookmark_url (){return "org.lenchan139.lightbrowser.TAG_pref_oc_bookmark_url";}
    public String URL_DDG(){ return "https://duckduckgo.com";}
}

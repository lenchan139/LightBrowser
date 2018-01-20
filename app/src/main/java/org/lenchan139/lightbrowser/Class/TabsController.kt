package org.lenchan139.lightbrowser.Class

import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import org.lenchan139.lightbrowser.R
import java.util.ArrayList

/**
 * Created by len on 10/16/16.
 */

class TabsController(homepage:String) {
    private val arrWebivew = ArrayList<WebViewOverride>()
    internal var currIndex = -1
    private var homeUrl = ""

    init {
        homeUrl =homepage
    }
    fun size():Int{
        return arrWebivew.size
    }

    fun getList() : ArrayList<WebViewOverride>{
        return arrWebivew
    }


    fun getCurrentWebview() :WebViewOverride{
        return arrWebivew.get(currIndex)
    }

    fun newWebView(activity: AppCompatActivity,linearLayout: LinearLayout,webview:WebViewOverride):WebViewOverride{



        webview.loadUrl(homeUrl)

        linearLayout.addView(webview);
        arrWebivew.add(webview)
        Log.v("ArrAyWebview",webview.url)
        return switchToTab(activity,arrWebivew.size-1)

    }

    fun switchToTab(activity: AppCompatActivity,  show: Int):WebViewOverride{
        for((i,w) in arrWebivew.withIndex()){
            if(i == show){
                arrWebivew.get(i).setVisibility(View.VISIBLE)
                currIndex = i
            }else{
                arrWebivew.get(i).setVisibility(View.GONE)
            }
        }

        return arrWebivew.get(show)
    }

}
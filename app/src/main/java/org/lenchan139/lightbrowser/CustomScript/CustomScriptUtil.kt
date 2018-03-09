package org.lenchan139.lightbrowser.CustomScript

import android.R
import android.content.Context
import org.lenchan139.lightbrowser.History.CustomScriptItem
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by len on 20/9/2017.
 */
class CustomScriptUtil{
    fun getFullCustomScriptList(context : Context):Array<String?> {
        val scriptList = CustomScriptSQLiteController(context).getScripts()
        val showlist = arrayOfNulls<String>(scriptList.size)
        for (i in scriptList.indices) {
            showlist[i] = scriptList[i].title + "\n" + scriptList[i].url
        }
        return showlist
    }
    fun getRawFullCustomScriptList(context : Context): ArrayList<CustomScriptItem>? {
        val scriptList = CustomScriptSQLiteController(context).getScripts()
        return scriptList
    }

    fun addScript(context: Context,title:String, url:String, script:String){
        CustomScriptSQLiteController(context).addScript(title,url,script)
    }

    fun updateScript(context: Context,title: String, url: String, script: String){
        CustomScriptSQLiteController(context).updateScript(title, url, script)
    }
    fun delScript(context: Context,title: String){
        CustomScriptSQLiteController(context).delScript(title)
    }
    fun getScript(context: Context,title: String):CustomScriptItem{
        return CustomScriptSQLiteController(context).getScript(title)[0]

    }
    fun getScriptsToRun(context: Context,url:String):ArrayList<String>{
        var result = ArrayList<String>()
        val scriptList = CustomScriptSQLiteController(context).getScripts()
        for (i in scriptList) {
            val isMatch = Pattern.compile(i.url).matcher(url).find()
            if(isMatch){
                result.add(i.script!!)
            }
        }
        return result
    }
}
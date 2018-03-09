package org.lenchan139.lightbrowser.History

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.lenchan139.lightbrowser.Class.Page
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by len on 14/4/2017.
 */

class HistorySQLiteHelper(context: Context) : SQLiteOpenHelper(context, HistorySQLiteHelper._DBName, null, HistorySQLiteHelper._DBVersion) {

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCreate = "CREATE TABLE " + _TableName + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_title VARCHAR(199), " +
                "_url TEXT, " +
                "_addDate DATE " +
                ")"
        db.execSQL(sqlCreate)
        val arrayToInit = arrayListOf<Page>(
                Page("https://google.com/","Google"),
                Page("https://duckduckgo.com/","DuckDuckGo"))
        for(p in arrayToInit) {
            val sqlInsert = "INSERT INTO " + get_TableName() +
                    "(_title,_url,_addDate) VALUES(\"" +
                    p.title + "\",\"" + p.url + "\",\"" +
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time) + "\")"
            db.execSQL(sqlInsert)
        }


    }

public fun get_TableName(): String {
        return _TableName
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val sqlDrop = "DROP TABLE " + _TableName
        db.execSQL(sqlDrop)

    }

    companion object {
        val _DBVersion = 1
        val _DBName = "HistoryList.db"
        val _TableName = "histroyItems"
    }
}

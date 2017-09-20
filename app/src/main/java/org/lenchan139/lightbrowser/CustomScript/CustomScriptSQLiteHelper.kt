package org.lenchan139.lightbrowser.History

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by len on 14/4/2017.
 */

class CustomScriptSQLiteHelper(context: Context) : SQLiteOpenHelper(context, CustomScriptSQLiteHelper._DBName, null, HistorySQLiteHelper._DBVersion) {

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCreate = "CREATE TABLE " + _TableName + "(" +
                "_title TEXT PRIMARY KEY UNIQUE, " +
                "_url TEXT, " +
                "_script TEXT " +
                ")"
        db.execSQL(sqlCreate)

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
        val _DBName = "customScriptListsV1.db"
        val _TableName = "CustomScripts"
    }
}

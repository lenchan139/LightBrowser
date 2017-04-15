package org.lenchan139.lightbrowser.History;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by len on 14/4/2017.
 */

public class HistorySQLiteHelper extends SQLiteOpenHelper {
    private final static int _DBVersion = 1;
    private final static String _DBName = "HistroyList.db";
    private final static String _TableName = "histroyItems";

    public static String get_DBName() {
        return _DBName;
    }

    public static String get_TableName() {
        return _TableName;
    }

    public static int get_DBVersion() {
        return _DBVersion;
    }


    public HistorySQLiteHelper(Context context) {
        super(context, _DBName, null, _DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String sqlCreate = "CREATE TABLE " + _TableName + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_title VARCHAR(199), " +
                "_url TEXT, " +
                "_addDate DATE " +
                ")";
        db.execSQL(sqlCreate);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String sqlDrop = "DROP TABLE " + _TableName;
        db.execSQL(sqlDrop);
    }
}

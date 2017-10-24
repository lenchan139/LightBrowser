package org.lenchan139.lightbrowser.History;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

/**
 * Created by len on 14/4/2017.
 */

public class HistroySQLiteController {
    private HistorySQLiteHelper historySQLiteHelper = null;
    private Context context;
    public HistroySQLiteController(Context c) {
        context = c;
        historySQLiteHelper =  new HistorySQLiteHelper(c);

    }

    public void addHistory(String title,String url){
        SQLiteDatabase db = historySQLiteHelper.getWritableDatabase();
        /*ContentValues values = new ContentValues();
        values.put("_title", title);
        values.put("_url", url);
        values.put("_addDate", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
        db.insert(historySQLiteHelper.get_TableName(), null, values);
        */
        final String sqlInsert = "INSERT INTO " + historySQLiteHelper.get_TableName() +
                                    "(_title,_url,_addDate) VALUES(\"" +
                                    title +"\",\"" + url +"\",\"" +
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()) + "\")";
        Log.v("s",sqlInsert);
        db.execSQL(sqlInsert);
        db.close();
    }
    public ArrayList<CustomScriptItem> getHistory(){
        ArrayList<CustomScriptItem> result = new ArrayList<CustomScriptItem>();
        SQLiteDatabase db = historySQLiteHelper.getReadableDatabase();
        final String sqlSelct = "SELECT * FROM " + historySQLiteHelper.get_TableName() + " ORDER BY _id DESC";
        Cursor cursor = null;
        cursor = db.rawQuery(sqlSelct,null);
        Log.v("ss", String.valueOf(cursor.getCount()));
        if(cursor.moveToFirst()){
            do {
                result.add(new CustomScriptItem(cursor.getString(1),cursor.getString(2),cursor.getString(3)));
                Log.v("status",new CustomScriptItem(cursor.getString(1),cursor.getString(2),cursor.getString(3)).toString());
            } while(cursor.moveToNext());
        }
        cursor.close();
        return  result;
    }
    public ArrayList<CustomScriptItem> getHistoryBySearchUrl(String keyword){
        keyword.replace(" ","%");
        ArrayList<CustomScriptItem> result = new ArrayList<CustomScriptItem>();
        SQLiteDatabase db = historySQLiteHelper.getReadableDatabase();
        String sqlSelct = "SELECT * FROM " + historySQLiteHelper.get_TableName() +
                " WHERE _url LIKE \"%" + keyword + "%\"" +
                " OR _title LIKE \"%" + keyword + "%\"" +
                " GROUP BY _title,_url" +
                " ORDER BY LENGTH(_url) + LENGTH(_title)*2 ASC";
        Cursor cursor = null;
        if(keyword == null || Objects.equals(keyword, "")){
            sqlSelct = "SELECT * FROM " + historySQLiteHelper.get_TableName() + "WHERE 2=1";
        }
        cursor = db.rawQuery(sqlSelct,null);
        Log.v("ss", String.valueOf(cursor.getCount()));
        if(cursor.moveToFirst()){
            do {
                result.add(new CustomScriptItem(cursor.getString(1),cursor.getString(2),cursor.getString(3)));
                Log.v("status",new CustomScriptItem(cursor.getString(1),cursor.getString(2),cursor.getString(3)).toString());
            } while(cursor.moveToNext());
        }
        cursor.close();
        return  result;
    }
}

package org.lenchan139.lightbrowser.CustomScript;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.lenchan139.lightbrowser.History.CustomScriptItem;
import org.lenchan139.lightbrowser.History.CustomScriptSQLiteHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

/**
 * Created by len on 14/4/2017.
 */

public class CustomScriptSQLiteController {
    private CustomScriptSQLiteHelper sqLiteHelper = null;
    private Context context;
    public CustomScriptSQLiteController(Context c) {
        context = c;
        sqLiteHelper =  new CustomScriptSQLiteHelper(c);

    }
    public void addScript(String title,String url,String script){
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        /*ContentValues values = new ContentValues();
        values.put("_title", title);
        values.put("_url", url);
        values.put("_addDate", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
        db.insert(sqLiteHelper.get_TableName(), null, values);
        */
        final String sqlInsert;
        try {
            sqlInsert = "INSERT INTO " + sqLiteHelper.get_TableName() +
                                        "(_title,_url,_script) VALUES(\"" +
                    URLEncoder.encode(title, StandardCharsets.UTF_8.toString()) +"\",\"" +
                    URLEncoder.encode(url, StandardCharsets.UTF_8.toString())+"\",\"" +
                    URLEncoder.encode(script, StandardCharsets.UTF_8.toString()) + "\")";
            Log.v("s",sqlInsert);
            db.execSQL(sqlInsert);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        db.close();
    }
    public void updateScript(String title,String url,String script){
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        try {
            final String sqlInsert = "UPDATE " + sqLiteHelper.get_TableName() +
                    " SET " +
                    " _url='" + URLEncoder.encode(url, StandardCharsets.UTF_8.toString())+ "'," +
                    " _script='" + URLEncoder.encode(script, StandardCharsets.UTF_8.toString()) + "'" +
                    " WHERE _title='" + URLEncoder.encode(title, StandardCharsets.UTF_8.toString()) + "'";
            Log.v("s", sqlInsert);
            db.execSQL(sqlInsert);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        db.close();
    }

    public void delScript(String title){
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        try {
            final String sqlInsert = "DELETE FROM " + sqLiteHelper.get_TableName() +
                    " WHERE _title='" + URLEncoder.encode(title, StandardCharsets.UTF_8.toString()) + "'";
            Log.v("s",sqlInsert);
            db.execSQL(sqlInsert);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        db.close();
    }
    public ArrayList<CustomScriptItem> getScripts(){

        ArrayList<CustomScriptItem> result = new ArrayList<CustomScriptItem>();
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        final String sqlSelct = "SELECT * FROM " + sqLiteHelper.get_TableName() + "";
        Cursor cursor = null;
        cursor = db.rawQuery(sqlSelct,null);

        Log.v("ss", String.valueOf(cursor.getCount()));
        if(cursor.moveToFirst()){
            do {
                try {
                    result.add(new CustomScriptItem(
                            URLDecoder.decode(cursor.getString(0), StandardCharsets.UTF_8.toString()),
                            URLDecoder.decode(cursor.getString(1),StandardCharsets.UTF_8.toString()),
                            URLDecoder.decode(cursor.getString(2),StandardCharsets.UTF_8.toString())));
                    Log.v("status", result.get(result.size()-1).toString());
                }catch (UnsupportedEncodingException e){

                }
            } while(cursor.moveToNext());
        }
        cursor.close();

        return  result;
    }

    public ArrayList<CustomScriptItem> getScript(String title){

        ArrayList<CustomScriptItem> result = new ArrayList<CustomScriptItem>();
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        final String sqlSelct;

        Cursor cursor = null;
        try {
            sqlSelct = "SELECT * FROM " + sqLiteHelper.get_TableName() + " WHERE _title='" +
                    URLEncoder.encode(title, StandardCharsets.UTF_8.toString()) + "'";
            cursor = db.rawQuery(sqlSelct,null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();;
        }

        Log.v("ss", String.valueOf(cursor.getCount()));
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {try {
                    result.add(new CustomScriptItem(
                            URLDecoder.decode(cursor.getString(0), StandardCharsets.UTF_8.toString()),
                            URLDecoder.decode(cursor.getString(1),StandardCharsets.UTF_8.toString()),
                            URLDecoder.decode(cursor.getString(2),StandardCharsets.UTF_8.toString())));
                    Log.v("status", result.get(result.size()-1).toString());
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                } while (cursor.moveToNext());
            }
        }
        cursor.close();

        return  result;
    }
     
    public ArrayList<CustomScriptItem> getHistoryBySearchUrl(String keyword){
        keyword.replace(" ","%");
        ArrayList<CustomScriptItem> result = new ArrayList<CustomScriptItem>();
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        String sqlSelct = "SELECT * FROM " + sqLiteHelper.get_TableName() +
                " WHERE _url LIKE \"%" + keyword + "%\"" +
                " OR _title LIKE \"%" + keyword + "%\"" +
                " GROUP BY _url" +
                " ORDER BY LENGTH(_url) + LENGTH(_title)*2 ASC";
        Cursor cursor = null;
        if(keyword == null || Objects.equals(keyword, "")){
            sqlSelct = "SELECT * FROM " + sqLiteHelper.get_TableName() + "WHERE 2=1";
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

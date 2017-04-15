package org.lenchan139.lightbrowser;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.lenchan139.lightbrowser.History.HistoryItem;
import org.lenchan139.lightbrowser.History.HistroySQLiteController;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private HistroySQLiteController histroySQLiteController;
    ArrayList<HistoryItem> historyList;
    ListView hList;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        histroySQLiteController = new HistroySQLiteController(this);
        historyList = histroySQLiteController.getHistory();
        hList = (ListView) findViewById(R.id.historyList);
        fab = (FloatingActionButton) findViewById(R.id.fab) ;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String[] showlist = new String[historyList.size()];
        for (int i=0;i<historyList.size();i++){
            showlist[i] = historyList.get(i).getTitle() + "\n" +historyList.get(i).getUrl();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, showlist);
        hList.setAdapter(adapter);
        hList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HistoryActivity.this,MainActivity.class);
                intent.putExtra("InURL",historyList.get(position).getUrl());
                startActivity(intent);
                finish();
            }
        });
    }



}

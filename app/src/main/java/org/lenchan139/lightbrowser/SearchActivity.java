package org.lenchan139.lightbrowser;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.droidparts.contract.SQL;
import org.lenchan139.lightbrowser.History.HistoryItem;
import org.lenchan139.lightbrowser.History.HistroySQLiteController;
import org.lenchan139.lightbrowser.R;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private HistroySQLiteController histroySQLiteController;
    ArrayList<HistoryItem> historyList = new ArrayList<>();
    ListView hList;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        histroySQLiteController = new HistroySQLiteController(this);
        hList = (ListView) findViewById(R.id.list);
        editText = (EditText) findViewById(R.id.editText);
        editText.setText(getIntent().getStringExtra("para"));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeybord();
                finish();
            }
        });
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        editText.setSelection(0,editText.getText().length());
        //update

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateListView();
                editText.setSelection(editText.getText().length());
            }
        });
        editText.setOnKeyListener(new View.OnKeyListener() {


            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    onEnter();

                    return true;
                }
                return false;
            }
        });
    }
    private void updateListView() {
        historyList.clear();
        try {
            historyList = histroySQLiteController.getHistoryBySearchUrl(editText.getText().toString());
        }catch (SQLiteException e){
            e.printStackTrace();
            hList.setAdapter(null);
        }
        String[] showlist = new String[historyList.size()];
        for (int i = 0; i < historyList.size(); i++) {
            showlist[i] = historyList.get(i).getTitle() + "\n" + historyList.get(i).getUrl();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, showlist);
        hList.setAdapter(adapter);
        hList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editText.setText(historyList.get(position).getUrl());
                onEnter();
            }
        });
    }
    private void onEnter(){
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        intent.putExtra("InURL", editText.getText().toString());
        startActivity(intent);
        finish();
    }
    public void hideKeybord(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}




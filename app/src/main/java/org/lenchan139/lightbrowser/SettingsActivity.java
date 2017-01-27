package org.lenchan139.lightbrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.lenchan139.lightbrowser.Class.ClearableEditText;
import org.lenchan139.lightbrowser.Class.CommonStrings;
import org.lenchan139.lightbrowser.Settings.SettingsLVAdpter;
import org.lenchan139.lightbrowser.Settings.SettingsViewItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private ListView listViewSetting;
    List<SettingsViewItem> listSetting = new ArrayList<SettingsViewItem>();
CommonStrings commonStrings = new CommonStrings();
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View.OnClickListener ocl = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initListView();
    }


    public void initListView(){

        listViewSetting = (ListView) findViewById(R.id.settingList);
        listSetting.clear();
        listSetting.add(new SettingsViewItem("Homepage",sp.getString(commonStrings.TAG_pref_home(),"Default"),null));
        listSetting.add(new SettingsViewItem("FAB Button",sp.getString(commonStrings.TAG_pref_fab(),"Disabled"),null));
        listViewSetting.setAdapter(new SettingsLVAdpter(this,listSetting));

        listViewSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(position == 0){
                    onClickHome();
                }else if(position == 1){
                    onClickFabButton();
                }
                Object o = listViewSetting.getItemAtPosition(position);
                SettingsViewItem str = (SettingsViewItem) o;//As you are using Default String Adapter

            }
        });
    }


    //onClick function below

    public void onClickHome(){
        final ClearableEditText txtUrl = new ClearableEditText(this);

// Set the default text to a link of the Queen
        txtUrl.setHint("Enter your homepage ...");
        txtUrl.setText(sp.getString(commonStrings.TAG_pref_home(),null));
        txtUrl.setPadding(30,15,30,15);
        txtUrl.setSingleLine(true);
        txtUrl.setImeOptions(EditorInfo.IME_ACTION_DONE);
        new AlertDialog.Builder(this)
                .setTitle("Custom Homepage")
                //.setMessage("Enter new URL of homepage")
                .setView(txtUrl)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String temp = txtUrl.getText().toString();
                        if((!temp.startsWith("http")) && (!temp.contains("."))){
                            Toast.makeText(SettingsActivity.this, "Discard! Due to Invaild URL.", Toast.LENGTH_SHORT).show();
                        }else if(!txtUrl.getText().toString().startsWith("http")){
                            temp = "http://"+temp;
                            sp.edit().putString(commonStrings.TAG_pref_home(),txtUrl.getText().toString()).commit();
                            Toast.makeText(SettingsActivity.this, "Change saved.", Toast.LENGTH_SHORT).show();
                        }else{
                            sp.edit().putString(commonStrings.TAG_pref_home(),txtUrl.getText().toString()).commit();
                            Toast.makeText(SettingsActivity.this, "Change saved.", Toast.LENGTH_SHORT).show();
                        }
                        initListView();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }


    public void onClickFabButton(){
        final String[] items = commonStrings.ARRAY_pref_fab();

        String curr = sp.getString(commonStrings.TAG_pref_fab(),null);
        int oldPos = 0;
        for(int i =0;i<items.length;i++){
            if(Objects.equals(items[i], curr)) {
                oldPos = i;
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Fab button mode").setIcon(R.mipmap.ic_launcher)
                .setSingleChoiceItems(items, oldPos, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sp.edit().putString(commonStrings.TAG_pref_fab(),items[which]).apply();
                        Toast.makeText(SettingsActivity.this, "Saved! Now turn to " +sp.getString(commonStrings.TAG_pref_fab(),null) + "!", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                        initListView();
                    }
                }).create();
        dialog.show();
    }

}

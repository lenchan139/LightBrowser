package org.lenchan139.lightbrowser;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.lenchan139.lightbrowser.Class.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    WebViewOverride webView;
    Button btnGo,btnBack,btnForward;
    ClearableEditText editText;
    String latestUrl = "https://ddg.gg/";
    SharedPreferences settings;
    Tab tab = new Tab(new Page("",latestUrl));
    CommonStrings commonStrings = new CommonStrings();
    final String TAG_HOME = "homePageUrl";
    ArrayList<String> backList = new ArrayList<>();
    boolean back = false;

    @Override
    public void onBackPressed() {
        //Log.v("backListString",backList.toString());
            if(backList.size() >1) {
                back=true;
                backList.remove(backList.size() - 1);
                webView.loadUrl(backList.get(backList.size() - 1));
            }else{
                exitDialog();
            }

    }
    protected void exitDialog() {
        final String[] items = new String[] {"Yes", "No"};
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Exit the Browser?")
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(MainActivity.this, items[which], Toast.LENGTH_SHORT).show();
                        if(which == 0){
                            finish();
                        }else if(which == 1){

                        }
                    }
                }).create();
        dialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        webView = (WebViewOverride) findViewById(R.id.webView);
        btnGo = (Button) findViewById(R.id.btnGo);
        editText = (ClearableEditText) findViewById(R.id.editText);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnForward = (Button) findViewById(R.id.btnForward);
        settings = getSharedPreferences(commonStrings.TAG_setting(),0);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        if(settings.getString(commonStrings.TAG_pref_home(),null) == null){
            settings.edit().putString(TAG_HOME,"https://ddg.gg");
        }

        registerForContextMenu(webView);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(Button.GONE);
        btnGo.setVisibility(btnGo.GONE);
        btnBack.setVisibility(btnBack.GONE);
        btnForward.setVisibility(btnForward.GONE);


        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrlFromEditText();
                hideKeybord();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String get = tab.moveToPervious().getUrl();
                if(get == null){
                    Toast.makeText(MainActivity.this, "No more page!", Toast.LENGTH_SHORT).show();
                }else {
                    webView.loadUrl(get);
                }
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String get = tab.moveToNext().getUrl();
                if(get == null){
                    Toast.makeText(MainActivity.this, "No more page!", Toast.LENGTH_SHORT).show();
                }else {
                    webView.loadUrl(get);
                }
            }
        });
        editText.setOnKeyListener(new View.OnKeyListener() {


            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    loadUrlFromEditText();
                    hideKeybord();
                    return true;
                }
                return false;
            }
        });
        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
        webView.setDownloadListener(new DownloadListener() {

            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));

                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/")));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); //This is important!
                intent.addCategory(Intent.CATEGORY_OPENABLE); //CATEGORY.OPENABLE
                intent.setType("*/*");//any application,any extension
                Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();

            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                editText.setText(url);

            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // handle different requests for different type of files
                // this example handles downloads requests for .apk and .mp3 files
                // everything else the webview can handle normally
            if(back) {
                back = false;
            }else{
                backList.add(url);
            }
                Log.v("backListString",backList.toString());
                tab.addPage(new Page(url, "Page"));

                view.loadUrl(url);

                return true;
            }
        });
        hideKeybord();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if(getIntent().getData()!= null){
            Log.v("InURL",getIntent().getData().toString());
            latestUrl = getIntent().getData().toString();
        }
        webView.loadUrl(latestUrl);
        webView.requestFocus();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Toast.makeText(this, "Developing!", Toast.LENGTH_SHORT).show();
            return true;
        }else if(id == R.id.menu_home){
            webView.loadUrl(new CommonStrings().homePage());
            return true;
        }else if(id == R.id.menu_share){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, latestUrl);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }else if(id == R.id.menu_moblize){
            Toast.makeText(this, "Developing!", Toast.LENGTH_SHORT).show();
            return true;
        }else if(id == R.id.menu_xmarks){

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.xmarks.android");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }else{
                Toast.makeText(this, "You havn't install Xmarks!", Toast.LENGTH_SHORT).show();
            }
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void hideKeybord(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public void loadUrlFromEditText(){
        String temp = editText.getText().toString().trim();
        if(temp.indexOf("ddg.gg") >=0 || temp.indexOf("duckduckgo.com") >= 0){
            webView.loadUrl(temp);
        }else if (temp.indexOf("https://") == 0 || temp.indexOf("http://") == 0) {
            webView.loadUrl( temp);
        } else if(!temp.contains(".")){
            webView.loadUrl(  commonStrings.searchHeader() + temp);
        }
        else{
            webView.loadUrl( "http://" + temp);
        }



    }
}

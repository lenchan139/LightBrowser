package org.lenchan139.lightbrowser;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.lenchan139.lightbrowser.Class.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    WebViewOverride webView;
    Button btnGo,btnBack,btnForward;
    ClearableEditText editText;
    String latestUrl = "https://duckduckgo.com";
    SharedPreferences settings;
    Tab tab = new Tab(new Page("",latestUrl));
    CommonStrings commonStrings = new CommonStrings();
    final String TAG_HOME = "homePageUrl";
    ArrayList<String> backList = new ArrayList<>();
    boolean back = false;
    ProgressBar progLoading;

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            final String[] items = new String[backList.size()];
            Collections.reverse(backList);
            for(int i=0;i<backList.size();i++){
                items[i] =backList.get(i);
            }
            Collections.reverse(backList);
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Back To(DESC):")
                    .setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(MainActivity.this, items[which], Toast.LENGTH_SHORT).show();
                            if(which != backList.size() -1 && backList.size() >= 2) {

                                Collections.reverse(backList);
                                String pushingUrl = backList.get(which);
                                backList = new ArrayList<String>( backList.subList(which,backList.size()-1));
                                Collections.reverse(backList);
                                webView.loadUrl(pushingUrl);

                            }
                        }
                    }).create();
            dialog.show();

            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        //Log.v("backListString",backList.toString());
            if(backList.size() >=2) {
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
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(MainActivity.this, items[which], Toast.LENGTH_SHORT).show();
                            finish();
                    }
                }).setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        String inUrl = intent.getStringExtra(getString(R.string.KEY_INURL_INTENT));
        if(inUrl != null){
            latestUrl = inUrl;
            webView.loadUrl(latestUrl);
        }else{
            super.onNewIntent(intent);
        }
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
        progLoading = (ProgressBar) findViewById(R.id.progressL) ;
        settings = getSharedPreferences(commonStrings.TAG_setting(),0);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        if(settings.getString(commonStrings.TAG_pref_home(),null) == null){
            settings.edit().putString(TAG_HOME,"https://duckduckgo.com");
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

        //permission reqeuest
        // Here, thisActivity is the current activity
        try {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.
                    Toast.makeText(this, "This Appp need permission for Downloading, please allow it.", Toast.LENGTH_LONG).show();
                    int STORAGE_PERMISSION_ID = 112;
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_ID);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "You are running Android 5 or lower, Skip Permission Checking.", Toast.LENGTH_SHORT).show();
        }


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
                String cm = CookieManager.getInstance().getCookie(url);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "d_" + url.substring(url.lastIndexOf("/")));
                request.addRequestHeader("Cookie",cm);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); //This is important!
                intent.addCategory(Intent.CATEGORY_OPENABLE); //CATEGORY.OPENABLE
                intent.setType("*/*");//any application,any extension
                Toast.makeText(getApplicationContext(), "Start downloading...", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();

            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if(progress < 100){
                    progLoading.setVisibility(ProgressBar.VISIBLE);
                    progLoading.setProgress(progress);
                }else if(progress >= 100){
                    progLoading.setProgress(progress);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //progLoading.setVisibility(ProgressBar.INVISIBLE);
                    progLoading.setProgress(0);
                    progLoading.setVisibility(ProgressBar.GONE);
                }

            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, final String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webView.requestFocus();
                editText.setText(url);
                if(url.indexOf("http:") >=0 || url.indexOf("https:") >= 0) {

                }else{
                    back = true;
                    runToExternal(url);
                    webView.loadUrl(backList.get(backList.size()-1));
                }

                        String cm = CookieManager.getInstance().getCookie(url);
                        if(cm == null){ cm = ""; }


                        if(back ) {
                            back = false;
                        }else{
                            backList.add(url);

                        }

                //progLoading.setProgress(50);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(backList.size() >=2) {
                    while (Objects.equals(backList.get(backList.size() - 1), backList.get(backList.size() - 2))) {
                        if(backList.size()>=2)
                        backList.remove(backList.size()-1);
                    }
                }
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.v("backListString",backList.toString());
                tab.addPage(new Page(url, "Page"));
                latestUrl = url;
                    view.loadUrl(url);


                return true;
            }
        });
        hideKeybord();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        String inUrl = getIntent().getStringExtra(getString(R.string.KEY_INURL_INTENT));
        if(inUrl != null){
            latestUrl = inUrl;
        }
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        Log.v("USERAGENT",webView.getSettings().getUserAgentString());
        webView.loadUrl(latestUrl);
        webView.requestFocus();

    }
    private void runToExternal(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(browserIntent);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this,"No Handler here.",Toast.LENGTH_SHORT);
        }
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
            startActivity(Intent.createChooser(sendIntent, "Send to..."));
            return true;
        }else if(id == R.id.menu_external){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(latestUrl));
            startActivity(browserIntent);
            return true;
        }else if(id == R.id.menu_bookmarks){

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("cz.nethar.owncloudbookmarks");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }else{
                Toast.makeText(this, "You havn't install OwnCloud Bookmarks!!", Toast.LENGTH_SHORT).show();
            }
            return  true;
        }else if(id == R.id.menu_exit){
            exitDialog();
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
        if(temp.contains("ddg.gg") || temp.contains("duckduckgo.com")){
            webView.loadUrl(temp);
        }else if (temp.indexOf("https://") == 0 || temp.indexOf("http://") == 0) {
            webView.loadUrl( temp);
        } else if(temp.indexOf(":") >= 1) {
            runToExternal(temp);
        }else if(!temp.contains(".")){
            webView.loadUrl(  commonStrings.searchHeader() + temp);
        }
        else{
            webView.loadUrl( "http://" + temp);
        }



    }
}

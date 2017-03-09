package org.lenchan139.lightbrowser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity {
    WebViewOverride webView;
    Button btnGo,btnBack,btnForward;
    ClearableEditText editText;
    String latestUrl = "https://duckduckgo.com";
    SharedPreferences settings;
    Tab tab = new Tab(new Page("",latestUrl));
    CommonStrings commonStrings = new CommonStrings();
    ArrayList<Page> backList = new ArrayList<>();
    boolean back = false;
    ProgressBar progLoading;

    private ValueCallback mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (result == null) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
                return;
            }
            //CLog.i("UPFILE", "onActivityResult" + result.toString());
            String path =  FileUtils.getPath(this, result);
            if (TextUtils.isEmpty(path)) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
                return;
            }
            Uri uri = Uri.fromFile(new File(path));
            ///CLog.i("UPFILE", "onActivityResult after parser uri:" + uri.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mUploadMessage.onReceiveValue(new Uri[]{uri});
            } else {
                mUploadMessage.onReceiveValue(uri);
            }
            mUploadMessage = null;
        }
        }



    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            final WebBackForwardList webList = webView.copyBackForwardList();

            //create String[] for showing
            final String[] items = new String[webList.getSize()];
            //store list to string[] with reverse sorting
            for(int i=0;i<webList.getSize();i++){
                String  temp = webList.getItemAtIndex(webList.getSize()-1-i).getTitle();
                //handling if current tab
                if (i == webList.getSize() -1 - webList.getCurrentIndex() ){
                    //Log.v("test",String.valueOf(webList.getSize() -1 - webList.getCurrentIndex()) );
                    temp = "◆" + temp;
                }else{
                    temp = "◇" + temp;
                }
                
                if(temp.length() >50){ temp = temp.substring(0,50) + " ...";}
                //if title too short, use url instead
                if(temp.length() > 3) {
                    items[i] = temp;
                }else{
                    items[i] = temp;
                }
            }

            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("History:")
                    .setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(MainActivity.this, items[which], Toast.LENGTH_SHORT).show();
                            if(which >= 0) {
                                //reverse the number
                                which = webList.getSize()-1-which;
                                String pushingUrl = webList.getItemAtIndex(which).getUrl();
                                //int a1 = which - webView.copyBackForwardList().getCurrentIndex();
                                //Log.v("test", String.valueOf(a1));
                                webView.goBackOrForward(which - webView.copyBackForwardList().getCurrentIndex());
                                //webView.loadUrl(pushingUrl);
                                latestUrl = pushingUrl;

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
            /*if(backList.size() >=2) {
                back=true;
                backList.remove(0);
                webView.loadUrl(backList.get(0).getUrl());
                latestUrl = backList.get(0).getUrl();

            }else{
                exitDialog();
            }*/
        if(webView.canGoBack()) {
            webView.goBack();
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
    protected void onPostResume() {
        super.onPostResume();
        initFabButton();
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
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        if(settings.getString(commonStrings.TAG_pref_home(),null) == null){
            latestUrl = "https://duckduckgo.com";
            settings.edit().putString(commonStrings.TAG_pref_home(),latestUrl).commit();
        }
        registerForContextMenu(webView);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callFabButton();
            }
        });
        initFabButton();
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
                Toast.makeText(getApplicationContext(), "File Downloading...", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();

            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onCloseWindow(WebView window) {
                onBackPressed();
                super.onCloseWindow(window);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                Log.v("currWebViewTitle",title);
            }

            //Android 5.0+ Uploads
            @Override
            @SuppressLint("NewApi")
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                }
                //CLog.i("UPFILE", "file chooser params：" + fileChooserParams.toString());
                mUploadMessage =  filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                if (fileChooserParams != null && fileChooserParams.getAcceptTypes() != null
                        && fileChooserParams.getAcceptTypes().length > 0) {
                    i.setType(fileChooserParams.getAcceptTypes()[0]);
                } else {
                    i.setType("*/*");
                }
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
                return true;
            }

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
            Boolean loadingFinish = true;
            Boolean redirectPage = false;
            @Override
            public void onPageStarted(WebView view, final String url, Bitmap favicon) {
                loadingFinish = false;
                super.onPageStarted(view, url, favicon);
                webView.requestFocus();
                editText.setText(url);
                if(url.indexOf("http:") >=0 || url.indexOf("https:") >= 0) {
                    //addToBack(url);
                }else{
                    back = true;
                    runToExternal(url);
                    webView.loadUrl(backList.get(backList.size()-1).getUrl());
                }

                        String cm = CookieManager.getInstance().getCookie(url);
                        if(cm == null){ cm = ""; }



            }


            public void addToBack(String url,String title){
                if(back ) {
                    back = false;
                }else{
                    backList.add(0,new Page(url,title));

                }

                //progLoading.setProgress(50);
                if(backList.size() >=2) {
                    try {


                        while (Objects.equals(backList.get(0), backList.get(1))) {

                            if (backList.size() >= 2)
                                backList.remove(0);
                        }
                    }catch (IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(!redirectPage){
                    loadingFinish = true;
                }

                if(loadingFinish && !redirectPage){
                    //HIDE LOADING IT HAS FINISHED
                    addToBack(url,view.getTitle());
                } else{
                    redirectPage = false;
                }

            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.v("backListString",backList.toString());
                tab.addPage(new Page(url, "Page"));
                if (!loadingFinish) {
                    redirectPage = true;
                }
                loadingFinish = false;
                latestUrl = url;
                    view.loadUrl(url);
                //addToBack(url);

                return true;
            }
        });
        hideKeybord();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        String inUrl = getIntent().getStringExtra(getString(R.string.KEY_INURL_INTENT));
        getIntent().putExtra(getString(R.string.KEY_INURL_INTENT), "");
        if(inUrl != null && inUrl !=""){
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
            Toast.makeText(this,"No Handler here.",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menu.findItem(R.id.menu_bookmarks).setVisible(false);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }


    public void shareCurrPage(){

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Send to..."));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
        Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.menu_home){
            webView.loadUrl(settings.getString(commonStrings.TAG_pref_home(),""));
            return true;
        }else if(id == R.id.menu_share){
            shareCurrPage();
            return true;
        }else if(id == R.id.menu_external){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl()));
            try {
                startActivity(browserIntent);
            }catch(ActivityNotFoundException e){
                Toast.makeText(this, "No handler.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }else if(id == R.id.menu_add_bookmark){
            String url3 = settings.getString(commonStrings.TAG_pref_oc_bookmark_url(),"");
            if(!url3.endsWith("/"))
                url3 = url3 + "/";
            String title = webView.getTitle();
            if(url3.startsWith("http")){
                String outUrl = url3
                        + "index.php/apps/bookmarks/bookmarklet?output=popup&url="
                        + url3
                        + "&title="
                        + title;
                webView.loadUrl(outUrl);
            }else{

            }
        }else if(id == R.id.menu_bookmarks){
            //Intent launchIntent = getPackageManager().getLaunchIntentForPackage("cz.nethar.owncloudbookmarks");
           //startActivity(launchIntent);//null pointer check in case package name was not found
            String url3 = settings.getString(commonStrings.TAG_pref_oc_bookmark_url(),"");
            if(!url3.endsWith("/"))
                url3 = url3 + "/";
            String title = webView.getTitle();
            if(url3.startsWith("http")) {
                String outUrl = url3 + "index.php/apps/bookmarks/";
                webView.loadUrl(outUrl);
            }
        }else if(id == R.id.menu_exit){
            exitDialog();
        }else if(id == R.id.menu_refresh){
            loadUrlFromEditText();
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
        if(false){
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

    public void initFabButton(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        String curr = settings.getString(commonStrings.TAG_pref_fab(),null);
        if(Objects.equals(curr, commonStrings.ARRAY_pref_fab()[1])){
            fab.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.fab_home);
        }else if(Objects.equals(curr, commonStrings.ARRAY_pref_fab()[2])){
            fab.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.fab_refresh);
        }else if(Objects.equals(curr, commonStrings.ARRAY_pref_fab()[3])){
            fab.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.fab_share);
        }else{
            fab.setVisibility(View.GONE);
        }

    }

    public void callFabButton(){

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        String curr = settings.getString(commonStrings.TAG_pref_fab(),null);
        if(Objects.equals(curr, commonStrings.ARRAY_pref_fab()[1])){
            webView.loadUrl(settings.getString(commonStrings.TAG_pref_home(),""));
        }else if(Objects.equals(curr, commonStrings.ARRAY_pref_fab()[2])){
            loadUrlFromEditText();
        }else if(Objects.equals(curr, commonStrings.ARRAY_pref_fab()[3])){
            shareCurrPage();
        }else{
            fab.setVisibility(View.GONE);
        }
    }
}

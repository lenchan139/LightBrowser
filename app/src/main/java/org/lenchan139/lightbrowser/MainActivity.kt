package org.lenchan139.lightbrowser

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.preference.Preference
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutCompat
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.ValueCallback
import android.webkit.WebBackForwardList
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import org.jsoup.Jsoup
import org.lenchan139.lightbrowser.Class.*
import org.lenchan139.lightbrowser.History.HistroySQLiteController

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import java.util.Objects

import android.R.attr.data
import android.R.attr.webViewStyle
import android.app.Dialog
import android.content.*
import android.support.annotation.RequiresApi
import android.text.Editable
import android.view.*
import android.widget.*

class MainActivity : AppCompatActivity() {
    private lateinit var  webView: WebViewOverride
    private lateinit var btnGo: Button
    private lateinit var btnBack: Button
    private lateinit var btnForward: Button
    private lateinit var editText: ClearableEditText
    private lateinit var settings: SharedPreferences
    private lateinit var commonStrings : CommonStrings
    private var backList = ArrayList<Page>()
    private var back = false
    private lateinit var progLoading: ProgressBar
    private lateinit var homeUrl: String
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    private var webviewBundleSaved = false
    private lateinit var btnSwitchWebView : Button

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        Log.i("here123","before")
        if (requestCode == FILECHOOSER_RESULTCODE) {
        Log.i("here123","after")
            if (null == mUploadMessage)
            {
                //mUploadMessage!!.onReceiveValue(null)
                return
            }
            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            if (result == null) {
                mUploadMessage!!.onReceiveValue(null)
                mUploadMessage = null
                return
            }

            Log.i("UPFILE", "onActivityResult" + result.toString());
            val path = FileUtils.getPath(this, result)
            if (TextUtils.isEmpty(path)) {
                mUploadMessage!!.onReceiveValue(null)
                mUploadMessage = null
                return
            }
            val uri = Uri.fromFile(File(path!!))
            Log.i("UPFILE", "onActivityResult after parser uri:" + uri.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mUploadMessage!!.onReceiveValue(arrayOf(uri))
            } else {
                mUploadMessage!!.onReceiveValue(null)
            }
            mUploadMessage = null
        }
    }


    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val webList = webView.copyBackForwardList()

            //create String[] for showing
            val items = arrayOfNulls<String>(webList.size)
            //store list to string[] with reverse sorting
            for (i in 0..webList.size - 1) {
                var temp = webList.getItemAtIndex(webList.size - 1 - i).title
                //handling if current tab
                if (i == webList.size - 1 - webList.currentIndex) {
                    //Log.v("test",String.valueOf(webList.getSize() -1 - webList.getCurrentIndex()) );
                    temp = "◆" + temp
                } else {
                    temp = "◇" + temp
                }

                if (temp.length > 50) {
                    temp = temp.substring(0, 50) + " ..."
                }
                //if title too short, use url instead
                if (temp.length > 3) {
                    items[i] = temp
                } else {
                    items[i] = temp
                }
            }

            val dialog = AlertDialog.Builder(this).setTitle("History:")
                    .setItems(items) { dialog, which ->
                        var which = which
                        //Toast.makeText(MainActivity.this, items[which], Toast.LENGTH_SHORT).show();
                        if (which >= 0) {
                            //reverse the number
                            which = webList.size - 1 - which
                            val pushingUrl = webList.getItemAtIndex(which).url
                            //int a1 = which - webView.copyBackForwardList().getCurrentIndex();
                            //Log.v("test", String.valueOf(a1));
                            webView.goBackOrForward(which - webView.copyBackForwardList().currentIndex)
                            //webView.loadUrl(pushingUrl);


                        }
                    }.create()
            dialog.show()

            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onBackPressed() {
        //Log.v("backListString",backList.toString());
        /*if(backList.size() >=2) {
                back=true;
                backList.remove(0);
                webView.loadUrl(backList.get(0).getUrl());
                latestUrl = backList.get(0).getUrl();

            }else{
                exitDialog();
            }*/
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            exitDialog()
        }

    }

    protected fun exitDialog() {
        val items = arrayOf("Yes", "No")
        val dialog = AlertDialog.Builder(this).setTitle("Exit the Browser?")
                .setPositiveButton("Exit") { dialog, which ->
                    //Toast.makeText(MainActivity.this, items[which], Toast.LENGTH_SHORT).show();
                    finish()
                    android.os.Process.killProcess(android.os.Process.myPid())
                }.setNegativeButton("Cancel", null)
                .create()
        dialog.show()
    }


    override fun onNewIntent(intent: Intent) {
        val inUrl = intent.getStringExtra(getString(R.string.KEY_INURL_INTENT))
        if (inUrl != null) {
            editText.setText(inUrl)
            loadUrlFromEditText()
        } else {
            super.onNewIntent(intent)
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        initFabButton()
        reloadPreference()
    }
    fun reloadPreference(){
        //reload user agent
        val wv1 = findViewById(R.id.webView1) as WebViewOverride
        val uas1 = wv1.settings.userAgentString
        wv1.settings.userAgentString = settings.getString(commonStrings.TAG_pref_custom_user_agent(),null)
        val wv2 = findViewById(R.id.webView2) as WebViewOverride
        val uas2 =wv2.settings.userAgentString
        wv2.settings.userAgentString = settings.getString(commonStrings.TAG_pref_custom_user_agent()
                ,settings.getString(commonStrings.TAG_pref_custom_user_agent_default(),null))

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        commonStrings = CommonStrings(applicationContext)
        webView = findViewById(R.id.webView1) as WebViewOverride
        btnGo = findViewById(R.id.btnGo) as Button
        editText = findViewById(R.id.editText) as ClearableEditText
        btnBack = findViewById(R.id.btnBack) as Button
        btnForward = findViewById(R.id.btnForward) as Button
        progLoading = findViewById(R.id.progressL) as ProgressBar
        settings = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        btnSwitchWebView = findViewById(R.id.btnSwitchView) as Button
        btnSwitchWebView.setOnClickListener {
            switchTab()
        }
        homeUrl = settings.getString(commonStrings.TAG_pref_home(), commonStrings.URL_DDG())

        registerForContextMenu(webView)
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { callFabButton() }
        initFabButton()
        btnGo.visibility = View.GONE
        btnBack.visibility = View.GONE
        btnForward.visibility = View.GONE

        //permission reqeuest
        // Here, thisActivity is the current activity
        try {
            if (ContextCompat.checkSelfPermission(this@MainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.
                    Toast.makeText(this, "This Appp need permission for Downloading, please allow it.", Toast.LENGTH_LONG).show()
                    val STORAGE_PERMISSION_ID = 112
                    ActivityCompat.requestPermissions(this@MainActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            STORAGE_PERMISSION_ID)

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "You are running Android 5 or lower, Skip Permission Checking.", Toast.LENGTH_SHORT).show()
        }


        btnGo.setOnClickListener {
            loadUrlFromEditText()
            hideKeybord()
        }
        btnBack.setOnClickListener { }

        btnForward.setOnClickListener { }
        editText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            // If the event is a key-down event on the "enter" button
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // Perform action on key press
                loadUrlFromEditText()
                hideKeybord()
                return@OnKeyListener true
            }
            false
        })
        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        editText.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            intent.putExtra("para", editText.text.toString())
            startActivity(intent)
            webView.requestFocus()
        }
        editText.isFocusable = false

        fun initWebView(webView : WebView){
            webView.settings.javaScriptEnabled = true
            webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            settings.edit().putString(commonStrings.TAG_pref_custom_user_agent_default(),webView.settings.userAgentString).commit()
            var default = webView.settings.userAgentString
            webView.settings.userAgentString = settings.getString(commonStrings.TAG_pref_custom_user_agent(),default)

            webView.setFindListener ( object : WebView.FindListener{
                override fun onFindResultReceived(activeMatchOrdinal: Int, numberOfMatches: Int, isDoneCounting: Boolean) {

                }
            }


            )
            webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                val request = DownloadManager.Request(
                        Uri.parse(url))
                val cm = CookieManager.getInstance().getCookie(url)
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "d_" + url.substring(url.lastIndexOf("/")))
                request.addRequestHeader("Cookie", cm)
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT) //This is important!
                intent.addCategory(Intent.CATEGORY_OPENABLE) //CATEGORY.OPENABLE
                intent.type = "*/*"//any application,any extension
                Toast.makeText(applicationContext, "Downloading...", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show()
            }
            webView.setWebChromeClient(object : WebChromeClient() {
                override fun onCloseWindow(window: WebView) {
                    onBackPressed()
                    super.onCloseWindow(window)
                }

                override fun onReceivedTitle(view: WebView, title: String) {
                    super.onReceivedTitle(view, title)
                    Log.v("currWebViewTitle", title)
                }

                override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                    if (mUploadMessage != null) {
                        mUploadMessage!!.onReceiveValue(null)
                    }
                    Log.i("UPFILE", "file chooser params：" + fileChooserParams!!.toString())
                    mUploadMessage = filePathCallback
                    val i = Intent(Intent.ACTION_GET_CONTENT)
                    i.addCategory(Intent.CATEGORY_OPENABLE)

                    Log.v("acceptType", fileChooserParams.acceptTypes[0].toString())
                    if (fileChooserParams != null && fileChooserParams.acceptTypes != null
                            && fileChooserParams.acceptTypes.size > 0) {
                        i.type = fileChooserParams.acceptTypes[0]
                        i.type = "*/*"
                    } else {
                        i.type = "*/*"
                    }
                    startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
                    return true
                }

                //Android 5.0+ Uploads
                 fun onShowFileChooser1(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams?): Boolean {
                   return false
                }

                override fun onProgressChanged(view: WebView, progress: Int) {
                    if (progress < 100) {
                        progLoading.visibility = ProgressBar.VISIBLE
                        progLoading.progress = progress
                    } else if (progress >= 100) {
                        progLoading.progress = progress
                        try {
                            Thread.sleep(300)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                        //progLoading.setVisibility(ProgressBar.INVISIBLE);
                        progLoading.progress = 0
                        progLoading.visibility = ProgressBar.GONE
                    }

                }
            })
            webView.setWebViewClient(object : WebViewClient() {
                internal var loadingFinish: Boolean? = true
                internal var redirectPage: Boolean? = false
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    loadingFinish = false
                    super.onPageStarted(view, url, favicon)
                    webView.requestFocus()
                    editText.setText(url)
                    if (url!!.indexOf("http:") >= 0 || url.indexOf("https:") >= 0) {
                        //addToBack(url);
                    } else {
                        back = true
                        runToExternal(url)
                        //webView.loadUrl(webView.copyBackForwardList().currentItem.originalUrl)
                    }

                    var cm: String? = CookieManager.getInstance().getCookie(url)
                    if (cm == null) {
                        cm = ""
                    }

                }



                fun addToBack(url: String, title: String) {
                    if (back) {
                        back = false
                    } else {
                        backList.add(0, Page(url, title))

                    }

                    //progLoading.setProgress(50);
                    if (backList.size >= 2) {
                        try {


                            while (backList[0] == backList[1]) {

                                if (backList.size >= 2)
                                    backList.removeAt(0)
                            }
                        } catch (e: IndexOutOfBoundsException) {
                            e.printStackTrace()
                        }

                    }
                }

                override fun onPageFinished(view: WebView, url: String) : Unit{

                    if (!redirectPage!!) {
                        loadingFinish = true
                    }

                    if (loadingFinish!! && (!redirectPage!!)) {
                        //HIDE LOADING IT HAS FINISHED
                        //addToBack(url,view.getTitle());
                        val hs = HistroySQLiteController(this@MainActivity)
                        hs.addHistory(view.title, view.url)
                    } else {
                        redirectPage = false
                    }
                    super.onPageFinished(view, url)
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    Log.v("backListString", backList.toString())
                    if ((!loadingFinish!!)) {
                        redirectPage = true
                    }
                    loadingFinish = false
                    view.loadUrl(url)
                    //addToBack(url);

                    return true
                }
            })
        }
        initWebView(findViewById(R.id.webView1) as WebViewOverride)
        initWebView(findViewById(R.id.webView2) as WebViewOverride)
        hideKeybord()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val inUrl = intent.getStringExtra(getString(R.string.KEY_INURL_INTENT))
        intent.putExtra(getString(R.string.KEY_INURL_INTENT), "")
        if (inUrl != null && inUrl != "") {
            editText.setText(inUrl)
            loadUrlFromEditText()

        } else {
            (findViewById(R.id.webView2) as WebViewOverride).loadUrl(homeUrl)
            (findViewById(R.id.webView1) as WebViewOverride).loadUrl(homeUrl)
        }
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        Log.v("USERAGENT", webView.settings.userAgentString)

        webView.requestFocus()

    }


    private fun runToExternal(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "No Handler here.", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menu.findItem(R.id.menu_bookmarks).setVisible(false);
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }


    fun shareCurrPage() {

        val sendIntent = Intent()
        var sfType = settings.getInt(CommonStrings(baseContext).TAG_pref_sharing_format_int(),0)
        var  sfContent = ""
        if(sfType == 0){
            sfContent = webView.url
        }else if(sfType == 1){
            sfContent = webView.title + "\n" + webView.url
        }
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, sfContent)
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Send to..."))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.menu_home) {
            webView.loadUrl(settings.getString(commonStrings.TAG_pref_home(), ""))
            return true
        } else if (id == R.id.menu_share) {
            shareCurrPage()
            return true
        } else if (id == R.id.menu_external) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webView.url))
            try {
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No handler.", Toast.LENGTH_SHORT).show()
            }

            return true
        } else if (id == R.id.menu_history) {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)

        }else if(id == R.id.menu_add_NCBookmarks){
            var launchIntent = Intent()
            //launchIntent.addCategory("android.intent.category.LAUNCHER")
            //launchIntent.setAction("org.lenchan139.ncbookmark.v2.addBookmarkAction")
            launchIntent.setComponent(ComponentName("org.lenchan139.ncbookmark","org.lenchan139.ncbookmark.v2.AddBookmarkActivityV2"))
            launchIntent.putExtra("inUrl",webView.url)
            launchIntent.putExtra("inTitle",webView.title)
            try {
                startActivity(launchIntent)//null pointer check in case package name was not found
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this, "NCBookmark not installed!", Toast.LENGTH_SHORT).show()
            }
        } else if (id == R.id.menu_view_ncbookmarks) {
            val launchIntent = packageManager.getLaunchIntentForPackage("org.lenchan139.ncbookmark")
            try {
                startActivity(launchIntent)//null pointer check in case package name was not found
            } catch (e: NullPointerException) {
                Toast.makeText(this, "NCBookmark not installed!", Toast.LENGTH_SHORT).show()
            }

            /*String url3 = settings.getString(commonStrings.TAG_pref_oc_bookmark_url(),"");
            if(!url3.endsWith("/"))
                url3 = url3 + "/";
            String title = webView.getTitle();
            if(url3.startsWith("http")) {
                String outUrl = url3 + "index.php/apps/bookmarks/";
                webView.loadUrl(outUrl);

            }*/
        } else if (id == R.id.menu_exit) {
            exitDialog()
        } else if (id == R.id.menu_refresh) {
            loadUrlFromEditText()
        }else if(id == R.id.menu_tab){
            switchTab()
        }else if(id == R.id.menu_find){
            findContent()
        }

        return super.onOptionsItemSelected(item)
    }
    fun findContent(){
        val dialog = AlertDialog.Builder(this)
        var editText = ClearableEditText(this)
        editText.setHint("your keyword...")
        editText.setSingleLine()
        dialog.setView(editText)
        dialog.setTitle("Find...")

        dialog.setPositiveButton("Find", DialogInterface.OnClickListener { dialog, which ->
            if(editText.text == null){

            } else {
            webView.findAllAsync(editText.text.toString())
            }
        })
        dialog.setNegativeButton("Cancel",null)

        dialog.create().show()
    }
    fun switchTab(){
        if(!webviewBundleSaved){
            webView = findViewById(R.id.webView2) as WebViewOverride
            (findViewById(R.id.webView2) as WebViewOverride).visibility = View.VISIBLE
            (findViewById(R.id.webView1) as WebViewOverride).visibility = View.GONE
            editText.text = Editable.Factory.getInstance().newEditable(webView.url)
            btnSwitchWebView.text = "2"
            Toast.makeText(this,getString(R.string.multi_tab_turn_secondary_tab),Toast.LENGTH_SHORT).show()
            webviewBundleSaved = !webviewBundleSaved
        }else{
            webView = findViewById(R.id.webView1) as WebViewOverride
            (findViewById(R.id.webView1) as WebViewOverride).visibility = View.VISIBLE
            (findViewById(R.id.webView2) as WebViewOverride).visibility = View.GONE
            editText.text = Editable.Factory.getInstance().newEditable(webView.url)
            Toast.makeText(this,getString(R.string.multi_tab_turn_primary_tab),Toast.LENGTH_SHORT).show()
            btnSwitchWebView.text = "1"
            webviewBundleSaved = !webviewBundleSaved
        }
    }
    fun hideKeybord() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun loadUrlFromEditText() {
        val temp = editText.text.toString().trim { it <= ' ' }
        if (false) {
            webView.loadUrl(temp)
        } else if (temp.indexOf("https://") == 0 || temp.indexOf("http://") == 0) {
            webView.loadUrl(temp)
        } else if (temp.indexOf(":") >= 1) {
            runToExternal(temp)
        } else if (!(temp.contains(".") && !temp.contains(" "))) {
            webView.loadUrl(settings.getString(commonStrings.TAG_pref_Search_Engine_Url(),
                    commonStrings.ARRAY_pref_Search_Engine_Default().get(0).url).replace("!@keywoard",temp))
        } else {
            webView.loadUrl("http://" + temp)
        }


    }

    fun initFabButton() {
        val fab = findViewById(R.id.fab) as FloatingActionButton
        val curr = settings.getString(commonStrings.TAG_pref_fab(), null)
        if (curr == commonStrings.ARRAY_pref_fab()[1]) {
            fab.visibility = View.VISIBLE
            fab.setImageResource(R.drawable.fab_home)
        } else if (curr == commonStrings.ARRAY_pref_fab()[2]) {
            fab.visibility = View.VISIBLE
            fab.setImageResource(R.drawable.fab_refresh)
        } else if (curr == commonStrings.ARRAY_pref_fab()[3]) {
            fab.visibility = View.VISIBLE
            fab.setImageResource(R.drawable.fab_share)
        } else {
            fab.visibility = View.GONE
        }

    }

    fun callFabButton() {

        val fab = findViewById(R.id.fab) as FloatingActionButton
        val curr = settings.getString(commonStrings.TAG_pref_fab(), null)
        if (curr == commonStrings.ARRAY_pref_fab()[1]) {
            webView.loadUrl(settings.getString(commonStrings.TAG_pref_home(), ""))
        } else if (curr == commonStrings.ARRAY_pref_fab()[2]) {
            loadUrlFromEditText()
        } else if (curr == commonStrings.ARRAY_pref_fab()[3]) {
            shareCurrPage()
        } else {
            fab.visibility = View.GONE
        }
    }

    companion object {
        private val FILECHOOSER_RESULTCODE = 859
    }
}

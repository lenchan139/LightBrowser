package org.lenchan139.lightbrowser

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import org.lenchan139.lightbrowser.Class.*
import org.lenchan139.lightbrowser.History.HistroySQLiteController
import java.io.File
import java.util.ArrayList
import android.content.*
import android.os.Message
import android.text.Editable
import android.view.*
import android.webkit.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.lenchan139.lightbrowser.CustomScript.CustomScriptUtil
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var btnGo: Button
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
    private var isWebViewInitDone = false
    private lateinit var arrWebivewLinear : LinearLayout
    private lateinit var webviewList : TabsController
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        //Log.i("here123","before")
        if (requestCode == FILECHOOSER_RESULTCODE) {
        //Log.i("here123","after")
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
            val webList = webviewList.getCurrentWebview().copyBackForwardList()

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
                            webviewList.getCurrentWebview().goBackOrForward(which - webviewList.getCurrentWebview().copyBackForwardList().currentIndex)
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
        if (webviewList.getCurrentWebview().canGoBack()) {
            webviewList.getCurrentWebview().goBack()
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
        var InURLFromExternal = intent.getBooleanExtra("InURLFromExternal",false)
        if(InURLFromExternal){
            newTab(this)
            intent.putExtra("InURLFromExternal",false)
        }
        if (inUrl != null) {
            editText.setText(inUrl)
            //newTab(this)
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
        editText = findViewById(R.id.editText) as ClearableEditText
        progLoading = findViewById(R.id.progressL) as ProgressBar
        settings = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        btnGo = findViewById(R.id.btnGo) as Button
        arrWebivewLinear = findViewById(R.id.webViewList) as LinearLayout
        homeUrl = settings.getString(commonStrings.TAG_pref_home(), commonStrings.URL_DDG())
        webviewList = TabsController(homeUrl)

        btnSwitchWebView = findViewById(R.id.btnSwitchView) as Button

        btnSwitchWebView.setOnClickListener {
            switchTab(this)
        }
        btnSwitchWebView.setOnLongClickListener {

            delTabDialog(this)
            true

        }
        newTab(this)

        registerForContextMenu(webviewList.getCurrentWebview())
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { callFabButton() }
        initFabButton()

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
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("Storage Access Required")
                            .setMessage("This Appp need internal storage permission for Download File, please allow it.")
                            .setCancelable(false)
                            .setPositiveButton("Grant", DialogInterface.OnClickListener { dialogInterface, i ->
                                val STORAGE_PERMISSION_ID = 112
                                ActivityCompat.requestPermissions(this@MainActivity,
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        STORAGE_PERMISSION_ID)

                            }).create().show()
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
        btnGo.visibility = View.GONE
        btnFindBack.setOnClickListener {
            webviewList.getCurrentWebview().findNext(false)
        }
        btnFindForward.setOnClickListener {
            webviewList.getCurrentWebview().findNext(true)
        }
        btnFindClose.setOnClickListener {
            tabBarFind.visibility = View.GONE
        }
        tabBarFind.visibility = View.GONE
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
            webviewList.getCurrentWebview().requestFocus()
        }
        editText.isFocusable = false


        //arrWebivew.add(findViewById(R.id.webView1) as WebViewOverride)
        //arrWebivew.add(findViewById(R.id.webView2) as WebViewOverride)
        //for(w in arrWebivew){
        //    initWebView(w)
       // }
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
        webviewList.getCurrentWebview().settings.builtInZoomControls = true
        webviewList.getCurrentWebview().settings.displayZoomControls = false
        Log.v("USERAGENT", webviewList.getCurrentWebview().settings.userAgentString)

        webviewList.getCurrentWebview().requestFocus()

    }

    override fun onDestroy() {
        super.onDestroy()
        System.exit(0)
    }

    private fun runToExternal(url: String) {
        val preIntent = Intent(Intent.ACTION_VIEW,Uri.parse(url))
        val targetedShareIntents = ArrayList<Intent>()
        val browserIntent = Intent.createChooser(preIntent,"Open with...")
        val resInfo = packageManager.queryIntentActivities(preIntent, PackageManager.MATCH_ALL)

        for (resolveInfo in resInfo){
            Log.v("listV",resolveInfo.activityInfo.packageName)
            val packageName = resolveInfo.activityInfo.packageName
            val targetedShareIntent = Intent(Intent.ACTION_VIEW,Uri.parse(url))
            targetedShareIntent.setPackage(packageName)
            if(!packageName.contains("org.lenchan139.lightbrowser")){
                targetedShareIntents.add(targetedShareIntent)
                Log.v("listVTureFalse","True")
            }
        }
        if(targetedShareIntents.size > 1 ) {
            val chooserIntent = Intent.createChooser(
                    targetedShareIntents.removeAt(targetedShareIntents.size - 1), "Open with...")

            chooserIntent.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS, JavaUtils().listToPracelable(targetedShareIntents))
            startActivity(chooserIntent)
        }else if(targetedShareIntents.size == 1) {
            val dialog = AlertDialog.Builder(this)
            val theIntent = packageManager.queryIntentActivities(targetedShareIntents.get(0), 0).get(0)
            dialog.setTitle("Open in " + theIntent.loadLabel(packageManager) + " ?")
                    .setIcon(theIntent.loadIcon(packageManager))
                    .setNegativeButton("Cancel" , DialogInterface.OnClickListener { dialogInterface, i ->

                    })
                    .setPositiveButton("Go!", DialogInterface.OnClickListener { dialogInterface, i ->
                        startActivity(targetedShareIntents.get(0))
                    }).create().show()
        }else{
            Toast.makeText(this,"No Handler here.",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menu.findItem(R.id.menu_bookmarks).setVisible(false);
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }
    fun initWebView(webView : WebViewOverride):WebViewOverride{
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
            val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype)
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) //Notify client once download is completed!
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,  fileName)
            request.addRequestHeader("Cookie", cm)
            request.setDescription("[Download task from Light Browser]")
            request.allowScanningByMediaScanner()
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Log.v("downloadMimeType", mimetype)
            Toast.makeText(applicationContext, "Downloading...", //To notify the Client that the file is being downloaded
                    Toast.LENGTH_LONG).show()
        }
        webView.setWebChromeClient(object : WebChromeClient() {
            override fun onCloseWindow(window: WebView) {
                onBackPressed()
                super.onCloseWindow(window)
            }

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                newTab(this@MainActivity)
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
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
                Log.v("webview " + view.id , "[onProgressChanged triggered]")
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
                Log.v("onPageLoadUrl",url)
                if (isUrlVaildRedirect(url!!)) {
                    //addToBack(url);
                } else {
                    back = true
                    view!!.stopLoading()
                    runToExternal(url!!)
                    editText.setText(webView.url)
                    //webView.loadUrl(webView.copyBackForwardList().currentItem.originalUrl)
                }

                var cm: String? = CookieManager.getInstance().getCookie(url)
                if (cm == null) {
                    cm = ""
                }

            }



            var isInitDone = false
            override fun onPageFinished(view: WebView, url: String) : Unit{

                if (!redirectPage!!) {
                    loadingFinish = true

                }

                if (loadingFinish!! && (!redirectPage!!)) {
                    //HIDE LOADING IT HAS FINISHED
                    //addToBack(url,view.getTitle());
                    val hs = HistroySQLiteController(this@MainActivity)
                    hs.addHistory(view.title, view.url)

                    //runCustomScript(s)
                    val runscripts = CustomScriptUtil().getScriptsToRun(baseContext,url)
                    if(runscripts.size > 0 && isInitDone){
                        for(i in runscripts){
                            view.evaluateJavascript(i,null)
                        }
                    }
                    isInitDone = true
                } else {
                    redirectPage = false
                }

                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                Log.v("loadingUrl",request!!.url.toString())
                return !(isUrlVaildRedirect(request!!.url.toString()))
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
        return webView
    }

    fun updateStichCount() {
        if (webviewList.size() <= 99) {
            btnSwitchWebView.text = webviewList.size().toString()
        } else if(webviewList.size()  <= 0) {
            btnSwitchWebView.text = "0"
        }else{
            btnSwitchWebView.text = "u+"
        }

    }
    fun newTab(activity: Activity){

        var nwv = WebViewOverride(this)
        nwv.setLayoutParams(LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT))
        var newWebview = initWebView(nwv)
        webviewList.newWebView(this, arrWebivewLinear, newWebview)
        updateStichCount()

    }
    fun delTab(activity: Activity,index:Int){
        if(webviewList.getList().size<=1){
            Toast.makeText(this,"You cannot delete the last tab.", Toast.LENGTH_SHORT).show()
        }else {
            webviewList.delWebView(this, arrWebivewLinear, index)
        }
        updateStichCount()

    }
    fun delTabDialog(activity: Activity){
        updateStichCount()
        var items = Array<String>(webviewList.size(), { "" })
        for ((i, w) in webviewList.getList().withIndex()) {
            if(i==webviewList.currIndex) {
                items.set(i, "☒ " + w.title + "\n     " + w.url)
            }else{
                items.set(i, "☒ " + w.title + "\n     " + w.url)
            }
        }
        var dialog = AlertDialog.Builder(this).setTitle("Delete Tab...")
                .setItems(items) { dialog, which ->
                    webviewList.delWebView(this,arrWebivewLinear, which)
                    updateStichCount()
                }
                .create()
        dialog.show()
    }
    fun switchTab(activity:Activity) {
            updateStichCount()
        var items = Array<String>(webviewList.size(), { "" })
        for ((i, w) in webviewList.getList().withIndex()) {
            if(i==webviewList.currIndex) {
                items.set(i, "▶" + w.title + "\n" + w.url)
            }else{
                items.set(i, "▷" + w.title + "\n" + w.url)
            }
        }
        var dialog = AlertDialog.Builder(this).setTitle("Tabs:")
                .setItems(items) { dialog, which ->
                    webviewList.switchToTab(this, which)
                    updateStichCount()
                }
                .setPositiveButton("New", DialogInterface.OnClickListener { dialog, which ->
                    newTab(activity)
                    updateStichCount()
                }).create()
        dialog.show()




    }
    fun shareCurrPage() {

        val sendIntent = Intent()
        var sfType = settings.getString(CommonStrings(baseContext).TAG_pref_sharing_format_int(),getString(R.string.common_string_array_sharing_format_0))
        var  sfContent = ""
        if(sfType == getString(R.string.common_string_array_sharing_format_0)){
            sfContent = webviewList.getCurrentWebview().url
        }else if(sfType == getString(R.string.common_string_array_sharing_format_1)){
            sfContent = webviewList.getCurrentWebview().title + "\n" + webviewList.getCurrentWebview().url
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
            val intent = Intent(this@MainActivity, PrefActivity::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.menu_home) {
            webviewList.getCurrentWebview().loadUrl(settings.getString(commonStrings.TAG_pref_home(), ""))
            return true
        } else if (id == R.id.menu_share) {
            shareCurrPage()
            return true
        } else if (id == R.id.menu_external) {
            runToExternal(webviewList.getCurrentWebview().url)

            return true
        } else if (id == R.id.menu_history) {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)

        }else if(id == R.id.menu_add_NCBookmarks){
            var launchIntent = Intent()
            //launchIntent.addCategory("android.intent.category.LAUNCHER")
            //launchIntent.setAction("org.lenchan139.ncbookmark.v2.addBookmarkAction")
            launchIntent.setComponent(ComponentName("org.lenchan139.ncbookmark","org.lenchan139.ncbookmark.v2.AddBookmarkActivityV2"))
            launchIntent.putExtra("inUrl",webviewList.getCurrentWebview().url)
            launchIntent.putExtra("inTitle",webviewList.getCurrentWebview().title)
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
            webviewList.getCurrentWebview().reload()
        }else if(id == R.id.menu_tab){
            switchTab(this@MainActivity)
        }else if(id == R.id.menu_find){
            findContent()
        }else if(id == R.id.menu_custom_script){
            openCustomScriptActivity()
        }else if(id == R.id.menu_desktop_mode_switch){
            webviewList.getCurrentWebview().setDesktopMode(!webviewList.getCurrentWebview().getDesktopModeStatus())
            webviewList.getCurrentWebview().reload()
        }
        return super.onOptionsItemSelected(item)
    }
    fun openCustomScriptActivity(){
        val intent = Intent(this,CustomScriptActivity::class.java)
        startActivity(intent)
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
                webviewList.getCurrentWebview().findAllAsync(editText.text.toString())
                tabBarFind.visibility = View.VISIBLE
            }
        })
        dialog.setNegativeButton("Cancel",null)

        dialog.create().show()
    }
    fun hideKeybord() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun loadUrlFromEditText() {
        var webView = webviewList.getCurrentWebview()
        val temp = editText.text.toString().trim { it <= ' ' }
        if (temp.startsWith("javascript:")) {
            webView.loadUrl(temp)
            editText.setText(webView.url)

        } else if (temp.startsWith("https://") || temp.startsWith("http://")) {
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
        } else if (curr == commonStrings.ARRAY_pref_fab()[4]){
            fab.visibility = View.VISIBLE
            fab.setImageResource(R.drawable.fab_context_menu)
        }else if (curr == commonStrings.ARRAY_pref_fab()[5]){
            fab.visibility = View.VISIBLE
            fab.setImageResource(R.drawable.fab_tab_switch)
        }else if (curr == commonStrings.ARRAY_pref_fab()[6]){
            fab.visibility = View.VISIBLE
            fab.setImageResource(R.drawable.fab_open_with)
        }else {
            fab.visibility = View.GONE
        }

    }

    fun callFabButton() {

        val fab = findViewById(R.id.fab) as FloatingActionButton
        val curr = settings.getString(commonStrings.TAG_pref_fab(), null)
        if (curr == commonStrings.ARRAY_pref_fab()[1]) {
            webviewList.getCurrentWebview().loadUrl(settings.getString(commonStrings.TAG_pref_home(), ""))
        } else if (curr == commonStrings.ARRAY_pref_fab()[2]) {
            loadUrlFromEditText()
        } else if (curr == commonStrings.ARRAY_pref_fab()[3]) {
            shareCurrPage()
        } else if (curr == commonStrings.ARRAY_pref_fab()[4]) {
            toolbar.showOverflowMenu()
        } else if (curr == commonStrings.ARRAY_pref_fab()[5]) {
            switchTab(this@MainActivity)
        } else if (curr == commonStrings.ARRAY_pref_fab()[6]) {
            runToExternal(webviewList.getCurrentWebview().url)
        } else {
            fab.visibility = View.GONE
        }
    }
    fun isUrlVaildRedirect(url: String):Boolean{
        if (url!!.startsWith("http:") || url.startsWith("https:") || url!!.startsWith("javascript:")) {
            return true
        } else {
            return false
        }
    }

    companion object {
        private val FILECHOOSER_RESULTCODE = 859
    }
}

package org.lenchan139.lightbrowser.Class

import android.app.Activity
import android.app.DownloadManager
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.webkit.DownloadListener
import android.webkit.WebView
import android.widget.Toast

import org.lenchan139.lightbrowser.MainActivity

import org.droidparts.Injector.getApplicationContext

/**
 * Created by len on 12/16/16.
 */

class WebViewOverride : WebView {
    internal var context: Context

    constructor(context: Context) : super(context) {
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, privateBrowsing: Boolean) : super(context, attrs, defStyleAttr, privateBrowsing) {
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.context = context
    }


    override fun onCreateContextMenu(menu: ContextMenu) {
        super.onCreateContextMenu(menu)

        val result = hitTestResult

        val handler = MenuItem.OnMenuItemClickListener { item ->
            val id = item.itemId

            val url = result.extra as String

            if (id == 1) {
                val request = DownloadManager.Request(
                        Uri.parse(url))

                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/")))
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                dm.enqueue(request)

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT) //This is important!
                intent.addCategory(Intent.CATEGORY_OPENABLE) //CATEGORY.OPENABLE
                intent.type = "*/*"//any application,any extension

                Toast.makeText(context, "Start Downloading!", Toast.LENGTH_SHORT).show()

            } else if (id == 2) {
                loadUrl(url)
                Log.v("menuId", "View Image")
            } else if (id == 3) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("cilps", url)
                clipboard.primaryClip = clip
                Toast.makeText(context, "URL Copied!", Toast.LENGTH_SHORT).show()
                Log.v("menuId", "Copy Link")
            } else if (id == 4) {

                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, url)
                sendIntent.type = "text/plain"
                context.startActivity(Intent.createChooser(sendIntent, "Send to..."))
                Log.v("menuId", "Share Link")
            }
            true
        }

        if (result.type == WebView.HitTestResult.IMAGE_TYPE || result.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            // Menu options for an image.
            //set the header title to the image url
            menu.setHeaderTitle(result.extra)
            menu.add(0, 1, 0, "Save Image").setOnMenuItemClickListener(handler)
            menu.add(0, 2, 0, "View Image").setOnMenuItemClickListener(handler)
        } else if (result.type == WebView.HitTestResult.ANCHOR_TYPE || result.type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            // Menu options for a hyperlink.
            //set the header title to the link url
            menu.setHeaderTitle(result.extra)
            menu.add(0, 3, 0, "Copy Link").setOnMenuItemClickListener(handler)
            menu.add(0, 4, 0, "Share Link").setOnMenuItemClickListener(handler)
        }
    }
}

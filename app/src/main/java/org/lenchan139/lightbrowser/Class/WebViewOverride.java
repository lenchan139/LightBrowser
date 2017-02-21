package org.lenchan139.lightbrowser.Class;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.Toast;

import org.lenchan139.lightbrowser.MainActivity;

import static org.droidparts.Injector.getApplicationContext;

/**
 * Created by len on 12/16/16.
 */

public class WebViewOverride extends WebView {
    Context context;
    public WebViewOverride(Context context) {
        super(context);
        this.context = context;
    }

    public WebViewOverride(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public WebViewOverride(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        this.context = context;
    }

    public WebViewOverride(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public WebViewOverride(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }


    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);

        final HitTestResult result = getHitTestResult();

        MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId() ;

                String url = result.getExtra();

                if(id == 1){
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));

                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/")));
                    DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                        dm.enqueue(request);

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); //This is important!
                    intent.addCategory(Intent.CATEGORY_OPENABLE); //CATEGORY.OPENABLE
                    intent.setType("*/*");//any application,any extension

                    Toast.makeText(context, "Start Downloading!", Toast.LENGTH_SHORT).show();

                }else if(id == 2){
                    loadUrl(url);
                    Log.v("menuId","View Image");
                }else if(id == 3){
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("cilps", url);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "URL Copied!", Toast.LENGTH_SHORT).show();
                    Log.v("menuId","Copy Link");
                }else if(id == 4){

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, "Send to..."));;
                    Log.v("menuId","Share Link");
                }
                return true;
            }
        };

        if (result.getType() == HitTestResult.IMAGE_TYPE ||
                result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            // Menu options for an image.
            //set the header title to the image url
            menu.setHeaderTitle(result.getExtra());
            menu.add(0, 1, 0, "Save Image").setOnMenuItemClickListener(handler);
            menu.add(0, 2, 0, "View Image").setOnMenuItemClickListener(handler);
        } else if (result.getType() == HitTestResult.ANCHOR_TYPE ||
                result.getType() == HitTestResult.SRC_ANCHOR_TYPE) {
            // Menu options for a hyperlink.
            //set the header title to the link url
            menu.setHeaderTitle(result.getExtra());
            menu.add(0, 3, 0, "Copy Link").setOnMenuItemClickListener(handler);
            menu.add(0, 4, 0, "Share Link").setOnMenuItemClickListener(handler);
        }
    }
}

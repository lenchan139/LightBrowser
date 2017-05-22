package org.lenchan139.lightbrowser

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class UrlFromExternalActivity : AppCompatActivity() {

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_url_from_external);
        var inURL: String?
        if (intent.data != null) {
            Log.v(getString(R.string.KEY_INURL_INTENT), intent.data.toString())
            inURL = intent.data.toString()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("InURL", inURL)
            startActivity(intent)
            inURL = null

        }
        finish()

    }
}

package org.lenchan139.lightbrowser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class UrlFromExternalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_url_from_external);
        String inURL;
        if(getIntent().getData()!= null){
            Log.v(getString(R.string.KEY_INURL_INTENT),getIntent().getData().toString());
            inURL = getIntent().getData().toString();
            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("InURL",inURL);
            startActivity(intent);

        }
        finish();

    }
}

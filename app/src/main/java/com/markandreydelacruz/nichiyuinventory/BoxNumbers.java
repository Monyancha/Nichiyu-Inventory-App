package com.markandreydelacruz.nichiyuinventory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class BoxNumbers extends AppCompatActivity {

    AdView adView;
    AdRequest adRequest;
    private String urlString;

    private WebView webViewSisGrades;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boxnumbers);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("Box Numbers");
        urlString = getIntent().getExtras().getString("urlString");


//        loadGrades();
        //show ads
        adView = (AdView) findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void loadGrades() {
        dialog = new ProgressDialog(BoxNumbers.this);
        dialog.setMessage("Please wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        webViewSisGrades  = (WebView) findViewById(R.id.webViewSisGrades);
        webViewSisGrades.getSettings().setJavaScriptEnabled(true); // enable javascript
        webViewSisGrades.getSettings().setLoadWithOverviewMode(true);
        webViewSisGrades.getSettings().setUseWideViewPort(true);
        webViewSisGrades.getSettings().setBuiltInZoomControls(true);
        webViewSisGrades.getSettings().setDisplayZoomControls(false);
        final Activity activity = this;
        webViewSisGrades.setWebViewClient(new WebViewClient() {
//            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
//            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
                Toast.makeText(getApplicationContext(), "Connection Failed. Try Again.", Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url){
                //True if the host application wants to leave the current WebView and handle the url itself, otherwise return false.
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Invalid Login Details. Try Again.", Toast.LENGTH_LONG).show();
                finish();
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        webViewSisGrades .loadUrl(urlString);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_all_records, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
//            new LowStocks.BackgroundTaskLowStocks().execute();
            loadGrades();
            return true;
        } else if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.markandreydelacruz.nichiyuinventory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.markandreydelacruz.nichiyuinventory.models.LowStocksModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.description;

public class MainActivity extends AppCompatActivity {

    EditText editTextHostAddress;
    String hostAddress;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextHostAddress = (EditText) findViewById(R.id.editTextHostAddress);
        hostAddress = editTextHostAddress.getText().toString();
        new MainActivity.BackgroundTaskTestHostAddress().execute("http://"+ hostAddress +"/nichiyuInventory/api/warehouse/testHostAddress.php");

        ImageButton imageButtonAllRecords = (ImageButton) findViewById(R.id.imageButtonAllRecords);
        imageButtonAllRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostAddress = editTextHostAddress.getText().toString();
                Intent intent = new Intent(MainActivity.this, AllRecords.class);
                intent.putExtra("urlString", "http://"+ hostAddress +"/nichiyuInventory/api/warehouse/getAllItems.php");
                startActivity(intent);
            }
        });

        ImageButton imageButtonLowStocks = (ImageButton) findViewById(R.id.imageButtonLowStocks);
        imageButtonLowStocks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostAddress = editTextHostAddress.getText().toString();
                Intent intent = new Intent(MainActivity.this, LowStocks.class);
                intent.putExtra("urlString", "http://"+ hostAddress +"/nichiyuInventory/api/warehouse/getLowStocks.php");
                startActivity(intent);
            }
        });

        ImageButton imageButtonScanQrCode = (ImageButton) findViewById(R.id.imageButtonScanQrCode);
        imageButtonScanQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostAddress = editTextHostAddress.getText().toString();
                Intent intent = new Intent(MainActivity.this, ScanQrCode.class);
                intent.putExtra("hostAddress", hostAddress);
                startActivity(intent);
            }
        });

        ImageButton imageButtonBoxNumbers = (ImageButton) findViewById(R.id.imageButtonBoxNumbers);
        imageButtonBoxNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostAddress = editTextHostAddress.getText().toString();
                Intent intent = new Intent(MainActivity.this, BoxNumbers.class);
                intent.putExtra("urlString", "http://"+ hostAddress +"/pupSisGradesWebCrawler/getContent.php");
                startActivity(intent);
            }
        });

        Button buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostAddress = editTextHostAddress.getText().toString();
                hideSoftKeyboard(MainActivity.this);
                new MainActivity.BackgroundTaskTestHostAddress().execute("http://"+ hostAddress +"/nichiyuInventory/api/warehouse/testHostAddress.php");
            }
        });
    }

    private class BackgroundTaskTestHostAddress extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;
        private String jsonString;
        private JSONObject jsonObject;
        private String status;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Testing Connection...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);

                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setConnectTimeout(5_000);
                httpURLConnection.setReadTimeout(5_000);
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while((jsonString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(jsonString + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                SystemClock.sleep(500);

                jsonString = stringBuilder.toString().trim();

                return jsonString;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            super.onPostExecute(jsonString);
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if(jsonString == null) {
                Snackbar snack = Snackbar.make(getWindow().getDecorView().getRootView(), "Connection Failed", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null);
                ViewGroup group = (ViewGroup) snack.getView();
                group.setBackgroundColor(Color.parseColor("#dd4e40"));
                snack.show();
            } else {
                try {
                    jsonObject = new JSONObject(jsonString);

                    int count = 0;
                    while(count < jsonObject.length()){
                        status = jsonObject.getString("status");
                        count++;
                    }
                    if(status.equals("success")){
                        Snackbar snack = Snackbar.make(getWindow().getDecorView().getRootView(), "Connection Established", Snackbar.LENGTH_LONG)
                                .setAction("Action", null);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(Color.parseColor("#1aa260"));
                        snack.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}

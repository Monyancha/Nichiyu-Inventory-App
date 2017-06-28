package com.markandreydelacruz.nichiyuinventory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ScanQrCode extends AppCompatActivity {
    private String item_id;
    private String description;
    private String partNumber;
    private String boxNumber;
    private String minStockCount;
    private String quantity;
    private String hostAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanqrcode);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("Scan Results");

        hostAddress = getIntent().getExtras().getString("hostAddress");

        new IntentIntegrator(ScanQrCode.this)
                .setBeepEnabled(true)
                .setPrompt("Place a QR Code inside the rectangle to scan it.")
                .initiateScan();


        Button buttonScanAgain = (Button) findViewById(R.id.buttonScanAgain);
        buttonScanAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(ScanQrCode.this)
                        .setBeepEnabled(true)
                        .setPrompt("Place a QR Code inside the rectangle to scan it.")
                        .initiateScan();
            }
        });
    }

    //Getting the scan results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                partNumber = result.getContents();
                new BackgroundTaskScanQrCode().execute();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public class BackgroundTaskScanQrCode extends AsyncTask<Void, Void, String> {
        String jsonUrl;
        String jsonString;
        ProgressDialog dialog;
        private JSONObject jsonObject;
        private JSONArray jsonArray;

        @Override
        protected void onPreExecute() {
            jsonUrl = "http://"+ hostAddress +"/nichiyuInventory/api/json/warehouse/getItemDetails.php?partNumber=\""+partNumber+"\"";
            super.onPreExecute();
            dialog = new ProgressDialog(ScanQrCode.this);
            dialog.setMessage("Please wait...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(jsonUrl);
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

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
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
                Toast.makeText(getApplicationContext(), "Connection Failed. Try Again.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                try {
                    jsonObject = new JSONObject(jsonString);
                    jsonArray = jsonObject.getJSONObject("data").getJSONArray("itemDetails");
                    int count = 0;
                    while(count < jsonArray.length()){
                        JSONObject jsonObject = jsonArray.getJSONObject(count);
                        item_id = jsonObject.getString("item_id");
                        description = jsonObject.getString("description");
                        partNumber = jsonObject.getString("partNumber");
                        boxNumber = jsonObject.getString("boxNumber");
                        minStockCount = jsonObject.getString("minStockCount");
                        quantity = jsonObject.getString("quantity");
                        count++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Item Not Found.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                setUpUIViews();
            }
        }
    }

    private void setUpUIViews() {
        setTitle(description);
        TextView tx_item_id = (TextView) findViewById(R.id.tx_item_id);
        tx_item_id.setText(item_id);
        TextView tx_description = (TextView) findViewById(R.id.tx_description);
        tx_description.setText(description);
        TextView tx_partNumber = (TextView) findViewById(R.id.tx_partNumber);
        tx_partNumber.setText(partNumber);
        TextView tx_boxNumber = (TextView) findViewById(R.id.tx_boxNumber);
        tx_boxNumber.setText(boxNumber);
        TextView tx_orderPoint = (TextView) findViewById(R.id.tx_orderPoint);
        tx_orderPoint.setText(minStockCount);
        TextView tx_stockOnHand = (TextView) findViewById(R.id.tx_stockOnHand);
        tx_stockOnHand.setText(quantity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

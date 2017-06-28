package com.markandreydelacruz.nichiyuinventory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.markandreydelacruz.nichiyuinventory.models.AllRecordsModel;

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

public class AllRecords extends AppCompatActivity {

    private ListView listViewAllRecords;
    private String urlString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allrecords);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("All Records");
        urlString = getIntent().getExtras().getString("urlString");

        listViewAllRecords = (ListView) findViewById(R.id.listViewAllRecords);
        new BackgroundTaskAllRecords().execute();
    }

    private class BackgroundTaskAllRecords extends AsyncTask<String, Integer, List<AllRecordsModel>>{

        private ProgressDialog dialog;
        private String jsonString;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(AllRecords.this);
            dialog.setMessage("Please wait...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected List<AllRecordsModel> doInBackground(String... params) {
            try {
                URL url = new URL(urlString);
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

                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("items");
                List<AllRecordsModel> allRecordsModelList = new ArrayList<>();
                Gson gson = new Gson();
                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject finalObject = jsonArray.getJSONObject(i);
                    AllRecordsModel allRecordsModel = gson.fromJson(finalObject.toString(), AllRecordsModel.class); // a single line json parsing using Gson
                    allRecordsModelList.add(allRecordsModel);
                }
                return allRecordsModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<AllRecordsModel> result) {
            super.onPostExecute(result);

            if(result != null) {
                AllRecordsAdapter adapter = new AllRecordsAdapter(getApplicationContext(), R.layout.row_allrecords_item, result);
                listViewAllRecords.setAdapter(adapter);
                listViewAllRecords.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // list item click opens a new detailed activity
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AllRecordsModel allRecordsModel = result.get(position); // getting the model
//                        Intent intent = new Intent(AllRecords.this, AllRecordsDetails.class);
//                        intent.putExtra("allRecordsModel", new Gson().toJson(allRecordsModel)); // converting model json into string type and sending it via intent
//                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), new Gson().toJson(allRecordsModel), Toast.LENGTH_SHORT).show();
                    }
                });
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Connection failed. Try again.", Toast.LENGTH_SHORT).show();
//                Snackbar.make(getWindow().getDecorView().getRootView(), "Connection Failed", Snackbar.LENGTH_INDEFINITE).show();
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
                finish();
            }

        }
    }

    private class AllRecordsAdapter extends ArrayAdapter {
        private List<AllRecordsModel> allRecordsModelList;
        private int resource;
        private LayoutInflater inflater;
        public AllRecordsAdapter(Context context, int resource, List<AllRecordsModel> objects) {
            super(context, resource, objects);
            allRecordsModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if(convertView == null){
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.textViewItemId = (TextView)convertView.findViewById(R.id.textViewItemId);
                holder.textViewDescription = (TextView)convertView.findViewById(R.id.textViewDescription);
                holder.textViewPartNumber = (TextView)convertView.findViewById(R.id.textViewPartNumber);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textViewItemId.setText(allRecordsModelList.get(position).getItemId());
            holder.textViewDescription.setText(allRecordsModelList.get(position).getDescription());
            holder.textViewPartNumber.setText(allRecordsModelList.get(position).getPartNumber());

            return convertView;
        }


        class ViewHolder{
            private TextView textViewItemId;
            private TextView textViewDescription;
            private TextView textViewPartNumber;
        }
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
            listViewAllRecords.setAdapter(null);
            new BackgroundTaskAllRecords().execute();
            return true;
        } else if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}

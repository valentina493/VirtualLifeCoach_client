package com.example.valentina.virtuallifecoach;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.valentina.virtuallifecoach.model.ExpiredGoal;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import utility.ExpiredGoalArrayAdapter;
import utility.ServicesUtility;

public class ExpiredGoalsShower extends ListActivity {
    final String checkExpiredeGoalsPath = "checkExpiringGoals/";
    SharedPreferences sharedPrefLogging = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_expiredgoals);
        sharedPrefLogging = this.getSharedPreferences(getString(R.string.logging_information_file), Context.MODE_PRIVATE);

        Integer userid = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);

        GetExpiredGoalsTask gegt = new GetExpiredGoalsTask();
        gegt.execute(ServicesUtility.baseURL + checkExpiredeGoalsPath + userid);
    }

    private class GetExpiredGoalsTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progressbar_expiredgoals).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            List<ExpiredGoal> expiredGoals = ServicesUtility.unmarshallListOfExpiredGoals(result);

            ExpiredGoalArrayAdapter expiredGoalArrayAdapter = new ExpiredGoalArrayAdapter(ExpiredGoalsShower.this, expiredGoals);
            setListAdapter(expiredGoalArrayAdapter);

            findViewById(R.id.progressbar_expiredgoals).setVisibility(View.GONE);

            TextView emptyView = new TextView(ExpiredGoalsShower.this);
            emptyView.setLayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            emptyView.setText("No elements to show");
            emptyView.setVisibility(View.GONE);
            emptyView.setTextColor(Color.BLACK);
            ((ViewGroup) getListView().getParent()).addView(emptyView);
            getListView().setEmptyView(emptyView);

        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String result = "";

            HttpURLConnection urlConnection;
            // HTTP Get
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Accept", "application/json");

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    result = IOUtils.toString(inputStream);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }
            return result;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_measurement_suggestion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.example.valentina.virtuallifecoach;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valentina.virtuallifecoach.model.Goal;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utility.GoalArrayAdapter;
import utility.ServicesUtility;

public class ActiveGoalsShower extends ListActivity {
    final String getActiveGoalsPath = "getActiveGoals/";
    final String createGoalPath = "newGoal";
    SharedPreferences sharedPrefLogging = null;
    static public List<String> activeMeasureTypes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activegoals);
        sharedPrefLogging = this.getSharedPreferences(getString(R.string.logging_information_file), Context.MODE_PRIVATE);

        Integer userid = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);

        GetActiveGoalsTask gagt = new GetActiveGoalsTask();
        gagt.execute(ServicesUtility.baseURL + getActiveGoalsPath + userid);
    }

    public void createNewGoal(View view) {
        final View dialogView = LayoutInflater.from(ActiveGoalsShower.this).inflate(R.layout.layout_newgoalcreation, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(ActiveGoalsShower.this);
        builder.setTitle("New Goal");
        builder.setView(dialogView);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String measureType = ((Spinner) dialogView.findViewById(R.id.mtypespinner_newgoalcreation)).getSelectedItem().toString();
                String minValue = ((TextView) dialogView.findViewById(R.id.goalminvalue_newgoalcreation)).getText().toString();
                String maxValue = ((TextView) dialogView.findViewById(R.id.goalmaxvalue_newgoalcreation)).getText().toString();
                String deadline = ((TextView) dialogView.findViewById(R.id.goaldeadline_newgoalcreation)).getText().toString();

                Date deadlineDate;

                try {
                    deadlineDate = new SimpleDateFormat("yyyy-MM-dd").parse(deadline);
                } catch (ParseException e) {
                    Toast.makeText(ActiveGoalsShower.this, "The deadline inserted is not valid", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (activeMeasureTypes.contains(measureType)) {
                    Toast.makeText(ActiveGoalsShower.this, "The goal for " + measureType + " exists already", Toast.LENGTH_SHORT).show();
                }else if (minValue.isEmpty() && maxValue.isEmpty()){
                    Toast.makeText(ActiveGoalsShower.this, "Either the minimum or the maximum value must be specified", Toast.LENGTH_SHORT).show();
                }else if (!minValue.isEmpty() && !maxValue.isEmpty() && Double.parseDouble(minValue) > Double.parseDouble(maxValue)) {
                    Toast.makeText(ActiveGoalsShower.this, "The minimum value cannot be greater than the maximum value", Toast.LENGTH_SHORT).show();
                }else if(deadlineDate.before(new Date())){
                    Toast.makeText(ActiveGoalsShower.this, "The deadline must not be past", Toast.LENGTH_SHORT).show();
                } else {
                    NewGoalTask ngt = new NewGoalTask(measureType, minValue, maxValue, deadline);
                    ngt.execute(ServicesUtility.baseURL + createGoalPath);
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);

        Dialog d = builder.create();
        d.show();

        ArrayAdapter<CharSequence> measureTypesAdapter = ArrayAdapter.createFromResource(ActiveGoalsShower.this, R.array.goalable_mtypes_array, android.R.layout.simple_spinner_item);
        measureTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner mtypeSpinner = ((Spinner) d.findViewById(R.id.mtypespinner_newgoalcreation));
        mtypeSpinner.setAdapter(measureTypesAdapter);
    }

    private class NewGoalTask extends AsyncTask<String, Integer, String> {
        private String minValue;
        private String maxValue;
        private String deadline;
        private String measureType;

        public NewGoalTask(String measureType, String minValue, String maxValue, String deadline) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.deadline = deadline;
            this.measureType = measureType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Goal createdGoal = ServicesUtility.unmarshallGoal(result);
            if (createdGoal != null) {
                GoalArrayAdapter gaa = (GoalArrayAdapter) getListAdapter();

                if (gaa != null) {
                    gaa.add(createdGoal);
                }

            } else {
                Toast.makeText(ActiveGoalsShower.this, "Something did not work, please try again later", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String result = "";

            HttpURLConnection urlConnection;
            // HTTP Post
            try {

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("personId", "" + sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0))
                        .appendQueryParameter("minValue", this.minValue)
                        .appendQueryParameter("measureType", this.measureType)
                        .appendQueryParameter("deadline", this.deadline);

                if(!this.maxValue.isEmpty()){
                    builder = builder.appendQueryParameter("maxValue", this.maxValue);
                }

                urlString = urlString + "?" + builder.build().getEncodedQuery();
                URL url = new URL(urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
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

    private class GetActiveGoalsTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progressbar_activegoals).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            List<Goal> activeGoals = ServicesUtility.unmarshallListOfGoals(result);
            activeMeasureTypes = new ArrayList<String>();
            if (activeGoals != null) {
                for (Goal g : activeGoals) {
                    activeMeasureTypes.add(g.getMeasureType().getName());
                }
            }

            GoalArrayAdapter goalArrayAdapter = new GoalArrayAdapter(ActiveGoalsShower.this, activeGoals);
            setListAdapter(goalArrayAdapter);

            findViewById(R.id.progressbar_activegoals).setVisibility(View.GONE);

            TextView emptyView = new TextView(ActiveGoalsShower.this);
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
        getMenuInflater().inflate(R.menu.menu_active_goals_shower, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_newgoal:
                createNewGoal(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

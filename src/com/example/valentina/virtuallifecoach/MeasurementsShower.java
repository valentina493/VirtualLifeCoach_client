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

import com.example.valentina.virtuallifecoach.model.Measurement;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import utility.MeasurementArrayAdapter;
import utility.ServicesUtility;

public class MeasurementsShower extends ListActivity {
    final String getMeasurementsPath = "getMeasurements/";
    final String createMeasurementPath = "newMeasurement";
    SharedPreferences sharedPrefLogging = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_measurements_shower);
        sharedPrefLogging = this.getSharedPreferences(getString(R.string.logging_information_file), Context.MODE_PRIVATE);

        getListView().setEmptyView(findViewById(android.R.id.empty));
        Integer userid = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);

        GetMeasurementsTask gmt = new GetMeasurementsTask();
        gmt.execute(ServicesUtility.baseURL + getMeasurementsPath + userid);
    }

    public void createNewMeasurement(View view) {

        final View dialogView = LayoutInflater.from(MeasurementsShower.this).inflate(R.layout.layout_newmeasurementcreation, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(MeasurementsShower.this);
        builder.setTitle("New Measurement");
        builder.setView(dialogView);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String measureType = ((Spinner) dialogView.findViewById(R.id.mtypespinner_newmsmtcreation)).getSelectedItem().toString();
                String value = ((TextView) dialogView.findViewById(R.id.value_newmsmtcreation)).getText().toString();
                String measuringDateString = ((TextView) dialogView.findViewById(R.id.measuringdate_newmsmtcreation)).getText().toString();

                Date measuringDate;
                if(!measuringDateString.isEmpty()) {
                    try {
                        measuringDate = new SimpleDateFormat("yyyy-MM-dd").parse(measuringDateString);
                    } catch (ParseException e) {
                        Toast.makeText(MeasurementsShower.this, "The measuring date inserted is not valid", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(measuringDate.after(new Date())){
                        Toast.makeText(MeasurementsShower.this, "The measuring date must not be future", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if(value.isEmpty()){
                    Toast.makeText(MeasurementsShower.this, "The measured value field cannot be empty", Toast.LENGTH_SHORT).show();

                } else {

                    NewMeasurementTask ngt = new NewMeasurementTask(measureType, value, measuringDateString);
                    ngt.execute(ServicesUtility.baseURL + createMeasurementPath);
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);

        Dialog d = builder.create();
        d.show();

        ArrayAdapter<CharSequence> measureTypesAdapter = ArrayAdapter.createFromResource(MeasurementsShower.this, R.array.mtypes_array, android.R.layout.simple_spinner_item);
        measureTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner mtypeSpinner = ((Spinner) d.findViewById(R.id.mtypespinner_newmsmtcreation));
        mtypeSpinner.setAdapter(measureTypesAdapter);
    }

    private class NewMeasurementTask extends AsyncTask<String, Integer, String> {
        private String value;
        private String measureType;
        private String measuringDate;

        public NewMeasurementTask(String measureType, String value, String measuringDate) {
            this.value = value;
            this.measuringDate = measuringDate;
            this.measureType = measureType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Measurement createdMeasurement = ServicesUtility.unmarshallMeasurement(result);
            if (createdMeasurement != null) {
                MeasurementArrayAdapter maa = (MeasurementArrayAdapter) getListAdapter();

                if (maa != null) {
                    maa.add(createdMeasurement);
                }

            } else {
                Toast.makeText(MeasurementsShower.this, "Something did not work, please try again later", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String result = "";

            HttpURLConnection urlConnection = null;
            // HTTP Post
            try {

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("personId", "" + sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0))
                        .appendQueryParameter("value", this.value)
                        .appendQueryParameter("measureType", this.measureType)
                        .appendQueryParameter("measuringDate", this.measuringDate);

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
            } finally {
                urlConnection.disconnect();
            }
            return result;
        }
    }

    private class GetMeasurementsTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progressbar_measurements).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            List<Measurement> measurements = ServicesUtility.unmarshallListOfMeasurements(result);

            MeasurementArrayAdapter measurementArrayAdapter = new MeasurementArrayAdapter(MeasurementsShower.this, measurements);
            setListAdapter(measurementArrayAdapter);

            findViewById(R.id.progressbar_measurements).setVisibility(View.GONE);

            TextView emptyView = new TextView(MeasurementsShower.this);
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
        getMenuInflater().inflate(R.menu.menu_measurements_shower, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_newmeasurement:
                createNewMeasurement(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}

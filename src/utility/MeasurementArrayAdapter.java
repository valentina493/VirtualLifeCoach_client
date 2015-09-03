package utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valentina.virtuallifecoach.R;
import com.example.valentina.virtuallifecoach.model.Measurement;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MeasurementArrayAdapter extends ArrayAdapter<Measurement> {
    private final Activity context;
    private final List<Measurement> measurements;
    private final String deleteMeasurementPath = "deleteMeasurement/";
    private final String updateMeasurementPath = "updateMeasurement";

    public MeasurementArrayAdapter(Activity context, List<Measurement> measurements) {
        super(context, R.layout.layout_goal_row, measurements);
        this.context = context;
        this.measurements = measurements;
    }

    static class ViewHolder {
        public TextView measureTypeName;
        public TextView measuringDate;
        public TextView value;
        public ImageButton deleteButton;
        public ImageButton editButton;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.layout_measurement_row, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.measureTypeName = (TextView) rowView.findViewById(R.id.measuretypenametextview_msmtadapterrow);
            viewHolder.measuringDate = (TextView) rowView.findViewById(R.id.measuringdatetextview_msmtadapterrow);
            viewHolder.value = (TextView) rowView.findViewById(R.id.valuetextview_msmtadapterrow);
            viewHolder.deleteButton = (ImageButton) rowView.findViewById(R.id.deleteitembutton_msmtadapterrow);
            viewHolder.editButton = (ImageButton) rowView.findViewById(R.id.edititembutton_msmtadapterrow);

            rowView.setTag(viewHolder);
        }

        // fill data

        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.measureTypeName.setText(measurements.get(position).getMeasureType().getName());
        holder.measuringDate.setText(measurements.get(position).getMeasuringDate());
        holder.value.setText("" + measurements.get(position).getValue());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Are you sure?");
                builder.setMessage("Do you want to permanently delete this measurement?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int measurementId = measurements.get(position).get_measurementId();

                        DeleteMeasurementTask dmt = new DeleteMeasurementTask(measurements.get(position));
                        dmt.execute(ServicesUtility.baseURL + deleteMeasurementPath + measurementId);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                Dialog d = builder.create();
                d.show();
            }
        });

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View dialogView = LayoutInflater.from(context).inflate(R.layout.layout_newmeasurementcreation, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Modify Measurement");
                builder.setView(dialogView);

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String value = ((TextView) dialogView.findViewById(R.id.value_newmsmtcreation)).getText().toString();
                        String measuringDateString = ((TextView) dialogView.findViewById(R.id.measuringdate_newmsmtcreation)).getText().toString();
                        String measureType = ((Spinner) dialogView.findViewById(R.id.mtypespinner_newmsmtcreation)).getSelectedItem().toString();

                        Date measuringDate;

                        try {
                            measuringDate = new SimpleDateFormat("yyyy-MM-dd").parse(measuringDateString);
                        } catch (ParseException e) {
                            Toast.makeText(context, "The measuring date inserted is not valid", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (measuringDate.after(new Date())) {
                            Toast.makeText(context, "The measuring date must not be future", Toast.LENGTH_SHORT).show();
                        } else if (value.isEmpty()) {
                            Toast.makeText(context, "The measured value field cannot be empty", Toast.LENGTH_SHORT).show();
                        } else {
                            UpdateMeasurementTask umt = new UpdateMeasurementTask("" + measurements.get(position).get_measurementId(), value, measureType, measuringDateString, position);
                            umt.execute(ServicesUtility.baseURL + updateMeasurementPath);
                        }

                    }
                });

                builder.setNegativeButton(android.R.string.cancel, null);

                Dialog d = builder.create();
                d.show();

                ((EditText) d.findViewById(R.id.value_newmsmtcreation)).setText("" + measurements.get(position).getValue());
                ((EditText) d.findViewById(R.id.measuringdate_newmsmtcreation)).setText("" + measurements.get(position).getMeasuringDate());

                ArrayAdapter<CharSequence> measureTypesAdapter = ArrayAdapter.createFromResource(context, R.array.mtypes_array, android.R.layout.simple_spinner_item);
                measureTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner mtypeSpinner = ((Spinner) d.findViewById(R.id.mtypespinner_newmsmtcreation));
                mtypeSpinner.setAdapter(measureTypesAdapter);
                int spinnerPosition = measureTypesAdapter.getPosition(measurements.get(position).getMeasureType().getName());
                mtypeSpinner.setSelection(spinnerPosition);

            }
        });

        return rowView;
    }

    private class UpdateMeasurementTask extends AsyncTask<String, Integer, String> {
        private String value;
        private String measureType;
        private String measuringDate;
        private String measurementId;
        private int positionToBeModified;

        public UpdateMeasurementTask(String measurementId, String value, String measureType, String measuringDate, int positionToBeModified) {
            this.value = value;
            this.measureType = measureType;
            this.measuringDate = measuringDate;
            this.measurementId = measurementId;
            this.positionToBeModified = positionToBeModified;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Measurement updatedMeasurement = ServicesUtility.unmarshallMeasurement(result);
            if (updatedMeasurement != null) {
                MeasurementArrayAdapter.this.measurements.set(positionToBeModified, updatedMeasurement);
                MeasurementArrayAdapter.this.notifyDataSetChanged();

            } else {
                Toast.makeText(context, "Something did not work, please try again later", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String result = "";

            HttpURLConnection urlConnection = null;
            // HTTP Put
            try {

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("value", this.value)
                        .appendQueryParameter("measureType", this.measureType)
                        .appendQueryParameter("measuringDate", this.measuringDate)
                        .appendQueryParameter("measurementId", this.measurementId);

                urlString = urlString + "?" + builder.build().getEncodedQuery();
                URL url = new URL(urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
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

    private class DeleteMeasurementTask extends AsyncTask<String, Integer, Integer> {
        Measurement measurementToBeDeleted = null;

        public DeleteMeasurementTask(Measurement toBeDeleted) {
            measurementToBeDeleted = toBeDeleted;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);

            if (responseCode == 200) {
                MeasurementArrayAdapter.this.remove(measurementToBeDeleted);
            } else {
                Toast.makeText(context, "Something went wrong with the deleting. Try again later", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected Integer doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            Integer responseCode;

            HttpURLConnection urlConnection = null;
            // HTTP Delete
            try {

                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
                urlConnection.setRequestProperty("Accept", "application/json");

                responseCode = urlConnection.getResponseCode();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            } finally {
                urlConnection.disconnect();
            }
            return responseCode;
        }
    }

}

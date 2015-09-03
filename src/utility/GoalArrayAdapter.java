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
import android.widget.TextView;
import android.widget.Toast;

import com.example.valentina.virtuallifecoach.ActiveGoalsShower;
import com.example.valentina.virtuallifecoach.R;
import com.example.valentina.virtuallifecoach.model.Goal;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GoalArrayAdapter extends ArrayAdapter<Goal> {
    private final Activity context;
    private final List<Goal> goals;
    private final String deleteGoalPath = "deleteGoal/";
    private final String updateGoalPath = "updateGoal";

    public GoalArrayAdapter(Activity context, List<Goal> goals) {
        super(context, R.layout.layout_goal_row, goals);
        this.context = context;
        this.goals = goals;
    }

    static class ViewHolder {
        public TextView measureTypeName;
        public TextView deadline;
        public TextView range;
        public ImageButton deleteButton;
        public ImageButton editButton;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.layout_goal_row, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.measureTypeName = (TextView) rowView.findViewById(R.id.measuretypenametextview_adapterrow);
            viewHolder.deadline = (TextView) rowView.findViewById(R.id.deadlinetextview_adapterrow);
            viewHolder.range = (TextView) rowView.findViewById(R.id.rangetextview_adapterrow);
            viewHolder.deleteButton = (ImageButton) rowView.findViewById(R.id.deleteitembutton_adapterrow);
            viewHolder.editButton = (ImageButton) rowView.findViewById(R.id.edititembutton_adapterrow);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.measureTypeName.setText(goals.get(position).getMeasureType().getName());
        holder.deadline.setText(goals.get(position).getDeadline());
        holder.range.setText("[" + goals.get(position).getMinvalue() + ", " + goals.get(position).getMaxvalue() + "]");
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Are you sure?");
                builder.setMessage("Do you want to permanently delete this goal?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int goalId = goals.get(position).get_goalId();

                        DeleteGoalTask dgt = new DeleteGoalTask(goals.get(position));
                        dgt.execute(ServicesUtility.baseURL + deleteGoalPath + goalId);

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
                final View dialogView = LayoutInflater.from(context).inflate(R.layout.layout_newgoalcreation, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Modify Goal");
                builder.setView(dialogView);

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String minValue = ((TextView) dialogView.findViewById(R.id.goalminvalue_newgoalcreation)).getText().toString();
                        String maxValue = ((TextView) dialogView.findViewById(R.id.goalmaxvalue_newgoalcreation)).getText().toString();
                        String deadline = ((TextView) dialogView.findViewById(R.id.goaldeadline_newgoalcreation)).getText().toString();

                        Date deadlineDate;

                        try {
                            deadlineDate = new SimpleDateFormat("yyyy-MM-dd").parse(deadline);
                        } catch (ParseException e) {
                            Toast.makeText(context, "The deadline inserted is not valid", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (minValue.isEmpty() && maxValue.isEmpty()) {
                            Toast.makeText(context, "Either the minimum or the maximum value must be specified", Toast.LENGTH_SHORT).show();
                        } else if (!minValue.isEmpty() && !maxValue.isEmpty() && Double.parseDouble(minValue) >= Double.parseDouble(maxValue)) {
                            Toast.makeText(context, "The minimum value cannot be greater than the maximum value", Toast.LENGTH_SHORT).show();
                        } else if (deadlineDate.before(new Date())) {
                            Toast.makeText(context, "The deadline must not be past", Toast.LENGTH_SHORT).show();
                        } else {
                            UpdateGoalTask ugt = new UpdateGoalTask("" + goals.get(position).get_goalId(), minValue, maxValue, deadline, position);
                            ugt.execute(ServicesUtility.baseURL + updateGoalPath);
                        }

                    }
                });

                builder.setNegativeButton(android.R.string.cancel, null);

                Dialog d = builder.create();
                d.show();

                d.findViewById(R.id.mtypespinner_newgoalcreation).setVisibility(View.GONE);
                ((EditText) d.findViewById(R.id.goalminvalue_newgoalcreation)).setText("" + goals.get(position).getMinvalue());
                ((EditText) d.findViewById(R.id.goalmaxvalue_newgoalcreation)).setText("" + goals.get(position).getMaxvalue());
                ((EditText) d.findViewById(R.id.goaldeadline_newgoalcreation)).setText(goals.get(position).getDeadline());

            }
        });

        return rowView;
    }

    private class UpdateGoalTask extends AsyncTask<String, Integer, String> {
        private String minValue;
        private String maxValue;
        private String deadline;
        private String goalId;
        private int positionToBeModified;

        public UpdateGoalTask(String goalId, String minValue, String maxValue, String deadline, int positionToBeModified) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.deadline = deadline;
            this.goalId = goalId;
            this.positionToBeModified = positionToBeModified;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Goal updatedGoal = ServicesUtility.unmarshallGoal(result);
            if (updatedGoal != null) {
                GoalArrayAdapter.this.goals.set(positionToBeModified, updatedGoal);
                GoalArrayAdapter.this.notifyDataSetChanged();

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
                        .appendQueryParameter("minValue", "" + this.minValue)
                        .appendQueryParameter("deadline", this.deadline)
                        .appendQueryParameter("goalId", this.goalId);

                if (!this.maxValue.isEmpty()) {
                    builder = builder.appendQueryParameter("maxValue", this.maxValue);
                }
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

    private class DeleteGoalTask extends AsyncTask<String, Integer, Integer> {
        Goal gToBeDeleted = null;

        public DeleteGoalTask(Goal toBeDeleted) {
            gToBeDeleted = toBeDeleted;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);

            if (responseCode == 200) {
                ActiveGoalsShower.activeMeasureTypes.remove(gToBeDeleted.getMeasureType().getName());
                GoalArrayAdapter.this.remove(gToBeDeleted);
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

package com.example.valentina.virtuallifecoach;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valentina.virtuallifecoach.model.GoalFeedback;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import utility.ServicesUtility;

public class GoalTrend extends Activity {
    final String checkGoalTrendPath = "checkGoal/";

    SharedPreferences sharedPrefLogging = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_goaltrend);
        sharedPrefLogging = this.getSharedPreferences(getString(R.string.logging_information_file), Context.MODE_PRIVATE);

        Integer userid = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);

        CheckGoalTrendTask cgtt = new CheckGoalTrendTask();
        cgtt.execute(ServicesUtility.baseURL + checkGoalTrendPath + userid);
    }

    private class CheckGoalTrendTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progressbar_goaltrend).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            GoalFeedback goalFeedback = ServicesUtility.unmarshallGoalFeedback(result);

            if (goalFeedback != null) {
                // general comment
                ((TextView) findViewById(R.id.generalcommenttextview_goaltrend)).setText(goalFeedback.getGeneralComment());
                if(goalFeedback.getGeneralComment().startsWith("You do not have defined") || goalFeedback.getGeneralComment().startsWith("You need to add")){
                    findViewById(R.id.progressbar_goaltrend).setVisibility(View.GONE);
                    return;
                }

                // suggestion
                ((TextView) findViewById(R.id.suggestiontextview_goaltrend)).setText(goalFeedback.getSuggestion());

                // color
                int trafficLightDrawableId;
                if (goalFeedback.getColor().contentEquals("red")) {
                    trafficLightDrawableId = R.drawable.red_dot_300;
                } else if (goalFeedback.getColor().contentEquals("yellow")) {
                    trafficLightDrawableId = R.drawable.yellow_dot_300;
                } else {
                    trafficLightDrawableId = R.drawable.green_dot_300;
                }
                final int trafficLightSize = 120;
                Bitmap bm = BitmapFactory.decodeResource(getResources(), trafficLightDrawableId);
                Drawable leftDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bm, trafficLightSize, trafficLightSize, true));
                ((TextView) findViewById(R.id.generalcommenttextview_goaltrend)).setCompoundDrawablePadding(10);
                ((TextView) findViewById(R.id.generalcommenttextview_goaltrend)).setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);

                // recipe
                if (goalFeedback.getRecipe() != null) {
                    ((TextView) findViewById(R.id.recipenametextview_goaltrend)).setText(goalFeedback.getRecipe().getTitle());
                    ((TextView) findViewById(R.id.recipetypetextview_goaltrend)).setText("course: " +goalFeedback.getRecipe().getType());
                    ((TextView) findViewById(R.id.recipeurltextview_goaltrend)).setText(goalFeedback.getRecipe().getUrl());
                    if (!goalFeedback.getRecipe().getImageUrl().isEmpty()) {
                        ImageView recipeImage = ((ImageView) findViewById(R.id.recipeimageview_goaltrend));
                        new DownloadImageTask(recipeImage).execute(goalFeedback.getRecipe().getImageUrl());
                    } else {
                        findViewById(R.id.progressbar_goaltrend).setVisibility(View.GONE);
                    }
                } else {
                    findViewById(R.id.recipenametextview_goaltrend).setVisibility(View.GONE);
                    findViewById(R.id.recipetypetextview_goaltrend).setVisibility(View.GONE);
                    findViewById(R.id.recipeurltextview_goaltrend).setVisibility(View.GONE);
                    findViewById(R.id.recipeimageview_goaltrend).setVisibility(View.GONE);

                    // quote
                    String authorComposedString = goalFeedback.getQuote().getAuthor() + " once said: ";
                    SpannableStringBuilder sb = new SpannableStringBuilder(authorComposedString);
                    StyleSpan boldStyleSpan = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
                    sb.setSpan(boldStyleSpan, 0,goalFeedback.getQuote().getAuthor().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    ((TextView) findViewById(R.id.quoteauthortextview_goaltrend)).setText(sb);
                    ((TextView) findViewById(R.id.quotesentencetextview_goaltrend)).setText(goalFeedback.getQuote().getSentence());

                    findViewById(R.id.progressbar_goaltrend).setVisibility(View.GONE);
                }
            } else {
                findViewById(R.id.progressbar_goaltrend).setVisibility(View.GONE);
                Toast.makeText(GoalTrend.this, "Something went wrong. Try again later", Toast.LENGTH_SHORT).show();
            }
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            findViewById(R.id.progressbar_goaltrend).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_goal_trend, menu);
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

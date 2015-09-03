package com.example.valentina.virtuallifecoach;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valentina.virtuallifecoach.model.Recipe;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import utility.ServicesUtility;

public class HealthyRecipe extends Activity {
    final String getRandomHealthyRecipelPath = "getRandomHealthyRecipe/";
    SharedPreferences sharedPrefLogging = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_healthyrecipe);
        sharedPrefLogging = this.getSharedPreferences(getString(R.string.logging_information_file), Context.MODE_PRIVATE);

        Integer userid = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);
        GetRandomHealhtyRecipeTask grhrt = new GetRandomHealhtyRecipeTask();
        grhrt.execute(ServicesUtility.baseURL + getRandomHealthyRecipelPath + userid);
    }

    private class GetRandomHealhtyRecipeTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.recipeprogressbar_recipe).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Recipe recipe = ServicesUtility.unmarshallRecipe(result);

            findViewById(R.id.recipeprogressbar_recipe).setVisibility(View.GONE);

            if (recipe != null) {
                TextView recipename = ((TextView) findViewById(R.id.recipenametextview_recipe));
                recipename.setVisibility(View.VISIBLE);
                recipename.setText(recipe.getTitle());

                TextView recipetype = ((TextView) findViewById(R.id.recipetypetextview_recipe));
                recipetype.setVisibility(View.VISIBLE);
                recipetype.setText("course: "+recipe.getType());

                TextView recipeurl = ((TextView) findViewById(R.id.recipeurltextview_recipe));
                recipeurl.setVisibility(View.VISIBLE);
                recipeurl.setText(recipe.getUrl());

                if (!recipe.getImageUrl().isEmpty()) {
                    ImageView recipeImage = ((ImageView) findViewById(R.id.recipeimageview_recipe));
                    new DownloadImageTask(recipeImage).execute(recipe.getImageUrl());
                }
            } else {
                Toast.makeText(HealthyRecipe.this, "Something went wrong. Try again later", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_healthy_recipe, menu);
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

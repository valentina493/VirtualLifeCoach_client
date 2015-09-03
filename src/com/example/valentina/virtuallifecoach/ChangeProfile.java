package com.example.valentina.virtuallifecoach;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.valentina.virtuallifecoach.model.Person;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import utility.ServicesUtility;

public class ChangeProfile extends Activity {
    EditText firstname = null;
    EditText lastname = null;
    EditText birthdate = null;
    SharedPreferences sharedPrefLogging = null;

    final String updateUserPath = "updateUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_change_profile);

        firstname = (EditText) findViewById(R.id.firstnameedittext_chprofile);
        lastname = (EditText) findViewById(R.id.lastnameedittext_chprofile);
        birthdate = (EditText) findViewById(R.id.birthdateedittext_chprofile);

        sharedPrefLogging = this.getSharedPreferences(getString(R.string.logging_information_file), Context.MODE_PRIVATE);
        firstname.setText(sharedPrefLogging.getString(getString(R.string.logging_information_file_userfname), ""));
        lastname.setText(sharedPrefLogging.getString(getString(R.string.logging_information_file_userlname), ""));
        birthdate.setText(sharedPrefLogging.getString(getString(R.string.logging_information_file_userbdate), ""));

    }

    public void modify(View view) {
        String userFirstname = firstname.getText().toString();
        String userLastname = lastname.getText().toString();
        String userBirthdate = birthdate.getText().toString();
        Integer userId = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);

        if (userFirstname.isEmpty() || userLastname.isEmpty()) {
            Toast.makeText(ChangeProfile.this, "Firstname and Lastname cannot be empty", Toast.LENGTH_LONG).show();
        }

        UpdateUserTask uut = new UpdateUserTask(userId, userFirstname, userLastname, userBirthdate);
        uut.execute(ServicesUtility.baseURL + updateUserPath);

    }

    // Params, progress, result
    private class UpdateUserTask extends AsyncTask<String, Integer, String> {
        private Integer personId;
        private String firstname;
        private String lastname;
        private String birthdate;

        public UpdateUserTask(Integer personId, String firstname, String lastname, String birthdate) {
            this.personId = personId;
            this.firstname = firstname;
            this.lastname = lastname;
            this.birthdate = birthdate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Person loggedPerson = ServicesUtility.unmarshallPerson(result);

            if (loggedPerson == null) {
                Toast.makeText(ChangeProfile.this, "Something went wrong with the updating. Try again later", Toast.LENGTH_LONG).show();
            } else {
                storeLoggedUserInSharedPref(loggedPerson);
                ChangeProfile.this.finish();
            }

        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String result = "";

            HttpURLConnection urlConnection;
            // HTTP Put
            try {

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("personId", "" + this.personId)
                        .appendQueryParameter("firstname", this.firstname)
                        .appendQueryParameter("lastname", this.lastname)
                        .appendQueryParameter("birthdate", this.birthdate);


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
            }
            return result;
        }
    }

    private void storeLoggedUserInSharedPref(Person loggedPerson) {
        SharedPreferences sharedPrefLogging = this.getSharedPreferences(getString(R.string.logging_information_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefLogging.edit();
        editor.putInt(getString(R.string.logging_information_file_userid), loggedPerson.get_personId());
        editor.putString(getString(R.string.logging_information_file_userfname), loggedPerson.getFirstname());
        editor.putString(getString(R.string.logging_information_file_userlname), loggedPerson.getLastname());
        editor.putString(getString(R.string.logging_information_file_userbdate), loggedPerson.getBirthdate());
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.example.valentina.virtuallifecoach;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valentina.virtuallifecoach.model.Person;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import utility.ServicesUtility;

public class ShowProfile extends Activity {
	final String getUserPath = "getUser/";
	final String createUserPath = "newUser";
	final String deleteUserPath = "deleteUser/";
	final String checkOldestMeasurementsPath = "checkOldestMeasurements/";

	SharedPreferences sharedPrefLogging = null;

	int userId = 0;

	TextView idTv;
	TextView firstNameTv;
	TextView lastNameTv;
	TextView birthdateTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_show_profile);

		sharedPrefLogging = this.getSharedPreferences(getString(R.string.logging_information_file),
				Context.MODE_PRIVATE);

		idTv = (TextView) findViewById(R.id.idtextview_profile);
		firstNameTv = (TextView) findViewById(R.id.firstnametextview_profile);
		lastNameTv = (TextView) findViewById(R.id.lastnametextview_profile);
		birthdateTv = (TextView) findViewById(R.id.birthdatetextview_profile);

		Intent recIntent = getIntent();
		String intentType = recIntent.getStringExtra(Login.EXTRA_GETORCREATE);

		if (intentType.contentEquals("get")) {
			LoginTask lt = new LoginTask();
			lt.execute(ServicesUtility.baseURL + getUserPath + recIntent.getStringExtra(Login.EXTRA_USERID));
		} else if (intentType.contentEquals("create")) {
			String firstname = recIntent.getStringExtra(Registration.EXTRA_USERFIRSTNAME);
			String lastname = recIntent.getStringExtra(Registration.EXTRA_USERLASTNAME);
			String birthdate = recIntent.getStringExtra(Registration.EXTRA_USERBIRTHDATE);

			SignupTask sut = new SignupTask(firstname, lastname, birthdate);
			sut.execute(ServicesUtility.baseURL + createUserPath);
		} else if (intentType.contentEquals("show")) {

		} else {
			Toast.makeText(ShowProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
			System.out.println("Something went wrong: intent type not recognized");
		}

	}

	public void getMeasurementSuggestion(View view) {
		if (userId == 0) {
			userId = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);
		}
		GetMeasurementSuggestionTask gmst = new GetMeasurementSuggestionTask();
		gmst.execute(ServicesUtility.baseURL + checkOldestMeasurementsPath + userId);
	}

	public void checkGoalTrend(View view) {
		Intent intent = new Intent(this, GoalTrend.class);
		startActivity(intent);
	}

	public void modifyProfile(View view) {
		Intent intent = new Intent(this, ChangeProfile.class);
		startActivity(intent);
	}

	public void logout(View view) {
		sharedPrefLogging.edit().clear().apply();

		Intent intent = new Intent(this, Login.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	public void retrieveActiveGoals(View view) {
		Intent intent = new Intent(this, ActiveGoalsShower.class);
		startActivity(intent);
	}

	public void retrieveMeasurements(View view) {
		Intent intent = new Intent(this, MeasurementsShower.class);
		startActivity(intent);
	}

	public void checkExpiredGoals(View view) {
		Intent intent = new Intent(this, ExpiredGoalsShower.class);
		startActivity(intent);
	}

	public void retrieveHealthyRecipe(View view) {
		Intent intent = new Intent(this, HealthyRecipe.class);
		startActivity(intent);
	}

	public void deleteUser(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ShowProfile.this);
		builder.setTitle("Are you sure?");
		builder.setMessage("Do you want to permanently delete this user?");
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (userId == 0) {
					userId = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);
				}
				DeleteUserTask dut = new DeleteUserTask();
				dut.execute(ServicesUtility.baseURL + deleteUserPath + userId);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		Dialog d = builder.create();
		d.show();
	}

	// Params, progress, result
	private class LoginTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			findViewById(R.id.loggingprogressbar_profile).setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Person loggedPerson = ServicesUtility.unmarshallPerson(result);

			if (loggedPerson == null) {
				findViewById(R.id.errortextview_profile).setVisibility(View.VISIBLE);
			} else {
				storeLoggedUserInSharedPref(loggedPerson);
				showUserProfile();
			}
			findViewById(R.id.loggingprogressbar_profile).setVisibility(View.GONE);

		}

		@Override
		protected String doInBackground(String... params) {
			String urlString = params[0];// URL to call
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

	// Params, progress, result
	private class SignupTask extends AsyncTask<String, Integer, String> {
		private String firstname;
		private String lastname;
		private String birthdate;

		public SignupTask(String firstname, String lastname, String birthdate) {
			this.firstname = firstname;
			this.lastname = lastname;
			this.birthdate = birthdate;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			findViewById(R.id.loggingprogressbar_profile).setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			Person loggedPerson = ServicesUtility.unmarshallPerson(result);

			if (loggedPerson == null) {
				findViewById(R.id.errortextview_profile).setVisibility(View.VISIBLE);
			} else {
				storeLoggedUserInSharedPref(loggedPerson);
				showUserProfile();
			}
			findViewById(R.id.loggingprogressbar_profile).setVisibility(View.GONE);

		}

		@Override
		protected String doInBackground(String... params) {
			String urlString = params[0];// URL to call
			String result = "";

			HttpURLConnection urlConnection;
			// HTTP Post
			try {

				Uri.Builder builder = new Uri.Builder().appendQueryParameter("firstname", this.firstname)
						.appendQueryParameter("lastname", this.lastname)
						.appendQueryParameter("birthdate", this.birthdate);

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

	// Params, progress, result
	private class DeleteUserTask extends AsyncTask<String, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Integer responseCode) {
			super.onPostExecute(responseCode);

			if (responseCode == 200) {
				logout(null);
			} else {
				Toast.makeText(ShowProfile.this, "Something went wrong with the deleting. Try again later",
						Toast.LENGTH_LONG).show();
			}

		}

		@Override
		protected Integer doInBackground(String... params) {
			String urlString = params[0];// URL to call
			Integer responseCode;

			HttpURLConnection urlConnection;
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
			}
			return responseCode;
		}
	}

	// Params, progress, result
	private class GetMeasurementSuggestionTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			findViewById(R.id.loggingprogressbar_profile).setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			String suggestion = ServicesUtility.unmarshallSuggestion(result);

			if (suggestion == null) {
				Toast.makeText(ShowProfile.this, "Something went wrong, try again later.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ShowProfile.this, suggestion, Toast.LENGTH_LONG).show();
			}
			findViewById(R.id.loggingprogressbar_profile).setVisibility(View.GONE);

		}

		@Override
		protected String doInBackground(String... params) {
			String urlString = params[0];// URL to call
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
	protected void onResume() {
		super.onResume();
		showUserProfile();
	}

	private void showUserProfile() {
		if (userId == 0) {
			userId = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);
		}
		if (userId != 0) {
			String userFn = sharedPrefLogging.getString(getString(R.string.logging_information_file_userfname), null);
			String userLn = sharedPrefLogging.getString(getString(R.string.logging_information_file_userlname), null);
			String userBdate = sharedPrefLogging.getString(getString(R.string.logging_information_file_userbdate),
					null);

			idTv.setVisibility(View.VISIBLE);
			idTv.setText("" + userId);
			firstNameTv.setVisibility(View.VISIBLE);
			firstNameTv.setText(userFn);
			lastNameTv.setVisibility(View.VISIBLE);
			lastNameTv.setText(userLn);
			birthdateTv.setVisibility(View.VISIBLE);
			birthdateTv.setText(userBdate);

			findViewById(R.id.getactivegoalsbutton_profile).setVisibility(View.VISIBLE);
			findViewById(R.id.getmeasurementsbutton_profile).setVisibility(View.VISIBLE);
			findViewById(R.id.deleteuserbutton_profile).setVisibility(View.VISIBLE);
			findViewById(R.id.checkgoaltrendbutton_profile).setVisibility(View.VISIBLE);
			findViewById(R.id.getrandomrecipebutton_profile).setVisibility(View.VISIBLE);
			findViewById(R.id.getmeasurementsuggestionbutton_profile).setVisibility(View.VISIBLE);
			findViewById(R.id.checkexpiredgoalsbutton_profile).setVisibility(View.VISIBLE);

		}
	}

	private void storeLoggedUserInSharedPref(Person loggedPerson) {
		SharedPreferences.Editor editor = sharedPrefLogging.edit();
		editor.putInt(getString(R.string.logging_information_file_userid), loggedPerson.get_personId());
		editor.putString(getString(R.string.logging_information_file_userfname), loggedPerson.getFirstname());
		editor.putString(getString(R.string.logging_information_file_userlname), loggedPerson.getLastname());
		editor.putString(getString(R.string.logging_information_file_userbdate), loggedPerson.getBirthdate());
		editor.apply();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_show_profile, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			logout(null);
			return true;
		case R.id.action_changeprofile:
			modifyProfile(null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

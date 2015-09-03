package com.example.valentina.virtuallifecoach;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {

    public final static String EXTRA_USERID = "EXTRA_USERID";
    public final static String EXTRA_GETORCREATE = "EXTRA_GETORCREATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPrefLogging = this.getSharedPreferences(getString(R.string.logging_information_file), Context.MODE_PRIVATE);
        Integer userId = sharedPrefLogging.getInt(getString(R.string.logging_information_file_userid), 0);

        if (userId == 0) {
            this.setContentView(R.layout.layout_login);
        } else {
            Intent intent = new Intent(this, ShowProfile.class);
            intent.putExtra(EXTRA_GETORCREATE, "show");
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    public void login(View view) {
        String userId = ((EditText) findViewById(R.id.idedittext_login)).getText().toString();

        if (userId.isEmpty()) {
            Toast.makeText(Login.this, "The user identifier field cannot be empty", Toast.LENGTH_LONG).show();
        } else {

            Intent intent = new Intent(this, ShowProfile.class);
            intent.putExtra(EXTRA_USERID, userId);
            intent.putExtra(EXTRA_GETORCREATE, "get");
            startActivity(intent);
        }
    }

    public void signUp(View view) {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
    }
}

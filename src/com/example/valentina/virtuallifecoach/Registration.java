package com.example.valentina.virtuallifecoach;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Registration extends Activity {

    public final static String EXTRA_USERFIRSTNAME = "EXTRA_USERFIRSTNAME";
    public final static String EXTRA_USERLASTNAME = "EXTRA_USERLASTNAME";
    public final static String EXTRA_USERBIRTHDATE = "EXTRA_USERBIRTHDATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_registration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
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

    public void signUp(View view) {
        String userFirstname = ((EditText) findViewById(R.id.firstnameedittext_registration)).getText().toString();
        String userLastname = ((EditText) findViewById(R.id.lastnameedittext_registration)).getText().toString();
        String userBirthdate = ((EditText) findViewById(R.id.birthdateedittext_registration)).getText().toString();

        if(userFirstname.isEmpty() || userLastname.isEmpty()){
            Toast.makeText(Registration.this, "Firstname and Lastname cannot be empty", Toast.LENGTH_LONG).show();
        } else {

            Intent intent = new Intent(this, ShowProfile.class);
            intent.putExtra(Login.EXTRA_GETORCREATE, "create");
            intent.putExtra(EXTRA_USERFIRSTNAME, userFirstname);
            intent.putExtra(EXTRA_USERLASTNAME, userLastname);
            intent.putExtra(EXTRA_USERBIRTHDATE, userBirthdate);
            startActivity(intent);
        }
    }
}

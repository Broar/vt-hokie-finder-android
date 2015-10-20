package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

/**
 * Activity that allows users to login to the application
 *
 * @author Steven Briggs
 * @version 2015.10.18
 */
public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // Skip login procedures if a current user already exists
        if (ParseUser.getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        // Start login procedures using the ParseUI library
        else {
            ParseLoginBuilder builder = new ParseLoginBuilder(this);
            //startActivityForResult(builder.setAppLogo(R.drawable.fighting_gobblers_medium).build(), 0);
            startActivityForResult(builder.build(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the user successfully logged in, then transition to main screen
        if (ParseUser.getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
        }

        finish();
    }
}

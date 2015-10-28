package com.parseapp.vthokiefinder;


import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An activity that allows the user to configure their personal settings
 *
 * @author Steven Briggs
 * @version 2015.10.24
 */
public class SettingsActivity extends AppCompatActivity {

    private LoginButton mFacebookLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializeToolbar();
        initializeFacebookLogin();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup the Toolbar as the ActionBar
     */
    public void initializeToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Setup the button to link / unlink a user's account with Facebook
     */
    public void initializeFacebookLogin() {
        mFacebookLogin = (LoginButton) findViewById(R.id.facebookLogin);

        // Setup alternative text for users whose Facebook accounts are already linked
        if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
            mFacebookLogin.setText("Unlink account with Facebook");
        }

        // Setup the onClickListener for linking / unlinking user accounts with Facebook
        mFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Unlink the user's Facebook account
                if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
                    ParseFacebookUtils.unlinkInBackground(ParseUser.getCurrentUser(), new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                mFacebookLogin.setText("Link account with Facebook");
                            }

                            else {
                                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // Link the user's Facebook account
                else {
                    List<String> permissions = Arrays.asList("email");

                    ParseFacebookUtils.linkWithReadPermissionsInBackground(ParseUser.getCurrentUser(),
                            SettingsActivity.this, permissions, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                mFacebookLogin.setText("Unlink account with Facebook");
                            }

                            else {
                                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}

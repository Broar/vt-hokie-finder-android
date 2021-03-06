package com.parseapp.vthokiefinder.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;
import com.parseapp.vthokiefinder.HomeActivity;
import com.parseapp.vthokiefinder.R;

import org.json.JSONObject;

/**
 * Activity that allows users to login to the application
 *
 * @author Steven Briggs
 * @version 2015.10.18
 */
public class LoginActivity extends AppCompatActivity implements GraphRequest.GraphJSONObjectCallback {


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
            startActivityForResult(builder.build(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the user successfully logged in, then transition to main screen
        if (resultCode == RESULT_OK) {

            // Handle users that just signed up through Facebook
            if (ParseUser.getCurrentUser().isNew() && ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
                handleNewFacebookUser();
            }

            // Handle normal users
            else {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }
        }
    }

    /**
     * Handle the case when a new user signs up through their Facebook account
     */
    private void handleNewFacebookUser() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), this);
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
        // Configure the user's account to have their username and email correspond to their
        // Facebook name and email, respectively
        ParseUser.getCurrentUser().setUsername(jsonObject.optString("name"));

        if (jsonObject.has("email")) {
            ParseUser.getCurrentUser().setEmail(jsonObject.optString("email"));
        }

        if (jsonObject.has("id")) {
            ParseUser.getCurrentUser().put("facebookId", jsonObject.optString("id"));
        }

        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                }

                else {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

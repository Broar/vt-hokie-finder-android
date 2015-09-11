package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Activity that allows users to login to the application
 *
 * @author Steven Briggs
 * @version 2015.09.10
 */
public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView newUserTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = (EditText) findViewById(R.id.usernameLoginEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordLoginEditText);
        loginButton = (Button) findViewById(R.id.loginButton);
        newUserTextView = (TextView) findViewById(R.id.newUserTextView);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Perform a login attempt. Move to the application's homepage if successful
                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }

                        else {
                            Toast.makeText(LoginActivity.this, "Invalid username and/or password",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        newUserTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }
}

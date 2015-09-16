package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Activity that allows a user to register themselves for the application
 *
 * @author Steven Briggs
 * @version 2015.09.11
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;
    private EditText mPasswordConfirmation;
    private EditText mEmail;
    private Button mRegister;
    private TextView mBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mPasswordConfirmation = (EditText) findViewById(R.id.passwordConfirmation);
        mEmail = (EditText) findViewById(R.id.email);
        mRegister = (Button) findViewById(R.id.register);
        mBackToLogin = (TextView) findViewById(R.id.backToLogin);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String passwordConfirm = mPasswordConfirmation.getText().toString().trim();
                String email = mEmail.getText().toString().trim();

                // Perform field validation. Return early if there is an issue
                if (!verifyFields(username, password, passwordConfirm, email)) {
                    return;
                }

                // Create a new ParseUser. Attempt to sign them up for the application
                ParseUser user = new ParseUser();
                user.setUsername(username);
                user.setPassword(password);
                user.setEmail(email);

                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        // Successful sign up. Transition to application's main activity
                        if (e == null) {
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                        }

                        // Something went wrong! Communicate the error to the user
                        else {
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        // Transition the user back to the login screen
        mBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Verify the registration fields: username, password, and email.
     *
     * @param username the username
     * @param password the password
     * @param passwordConfirm the password confirmation
     * @param email the email
     * @return true if all the fields were validated, false if not
     */
    private boolean verifyFields(String username, String password, String passwordConfirm, String email) {
        return verifyUsername(username) && verifyPassword(password, passwordConfirm) && verifyEmail(email);
    }

    /**
     * Determine if the username is set correctly
     *
     * @param username the username to verify
     * @return true if the username is correct, false if not
     */
    private boolean verifyUsername(String username) {
        if (username.isEmpty()) {
            mUsername.setError("Enter your username!");
            return false;
        }

        else {
            return true;
        }
    }

    /**
     * Determine whether the password and password confirmation match
     *
     * @param password the password of the user
     * @param passwordConfirm the password confirmation of the user
     * @return true if the passwords match, false if not
     */
    private boolean verifyPassword(String password, String passwordConfirm) {
        if (password.isEmpty() || passwordConfirm.isEmpty()) {
            mPassword.setError("Enter your password!");
            mPasswordConfirmation.setError("Confirm your password!");
            return false;
        }

        else if (!password.equals(passwordConfirm)) {
            mPassword.setError("Passwords didn't match!");
            mPasswordConfirmation.setError("Passwords didn't match!");
            return false;
        }

        else {
            return true;
        }
    }

    /**
     * Determine if the specified email is an official Virginia Tech email
     *
     * @param email the email to verify
     * @return true if the email is a VT email, false if not
     */
    private boolean verifyEmail(String email) {
        if (email.isEmpty()) {
            mEmail.setError("Enter your email!");
            return false;
        }

        else if (!email.contains("@vt.edu")) {
            mEmail.setError("Email must be @vt.edu");
            return false;
        }

        else {
            return true;
        }
    }
}

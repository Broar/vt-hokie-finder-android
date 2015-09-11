package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
 * @version 2015.09.09
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String INVALID_FIELD_COLOR = "#80F44336";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;
    private EditText emailEditText;
    private Button registerButton;
    private TextView signInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = (EditText) findViewById(R.id.usernameRegisterEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordRegisterEditText);
        passwordConfirmEditText = (EditText) findViewById(R.id.passwordConfirmRegisterEditText);
        emailEditText = (EditText) findViewById(R.id.emailRegisterEditText);
        registerButton = (Button) findViewById(R.id.registerButton);
        signInTextView = (TextView) findViewById(R.id.signInTextView);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String passwordConfirm = passwordConfirmEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                // Perform field validation. Return early if there is an issue
                if (!verifyPassword(password, passwordConfirm) || !verifyEmail(email)) {
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
        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Determine whether the password and password confirmation match
     *
     * @param password the password of the user
     * @param passwordConfirm the password confirmation of the user
     * @return true if the passwords match, false if not
     */
    private boolean verifyPassword(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
            passwordEditText.setBackgroundColor(Color.parseColor(INVALID_FIELD_COLOR));
            passwordConfirmEditText.setBackgroundColor(Color.parseColor(INVALID_FIELD_COLOR));
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
        if (!email.contains("@vt.edu")) {
            Toast.makeText(this, "Email must be @vt.edu", Toast.LENGTH_SHORT).show();
            emailEditText.setBackgroundColor(Color.parseColor(INVALID_FIELD_COLOR));
            return false;
        }

        else {
            return true;
        }
    }
}

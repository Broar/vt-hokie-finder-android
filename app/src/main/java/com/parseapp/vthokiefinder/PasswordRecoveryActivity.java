package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

/**
 * An activity that allows a user to submit their email address and receive
 * a password recovery email from Parse
 *
 * @author Steven Briggs
 * @version 2015.09.17
 */
public class PasswordRecoveryActivity extends AppCompatActivity {

    private EditText mRecoveryEmail;
    private Button mSubmitRecovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mRecoveryEmail = (EditText) findViewById(R.id.recoveryEmail);
        mSubmitRecovery = (Button) findViewById(R.id.submitRecovery);
        mSubmitRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recoveryEmail = mRecoveryEmail.getText().toString().trim();
                ParseUser.requestPasswordResetInBackground(recoveryEmail, new RequestPasswordResetCallback() {
                    @Override
                    public void done(ParseException e) {
                        // Success! Inform the user a recovery email is on the way
                        if (e == null) {
                            Toast.makeText(PasswordRecoveryActivity.this, "Recovery email sent!", Toast.LENGTH_LONG).show();
                        }

                        // Failure! Inform the user about what went wrong
                        else {
                            Toast.makeText(PasswordRecoveryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}

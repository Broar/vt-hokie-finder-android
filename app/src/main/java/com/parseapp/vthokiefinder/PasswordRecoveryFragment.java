package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import java.util.regex.PatternSyntaxException;

/**
 * A fragment that allows users to recovery their passwords via email
 *
 * @author Steven Briggs
 * @version 2015.09.21
 */
public class PasswordRecoveryFragment extends Fragment {

    public static final String TAG = PasswordRecoveryFragment.class.getSimpleName();

    private EditText mRecoveryEmail;
    private Button mSubmitRecovery;

    /**
     * A factory method to return a new PasswordRecoveryFragment that has been configured
     *
     * @return a new PasswordRecoveryFragment that has been configured
     */
    public static PasswordRecoveryFragment newInstance() {
        return new PasswordRecoveryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_recovery, container, false);
        mRecoveryEmail = (EditText) view.findViewById(R.id.recoveryEmail);
        mSubmitRecovery = (Button) view.findViewById(R.id.submitRecovery);

        mSubmitRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recoveryEmail = mRecoveryEmail.getText().toString().trim();
                ParseUser.requestPasswordResetInBackground(recoveryEmail, new RequestPasswordResetCallback() {
                    @Override
                    public void done(ParseException e) {
                        // Success! Inform the user a recovery email is on the way
                        if (e == null) {
                            Toast.makeText(getActivity(), "Recovery email sent!", Toast.LENGTH_LONG).show();
                        }

                        // Failure! Inform the user about what went wrong
                        else {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        return view;
    }
}

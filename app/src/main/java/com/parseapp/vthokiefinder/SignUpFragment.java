package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * A fragment that allows the user to create a new account
 *
 * @author Steven Briggs
 * @version 2015.09.21
 */
public class SignUpFragment extends Fragment {

    public static final String TAG = SignUpFragment.class.getSimpleName();

    private Callbacks mListener;

    private EditText mUsername;
    private EditText mPassword;
    private EditText mPasswordConfirmation;
    private EditText mEmail;
    private Button mRegister;
    private TextView mBackToLogin;

    public interface Callbacks {
        void onBackToLoginClicked();
    }

    /**
     * A factory method to return a new SignUpFragment that has been configured
     *
     * @return a new SignUpFragment that has been configured
     */
    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            mListener = (Callbacks) activity;
        }

        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        mUsername = (EditText) view.findViewById(R.id.username);
        mPassword = (EditText) view.findViewById(R.id.password);
        mPasswordConfirmation = (EditText) view.findViewById(R.id.passwordConfirmation);
        mEmail = (EditText) view.findViewById(R.id.email);
        mRegister = (Button) view.findViewById(R.id.register);
        mBackToLogin = (TextView) view.findViewById(R.id.backToLogin);

        initializeRegister();
        initializeBackToLogin();

        return view;
    }

    /**
     * Setup the Register button
     */
    private void initializeRegister() {
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    /**
     * Setup the Back to Login link
     */
    private void initializeBackToLogin() {
        mBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onBackToLoginClicked();
            }
        });
    }

    /**
     * Register a new user for the application on the Parse backend
     */
    private void registerUser() {
        String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String passwordConfirm = mPasswordConfirmation.getText().toString().trim();
        String email = mEmail.getText().toString().trim();

        // Verify passwords are matching
        if (!password.equals(passwordConfirm)) {
            mPassword.setError("Passwords do not match");
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
                    startActivity(new Intent(getActivity(), HomeActivity.class));
                    getActivity().finish();
                }

                // Something went wrong! Communicate the error to the user
                else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

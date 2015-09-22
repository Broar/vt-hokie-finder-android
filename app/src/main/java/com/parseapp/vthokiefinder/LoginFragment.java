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

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * A fragment that allows the user to login to their account
 *
 * @author Steven Briggs
 * @version 2015.09.21
 */
public class LoginFragment extends Fragment {
    public static final String TAG = LoginFragment.class.getSimpleName();

    private Callbacks mListener;

    private EditText mUsername;
    private EditText mPassword;
    private Button mLogin;
    private Button mSignUp;
    private TextView mForgotPassword;

    public interface Callbacks {
        void onSignUpClicked();
        void onForgotPasswordClicked();
    }

    /**
     * A factory method to return a new LoginFragment that has been configured
     *
     * @return a new LoginFragment that has been configured
     */
    public static LoginFragment newInstance() {
        return new LoginFragment();
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mUsername = (EditText) view.findViewById(R.id.username);
        mPassword = (EditText) view.findViewById(R.id.password);
        mLogin = (Button) view.findViewById(R.id.login);
        mSignUp = (Button) view.findViewById(R.id.signUp);
        mForgotPassword = (TextView) view.findViewById(R.id.forgotPassword);

        initializeLogin();
        initializeSignUp();
        initializeForgotPassword();

        return view;
    }

    /**
     * Setup the Login button
     */
    private void initializeLogin() {
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                // Perform a login attempt. Move to the application's homepage if successful
                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {
                            startActivity(new Intent(getActivity(), HomeActivity.class));
                            getActivity().finish();
                        } else {
                            mUsername.setError("Username and password not found!");
                            mPassword.setError("Username and password not found!");
                        }
                    }
                });
            }
        });
    }

    /**
     * Setup the Sign Up button
     */
    private void initializeSignUp() {
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSignUpClicked();
            }
        });
    }

    /**
     * Setup the Forgot Password link
     */
    private void initializeForgotPassword() {
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onForgotPasswordClicked();
            }
        });
    }
}

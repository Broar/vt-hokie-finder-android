package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.parse.ParseUser;

/**
 * Activity that allows users to login to the application
 *
 * @author Steven Briggs
 * @version 2015.09.21
 */
public class LoginActivity extends AppCompatActivity implements
        LoginFragment.Callbacks,
        SignUpFragment.Callbacks {

    private LoginFragment mLoginFragment;
    private SignUpFragment mSignUpFragment;
    private PasswordRecoveryFragment mPasswordRecoveryFragment;

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

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            mLoginFragment = LoginFragment.newInstance();
            mSignUpFragment = SignUpFragment.newInstance();
            mPasswordRecoveryFragment = PasswordRecoveryFragment.newInstance();

            fm.beginTransaction()
                    .add(R.id.fragmentContainer, mLoginFragment, LoginFragment.TAG)
                    .commit();
        }

        else {
            mLoginFragment = (LoginFragment) fm.findFragmentByTag(LoginFragment.TAG);
            mSignUpFragment = (SignUpFragment) fm.findFragmentByTag(SignUpFragment.TAG);
            mPasswordRecoveryFragment = (PasswordRecoveryFragment) fm.findFragmentByTag(PasswordRecoveryFragment.TAG);
        }
    }

    @Override
    public void onSignUpClicked() {
        if (mSignUpFragment == null) {
            mSignUpFragment = SignUpFragment.newInstance();
        }

        replaceFragment(mSignUpFragment, SignUpFragment.TAG);
    }

    @Override
    public void onForgotPasswordClicked() {
        if (mPasswordRecoveryFragment == null) {
            mPasswordRecoveryFragment = PasswordRecoveryFragment.newInstance();
        }

        replaceFragment(mPasswordRecoveryFragment, PasswordRecoveryFragment.TAG);
    }
    @Override
    public void onBackToLoginClicked() {
        if (mLoginFragment == null) {
            mLoginFragment = LoginFragment.newInstance();
        }

        replaceFragment(mLoginFragment, LoginFragment.TAG);
    }

    /**
     * Replace the existing fragment with the specified one
     *
     * @param fragment the fragment to be shown
     * @param tag the fragment's associated tag
     */
    private void replaceFragment(Fragment fragment, String tag) {
        // LoginFragment is the lowest level fragment in the Activity, so only need to pop a
        // fragment off to show return to it
        if (tag.equals(LoginFragment.TAG)) {
            getSupportFragmentManager().popBackStack();
        }

        // Just replace the existing fragment
        else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, tag)
                    .addToBackStack(null)
                    .commit();
        }
    }
}

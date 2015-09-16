package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * An activity that acts as the applications "homepage". Provides the user with
 * an interface to access the main features of HokieFinder.
 *
 * @author Steven Briggs
 * @version 2015.09.15
 */
public class HomeActivity extends AppCompatActivity {

    private CirclesFragment mCirclesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FragmentManager fm = getSupportFragmentManager();

        // This is first time the activity has been created, so create all the fragments
        if (savedInstanceState == null) {
            mCirclesFragment = CirclesFragment.newInstance();
        }

        // There was an configuration change, so just reclaim any fragments
        else {
            mCirclesFragment = (CirclesFragment) fm.findFragmentByTag(CirclesFragment.TAG);
        }

        fm.beginTransaction().replace(R.id.fragmentContainer, mCirclesFragment, CirclesFragment.TAG).commit();
    }
}

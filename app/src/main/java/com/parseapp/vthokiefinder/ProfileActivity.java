package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * An activity that manages display a user's profile
 *
 * @author Steven Briggs
 * @version 2015.11.03
 */
public class ProfileActivity extends AppCompatActivity {

    public static final String USER_ID_KEY = "userId";

    private ProfileFragment mProfileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            mProfileFragment = ProfileFragment.newInstance(getIntent().getStringExtra(USER_ID_KEY));
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, mProfileFragment, ProfileFragment.TAG)
                    .commit();
        }

        else {
            mProfileFragment = (ProfileFragment) fm.findFragmentByTag(ProfileFragment.TAG);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

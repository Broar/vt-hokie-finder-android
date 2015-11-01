package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * An activity that allows the user to discover new friends
 *
 * @author Steven Briggs
 * @version 2015.10.31
 */
public class FindFriendsActivity extends AppCompatActivity {

    private FindFriendsFragment mFindFriendsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        FragmentManager fm = getSupportFragmentManager();

        // Create new instances of fragments
        if (savedInstanceState == null) {
            mFindFriendsFragment = FindFriendsFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, mFindFriendsFragment, FindFriendsFragment.TAG)
                    .commit();
        }

        // Retrieve all the existing fragments
        else {
            mFindFriendsFragment = (FindFriendsFragment) fm.findFragmentByTag(FindFriendsFragment.TAG);
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }
}

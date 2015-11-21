package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * An activity that displays the details pages of circles and users
 *
 * @author Steven Briggs
 * @version 2015.11.13
 */
public class DetailActivity extends AppCompatActivity implements
        CircleDetailFragment.Callbacks,
        ProfileFragment.Callbacks,
        CirclesFragment.Callbacks,
        FriendsFragment.Callbacks {

    public static final String CIRCLE_ID_KEY = "circleId";
    public static final String USER_ID_KEY = "userId";

    private ProfileFragment mProfileFragment;
    private CircleDetailFragment mCircleDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final FragmentManager fm = getSupportFragmentManager();

        // Create a new instance of the initial fragment to display
        if (savedInstanceState == null) {
            final String circleId = getIntent().getStringExtra(CIRCLE_ID_KEY);
            final String userId = getIntent().getStringExtra(USER_ID_KEY);

            // Determine which detail view to display (circle or user)
            if (circleId == null) {
                mProfileFragment = ProfileFragment.newInstance(userId);
                fm.beginTransaction()
                        .add(R.id.fragment_container, mProfileFragment, ProfileFragment.TAG)
                        .addToBackStack(null)
                        .commit();
            }

            else {
                mCircleDetailFragment = CircleDetailFragment.newInstance(circleId);
                fm.beginTransaction()
                        .add(R.id.fragment_container, mCircleDetailFragment, CircleDetailFragment.TAG)
                        .addToBackStack(null)
                        .commit();
            }
        }

        // Retrieve any existing fragments
        else {
            mProfileFragment = (ProfileFragment) fm.findFragmentByTag(ProfileFragment.TAG);
            mCircleDetailFragment = (CircleDetailFragment) fm.findFragmentByTag(CircleDetailFragment.TAG);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStackImmediate();
        }

        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMemberClicked(ParseUser member) {
        mProfileFragment = ProfileFragment.newInstance(member.getObjectId());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mProfileFragment, ProfileFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCircleClicked(Circle circle) {
        mCircleDetailFragment = CircleDetailFragment.newInstance(circle.getObjectId());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mCircleDetailFragment, CircleDetailFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFriendClicked(ParseUser friend) {
        mProfileFragment = ProfileFragment.newInstance(friend.getObjectId());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mProfileFragment, ProfileFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCircleDestroyed() {
        onBackPressed();
    }

    @Override
    public void onHomeClicked() {
        // Clean up the backstack before finishing the activity to avoid memory leaks
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        finish();
    }
}

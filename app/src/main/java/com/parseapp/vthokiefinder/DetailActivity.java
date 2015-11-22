package com.parseapp.vthokiefinder;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
        FriendsFragment.Callbacks,
        EditCircleFragment.Callbacks,
        ConfirmDialog.Callbacks {

    public static final String CIRCLE_ID_KEY = "circleId";
    public static final String USER_ID_KEY = "userId";

    private ProfileFragment mProfileFragment;
    private CircleDetailFragment mCircleDetailFragment;
    private EditCircleFragment mEditCircleFragment;

    private RetainedFragment<Uri> mImageUriHolder;

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

            // Retained fragment for holding an image URI during circle editing
            mImageUriHolder = new RetainedFragment<Uri>();
            fm.beginTransaction()
                    .add(mImageUriHolder, RetainedFragment.TAG)
                    .commit();
        }

        // Retrieve any existing fragments
        else {
            mProfileFragment = (ProfileFragment) fm.findFragmentByTag(ProfileFragment.TAG);
            mCircleDetailFragment = (CircleDetailFragment) fm.findFragmentByTag(CircleDetailFragment.TAG);
            mEditCircleFragment = (EditCircleFragment) fm.findFragmentByTag(EditCircleFragment.TAG);
            mImageUriHolder = (RetainedFragment<Uri>) fm.findFragmentByTag(RetainedFragment.TAG);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 1) {

            // Clear whatever URI the retained fragment is holding
            if (mEditCircleFragment != null && mEditCircleFragment.isVisible()) {
                mImageUriHolder.setData(null);
            }

            fm.popBackStackImmediate();
        }

        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMemberClicked(ParseUser member) {
        mProfileFragment = ProfileFragment.newInstance(member.getObjectId());
        replaceFragment(mProfileFragment, ProfileFragment.TAG);
    }

    @Override
    public void onEditClicked(Circle circle) {
        mEditCircleFragment = EditCircleFragment.newInstance(circle.getObjectId());
        replaceFragment(mEditCircleFragment, EditCircleFragment.TAG);
    }

    @Override
    public void onCircleClicked(Circle circle) {
        mCircleDetailFragment = CircleDetailFragment.newInstance(circle.getObjectId());
        replaceFragment(mCircleDetailFragment, CircleDetailFragment.TAG);
    }

    @Override
    public void onFriendClicked(ParseUser friend) {
        mProfileFragment = ProfileFragment.newInstance(friend.getObjectId());
        replaceFragment(mProfileFragment, ProfileFragment.TAG);
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

    /**
     * Replace the existing fragment with a specified one
     *
     * @param fragment the fragment to replace the current one with
     * @param tag the tag to associate with the fragment
     */
    private void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onImageSet(Uri imageUri) {
        mImageUriHolder.setData(imageUri);
    }

    @Override
    public Uri onImageUriRequested() {
        return mImageUriHolder.getData();
    }

    @Override
    public void onSaveSuccessful() {
        mImageUriHolder.setData(null);
        getSupportFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onPositiveButtonClicked() {
        // Handle the user confirming they want to discard their edits to a circle
        mImageUriHolder.setData(null);
        getSupportFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onNegativeButtonClicked() {
        // Do nothing
    }
}

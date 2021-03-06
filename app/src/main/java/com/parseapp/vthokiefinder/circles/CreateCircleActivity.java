package com.parseapp.vthokiefinder.circles;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parseapp.vthokiefinder.model.Circle;
import com.parseapp.vthokiefinder.model.UserCircle;
import com.parseapp.vthokiefinder.utils.GoogleApiManagerFragment;
import com.parseapp.vthokiefinder.invites.InviteFriendsFragment;
import com.parseapp.vthokiefinder.R;
import com.parseapp.vthokiefinder.common.RetainedFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An activity allowing the user to create a new circle
 *
 * @author Steven Briggs
 * @version 2015.12.04
 */
public class CreateCircleActivity extends AppCompatActivity implements
        CreateCircleFragment.Callbacks,
        GoogleApiManagerFragment.Callbacks,
        InviteFriendsFragment.Callbacks {

    private static final String URI_TAG = "uri";
    private static final String INVITED_FRIENDS_TAG = "invitedFriends";
    private static final String IS_INVITING_KEY = "isInviting";

    private boolean mIsInviting;

    private CreateCircleFragment mCreateCircleFragment;
    private GoogleApiManagerFragment mGoogleApiManagerFragment;
    private InviteFriendsFragment mInviteFriendsFragment;
    private RetainedFragment<Uri> mImageUriHolder;
    private RetainedFragment<HashMap<ParseUser, Boolean>> mInvitedFriendsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_circle);

        FragmentManager fm = getSupportFragmentManager();

        // Create new instances of the fragments for circle creation
        if (savedInstanceState == null) {
            mCreateCircleFragment = CreateCircleFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragment_container, mCreateCircleFragment, CreateCircleFragment.TAG)
                    .commit();

            mGoogleApiManagerFragment = GoogleApiManagerFragment.newInstance();
            fm.beginTransaction()
                    .add(mGoogleApiManagerFragment, GoogleApiManagerFragment.TAG)
                    .commit();

            mImageUriHolder = new RetainedFragment<Uri>();
            fm.beginTransaction()
                    .add(mImageUriHolder, URI_TAG)
                    .commit();

            mInvitedFriendsHolder = new RetainedFragment<HashMap<ParseUser, Boolean>>();
            fm.beginTransaction()
                    .add(mInvitedFriendsHolder, INVITED_FRIENDS_TAG)
                    .commit();

            mInvitedFriendsHolder.setData(new HashMap<ParseUser, Boolean>());
        }

        // Restore the existing fragment instances and activity state
        else {
            mIsInviting = savedInstanceState.getBoolean(IS_INVITING_KEY);
            mCreateCircleFragment = (CreateCircleFragment) fm.findFragmentByTag(CreateCircleFragment.TAG);
            mGoogleApiManagerFragment = (GoogleApiManagerFragment) fm.findFragmentByTag(GoogleApiManagerFragment.TAG);
            mImageUriHolder = (RetainedFragment<Uri>) fm.findFragmentByTag(URI_TAG);
            mInvitedFriendsHolder = (RetainedFragment<HashMap<ParseUser, Boolean>>) fm.findFragmentByTag(INVITED_FRIENDS_TAG);
            mInviteFriendsFragment = (InviteFriendsFragment) fm.findFragmentByTag(InviteFriendsFragment.TAG);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_INVITING_KEY, mIsInviting);
    }

    @Override
    public void onBackPressed() {
        if (mIsInviting) {
            mIsInviting = false;
            super.onBackPressed();
        }

        else {
            showDiscardDialog();
        }
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
    public void onInviteFriendsClicked() {
        mIsInviting = true;

        if (mInviteFriendsFragment == null) {
            mInviteFriendsFragment = InviteFriendsFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mInviteFriendsFragment, InviteFriendsFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSaveSuccessful(Circle circle) {
        sendInvites(circle);
    }

    @Override
    public void onCurrentLocationRequested(GoogleApiManagerFragment.OnLocationFoundListener listener) {
        mGoogleApiManagerFragment.getCurrentLocation(listener);
    }

    @Override
    public void onClientConnected() {
        // Do nothing
    }

    @Override
    public void onInviteClicked(ParseUser friend, boolean isInvited) {
        mInvitedFriendsHolder.getData().put(friend, isInvited);
    }

    @Override
    public boolean onIsInvitedRequested(ParseUser friend) {
        if (mInvitedFriendsHolder.getData().containsKey(friend)) {
            return mInvitedFriendsHolder.getData().get(friend);
        }

        return false;
    }

    private void sendInvites(final Circle circle) {
        HashMap<ParseUser, Boolean> invitedFriends = mInvitedFriendsHolder.getData();

        // Determine if the user decided to send any invitations
        if (!invitedFriends.isEmpty()) {
            List<UserCircle> invites = new ArrayList<UserCircle>();

            // Create invitations for all of the friends the user selected
            for (ParseUser friend : invitedFriends.keySet()) {
                if (invitedFriends.get(friend)) {
                    UserCircle invite = new UserCircle();
                    invite.setUser(friend);
                    invite.setCircle(circle);
                    invite.setIsBroadcasting(false);
                    invite.setIsPending(true);
                    invite.setIsAccepted(false);
                    invite.setFriend(ParseUser.getCurrentUser());
                    invite.setIsInvite(true);

                    invites.add(invite);
                }
            }

            // Send the invitations out to the user's friends
            UserCircle.saveAllInBackground(invites, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(CreateCircleActivity.this, "Couldn't send invites!", Toast.LENGTH_LONG).show();
                    }

                    // Open the detail view of the newly created circle
                    finish();
                    Intent intent = new Intent(CreateCircleActivity.this, DetailActivity.class);
                    intent.putExtra(DetailActivity.CIRCLE_ID_KEY, circle.getObjectId());
                    startActivity(intent);
                }
            });
        }

        else {
            // Open the detail view of the newly created circle
            finish();
            Intent intent = new Intent(CreateCircleActivity.this, DetailActivity.class);
            intent.putExtra(DetailActivity.CIRCLE_ID_KEY, circle.getObjectId());
            startActivity(intent);
        }
    }

    /**
     * Display a dialog prompting users to confirm they want exit and discard all changes
     */
    private void showDiscardDialog() {
        if (!mIsInviting) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Discard new circle?")
                    .setCancelable(true)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NavUtils.navigateUpFromSameTask(CreateCircleActivity.this);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    });

            builder.create().show();
        }
    }
}

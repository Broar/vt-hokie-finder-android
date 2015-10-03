package com.parseapp.vthokiefinder;

import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseUser;
import com.parseapp.vthokiefinder.widgets.SlidingTabLayout;

/**
 * An activity that acts as the applications "homepage". Provides the user with
 * an interface to access the main features of HokieFinder.
 *
 * @author Steven Briggs
 * @version 2015.10.03
 */
public class HomeActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        CircleListFragment.Callbacks,
        CircleMapFragment.Callbacks {

    private static final int BROADCAST_NOTIFICATION_ID = 0;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolvingError;

    private PendingIntent mBroadcastIntent;
    private LocationRequest mLocationRequest;
    private boolean mIsBroadcasting;

    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;

    private HomeFragment mHomeFragment;
    private CircleDetailFragment mCircleDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mIsResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        FragmentManager fm = getSupportFragmentManager();

        // Create new Fragments for the Activity
        if (savedInstanceState == null) {
            mHomeFragment = HomeFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, mHomeFragment, HomeFragment.TAG)
                    .commit();
        }

        // Retrieve existing Fragments
        else {
            mHomeFragment = (HomeFragment) fm.findFragmentByTag(HomeFragment.TAG);
            mCircleDetailFragment = (CircleDetailFragment) fm.findFragmentByTag(CircleDetailFragment.TAG);
        }

        // Initialize all elements of the Activity
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        buildGoogleApiClient();
        initializeBroadcast();
        initializeSupportActionBar();
        initializeDrawerLayout();
        initializeFloatingActionButton();
    }

    /**
     * Replace the existing fragment with the specified one
     *
     * @param fragment the fragment to be shown
     * @param tag the fragment's associated tag
     */
    private void replaceFragment(Fragment fragment, String tag) {
        // LoginFragment is the lowest level fragment in the Activity, so only need to pop a
        // fragment off to return to it
        if (tag.equals(HomeFragment.TAG)) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_broadcast:
                broadcastLocation();
                return true;
            case R.id.action_logout:
                logout();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mIsResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mIsResolvingError = false;

            // Make sure the app is not already connected or attempting to connect
            if (resultCode == RESULT_OK) {
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mIsResolvingError);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Space purposefully left empty
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Space purposefully left empty
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Already handling the error
        if (mIsResolvingError) {
            return;
        }

        else if (result.hasResolution()) {
            try {
                mIsResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            }

            // There was an error. Try again
            catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        }

        // Show dialog using GoogleApiAvailability.getErrorDialog()
        else {
            showErrorDialog(result.getErrorCode());
            mIsResolvingError = true;
        }
    }

    @Override
    public void onCircleClicked(Circle circle, boolean isMember) {
        mCircleDetailFragment = CircleDetailFragment.newInstance(circle, isMember);
        replaceFragment(mCircleDetailFragment, CircleDetailFragment.TAG);
    }

    @Override
    public GoogleApiClient requestGoogleApi() {
        return mGoogleApiClient;
    }

    /**
     * Switch the user's public location broadcast on/off
     */
    private void broadcastLocation() {
        // Switch broadcasting on
        if (mGoogleApiClient.isConnected() && !mIsBroadcasting) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mBroadcastIntent);
            issueNotification();
            mIsBroadcasting = true;

            Snackbar.make(mCoordinatorLayout, "Public broadcast on!", Snackbar.LENGTH_LONG).show();
        }

        // Switch broadcasting off and cancel the ongoing notification
        else if (mGoogleApiClient.isConnected() && mIsBroadcasting) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mBroadcastIntent);
            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.cancel(BROADCAST_NOTIFICATION_ID);
            mIsBroadcasting = false;

            Snackbar.make(mCoordinatorLayout, "Public broadcast off!", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Issue a status notification that the user is broadcasting their location
     */
    private void issueNotification() {
        // The activity should be brought back to the front if it already exists
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent openHomeIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build a sticky notification to inform the user they are broadcasting
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_fighting_gobbler)
                .setContentTitle("Location broadcast")
                .setContentText("Your location is being broadcast to circles")
                .setContentIntent(openHomeIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(BROADCAST_NOTIFICATION_ID, notification);
    }

    /**
     * Log the current user out of the application
     */
    private void logout() {
        // Ensure the user is not broadcasting their location after logout
        if (mGoogleApiClient.isConnected() && mIsBroadcasting) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mBroadcastIntent);
        }

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.cancel(BROADCAST_NOTIFICATION_ID);
        ParseUser.logOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /**
     * Setup the location broadcasting.
     */
    private void initializeBroadcast() {
        Intent intent = new Intent(this, LocationPushService.class);
        mBroadcastIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mLocationRequest = LocationRequest.create()
                .setInterval(10000L)
                .setFastestInterval(5000L)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mIsBroadcasting = false;
    }

    /**
     * Build a new instance of the GoogleApiClient.
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Setup the SupportActionBar for this screen
     */
    private void initializeSupportActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Setup the DrawerLayout for this screen
     */
    private void initializeDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    /**
     * Setup the FloatingActionButton to transition to a "Create Circle" screen
     */
    private void initializeFloatingActionButton() {
        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(mCoordinatorLayout, "Clicked FAB!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Display an ErrorDialogFragment
     *
     * https://developers.google.com/android/guides/api-client#handle_connection_failures
     *
     * @param errorCode the error code that prompted the dialog
     */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /**
     * Callback method when an ErrorDialogFragment is dismissed
     *
     * https://developers.google.com/android/guides/api-client#handle_connection_failures
     */
    public void onDialogDismissed() {
        mIsResolvingError = false;
    }

    /**
     * A DialogFragment to prompt the user to update Google Play Services
     *
     * https://developers.google.com/android/guides/api-client#handle_connection_failures
     */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((HomeActivity) getActivity()).onDialogDismissed();
        }
    }
}

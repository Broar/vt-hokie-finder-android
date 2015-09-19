package com.parseapp.vthokiefinder;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
 * @version 2015.09.15
 */
public class HomeActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int NUM_OF_TABS = 2;
    private static final CharSequence[] TAB_TITLES = { "MY CIRCLES", "CIRCLES" };

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mBroadcastIntent;
    private LocationRequest mLocationRequest;
    private boolean mIsBroadcasting;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private SlidingTabLayout mTabs;
    private ViewPager mPager;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        buildGoogleApiClient();
        initializeBroadcast();
        initializeSupportActionBar();
        initializeDrawerLayout();
        initializeTabs();
        initializeFloatingActionButton();
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
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Space purposefully left empty
    }

    /**
     * Switch the user's public location broadcast on/off
     */
    private void broadcastLocation() {
        // Switch broadcasting on
        if (mGoogleApiClient.isConnected() && !mIsBroadcasting) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mBroadcastIntent);
            mIsBroadcasting = true;

            Snackbar.make(mPager, "Public broadcast on!", Snackbar.LENGTH_LONG).show();
        }

        // Switch broadcasting off
        else if (mGoogleApiClient.isConnected() && mIsBroadcasting) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mBroadcastIntent);
            mIsBroadcasting = false;

            Snackbar.make(mPager, "Public broadcast off!", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Log the current user out of the application
     */
    private void logout() {
        // Ensure the user is not broadcasting their location after logout
        if (mGoogleApiClient.isConnected() && mIsBroadcasting) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mBroadcastIntent);
        }

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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Setup the DrawerLayout for this screen
     */
    private void initializeDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    /**
     * Setup tabular navigation for this screen
     */
    private void initializeTabs() {
        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), TAB_TITLES, NUM_OF_TABS));
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return R.color.tabs_scroll_color;
            }
        });
        mTabs.setViewPager(mPager);
    }

    /**
     * Setup the FloatingActionButton to transition to a "Create Circle" screen
     */
    private void initializeFloatingActionButton() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(mPager, "Clicked FAB!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}

package com.parseapp.vthokiefinder;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseFile;
import com.parse.ParseUser;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * An activity that acts as the applications "homepage". Provides the user with
 * an interface to access the main features of HokieFinder.
 *
 * @author Steven Briggs
 * @version 2015.10.23
 */
public class HomeActivity extends AppCompatActivity implements
        CirclesFragment.Callbacks,
        FriendsFragment.Callbacks,
        CircleMapFragment.Callbacks,
        BroadcastFragment.Callbacks,
        GoogleApiManagerFragment.Callbacks {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;

    private HomeFragment mHomeFragment;
    private BroadcastFragment mBroadcastFragment;
    private GoogleApiManagerFragment mGoogleApiManagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FragmentManager fm = getSupportFragmentManager();

        // Create new Fragments for the Activity
        if (savedInstanceState == null) {
            mHomeFragment = HomeFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragment_container, mHomeFragment, HomeFragment.TAG)
                    .commit();

            mBroadcastFragment = BroadcastFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.broadcast_select, mBroadcastFragment, BroadcastFragment.TAG)
                    .commit();

            mGoogleApiManagerFragment = new GoogleApiManagerFragment();
            fm.beginTransaction()
                    .add(mGoogleApiManagerFragment, GoogleApiManagerFragment.TAG)
                    .commit();
        }

        // Retrieve existing Fragments
        else {
            mHomeFragment = (HomeFragment) fm.findFragmentByTag(HomeFragment.TAG);
            mBroadcastFragment =
                    (BroadcastFragment) fm.findFragmentByTag(BroadcastFragment.TAG);
            mGoogleApiManagerFragment =
                    (GoogleApiManagerFragment) fm.findFragmentByTag(GoogleApiManagerFragment.TAG);
        }

        // Initialize all elements of the Activity
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        initializeDrawerLayout();
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
                // Pop the backstack if there happens to be a fragment on it. This allows us
                // to display the default home button as up in hosted fragments
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                }

                return true;

            case R.id.action_show_circles:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }

                mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }

        else if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        }

        else {
            super.onBackPressed();
        }
    }

    @Override
    public GoogleApiClient requestGoogleApiClient() {
        return mGoogleApiManagerFragment.getClient();
    }

    @Override
    public void onClientConnected() {
        // Space purposefully left empty
    }

    /**
     * Log the current user out of the application
     */
    private void logout() {
        mBroadcastFragment.stopBroadcast();
        ParseUser.getCurrentUser().put("masterBroadcast", false);
        ParseUser.getCurrentUser().saveEventually();
        ParseUser.logOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }

    /**
     * Setup the DrawerLayout for this screen
     */
    private void initializeDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle mToogle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                // Prevent hamburger icon from turning into a back arrow
                super.onDrawerOpened(drawerView);
                super.onDrawerSlide(drawerView, 0);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Completely disable drawer animation
                super.onDrawerSlide(drawerView, 0);
            }
        };

        mDrawerLayout.setDrawerListener(mToogle);
        mToogle.syncState();

        // Create a listener to handle clicks on the drawer's menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.drawer_profile:
                        return true;

                    case R.id.drawer_friends:
                        return true;

                    case R.id.drawer_settings:
                        startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                        return true;

                    case R.id.drawer_logout:
                        logout();
                        return true;
                }

                return true;
            }
        });

        // Inflate the header and attach it to the drawer. The header displays user information
        // similar to several other Google applications
        RelativeLayout header = (RelativeLayout) getLayoutInflater().inflate(R.layout.header, navigationView, false);
        CircleImageView avatar = (CircleImageView) header.findViewById(R.id.avatar);

        // Attempt to load the user's avatar. If there isn't one, then use the default image
        ParseFile imageFile = ParseUser.getCurrentUser().getParseFile("avatar");
        if (imageFile != null) {
            Glide.with(this)
                    .load(Uri.parse(imageFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(avatar);
        }

        ((TextView) header.findViewById(R.id.username)).setText(ParseUser.getCurrentUser().getUsername());
        ((TextView) header.findViewById(R.id.email)).setText(ParseUser.getCurrentUser().getEmail());
        navigationView.addHeaderView(header);
    }

    @Override
    public void onCircleClicked(Circle circle) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.CIRCLE_ID_KEY, circle.getObjectId());
        startActivity(intent);
    }

    @Override
    public void onFriendClicked(ParseUser friend) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.USER_ID_KEY, friend.getObjectId());
        startActivity(intent);
    }
}

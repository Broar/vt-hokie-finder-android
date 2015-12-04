package com.parseapp.vthokiefinder;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseFile;
import com.parse.ParseUser;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * An activity that acts as the application's "homepage". Provides the user with
 * an interface to access the main features of HokieFinder.
 *
 * @author Steven Briggs
 * @version 2015.12.04
 */
public class HomeActivity extends AppCompatActivity implements
        CirclesFragment.Callbacks,
        FriendsFragment.Callbacks,
        MapFragment.Callbacks,
        BroadcastFragment.Callbacks,
        GoogleApiManagerFragment.Callbacks,
        ViewPagerAdapter.Callbacks {

    public static final int REQUEST_DETAIL_VIEWS = 1;
    public static final int REQUEST_PROFILE_UPDATE = 2;

    private static final CharSequence[] TITLES = { "CIRCLES", "FRIENDS", "MAP" };

    private static final int CIRCLES = 0;
    private static final int FRIENDS = 1;
    private static final int MAP = 2;

    private BroadcastFragment mBroadcastFragment;
    private GoogleApiManagerFragment mGoogleApiManagerFragment;

    private int mPagePosition;
    private ViewPagerAdapter mAdapter;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private FloatingActionMenu mFabMenu;
    private FloatingActionButton mFab;
    private FloatingActionButton mFabCreateCircle;
    private FloatingActionButton mFabAddCircles;
    private ViewPager mViewPager;
    private TabLayout mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FragmentManager fm = getSupportFragmentManager();

        // Create new Fragments for the Activity
        if (savedInstanceState == null) {
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
            mBroadcastFragment =
                    (BroadcastFragment) fm.findFragmentByTag(BroadcastFragment.TAG);
            mGoogleApiManagerFragment =
                    (GoogleApiManagerFragment) fm.findFragmentByTag(GoogleApiManagerFragment.TAG);
        }

        // Initialize all elements of the Activity
        bindActivity();
        setSupportActionBar(mToolbar);
        setupDrawerLayout();
        setupPager();
        setupFab();
    }

    /**
     * Bind the activity to the views of its layout
     */
    private void bindActivity() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFabMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFabCreateCircle = (FloatingActionButton) findViewById(R.id.fab_create_circle);
        mFabAddCircles = (FloatingActionButton) findViewById(R.id.fab_add_circles);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabs = (TabLayout) findViewById(R.id.tabs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_show_circles) {
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
        // If any drawers are open, then close them
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }

        else if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        }

        // Close the floating action menu if it is visible and opened
        else if (mPagePosition == CIRCLES && mFabMenu.isOpened()) {
            mFabMenu.close(true);
        }

        // Continue with normal back press
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            // Update the lists of items maintained on the main screen
            if (requestCode == REQUEST_DETAIL_VIEWS) {
                CirclesFragment circlesFragment = (CirclesFragment) mAdapter.getFragment(CIRCLES);
                FriendsFragment friendsFragment = (FriendsFragment) mAdapter.getFragment(FRIENDS);

                if (circlesFragment != null) {
                    circlesFragment.onRefresh();
                }

                if (friendsFragment != null) {
                    friendsFragment.onRefresh();
                }

                if (mBroadcastFragment != null) {
                    mBroadcastFragment.onRefresh();
                }
            }
        }
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
    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
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

        mDrawerLayout.setDrawerListener(toogle);
        toogle.syncState();

        // Create a listener to handle clicks on the drawer's menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.drawer_profile:
                        startActivity(new Intent(HomeActivity.this, EditProfileActivity.class));
                        return true;

                    case R.id.drawer_invites:
                        startActivityForResult(new Intent(HomeActivity.this, InvitesActivity.class), REQUEST_DETAIL_VIEWS);
                        return true;

                    case R.id.drawer_settings:
                        startActivityForResult(new Intent(HomeActivity.this, SettingsActivity.class), REQUEST_DETAIL_VIEWS);
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
        CircleImageView mAvatar = (CircleImageView) header.findViewById(R.id.avatar);

        // Attempt to load the user's avatar. If there isn't one, then use the default image
        ParseFile imageFile = ParseUser.getCurrentUser().getParseFile("avatar");
        if (imageFile != null) {
            Glide.with(this)
                    .load(Uri.parse(imageFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mAvatar);
        }

        ((TextView) header.findViewById(R.id.username)).setText(ParseUser.getCurrentUser().getUsername());
        ((TextView) header.findViewById(R.id.email)).setText(ParseUser.getCurrentUser().getEmail());
        navigationView.addHeaderView(header);
    }

    /**
     * Setup the floating action buttons
     */
    private void setupFab() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPagePosition == FRIENDS) {
                    startActivity(new Intent(HomeActivity.this, FindFriendsActivity.class));
                }

                else if (mPagePosition == MAP) {
                    MapFragment mapFragment = (MapFragment) mAdapter.getFragment(MAP);
                    if (mapFragment != null) {
                        mapFragment.refreshLocations(mBroadcastFragment.getViewedCircle());
                    }
                }
            }
        });

        mFabCreateCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, CreateCircleActivity.class));
            }
        });

        mFabAddCircles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(HomeActivity.this, FindCirclesActivity.class), REQUEST_DETAIL_VIEWS);
            }
        });
    }

    /**
     * Setup the view pager and tabs to display the primary fragments
     */
    private void setupPager() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Space purposefully left empty
            }

            @Override
            public void onPageSelected(int position) {
                mPagePosition = position;
                updateVisibleFab();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Space purposefully left empty
            }
        });

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), TITLES, this);
        mViewPager.setAdapter(mAdapter);
        mTabs.setupWithViewPager(mViewPager);
    }

    /**
     * Update the floating action button that is visible based on the page displayed
     */
    private void updateVisibleFab() {
        switch (mPagePosition) {
            case CIRCLES:
                if (mFab.getVisibility() != View.INVISIBLE) {
                    mFab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_down));
                    mFab.setVisibility(View.INVISIBLE);
                }

                mFabMenu.setVisibility(View.VISIBLE);
                mFabMenu.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_up));
                break;

            case FRIENDS:
                if (mFabMenu.getVisibility() != View.INVISIBLE) {
                    mFabMenu.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_down));
                    mFabMenu.setVisibility(View.INVISIBLE);
                }

                mFab.setImageResource(R.drawable.ic_person_add_white_24dp);
                mFab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_up));
                mFab.setVisibility(View.VISIBLE);
                break;

            case MAP:
                if (mFabMenu.getVisibility() != View.INVISIBLE) {
                    mFabMenu.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_down));
                    mFabMenu.setVisibility(View.INVISIBLE);
                }

                mFab.setImageResource(R.drawable.ic_refresh_white_24dp);
                mFab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_up));
                mFab.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onCircleClicked(Circle circle) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.CIRCLE_ID_KEY, circle.getObjectId());
        startActivityForResult(intent, REQUEST_DETAIL_VIEWS);
    }

    @Override
    public void onFriendClicked(ParseUser friend) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.USER_ID_KEY, friend.getObjectId());
        startActivityForResult(intent, REQUEST_DETAIL_VIEWS);
    }

    @Override
    public GoogleApiClient requestGoogleApiClient() {
        return mGoogleApiManagerFragment.getClient();
    }

    @Override
    public Circle onViewedCircleRequested() {
        return mBroadcastFragment.getViewedCircle();
    }


    @Override
    public void onViewedCircleClicked(Circle circle) {
        MapFragment mapFragment = (MapFragment) mAdapter.getFragment(MAP);

        if (mapFragment != null && mPagePosition == MAP) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
            }

            mapFragment.refreshLocations(circle);
        }
    }

    @Override
    public void onClientConnected() {
        // Space purposefully left empty
    }

    @Override
    public Fragment onItemRequested(int position) {
        if (position == CIRCLES) {
            return CirclesFragment.newInstance(ParseUser.getCurrentUser().getObjectId());
        }

        else if (position == FRIENDS) {
            return FriendsFragment.newInstance(ParseUser.getCurrentUser().getObjectId());
        }

        else {
            return MapFragment.newInstance();
        }
    }
}

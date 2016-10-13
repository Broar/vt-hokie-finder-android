package com.parseapp.vthokiefinder.circles;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.parseapp.vthokiefinder.R;
import com.parseapp.vthokiefinder.model.Circle;
import com.parseapp.vthokiefinder.common.ViewPagerAdapter;
import com.parseapp.vthokiefinder.utils.GoogleApiManagerFragment;

/**
 * An activity that allows the user to search for circles / communities to join
 *
 * @author Steven Briggs
 * @version 2015.12.01
 */
public class FindCirclesActivity extends AppCompatActivity implements
        FindCirclesFragment.Callbacks,
        GoogleApiManagerFragment.Callbacks,
        ViewPagerAdapter.Callbacks {

    private static final CharSequence[] TITLES = { "CIRCLES", "COMMUNITIES" };
    private static final int CIRCLES = 0;
    private static final int COMMUNITEIS = 1;

    private FindCirclesFragment mFindCommunitiesFragment;
    private GoogleApiManagerFragment mGoogleApiManagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            mGoogleApiManagerFragment = GoogleApiManagerFragment.newInstance();
            fm.beginTransaction()
                    .add(mGoogleApiManagerFragment, GoogleApiManagerFragment.TAG)
                    .commit();
        }

        else {
            mGoogleApiManagerFragment = (GoogleApiManagerFragment) fm.findFragmentByTag(GoogleApiManagerFragment.TAG);
        }

        // Setup the action bar
        setContentView(R.layout.activity_find_circles);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public void onCircleClicked(Circle circle) {
        // Do nothing for now
    }

    @Override
    public void onCurrentLocationRequested(GoogleApiManagerFragment.OnLocationFoundListener listener) {
        mGoogleApiManagerFragment.getCurrentLocation(listener);
    }

    @Override
    public void onClientConnected() {
        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), TITLES, this));
        ((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(pager);
    }

    @Override
    public Fragment onItemRequested(int position) {
        if (position == CIRCLES) {
            return FindCirclesFragment.newInstance(FindCirclesFragment.FIND_CIRCLES);
        }

        else {
            mFindCommunitiesFragment = FindCirclesFragment.newInstance(FindCirclesFragment.FIND_COMMUNITIES);
            return mFindCommunitiesFragment;
        }
    }
}

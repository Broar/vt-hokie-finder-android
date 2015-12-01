package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * An activity that allows the user to search for circles / communities to join
 *
 * @author Steven Briggs
 * @version 2015.12.01
 */
public class FindCirclesActivity extends AppCompatActivity implements
        CirclesFragment.Callbacks,
        ViewPagerAdapter.Callbacks {

    private static final CharSequence[] TITLES = { "CIRCLES", "COMMUNITIES" };
    private static final int CIRCLES = 0;
    private static final int COMMUNITEIS = 1;

    private FindCirclesFragment mFindCirclesFragment;
    private FindCirclesFragment mFindCommuntiesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the action bar
        setContentView(R.layout.activity_find_circles);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup the tab layout
        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), TITLES, this));
        ((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(pager);
    }

    @Override
    public void onCircleClicked(Circle circle) {
        // Do nothing for now
    }

    @Override
    public Fragment onItemRequested(int position) {
        if (position == CIRCLES) {
            return FindCirclesFragment.newInstance(FindCirclesFragment.FIND_CIRCLES);
        }

        else {
            return FindCirclesFragment.newInstance(FindCirclesFragment.FIND_COMMUNITIES);
        }
    }
}

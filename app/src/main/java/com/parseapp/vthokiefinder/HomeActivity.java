package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.parseapp.vthokiefinder.widgets.SlidingTabLayout;

/**
 * An activity that acts as the applications "homepage". Provides the user with
 * an interface to access the main features of HokieFinder.
 *
 * @author Steven Briggs
 * @version 2015.09.15
 */
public class HomeActivity extends AppCompatActivity {

    private static final int NUM_OF_TABS = 2;
    private static final CharSequence[] TAB_TITLES = { "MY CIRCLES", "CIRCLES" };

    private SlidingTabLayout tabs;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        initializeTabs();
    }

    /**
     * Setup tabular navigation for the homescreen
     */
    private void initializeTabs() {
        pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), TAB_TITLES, NUM_OF_TABS));
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return R.color.tabs_scroll_color;
            }
        });
        tabs.setViewPager(pager);
    }
}

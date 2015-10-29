package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parseapp.vthokiefinder.widgets.SlidingTabLayout;

/**
 * A fragment providing tabular navigation between the main features of the application
 *
 * @author Steven Briggs
 * @version 2015.10.03
 */
public class HomeFragment extends Fragment {
    public static final String TAG = HomeFragment.class.getSimpleName();

    private static final int NUM_TABS = 4;
    private static final CharSequence[] TAB_TITLES = { "MY CIRCLES", "CIRCLES", "FRIENDS", "MAP" };

    private ViewPager mViewPager;
    private SlidingTabLayout mTabs;

    /**
     * A factory method to return a new HomeFragment that has been configured
     *
     * @return a new HomeFragment that has been configured
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mTabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        initializeTabs();
        return view;
    }

    /**
     * Setup tabular navigation
     */
    private void initializeTabs() {
        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), TAB_TITLES, NUM_TABS));
        mTabs.setDistributeEvenly(true);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return R.color.tabs_scroll_color;
            }
        });

        mTabs.setViewPager(mViewPager);
    }
}

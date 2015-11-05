package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.parse.ParseUser;
import com.parseapp.vthokiefinder.widgets.SlidingTabLayout;

/**
 * A fragment providing tabular navigation between the main features of the application
 *
 * @author Steven Briggs
 * @version 2015.10.
 */
public class HomeFragment extends Fragment implements ViewPagerAdapter.Callbacks {
    public static final String TAG = HomeFragment.class.getSimpleName();

    private static final CharSequence[] TITLES = { "MY CIRCLES", "CIRCLES", "FRIENDS", "MAP" };

    private static final int MY_CIRCLES = 0;
    private static final int CIRCLES = 1;
    private static final int FRIENDS = 2;
    private static final int MAP = 3;

    private int mPagePosition;
    private FloatingActionButton mFab;
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

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mTabs = (SlidingTabLayout) view.findViewById(R.id.tabs);

        initializeFab();
        initializePager();
        initializeTabs();

        return view;
    }

    /**
     * Setup the floating action button
     */
    private void initializeFab() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mPagePosition) {
                    case MY_CIRCLES:
                        startActivity(new Intent(getContext(), CreateCircleActivity.class));
                        break;
                    case CIRCLES:
                        startActivity(new Intent(getContext(), CreateCircleActivity.class));
                        break;
                    case FRIENDS:
                        startActivity(new Intent(getContext(), FindFriendsActivity.class));
                        break;
                    case MAP:
                        break;
                }
            }
        });
    }

    /**
     * Setup the view pager to display the primary fragments
     */
    private void initializePager() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Space purposefully left empty
            }

            @Override
            public void onPageSelected(int position) {
                mPagePosition = position;
                changeFabIcon();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Space purposefully left empty
            }
        });

        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), TITLES, this));
    }

    /**
     * Change the floating action bar's icon based on the Fragment that is displayed
     */
    private void changeFabIcon() {

        if (!mFab.isShown()) {
            mFab.show();
        }

        mFab.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_disappear));

        // Determine what the FAB's icon should change to based on the page position
        // getDrawable() is only available on API 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switch (mPagePosition) {
                case MY_CIRCLES:
                    mFab.setImageDrawable(getContext().getDrawable(R.drawable.ic_add_white_24dp));
                    break;
                case CIRCLES:
                    mFab.setImageDrawable(getContext().getDrawable(R.drawable.ic_add_white_24dp));
                    break;
                case FRIENDS:
                    mFab.setImageDrawable(getContext().getDrawable(R.drawable.ic_person_add_white_24dp));
                    break;
                case MAP:
                    mFab.hide();
                    break;
            }
        }

        // Provide a deprecated call to getDrawable() for APIs less than 21
        else {
            switch (mPagePosition) {
                case MY_CIRCLES:
                    mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp));
                    break;
                case CIRCLES:
                    mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp));
                    break;
                case FRIENDS:
                    mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_add_white_24dp));
                    break;
                case MAP:
                    mFab.hide();
                    break;
            }
        }

        if (mPagePosition != MAP) {
            mFab.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_appear));
        }
    }

    /**
     * Setup the tabular navigation
     */
    private void initializeTabs() {

        // Alter the space distribution of the tabs depending on the orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTabs.setDistributeEvenly(true);
        }

        else {
            mTabs.setDistributeEvenly(false);
        }

        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return R.color.tabs_scroll_color;
            }
        });

        mTabs.setViewPager(mViewPager);
    }

    @Override
    public Fragment onItemRequested(int position) {
        if (position == 0) {
            return CirclesFragment.newInstance(ParseUser.getCurrentUser().getObjectId());
        }

        else if (position == 1) {
            return FindCirclesFragment.newInstance();
        }

        else if (position == 2) {
            return FriendsFragment.newInstance(ParseUser.getCurrentUser().getObjectId());
        }

        else {
            return CircleMapFragment.newInstance();
        }
    }
}

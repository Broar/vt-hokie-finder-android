package com.parseapp.vthokiefinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.parse.ParseUser;

/**
 * A fragment providing tabular navigation between the main features of the application
 *
 * @author Steven Briggs
 * @version 2015.10.
 */
public class HomeFragment extends Fragment implements
        HomeActivity.OnBackPressedListener,
        ViewPagerAdapter.Callbacks {

    public static final String TAG = HomeFragment.class.getSimpleName();

    private static final CharSequence[] TITLES = { "CIRCLES", "FRIENDS", "MAP" };

    private static final int CIRCLES = 0;
    private static final int FRIENDS = 1;
    private static final int MAP = 2;

    private int mPagePosition;
    private FloatingActionMenu mFabMenu;
    private FloatingActionButton mFab;
    private FloatingActionButton mFabCreateCircle;
    private FloatingActionButton mFabAddCircles;
    private ViewPager mViewPager;
    private TabLayout mTabs;

    /**
     * A factory method to return a new HomeFragment that has been configured
     *
     * @return a new HomeFragment that has been configured
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((HomeActivity) context).setOnBackPressedListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bindFragment(view);
        initializeFab();
        initializePager();
        initializeTabs();

        return view;
    }

    /**
     * Bind the fragment to the views of its layout
     *
     * @param view the layout view
     */
    private void bindFragment(View view) {
        mFabMenu = (FloatingActionMenu) view.findViewById(R.id.fab_menu);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFabCreateCircle = (FloatingActionButton) view.findViewById(R.id.fab_create_circle);
        mFabAddCircles = (FloatingActionButton) view.findViewById(R.id.fab_add_circles);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mTabs = (TabLayout) view.findViewById(R.id.tabs);
    }

    /**
     * Setup the floating action buttons
     */
    private void initializeFab() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), FindFriendsActivity.class));
            }
        });

        mFabCreateCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreateCircleActivity.class));
            }
        });

        mFabAddCircles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do nothing for now
                // startActivity(new Intent(getContext(), FindCirclesActivity.class));
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
                updateVisibleFab();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Space purposefully left empty
            }
        });

        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), TITLES, this));
    }

    /**
     * Update the floating action button that is visible based on the page displayed
     */
    private void updateVisibleFab() {
        switch (mPagePosition) {
            case CIRCLES:
                if (mFab.getVisibility() != View.INVISIBLE) {
                    mFab.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_scale_down));
                    mFab.setVisibility(View.INVISIBLE);
                }

                mFabMenu.setVisibility(View.VISIBLE);
                mFabMenu.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_scale_up));
                break;

            case FRIENDS:
                if (mFabMenu.getVisibility() != View.INVISIBLE) {
                    mFabMenu.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_scale_down));
                    mFabMenu.setVisibility(View.INVISIBLE);
                }

                mFab.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_scale_up));
                mFab.setVisibility(View.VISIBLE);
                break;

            case MAP:
                mFab.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_scale_down));
                mFab.setVisibility(View.INVISIBLE);
                break;
        }
    }

    /**
     * Setup the tabular navigation
     */
    private void initializeTabs() {
        mTabs.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onBackPressed() {
        // Close the floating action menu if it is opened and currently visible
        if (mPagePosition == CIRCLES && mFabMenu.isOpened()) {
            mFabMenu.close(true);
            return true;
        }

        // Otherwise, let the parent activity handle the back press
        else {
            return false;
        }
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
            return CircleMapFragment.newInstance();
        }
    }
}

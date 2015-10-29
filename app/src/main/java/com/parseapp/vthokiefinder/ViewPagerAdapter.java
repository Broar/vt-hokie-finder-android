package com.parseapp.vthokiefinder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * A simple ViewPager required to utilize the SlidingTabLayout
 *
 * @author Steven Briggs
 * @version 2015.09.22
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence[] mTitles;
    private int mNumOfTabs;

    public ViewPagerAdapter(FragmentManager fm, CharSequence[] titles, int numOfTabs) {
        super(fm);
        mTitles = titles;
        mNumOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return MyCirclesFragment.newInstance();
        }

        else if (position == 1) {
            return FindCirclesFragment.newInstance();
        }

        else if (position == 2) {
            return MyFriendsFragment.newInstance();
        }

        else {
            return CircleMapFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}

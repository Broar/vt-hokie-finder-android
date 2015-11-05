package com.parseapp.vthokiefinder;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * A simple adapter to be used by ViewPagers
 *
 * @author Steven Briggs
 * @version 2015.11.03
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence[] mTitles;
    private Callbacks mListener;

    public interface Callbacks {
        Fragment onItemRequested(int position);
    }

    public ViewPagerAdapter(FragmentManager fm, CharSequence[] titles, @NonNull Callbacks listener) {
        super(fm);
        mTitles = titles;
        mListener = listener;
    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        return mListener.onItemRequested(position);
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}

package com.parseapp.vthokiefinder.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * A simple adapter to be used by ViewPagers
 *
 * @author Steven Briggs
 * @version 2015.12.04
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence[] mTitles;
    //private Fragment[] mFragments;
    private SparseArray<WeakReference<Fragment>> mFragments;
    private Callbacks mListener;

    public interface Callbacks {
        Fragment onItemRequested(int position);
    }

    public ViewPagerAdapter(FragmentManager fm, CharSequence[] titles, @NonNull Callbacks listener) {
        super(fm);
        mTitles = titles;
        mFragments = new SparseArray<>(titles.length);
        mListener = listener;
    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        Fragment fragment = mListener.onItemRequested(position);
        //mFragments[position] = fragment;
        mFragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        //mFragments[position] = fragment;
        mFragments.put(position, new WeakReference<Fragment>(fragment));
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        //mFragments[position] = null;
    }

    /**
     * Return the fragment located at position
     *
     * @param position the position of the fragment
     * @return the fragment located at position, if the fragment does not exist null will be returned
     */
    @Nullable
    public Fragment getFragment(int position) {
        assert position > -1 && position < mFragments.size();
        //return mFragments[position];
        WeakReference<Fragment> reference = mFragments.get(position);
        return reference != null ? reference.get() : null;
    }
}

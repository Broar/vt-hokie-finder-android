package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * A retained fragment that hosts a data element of class T
 *
 * @author Steven Briggs
 * @version 2015.11.17
 */
public class RetainedFragment<T> extends Fragment {

    public static final String TAG = RetainedFragment.class.getSimpleName();

    private T mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public T getData() {
        return mData;
    }

    public void setData(T data) {
        mData = data;
    }
}

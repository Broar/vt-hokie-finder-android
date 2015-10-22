package com.parseapp.vthokiefinder;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * A retained fragment to hold the Bitmap of the user's selected image during circle creation
 *
 * @author Steven Briggs
 * @version 2015.10.22
 */
public class BitmapHolderFragment extends Fragment {

    public static final String TAG = BitmapHolderFragment.class.getSimpleName();

    private Bitmap mIconBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public Bitmap getIconBitmap() {
        return mIconBitmap;
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        mIconBitmap = iconBitmap;
    }
}

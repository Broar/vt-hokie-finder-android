package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A fragment that display the profile of a user
 *
 * @author Steven Briggs
 * @version 2015.11.03
 */
public class ProfileFragment extends Fragment implements ViewPagerAdapter.Callbacks {

    public static final String TAG = ProfileFragment.class.getSimpleName();
    public static final String USER_ID_KEY = "userId";

    private static final CharSequence[] TITLES = { "CIRCLES", "FRIENDS" };

    private Callbacks mListener;
    private ParseUser mUser;

    private CircleImageView mAvatar;
    private TextView mUsername;
    private TextView mEmail;
    private TabLayout mTabs;
    private ViewPager mPager;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;

    public interface Callbacks {
        void onHomeClicked();
    }

    /**
     * A factory method to return a new ProfileFragment that has been configured
     *
     * @param userId the id of the user whose profile is to be shown
     * @return a new ProfileFragment that has been configured
     */
    public static ProfileFragment newInstance(String userId) {
        Bundle args = new Bundle();
        args.putString(USER_ID_KEY, userId);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            mListener = (Callbacks) activity;
        }

        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = ParseObject.createWithoutData(ParseUser.class, getArguments().getString(USER_ID_KEY));
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAvatar = (CircleImageView) view.findViewById(R.id.avatar);
        mUsername = (TextView) view.findViewById(R.id.username);
        mEmail = (TextView) view.findViewById(R.id.email);
        mTabs = (TabLayout) view.findViewById(R.id.tabs);
        mPager = (ViewPager) view.findViewById(R.id.view_pager);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);

        initializeToolbar();
        initializeFab();
        initializeTabs();

        getProfile();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mListener.onHomeClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Setup the toolbar to function as the SupportActionBar
     */
    private void initializeToolbar() {
        AppCompatActivity parent = (AppCompatActivity) getActivity();
        parent.setSupportActionBar(mToolbar);
        parent.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        parent.getSupportActionBar().setTitle("");
    }

    /**
     * Setup the tabular navigation
     */
    private void initializeTabs() {
        mPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), TITLES, this));
        mTabs.setupWithViewPager(mPager);
    }

    /**
     * Setup the floating action bar
     */
    private void initializeFab() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }

    /**
     * Retrieve the profile information of the user
     */
    private void getProfile() {
        ParseFile imageFile = mUser.getParseFile("avatar");
        if (imageFile != null) {
            Glide.with(getContext())
                    .load(Uri.parse(imageFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mAvatar);
        }

        mUsername.setText(mUser.getUsername());
        mEmail.setText(mUser.getEmail());
    }

    @Override
    public Fragment onItemRequested(int position) {
        if (position == 0) {
            return CirclesFragment.newInstance(mUser.getObjectId());
        }

        else {
            return FriendsFragment.newInstance(mUser.getObjectId());
        }
    }
}

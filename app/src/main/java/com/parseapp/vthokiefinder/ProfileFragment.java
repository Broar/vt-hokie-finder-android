package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parseapp.vthokiefinder.widgets.SlidingTabLayout;

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

    private static final CharSequence[] TITLES = { "FRIENDS", "CIRCLES" };

    private CircleImageView mAvatar;
    private TextView mUsername;
    private TextView mEmail;
    private SlidingTabLayout mTabs;
    private ViewPager mPager;
    private FloatingActionButton mFab;

    /**
     * A factory method to return a new FindFriendsFragment that has been configured
     *
     * @param userId the id of the user whose profile is to be shown
     * @return a new FindFriendsFragment that has been configured
     */
    public static ProfileFragment newInstance(String userId) {
        Bundle args = new Bundle();
        args.putString(USER_ID_KEY, userId);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAvatar = (CircleImageView) view.findViewById(R.id.avatar);
        mUsername = (TextView) view.findViewById(R.id.username);
        mEmail = (TextView) view.findViewById(R.id.email);
        mTabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        mPager = (ViewPager) view.findViewById(R.id.viewPager);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);

        initializeFab();
        initializeTabs();

        getProfile(getArguments().getString(USER_ID_KEY));

        return view;
    }

    /**
     * Setup the tabular navigation
     */
    private void initializeTabs() {
        mPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), TITLES, this));
        mTabs.setViewPager(mPager);
        mTabs.setDistributeEvenly(true);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return R.color.tabs_scroll_color;
            }
        });
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
     * Retrieve the profile information of the user specified by userId
     *
     * @param userId the id of the user
     */
    public void getProfile(String userId) {
        ParseUser.getQuery().getInBackground(userId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    mUsername.setText(user.getUsername());
                    mEmail.setText(user.getEmail());
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public Fragment onItemRequested(int position) {
        String userId = getArguments().getString(USER_ID_KEY);

        if (position == 0) {
            return CirclesFragment.newInstance(userId);
        }

        else {
            return FriendsFragment.newInstance(userId);
        }
    }
}

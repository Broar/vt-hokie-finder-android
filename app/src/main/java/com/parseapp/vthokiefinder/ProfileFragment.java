package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int mFriendStatus;

    private Menu mMenu;

    private AppBarLayout mAppBar;
    private Toolbar mToolbar;
    private TextView mTitle;
    private CircleImageView mAvatar;
    private TextView mUsername;
    private TextView mEmail;
    private TabLayout mTabs;
    private ViewPager mPager;

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
        setHasOptionsMenu(true);
        mUser = ParseObject.createWithoutData(ParseUser.class, getArguments().getString(USER_ID_KEY));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAppBar = (AppBarLayout) view.findViewById(R.id.appbar);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mTitle = (TextView) view.findViewById(R.id.title);
        mAvatar = (CircleImageView) view.findViewById(R.id.avatar);
        mUsername = (TextView) view.findViewById(R.id.username);
        mEmail = (TextView) view.findViewById(R.id.email);
        mTabs = (TabLayout) view.findViewById(R.id.tabs);
        mPager = (ViewPager) view.findViewById(R.id.view_pager);

        initializeToolbar();
        initializeTabs();

        getProfile();

        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == -appBarLayout.getTotalScrollRange()) {
                    mTitle.setVisibility(View.VISIBLE);
                }

                else {
                    mTitle.setVisibility(View.INVISIBLE);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Determine the friendship status between the current user and this one
        Friend.findFriendshipStatus(ParseUser.getCurrentUser(), mUser, new Friend.OnFriendshipFoundListener() {
            @Override
            public void onFriendshipFound(int friendStatus) {
                mFriendStatus = friendStatus;

                if (getActivity() != null) {
                    getActivity().invalidateOptionsMenu();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        switch (mFriendStatus) {
            case Friend.FRIENDS:
                mMenu.findItem(R.id.action_add_friend).setVisible(false);
                mMenu.findItem(R.id.action_remove_friend).setVisible(true);
                mMenu.findItem(R.id.action_cancel_pending).setVisible(false);
                break;

            case Friend.NOT_FRIENDS:
                mMenu.findItem(R.id.action_add_friend).setVisible(true);
                mMenu.findItem(R.id.action_remove_friend).setVisible(false);
                mMenu.findItem(R.id.action_cancel_pending).setVisible(false);
                break;

            case Friend.INCOMING:
                // Intentional fallthrough

            case Friend.OUTGOING:
                mMenu.findItem(R.id.action_add_friend).setVisible(false);
                mMenu.findItem(R.id.action_remove_friend).setVisible(false);
                mMenu.findItem(R.id.action_cancel_pending).setVisible(true);
                break;

            default:
                mMenu.findItem(R.id.action_add_friend).setVisible(false);
                mMenu.findItem(R.id.action_remove_friend).setVisible(false);
                mMenu.findItem(R.id.action_cancel_pending).setVisible(false);
                break;
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mListener.onHomeClicked();
                return true;
            case R.id.action_add_friend:
                createFriendRequest();
                return true;
            case R.id.action_remove_friend:
                removeFriend();
                return true;
            case R.id.action_cancel_pending:
                cancelPendingFriendship();
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
        parent.getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * Setup the tabular navigation
     */
    private void initializeTabs() {
        mPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), TITLES, this));
        mTabs.setupWithViewPager(mPager);
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

        mTitle.setText(mUser.getUsername());
        mUsername.setText(mUser.getUsername());
        mEmail.setText(mUser.getEmail());
    }

    /**
     * Create a friend request from the current user to this one
     */
    private void createFriendRequest() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("friendId", mUser.getObjectId());

        ParseCloud.callFunctionInBackground("createFriendRequest", params, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean requestMade, ParseException e) {
                if (e == null) {
                    if (requestMade) {
                        mFriendStatus = Friend.OUTGOING;
                        getActivity().invalidateOptionsMenu();
                        Toast.makeText(getContext(), "Request sent!", Toast.LENGTH_LONG).show();
                    }

                    else {
                        Toast.makeText(getContext(), "Couldn't send request!", Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Remove this user from the current user's friend list
     */
    private void removeFriend() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("friendId", mUser.getObjectId());
        
        ParseCloud.callFunctionInBackground("deleteFriendship", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if (e == null) {
                    mFriendStatus = Friend.NOT_FRIENDS;
                    getActivity().invalidateOptionsMenu();
                    Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Delete the pending friendship invite / request between this user and the current user
     */
    private void cancelPendingFriendship() {
        // Get the friend relationship regardless of the direction it was initiated in
        // by creating sub-queries to use
        ParseQuery<Friend> aToB = Friend.getQuery()
                .whereEqualTo("user", ParseUser.getCurrentUser())
                .whereEqualTo("friend", mUser)
                .whereEqualTo("pending", true);

        ParseQuery<Friend> bToA = Friend.getQuery()
                .whereEqualTo("user", mUser)
                .whereEqualTo("friend", ParseUser.getCurrentUser())
                .whereEqualTo("pending", true);

        ParseQuery<Friend> mainQuery = ParseQuery.or(Arrays.asList(aToB, bToA));

        // Delete the pending friend request between the current user and this one
        mainQuery.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(List<Friend> friends, ParseException e) {
                if (e == null) {
                    if (!friends.isEmpty()) {
                        friends.get(0).deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    mFriendStatus = Friend.NOT_FRIENDS;
                                    getActivity().invalidateOptionsMenu();
                                    Toast.makeText(getContext(), "Request cancelled!", Toast.LENGTH_LONG).show();
                                }

                                else {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    else {
                        Toast.makeText(getContext(), "Couldn't cancel request", Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
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

package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A fragment that displays the details of a circle
 *
 * @author Steven Briggs
 * @version 2015.11.20
 */
public class CircleDetailFragment extends RecyclerFragment<ParseUser, UserAdapter> {

    public static final String TAG = CircleDetailFragment.class.getSimpleName();
    public static final String CIRCLE_ID_KEY = "circleId";

    private Callbacks mListener;
    private Circle mCircle;
    private int mMemberStatus;

    private Menu mMenu;

    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private CircleImageView mIcon;
    private TextView mName;
    private TextView mDescription;

    public interface Callbacks {
        void onMemberClicked(ParseUser user);
        void onCircleDestroyed();
        void onHomeClicked();
    }

    /**
     * A factory method to return a new CircleDetailFragment that has been configured
     *
     * @param circleId the object id of the circle to display
     * @return a new CircleDetailFragment that has been configured
     */
    public static CircleDetailFragment newInstance(String circleId) {
        Bundle args = new Bundle();
        args.putString(CIRCLE_ID_KEY, circleId);
        CircleDetailFragment fragment = new CircleDetailFragment();
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

        // Determine the membership status of the current user for this circle
        mCircle = ParseObject.createWithoutData(Circle.class, getArguments().getString(CIRCLE_ID_KEY));
        mCircle.isMember(ParseUser.getCurrentUser(), new Circle.OnMembershipFoundListener() {
            @Override
            public void onMembershipFound(int memberStatus) {
                mMemberStatus = memberStatus;
                getActivity().invalidateOptionsMenu();
            }
        });

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflateFragment(R.layout.fragment_circle_detail, inflater, container);

        // Request the circle icon to display
        mIcon = (CircleImageView) view.findViewById(R.id.circle_icon);
        ParseFile imageFile = mCircle.getIcon();
        if (imageFile != null) {
            Glide.with(getContext())
                    .load(Uri.parse(imageFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mIcon);
        }

        mName = (TextView) view.findViewById(R.id.circle_name);
        mName.setText(mCircle.getName());
        mDescription = (TextView) view.findViewById(R.id.circle_description);
        mDescription.setText(mCircle.getDescription());

        // Setup the FAB to allow the user to edit the circle if they are a member
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        if (mMemberStatus == Circle.MEMBER) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Transition to edit page
                }
            });
        }

        else {
            mFab.hide();
        }

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        setupToolbar();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_circle_detail, menu);
        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        switch (mMemberStatus) {
            case Circle.MEMBER:
                mMenu.findItem(R.id.action_add_circle).setVisible(false);
                mMenu.findItem(R.id.action_leave_circle).setVisible(true);
                mMenu.findItem(R.id.action_cancel_circle_request).setVisible(false);
                break;

            case Circle.NOT_MEMBER:
                mMenu.findItem(R.id.action_add_circle).setVisible(true);
                mMenu.findItem(R.id.action_leave_circle).setVisible(false);
                mMenu.findItem(R.id.action_cancel_circle_request).setVisible(false);
                break;

            case Circle.PENDING:
                mMenu.findItem(R.id.action_add_circle).setVisible(false);
                mMenu.findItem(R.id.action_leave_circle).setVisible(false);
                mMenu.findItem(R.id.action_cancel_circle_request).setVisible(true);
                break;

            default:
                mMenu.findItem(R.id.action_add_circle).setVisible(false);
                mMenu.findItem(R.id.action_leave_circle).setVisible(false);
                mMenu.findItem(R.id.action_cancel_circle_request).setVisible(false);
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
            case R.id.action_add_circle:
                joinCircle();
                return true;
            case R.id.action_leave_circle:
                leaveCircle();
                return true;
            case R.id.action_cancel_circle_request:
                cancelRequest();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected UserAdapter buildAdapter() {
        return new UserAdapter(getContext(), getItems(), new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                mListener.onMemberClicked(getItems().get(position));
            }

            @Override
            public void onAddFriendClicked(int position) {
                // Add the user at position as a friend
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("circle", mCircle)
                .include("user")
                .setSkip(getNextPage())
                .setLimit(getLimit());

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null && !userCircles.isEmpty()) {
                    for (UserCircle uc : userCircles) {
                        getItems().add(uc.getParseUser("user"));
                    }

                    getAdapter().onDataReady(true);
                    nextPage();
                }

                else if (e == null) {
                    getAdapter().onDataReady(false);
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Setup the toolbar to function as the SupportActionBar
     */
    private void setupToolbar() {
        AppCompatActivity parent = (AppCompatActivity) getActivity();
        parent.setSupportActionBar(mToolbar);
        parent.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        parent.getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * Add the user to this circle
     */
    private void joinCircle() {
        // Create a UserCircle object to represent the new relationship
        ParseObject userCircle = ParseObject.create(UserCircle.class);
        userCircle.put("user", ParseUser.getCurrentUser());
        userCircle.put("circle", mCircle);
        userCircle.put("isBroadcasting", false);
        userCircle.put("pending", true);

        // Save the UserCircle object to the Parse backend
        userCircle.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mMemberStatus = Circle.PENDING;
                    getActivity().invalidateOptionsMenu();
                    Toast.makeText(getContext(), "Request sent!", Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Remove the user from this circle
     */
    private void leaveCircle() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("circleId", mCircle.getObjectId());
        params.put("userId", ParseUser.getCurrentUser().getObjectId());

        ParseCloud.callFunctionInBackground("removeUserFromCircle", params, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean isCircleDestroyed, ParseException e) {
                if (e == null) {
                    getItems().remove(ParseUser.getCurrentUser());
                    getBaseAdapter().notifyDataSetChanged();
                    mMemberStatus = Circle.NOT_MEMBER;

                    // If the user was the last remaining member of the circle, then it will no
                    // longer exist because we assume circles are deleted if they have no members.
                    // We allow the parent of the fragment to handle this case; otherwise, the
                    // fragment just updates its UI
                    if (isCircleDestroyed) {
                        mListener.onCircleDestroyed();
                        Toast.makeText(getContext(), "Circle destroyed!", Toast.LENGTH_LONG).show();
                    }

                    else {
                        getActivity().invalidateOptionsMenu();
                        Toast.makeText(getContext(), "Left circle!", Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Delete the pending request for the user to join this circle
     */
    private void cancelRequest() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser())
                .whereEqualTo("circle", mCircle)
                .whereEqualTo("pending", true);

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null) {
                    if (!userCircles.isEmpty()) {
                        userCircles.get(0).deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    mMemberStatus = Circle.NOT_MEMBER;
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
                        Toast.makeText(getContext(), "Couldn't cancel request!", Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

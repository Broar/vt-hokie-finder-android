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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

/**
 * A fragment that displays the details of a circle
 *
 * @author Steven Briggs
 * @version 2015.11.13
 */
public class CircleDetailFragment extends RecyclerFragment<ParseUser, UserAdapter> {

    public static final String TAG = CircleDetailFragment.class.getSimpleName();
    public static final String CIRCLE_ID_KEY = "circleId";
    public static final String IS_MEMBER_KEY = "isMember";

    private Callbacks mListener;
    private Circle mCircle;
    private boolean mIsMember;

    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private ImageView mIcon;

    public interface Callbacks {
        void onMemberClicked(String memberId);
        void onCircleDestroyed();
        void onHomeClicked();
    }

    /**
     * A factory method to return a new CircleDetailFragment that has been configured
     *
     * @param circleId the object id of the circle to display
     * @param isMember true if the current user is a member of the circle, false if not
     * @return a new CircleDetailFragment that has been configured
     */
    public static CircleDetailFragment newInstance(String circleId, boolean isMember) {
        Bundle args = new Bundle();
        args.putString(CIRCLE_ID_KEY, circleId);
        args.putBoolean(IS_MEMBER_KEY, isMember);
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
        mCircle = ParseObject.createWithoutData(Circle.class, getArguments().getString(CIRCLE_ID_KEY));
        mIsMember = getArguments().getBoolean(IS_MEMBER_KEY);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflateFragment(R.layout.fragment_circle_detail, inflater, container);

        // Request the circle icon to display
        mIcon = (ImageView) view.findViewById(R.id.circle_icon);
        ParseFile imageFile = mCircle.getIcon();
        if (imageFile != null) {
            Glide.with(getContext())
                    .load(Uri.parse(imageFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mIcon);
        }

        // Setup the FAB to handle the user leaving or joining the circle
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsMember) {
                    leaveCircle();
                }

                else {
                    joinCircle();
                }
            }
        });

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        setupToolbar();
        changeFabIcon();

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

    @Override
    protected UserAdapter buildAdapter() {
        return new UserAdapter(getContext(), getItems(), new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                mListener.onMemberClicked(getItems().get(position).getObjectId());
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
        parent.getSupportActionBar().setTitle(mCircle.getName());
    }

    /**
     * Change the floating action button's icon to represent either joining or leaving a circle
     */
    private void changeFabIcon() {
        // Determine if the FAB's icon should change from join to leave
        // getDrawable() is only available on API 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mIsMember) {
                mFab.setImageDrawable(getContext().getDrawable(R.drawable.ic_remove_white_24dp));
            }

            else {
                mFab.setImageDrawable(getContext().getDrawable(R.drawable.ic_add_white_24dp));
            }
        }

        // Provide a deprecated call to getDrawable() for APIs less than 21
        else {
            if (mIsMember) {
                mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_white_24dp));
            }

            else {
                mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp));
            }
        }
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
        userCircle.put("pending", false);

        // Save the UserCircle object to the Parse backend
        userCircle.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mIsMember = true;
                    getItems().add(ParseUser.getCurrentUser());
                    getBaseAdapter().notifyItemInserted(getItems().size() - 1);
                    changeFabIcon();
                    Toast.makeText(getContext(), "Successfully joined!", Toast.LENGTH_LONG).show();
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

                    mIsMember = false;
                    getItems().remove(ParseUser.getCurrentUser());
                    getBaseAdapter().notifyDataSetChanged();
                    changeFabIcon();

                    if (isCircleDestroyed) {
                        mListener.onCircleDestroyed();
                        Toast.makeText(getContext(), "Circle destroyed!", Toast.LENGTH_LONG).show();
                    }

                    else {
                        Toast.makeText(getContext(), "Left circle!", Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that presents information about a circle to the user
 *
 * @author Steven Briggs
 * @version 2015.10.03
 */
public class CircleDetailFragment extends Fragment {

    public static final String TAG = CircleDetailFragment.class.getSimpleName();

    private static final String CIRCLE_ID_KEY = "circleId";
    private static final String CIRCLE_NAME_KEY = "name";
    private static final String IS_MEMBER_KEY = "isMember";

    private Circle mCircle;
    private RecyclerView mRecyclerView;
    private Button mActionButton;

    /**
     * A factory method to return a new CircleDetailFragment that has been configured
     *
     * @param circle the circle whose details are to be viewed
     * @param isMember true if the user is a member of the circle, false if not
     * @return a new CircleDetailFragment that has been configured
     */
    public static CircleDetailFragment newInstance(Circle circle, boolean isMember) {
        Bundle args = new Bundle();
        args.putString(CIRCLE_ID_KEY, circle.getObjectId());
        args.putString(CIRCLE_NAME_KEY, circle.getName());
        args.putBoolean(IS_MEMBER_KEY, isMember);
        CircleDetailFragment fragment = new CircleDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mCircle = new Circle(args.getString(CIRCLE_ID_KEY), args.getString(CIRCLE_NAME_KEY));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_circle_detail, container, false);

        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsingToolbarLayout);
        toolbarLayout.setTitle(mCircle.getName());

        mCircle.setMembers(getCircleMembers());
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(new MemberAdapter(mCircle.getMembers()));

        mActionButton = (Button) view.findViewById(R.id.action);
        initializeActionButton(getArguments().getBoolean(IS_MEMBER_KEY));

        return view;
    }

    /**
     * Query a list of all members within the Circle
     *
     * @return a list of all members within the Circle
     */
    private ArrayList<String> getCircleMembers() {
        final ArrayList<String> members = new ArrayList<String>();

        // Get all of the members of the Circle
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("UserCircle");
        ParseObject circle = ParseObject.createWithoutData("Circle", mCircle.getObjectId());
        query.whereEqualTo("circle", circle);
        query.include("user");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> userCircles, ParseException e) {
                if (e == null) {
                    for (ParseObject uc : userCircles) {
                        members.add(uc.getParseObject("user").getString("username"));
                    }

                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }

                else {
                    Toast.makeText(getContext(), "Couldn't load members!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return members;
    }

    /**
     * Setup the circle action button to handle the current user joining / leaving the circle
     *
     * @param isMember true if the user is member of the circle, false if not
     */
    private void initializeActionButton(boolean isMember) {
        if (isMember) {
            mActionButton.setText("LEAVE");
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leaveCircle();
                }
            });
        }

        else {
            mActionButton.setText("JOIN");
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    joinCircle();
                }
            });
        }
    }

    /**
     * Add the current user as a member to the circle
     */
    private void joinCircle() {

        // Create a UserCircle object to represent the new relationship
        ParseObject userCircle = new ParseObject("UserCircle");
        userCircle.put("user", ParseUser.getCurrentUser());
        userCircle.put("circle", ParseObject.createWithoutData("Circle", mCircle.getObjectId()));

        // Save the UserCircle object to the Parse backend
        userCircle.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                // Success! Let's inform the user they have joined
                if (e == null) {
                    mCircle.getMembers().add(ParseUser.getCurrentUser().getUsername());
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    initializeActionButton(true);
                    Toast.makeText(getContext(), "Successfully joined!", Toast.LENGTH_LONG).show();
                }

                // Failure! Let's let the user know about what went wrong
                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Remove the current user as a member from the circle
     */
    private void leaveCircle() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("UserCircle");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("circle", ParseObject.createWithoutData("Circle", mCircle.getObjectId()));

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> userCircles, ParseException e) {

                // Success! Let's try to remove the current user to the circle
                if (e == null) {

                    try {
                        userCircles.get(0).delete();
                    }

                    catch (ParseException e1) {
                        // Display an error message and exit out of the function early
                        Toast.makeText(getContext(), e1.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Update the list of current users for the circle
                    mCircle.getMembers().remove(ParseUser.getCurrentUser().getUsername());
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    initializeActionButton(false);
                    Toast.makeText(getContext(), "Successfully left!", Toast.LENGTH_LONG).show();
                }

                // Failure! Let's let the user know about what went wrong
                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

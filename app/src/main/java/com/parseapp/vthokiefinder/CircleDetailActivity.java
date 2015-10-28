package com.parseapp.vthokiefinder;


import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An activity that presents information about a circle to the user
 *
 * @author Steven Briggs
 * @version 2015.10.05
 */
public class CircleDetailActivity extends AppCompatActivity {

    public static final String CIRCLE_ID_KEY = "circleId";
    public static final String IS_MEMBER_KEY = "isMember";

    private Circle mCircle;
    private ArrayList<ParseUser> mMembers;

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_detail);
        mMembers = new ArrayList<ParseUser>();

        // Retrieve the requested circle from the Parse backend
        try {
            mCircle = Circle.getQuery().get(getIntent().getStringExtra(CIRCLE_ID_KEY));
        }

        catch (ParseException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Initialize the Toolbar to be the ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        initializeCircleAction(getIntent().getBooleanExtra(IS_MEMBER_KEY, false));
        initializeRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_circle_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup the circle action button to handle the current user joining / leaving the circle
     *
     * @param isMember true if the user is member of the circle, false if not
     */
    private void initializeCircleAction(boolean isMember) {

        // Determine if the FAB's icon should change from join to leave
        // getDrawable() is only available on API 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isMember) {
                mFab.setImageDrawable(getDrawable(R.drawable.ic_remove_white_24dp));
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        leaveCircle();
                    }
                });
            }

            else {
                mFab.setImageDrawable(getDrawable(R.drawable.ic_add_white_24dp));
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        joinCircle();
                    }
                });
            }
        }

        // Provide a deprecated call to getDrawable() for APIs less than 21
        else {
            if (isMember) {
                mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_white_24dp));
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        leaveCircle();
                    }
                });
            }

            else {
                mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp));
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        joinCircle();
                    }
                });
            }
        }
    }

    /**
     * Setup the RecyclerView containing a list of members within the circle
     */
    private void initializeRecyclerView() {
        mMembers = getMembers();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(new MemberAdapter(mMembers));
    }

    /**
     * Add the current user as a member to the circle
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

                // Success! Let's inform the user they have joined
                if (e == null) {
                    mMembers.add(ParseUser.getCurrentUser());
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    initializeCircleAction(true);
                    Toast.makeText(CircleDetailActivity.this, "Successfully joined!", Toast.LENGTH_LONG).show();
                }

                // Failure! Let's let the user know about what went wrong
                else {
                    Toast.makeText(CircleDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Remove the current user as a member from the circle
     */
    private void leaveCircle() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("circle", mCircle);

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {

                // Success! Let's try to remove the current user from the circle
                if (e == null) {
                    callDeleteUserFromCircle();
                    finish();
                }

                // Failure! Let's let the user know about what went wrong
                else {
                    Toast.makeText(CircleDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Call the "deleteUserFromCircle()" function on the Parse backend
     */
    private void callDeleteUserFromCircle() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("circleId", mCircle.getObjectId());
        params.put("userId", ParseUser.getCurrentUser().getObjectId());

        ParseCloud.callFunctionInBackground("removeUserFromCircle", params, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean isCircleDeleted, ParseException e) {
                if (e == null && isCircleDeleted) {
                    Toast.makeText(CircleDetailActivity.this,
                            "No members remaining. Circle deleted!", Toast.LENGTH_LONG).show();
                }

                else if (e == null && !isCircleDeleted) {
                    Toast.makeText(CircleDetailActivity.this,
                            "Successfully left circle!", Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(CircleDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Query a list of all members within the circle
     *
     * @return a list of all members within the circle
     */
    private ArrayList<ParseUser> getMembers() {
        final ArrayList<ParseUser> members = new ArrayList<ParseUser>();

        // Get all of the members of the Circle
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("circle", mCircle).include("user");
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null) {
                    for (UserCircle uc : userCircles) {
                        members.add(uc.getParseUser("user"));
                    }

                    mRecyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    Toast.makeText(CircleDetailActivity.this, "Couldn't load members!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return members;
    }
}

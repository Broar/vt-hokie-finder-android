package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity that presents information about a specific Circle to the user
 *
 * @author Steven Briggs
 * @version 2015.09.17
 */
public class CircleDetailActivity extends AppCompatActivity {
    public static final String CIRCLE_OBJECT_ID_KEY = "circle_object_id";
    public static final String CIRCLE_NAME_KEY = "circle_name";
    public static final String CIRCLE_ACTION_KEY = "circle_action";
    public static final int JOIN_ACTION = 0;
    public static final int LEAVE_ACTION = 1;

    private Circle mCircle;
    private TextView mCircleName;
    private Button mCircleAction;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // Initialize the TextView
        String objectId = getIntent().getStringExtra(CIRCLE_OBJECT_ID_KEY);
        String name = getIntent().getStringExtra(CIRCLE_NAME_KEY);
        mCircle = new Circle(objectId, name);
        mCircleName = (TextView) findViewById(R.id.circleName);
        mCircleName.setText(name);

        // Initialize the Join Button
        mCircleAction = (Button) findViewById(R.id.circleAction);
        setupCircleAction(getIntent().getIntExtra(CIRCLE_ACTION_KEY, 0));

        // Initialize the RecyclerView
        mCircle.setMembers(getCircleMembers());
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(new MemberAdapter(mCircle.getMembers()));
    }

    /**
     * Setup the onClickListener for the CircleAction button
     *
     * @param action the action to be performed when the CircleAction button is clicked
     */
    private void setupCircleAction(int action) {
        if (action == JOIN_ACTION) {
            mCircleAction.setText("Join");
            mCircleAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    joinCircle();
                }
            });
        }

        else {
            mCircleAction.setText("Leave");
            mCircleAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leaveCircle();
                }
            });
        }
    }

    /**
     * Add the logged in ParseUser to this circle
     */
    private void joinCircle() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Circle");
        query.getInBackground(mCircle.getObjectId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                // Success! Let's try to add the current user to the circle
                if (e == null) {
                    ParseRelation<ParseObject> relation = object.getRelation("members");
                    relation.add(ParseUser.getCurrentUser());
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            // Success! Let's inform the user they have joined
                            if (e == null) {
                                mCircle.getMembers().add(ParseUser.getCurrentUser().getUsername());
                                mRecyclerView.getAdapter().notifyDataSetChanged();
                                setupCircleAction(LEAVE_ACTION);
                                Toast.makeText(CircleDetailActivity.this, "Successfully joined!", Toast.LENGTH_LONG).show();
                            }

                            // Failure! Let's let the user know about what went wrong
                            else {
                                Toast.makeText(CircleDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // Failure! Let's let the user know about what went wrong
                else {
                    Toast.makeText(CircleDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Remove the logged in ParseUser from this Circle
     */
    private void leaveCircle() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Circle");
        query.getInBackground(mCircle.getObjectId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                // Success! Let's try to remove the current user to the circle
                if (e == null) {
                    ParseRelation<ParseObject> relation = object.getRelation("members");
                    relation.remove(ParseUser.getCurrentUser());
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            // Success! Let's inform the user they have joined
                            if (e == null) {
                                mCircle.getMembers().remove(ParseUser.getCurrentUser().getUsername());
                                mRecyclerView.getAdapter().notifyDataSetChanged();
                                setupCircleAction(JOIN_ACTION);
                                Toast.makeText(CircleDetailActivity.this, "Successfully left!", Toast.LENGTH_LONG).show();
                            }

                            // Failure! Let's let the user know about what went wrong
                            else {
                                Toast.makeText(CircleDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // Failure! Let's let the user know about what went wrong
                else {
                    Toast.makeText(CircleDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Query a list of all members within the Circle
     *
     * @return a list of all members within the Circle
     */
    private ArrayList<String> getCircleMembers() {
        final ArrayList<String> members = new ArrayList<String>();

        // Get all of the members of the Circle
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Circle");
        query.getInBackground(mCircle.getObjectId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {

                // Success! Try to query all of the members belonging to this Circle
                if (e == null) {
                    ParseRelation<ParseObject> relation = object.getRelation("members");
                    relation.getQuery().findInBackground(new FindCallback<ParseObject>() {

                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            // Success! Collect all of the names of the members
                            if (e == null) {
                                for (ParseObject o : objects) {
                                    members.add(o.getString("username"));
                                }

                                // Be sure to update the RecyclerView
                                mRecyclerView.getAdapter().notifyDataSetChanged();
                            }

                            // Failure! Let the user know what went wrong
                            else {
                                Toast.makeText(CircleDetailActivity.this, "Couldn't load members!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // Failure! Let the user know what went wrong
                else {
                    Toast.makeText(CircleDetailActivity.this, "Couldn't load members!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return members;
    }
}

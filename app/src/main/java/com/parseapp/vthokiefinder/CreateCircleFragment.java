package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;


/**
 * A fragment allowing the user to set the primary details of a new circle
 *
 * @author Steven Briggs
 * @version 2015.10.06
 */
public class CreateCircleFragment extends Fragment {

    public static final String TAG = CreateCircleFragment.class.getSimpleName();

    private Callbacks mListener;

    private EditText mCircleName;
    private EditText mCircleDescription;
    private Button mInviteFriends;
    private Button mSaveCircle;

    public interface Callbacks {
        void onSaveSuccessful(Circle circle);
    }

    /**
     * A factory method to return a new CreateCircleFragment that has been configured
     *
     * @return a new CreateCircleFragment that has been configured
     */
    public static CreateCircleFragment newInstance() {
        return new CreateCircleFragment();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_circle, container, false);
        mCircleName = (EditText) view.findViewById(R.id.circleName);
        mCircleDescription = (EditText) view.findViewById(R.id.circleDescription);
        initializeInviteFriends(view);
        initializeSave(view);
        return view;
    }

    /**
     * Setup the button to invite a user's friends
     *
     * @param view the parent view of the button
     */
    private void initializeInviteFriends(View view) {
        mInviteFriends = (Button) view.findViewById(R.id.inviteFriends);
        mInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * Setup the button to save a new circle
     *
     * @param view the parent view of the button
     */
    private void initializeSave(View view) {
        mSaveCircle = (Button) view.findViewById(R.id.saveCircle);
        mSaveCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mCircleName.getText().toString();
                String description = mCircleDescription.getText().toString();

                // A circle's name is a required field
                if (!name.isEmpty()) {
                    saveCircle(name, description);
                }

                else {
                    mCircleName.setError("Missing name");
                }
            }
        });
    }

    /**
     * Save the new circle to the Parse backend
     *
     * @param name the name of the circle
     * @param description the description of the circle
     */
    private void saveCircle(String name, String description) {

        // First, we must save the Circle
        final Circle circle = ParseObject.create(Circle.class);
        circle.setName(mCircleName.getText().toString());
        circle.setDescription(mCircleDescription.getText().toString());
        circle.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // Success! Create a new UserCircle relationship
                if (e == null) {
                    UserCircle uc = ParseObject.create(UserCircle.class);
                    uc.setCircle(circle);
                    uc.setUser(ParseUser.getCurrentUser());
                    uc.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            // Success! Let the parent activity know that we are done
                            if (e == null) {
                                mListener.onSaveSuccessful(circle);
                            }

                            // Failure! Inform the user about the error
                            else {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // Failure! Inform the user about the error
                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

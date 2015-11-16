package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * A fragment allowing the user to set the primary details of a new circle
 *
 * @author Steven Briggs
 * @version 2015.10.22
 */
public class CreateCircleFragment extends Fragment {

    public static final String TAG = CreateCircleFragment.class.getSimpleName();

    private Callbacks mListener;

    private ImageView mCircleIcon;
    private EditText mCircleName;
    private EditText mCircleDescription;
    private Button mInviteFriends;
    private Button mSaveCircle;

    public interface Callbacks {
        void onIconBitmapSet(Bitmap bm);
        Bitmap onIconBitmapRequested();
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
        initializeIconPicker(view);
        initializeInviteFriends(view);
        initializeSave(view);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.PICK_IMAGE) {
            Uri imageUri = data.getData();

            // Change the icon to display the user's selected image
            try {
                Bitmap bm = ImagePicker.getBitmapFromUri(getContext(), imageUri);
                mCircleIcon.setImageBitmap(bm);

                // Remember to save the Bitmap, so that the user's image can be redisplayed
                // upon returning from configuration changes
                mListener.onIconBitmapSet(bm);
            }

            catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Could not load image!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Setup the circle icon picker
     *
     * @param view the parent view of the image
     */
    private void initializeIconPicker(View view) {
        mCircleIcon = (ImageView) view.findViewById(R.id.circle_icon);

        // If the user already selected an image, then redisplay it
        Bitmap bm = mListener.onIconBitmapRequested();
        if (bm != null) {
            mCircleIcon.setImageBitmap(bm);
        }

        mCircleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(ImagePicker.getIntent(), ImagePicker.PICK_IMAGE);
            }
        });
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
                // Does nothing for now
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

        // Retrieve the byte array from the ImageView
        mCircleIcon.setDrawingCacheEnabled(true);
        mCircleIcon.buildDrawingCache();
        Bitmap bm = mCircleIcon.getDrawingCache();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);

        circle.setIcon(new ParseFile(stream.toByteArray()));
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

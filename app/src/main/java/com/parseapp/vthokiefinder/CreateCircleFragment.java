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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;


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

    public interface Callbacks {
        void onImageSet(Uri imageUri);
        Uri onImageUriRequested();
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
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The user selected an image, so display it as the circle icon
        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.PICK_IMAGE) {
            Uri imageUri = data.getData();

            Glide.with(this)
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mCircleIcon);

            mListener.onImageSet(imageUri);
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
        if (mListener.onImageUriRequested() != null) {
            Glide.with(this)
                    .load(mListener.onImageUriRequested())
                    .into(mCircleIcon);
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
     * Save the user's newly created Circle to the backend
     */
    public void saveCircle() {

        String name = mCircleName.getText().toString().trim();
        String description = mCircleDescription.getText().toString().trim();

        // The circle name is a require field. Display an error and do not save if it is empty
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        final Circle circle = ParseObject.create(Circle.class);

        // Retrieve the byte array from the ImageView
        mCircleIcon.setDrawingCacheEnabled(true);
        mCircleIcon.buildDrawingCache();
        Bitmap bm = mCircleIcon.getDrawingCache();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        circle.setIcon(new ParseFile("icon.jpg", stream.toByteArray()));
        circle.setName(name);
        circle.setDescription(description);
        circle.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                // Add the user as the first member of the new circle
                if (e == null) {
                    UserCircle uc = ParseObject.create(UserCircle.class);
                    uc.setCircle(circle);
                    uc.setUser(ParseUser.getCurrentUser());
                    uc.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            // Inform the parent activity we are finished
                            if (e == null) {
                                mListener.onSaveSuccessful(circle);
                            }

                            else {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A fragment allowing the user to set the primary details of a new circle
 *
 * @author Steven Briggs
 * @version 2015.11.29
 */
public class CreateCircleFragment extends Fragment {

    public static final String TAG = CreateCircleFragment.class.getSimpleName();

    private static final int MAXIMUM_GEOCODER_RESULTS = 10;
    private static final String CURRENT_LOCATION_ERRORR = "Couldn't fetch your location. Please try again later";
    private static final String NO_GEOCODER_WARNING = "Your device cannot perform address lookups. Do you still want to create a community?";
    private static final String ADDRESS_LOOKUP_ERROR = "Couldn't determine your address. Please try again later";

    private Callbacks mListener;

    private Toolbar mToolbar;
    private ImageView mCircleIcon;
    private EditText mCircleName;
    private EditText mCircleDescription;
    private CheckBox mIsCommunity;
    private Button mInviteFriends;

    public interface Callbacks {
        void onImageSet(Uri imageUri);
        Uri onImageUriRequested();
        void onInviteFriendsClicked();
        void onSaveSuccessful(Circle circle);
        void onCurrentLocationRequested(GoogleApiManagerFragment.OnLocationFoundListener listener);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_circle, container, false);
        bindFragment(view);
        setupToolbar();
        setupIconSelect();
        setupInviteFriends();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_create_circle, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showDiscardDialog();
                return true;

            case R.id.action_save_circle:
                createNewCircle();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
     * Bind the fragment to the views of its layout
     *
     * @param view the layout view
     */
    private void bindFragment(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mCircleName = (EditText) view.findViewById(R.id.name);
        mCircleDescription = (EditText) view.findViewById(R.id.description);
        mCircleIcon = (ImageView) view.findViewById(R.id.icon);
        mIsCommunity = (CheckBox) view.findViewById(R.id.is_community);
        mInviteFriends = (Button) view.findViewById(R.id.invite_friends);
    }

    /**
     * Setup the circle icon picker
     */
    private void setupIconSelect() {
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
     */
    private void setupInviteFriends() {
        mInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onInviteFriendsClicked();
            }
        });
    }

    /**
     * Setup the Toolbar to be the SupportActionBar
     */
    private void setupToolbar() {
        AppCompatActivity parent = (AppCompatActivity) getActivity();
        parent.setSupportActionBar(mToolbar);
        parent.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        parent.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    public void createNewCircle() {
        String name = mCircleName.getText().toString().trim();
        String description = mCircleDescription.getText().toString().trim();

        // The circle name is a require field. Display an error and do not save if it is empty
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        final Circle newCircle = ParseObject.create(Circle.class);

        // Retrieve the byte array from the ImageView
        mCircleIcon.setDrawingCacheEnabled(true);
        mCircleIcon.buildDrawingCache();
        Bitmap bm = mCircleIcon.getDrawingCache();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        newCircle.setIcon(new ParseFile("icon.jpg", stream.toByteArray()));
        newCircle.setName(name);
        newCircle.setDescription(description);
        newCircle.setIsCommunity(mIsCommunity.isChecked());

        // If this is a community, then retrieve the user's current location and address
        if (mIsCommunity.isChecked()) {
            mListener.onCurrentLocationRequested(new GoogleApiManagerFragment.OnLocationFoundListener() {
                @Override
                public void onLocationFound(Location location) {
                    ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(),
                            location.getLongitude());
                    newCircle.setLocation(geoPoint);
                    handleReverseGeocode(newCircle, geoPoint);
                }

                @Override
                public void onLocationNotFound() {
                    Toast.makeText(getContext(), CURRENT_LOCATION_ERRORR, Toast.LENGTH_LONG).show();
                }
            });
        }

        // Otherwise, just save the circle as normal
        else {
            saveCircle(newCircle);
        }
    }

    /**
     * Determine the user's current address via a reverse geocode lookup
     *
     * @param circle the circle
     * @param location the current location of the user
     */
    private void handleReverseGeocode(final Circle circle, ParseGeoPoint location) {
        // Perform the reverse geocode lookup of the user's address
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> foundAddresses = new ArrayList<Address>();

            try {
                foundAddresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), MAXIMUM_GEOCODER_RESULTS);
            }

            catch (IOException e) {
                e.printStackTrace();
            }

            // Extract a locality and administrative area from one of the addresses
            String formattedAddress = "";
            for (int i = 0; i < foundAddresses.size(); i++) {
                String locality = foundAddresses.get(i).getLocality();
                String adminArea = foundAddresses.get(i).getAdminArea();

                if (locality != null && adminArea != null) {
                    circle.setCity(locality);
                    circle.setState(adminArea);
                    formattedAddress = locality + ", " + adminArea;
                    break;
                }
            }

            // Display a dialog for the user to confirm the community location
            if (!formattedAddress.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Create a community in " + formattedAddress + "?")
                        .setCancelable(true)
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveCircle(circle);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        });
                builder.create().show();
            }

            else {
                Toast.makeText(getContext(), ADDRESS_LOOKUP_ERROR, Toast.LENGTH_LONG).show();
            }
        }

        // Display a dialog warning the user that their community will not have an address
        // associated with it
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(NO_GEOCODER_WARNING)
                    .setCancelable(true)
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveCircle(circle);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    });
            builder.create().show();
        }
    }

    /**
     * Save the user's newly created circle
     */
    private void saveCircle(final Circle circle) {
        circle.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // Add the user as the first member of the new circle
                if (e == null) {
                    UserCircle uc = ParseObject.create(UserCircle.class);
                    uc.setCircle(circle);
                    uc.setUser(ParseUser.getCurrentUser());
                    uc.setIsBroadcasting(false);
                    uc.setIsPending(false);
                    uc.setIsAccepted(true);
                    uc.setIsInvite(false);

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

    /**
     * Display a dialog prompting users to confirm they want exit and discard all changes
     */
    private void showDiscardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Discard new circle?")
                .setCancelable(true)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(getActivity());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });

        builder.create().show();
    }
}

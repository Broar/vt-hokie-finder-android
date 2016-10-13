package com.parseapp.vthokiefinder.circles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.parseapp.vthokiefinder.R;
import com.parseapp.vthokiefinder.model.Circle;
import com.parseapp.vthokiefinder.common.ConfirmDialog;
import com.parseapp.vthokiefinder.utils.ImagePicker;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A fragment that allows the user to edit the details of a circle
 *
 * @author Steven Briggs
 * @version 2015.11.20
 */
public class EditCircleFragment extends Fragment {
    public static final String TAG = EditCircleFragment.class.getSimpleName();

    private static final String CIRCLE_ID_KEY = "circleId";

    private Callbacks mListener;

    private Circle mCircle;

    private Toolbar mToolbar;
    private CircleImageView mIcon;
    private EditText mName;
    private EditText mDescription;

    public interface Callbacks {
        void onImageSet(Uri imageUri);
        Uri onImageUriRequested();
        void onSaveSuccessful();
    }

    /**
     * A factory method to return a new EditCircleFragment that has been configured
     *
     * @param circleId the object id of the circle to be edited
     * @return a new EditCircleFragment that has been configured
     */
    public static EditCircleFragment newInstance(String circleId) {
        Bundle args = new Bundle();
        args.putString(CIRCLE_ID_KEY, circleId);
        EditCircleFragment fragment = new EditCircleFragment();
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
        mCircle = ParseObject.createWithoutData(Circle.class, getArguments().getString(CIRCLE_ID_KEY));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_circle, container, false);
        bindFragment(view);
        setupToolbar();
        loadIcon();
        mName.setText(mCircle.getName());
        mDescription.setText(mCircle.getDescription());
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
                    .into(mIcon);

            mListener.onImageSet(imageUri);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_circle, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showDiscardChangesDialog();
                return true;
            case R.id.action_save_circle:
                saveCircle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Bind the fragment to the views of its layout
     *
     * @param view the layout view
     */
    private void bindFragment(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mIcon = (CircleImageView) view.findViewById(R.id.icon);
        mName = (EditText) view.findViewById(R.id.name);
        mDescription = (EditText) view.findViewById(R.id.description);
    }

    /**
     * Setup the toolbar to function as the support action bar
     */
    private void setupToolbar() {
        AppCompatActivity parent = (AppCompatActivity) getActivity();
        parent.setSupportActionBar(mToolbar);
        parent.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        parent.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Load an image into the circle icon
     */
    private void loadIcon() {
        // Determine if the user already selected an image. If so, then display it; otherwise,
        // show the user the circle's original icon
        if (mListener.onImageUriRequested() != null) {
            Glide.with(this)
                    .load(mListener.onImageUriRequested())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mIcon);
        }

        else {
            ParseFile imageFile = mCircle.getIcon();
            if (imageFile != null) {
                Glide.with(this)
                        .load(Uri.parse(imageFile.getUrl()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mIcon);
            }
        }

        mIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(ImagePicker.getIntent(), ImagePicker.PICK_IMAGE);
            }
        });
    }

    /**
     * Save the user's edits to this circle
     */
    public void saveCircle() {

        String name = mName.getText().toString().trim();
        String description = mDescription.getText().toString().trim();

        // The circle name is a require field. Display an error and do not save if it is empty
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        // Retrieve the byte array from the ImageView
        mIcon.setDrawingCacheEnabled(true);
        mIcon.buildDrawingCache();
        Bitmap bm = mIcon.getDrawingCache();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        mCircle.setIcon(new ParseFile("icon.jpg", stream.toByteArray()));
        mCircle.setName(name);
        mCircle.setDescription(description);

        // Save any edits to the circle
        mCircle.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mListener.onSaveSuccessful();

                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Display a dialog to the user to confirm they wish to discard all their changes to
     * the circle
     */
    private void showDiscardChangesDialog() {
        ConfirmDialog dialog = ConfirmDialog.newInstance("Discard all changes?", "Confirm", "Cancel");
        dialog.show(getActivity().getSupportFragmentManager(), ConfirmDialog.TAG);
    }
}

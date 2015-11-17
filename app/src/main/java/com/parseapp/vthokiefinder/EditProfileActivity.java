package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * An activity that allows the user to edit their profile
 *
 * @author Steven Briggs
 * @version 2015.11.17
 */
public class EditProfileActivity extends AppCompatActivity implements ConfirmDialog.Callbacks {

    private RetainedFragment<Uri> mAvatarUriHolder;

    private CircleImageView mAvatar;
    private EditText mUsername;
    private EditText mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_edit_profile);

        FragmentManager fm = getSupportFragmentManager();

        // Create a new instance of a fragment to retain the URI of the image the user selects
        // to use an avatar
        if (savedInstanceState == null) {
            mAvatarUriHolder = new RetainedFragment<Uri>();
            fm.beginTransaction()
                    .add(mAvatarUriHolder, RetainedFragment.TAG)
                    .commit();
        }

        // Obtain a reference to the existing retained fragment
        else {
            mAvatarUriHolder = (RetainedFragment<Uri>) fm.findFragmentByTag(RetainedFragment.TAG);
        }

        // Obtain references to all of our views and set them up
        mAvatar = (CircleImageView) findViewById(R.id.avatar);
        mUsername = (EditText) findViewById(R.id.username);
        mEmail = (EditText) findViewById(R.id.email);

        mUsername.setText(ParseUser.getCurrentUser().getUsername());
        mEmail.setText(ParseUser.getCurrentUser().getEmail());

        // The user should be able to select an image whenever clicking on their avatar
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(ImagePicker.getIntent(), ImagePicker.PICK_IMAGE);
            }
        });

        // Redisplay the user's selected image if necessary
        if (mAvatarUriHolder.getData() != null) {
            Glide.with(this)
                    .load(mAvatarUriHolder.getData())
                    .into(mAvatar);
        }

        // Otherwise, just display the user's current avatar
        else if (ParseUser.getCurrentUser().getParseFile("avatar") != null) {
            ParseFile avatarFile = ParseUser.getCurrentUser().getParseFile("avatar");
            Glide.with(this)
                    .load(Uri.parse(avatarFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mAvatar);
        }

        // Setup the action bar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showDiscardChangesDialog();
                return true;
            case R.id.action_save_profile:
                saveChangesToProfile();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The user selected an image, so display it as their avatar
        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.PICK_IMAGE) {
            Uri imageUri = data.getData();
            mAvatarUriHolder.setData(imageUri);
            Glide.with(this)
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mAvatar);
        }
    }

    /**
     * Present a confirmation dialog to the user to determine if they want discard any changes
     * to their profile
     */
    private void showDiscardChangesDialog() {
        ConfirmDialog dialog = ConfirmDialog.newInstance("Discard any changes?", "Confirm", "Cancel");
        dialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);
    }

    @Override
    public void onPositiveButtonClicked() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onNegativeButtonClicked() {
        // Do nothing
    }

    /**
     * Save any changes to the user's profile information
     */
    public void saveChangesToProfile() {
        String username = mUsername.getText().toString().trim();
        String email = mEmail.getText().toString().trim();

        // Save the updates to the username, email, and avatar fields
        // Perform the profile update only if the username / email fields are non-empty
        if (!username.isEmpty() && !email.isEmpty()) {

            ParseUser.getCurrentUser().put("username", username);
            ParseUser.getCurrentUser().put("email", email);

            // Retrieve the byte array for the user's avatar
            mAvatar.setDrawingCacheEnabled(true);
            mAvatar.buildDrawingCache();
            Bitmap bm = mAvatar.getDrawingCache();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            ParseUser.getCurrentUser().put("avatar", new ParseFile("username.jpg", stream.toByteArray()));

            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(EditProfileActivity.this, "Profile saved!", Toast.LENGTH_LONG).show();
                        NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
                    }

                    else {
                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        // Inform the user they must fill in the fields if they did not
        else {
            Toast.makeText(this, "Username / email cannot be empty", Toast.LENGTH_LONG).show();
        }
    }
}

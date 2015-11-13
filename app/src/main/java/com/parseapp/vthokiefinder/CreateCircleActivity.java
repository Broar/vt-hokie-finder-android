package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity allowing the user to create a new circle
 *
 * @author Steven Briggs
 * @version 2015.10.26
 */
public class CreateCircleActivity extends AppCompatActivity implements
        CreateCircleFragment.Callbacks,
        ConfirmDialog.Callbacks {

    private CreateCircleFragment mCreateCircleFragment;
    private BitmapHolderFragment mBitmapHolderFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_circle);

        // Initialize the Toolbar to be the ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        FragmentManager fm = getSupportFragmentManager();

        // Create new instances of the fragments for circle creation
        if (savedInstanceState == null) {
            mCreateCircleFragment = CreateCircleFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, mCreateCircleFragment, CreateCircleFragment.TAG)
                    .commit();

            mBitmapHolderFragment = new BitmapHolderFragment();
            fm.beginTransaction()
                    .add(mBitmapHolderFragment, BitmapHolderFragment.TAG)
                    .commit();
        }

        // Retrieve the existing fragment instances
        else {
            mCreateCircleFragment = (CreateCircleFragment) fm.findFragmentByTag(CreateCircleFragment.TAG);
            mBitmapHolderFragment = (BitmapHolderFragment) fm.findFragmentByTag(BitmapHolderFragment.TAG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_circle, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showDiscardDialog();
                return true;
            case R.id.action_save_circle:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Users should only be prompted to discarded changes if they actually made any
        if (mCreateCircleFragment.isDirty()) {
            showDiscardDialog();
        }

        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onIconBitmapSet(Bitmap bm) {
        mBitmapHolderFragment.setIconBitmap(bm);
    }

    @Override
    public Bitmap onIconBitmapRequested() {
        return mBitmapHolderFragment.getIconBitmap();
    }

    @Override
    public void onSaveSuccessful(Circle circle) {
        // Finish this Activity. Open the detail view of the newly created circle
        finish();
        Intent intent = new Intent(this, CircleDetailActivity.class);
        intent.putExtra(CircleDetailActivity.CIRCLE_ID_KEY, circle.getObjectId());
        startActivity(intent);
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
     * Display a dialog prompting users to confirm they want exit and discard all changes
     */
    private void showDiscardDialog() {
        ConfirmDialog dialog = ConfirmDialog.newInstance("Discard new circle?", "Confirm", "Cancel");
        dialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);
    }
}

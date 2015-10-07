package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity allowing the user to create a new circle
 *
 * @author Steven Briggs
 * @version 2015.10.06
 */
public class CreateCircleActivity extends AppCompatActivity implements
        CreateCircleFragment.Callbacks {

    private CreateCircleFragment mCreateCircleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_circle);

        // Initialize the Toolbar to be the ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Circle");
        }

        FragmentManager fm = getSupportFragmentManager();

        // Create new instances of the fragments for circle creation
        if (savedInstanceState == null) {
            mCreateCircleFragment = CreateCircleFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, mCreateCircleFragment, CreateCircleFragment.TAG)
                    .commit();
        }

        // Retrieve the existing fragment instances
        else {
            mCreateCircleFragment = (CreateCircleFragment) fm.findFragmentByTag(CreateCircleFragment.TAG);
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
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveSuccessful(Circle circle) {
        // Finish this Activity. Open the detail view of the newly created circle
        finish();
        Intent intent = new Intent(this, CircleDetailActivity.class);
        intent.putExtra(CircleDetailActivity.CIRCLE_ID_KEY, circle.getObjectId());
        intent.putExtra(CircleDetailActivity.IS_MEMBER_KEY, true);
        startActivity(intent);
    }
}

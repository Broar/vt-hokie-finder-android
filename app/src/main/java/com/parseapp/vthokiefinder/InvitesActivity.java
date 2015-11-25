package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * An activity that allows the user to manage their circle/friend invites and requests
 *
 * @author Steven Briggs
 * @version 2015.11.23
 */
public class InvitesActivity extends AppCompatActivity implements ViewPagerAdapter.Callbacks {

    private static final String[] TITLES = { "CIRCLE INVITES", "CIRCLE REQUESTS",
            "MEMBERSHIP REQUESTS", "FRIEND INVITES", "FRIEND REQUESTS" };

    private static final int CIRCLE_INCOMING = 0;
    private static final int CIRCLE_OUTGOING = 1;
    private static final int CIRCLE_MEMBERSHIP = 2;
    private static final int FRIEND_INCOMING = 3;
    private static final int FRIEND_OUTGOING = 4;

    private Toolbar mToolbar;
    private TabLayout mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabs = (TabLayout) findViewById(R.id.tabs);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), TITLES, this));
        mTabs.setupWithViewPager(pager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Fragment onItemRequested(int position) {
        switch (position) {
            case CIRCLE_INCOMING:
                return CircleInvitesFragment.newInstance(CircleInvitesFragment.TYPE_INVITES);
            case CIRCLE_OUTGOING:
                return CircleInvitesFragment.newInstance(CircleInvitesFragment.TYPE_REQUESTS);
            case CIRCLE_MEMBERSHIP:
                return CircleInvitesFragment.newInstance(CircleInvitesFragment.TYPE_MEMBERSHIP);
            case FRIEND_INCOMING:
                return FriendInvitesFragment.newInstance(FriendInvitesFragment.INCOMING_INVITES);
            case FRIEND_OUTGOING:
                return FriendInvitesFragment.newInstance(FriendInvitesFragment.OUTGOING_INVITES);
            default:
                return null;
        }
    }
}

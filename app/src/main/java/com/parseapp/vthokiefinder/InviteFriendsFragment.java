package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment that allows the user to invite friends to a circle
 *
 * @author Steven Briggs
 * @version 2015.11.26
 */
public class InviteFriendsFragment extends RecyclerFragment<ParseUser, InvitesAdapter> {

    public static final String TAG = InviteFriendsFragment.class.getSimpleName();

    private Callbacks mListener;

    public interface Callbacks {
        void onInviteClicked(ParseUser friend, boolean isInvited);
        boolean onIsInvitedRequested(ParseUser friend);
    }

    /**
     * A factory method to return a new InviteFriendsFragment that has been configured
     *
     * @return a new InviteFriendsFragment that has been configured
     */
    public static InviteFriendsFragment newInstance() {
        return new InviteFriendsFragment();
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
        View view = inflateFragment(R.layout.fragment_invite_friends, inflater, container);
        AppCompatActivity parent = (AppCompatActivity) getActivity();
        parent.setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
        parent.getSupportActionBar().setTitle("Invite friends");
        return view;
    }

    @Override
    protected InvitesAdapter buildAdapter() {
        return new InvitesAdapter(getItems(), new InvitesAdapter.OnItemClickedListener() {
            @Override
            public void onInviteClicked(int position, boolean isInvited) {
                mListener.onInviteClicked(getItems().get(position), isInvited);
            }

            @Override
            public boolean onIsInvitedRequested(int position) {
                return mListener.onIsInvitedRequested(getItems().get(position));
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());

        ParseCloud.callFunctionInBackground("getFriends", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e == null) {
                    getItems().addAll(friends);
                    getBaseAdapter().notifyDataSetChanged();
                    getAdapter().onDataReady(false);
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

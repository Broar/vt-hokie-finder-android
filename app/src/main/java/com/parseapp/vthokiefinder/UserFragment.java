package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * A fragment that displays a list of ParseUsers that belong to a circle
 *
 * @author Steven Briggs
 * @version 2015.11.12
 */
public class UserFragment extends ListFragment<ParseUser, UserAdapter> {

    public static final String TAG = UserFragment.class.getSimpleName();

    private Callbacks mListener;

    public interface Callbacks {
        void onUserClicked(String userId);
        Circle onCircleRequested();
    }

    /**
     * A factory method to return a new UserFragment that has been configured
     *
     * @return a new UserFragment that has been configured
     */
    public static UserFragment newInstance() {
        return new UserFragment();
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
        return inflateFragment(R.layout.fragment_members_list, inflater, container);
    }

    @Override
    protected UserAdapter buildAdapter() {
        return new UserAdapter(getItems(), new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                mListener.onUserClicked(getItems().get(position).getObjectId());
            }

            @Override
            public void onAddFriendClicked(int position) {
                // Add user as friend
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("circle", mListener.onCircleRequested())
                .include("user")
                .setSkip(getNextPage())
                .setLimit(getLimit());

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null) {

                    if (!userCircles.isEmpty()) {
                        for (UserCircle uc : userCircles) {
                            getItems().add(uc.getParseUser("user"));
                        }

                        getAdapter().onDataReady(true);
                    }

                    else {
                        getAdapter().onDataReady(false);
                    }

                    nextPage();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void notifyUsersChanged(ParseUser user) {
        getItems().add(user);
        getBaseAdapter().notifyItemInserted(getItems().size() - 1);
    }
}

package com.parseapp.vthokiefinder.friends;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parseapp.vthokiefinder.R;
import com.parseapp.vthokiefinder.common.RecyclerFragment;
import com.parseapp.vthokiefinder.model.Friend;

import java.util.HashMap;
import java.util.List;

/**
 * A fragment that displays a list of a specified user's friends
 *
 * @author Steven Briggs
 * @version 2015.11.03
 */
public class FriendsFragment extends RecyclerFragment<Friend, FriendAdapter> {

    public static final String TAG = FriendsFragment.class.getSimpleName();
    public static final String USER_ID_KEY = "userId";

    private Callbacks mListener;

    public interface Callbacks {
        void onFriendClicked(ParseUser friend);
    }

    /**
     * A factory method to return a new FriendsFragment that has been configured
     *
     * @param userId the id of the user
     * @return a new FriendsFragment that has been configured
     */
    public static FriendsFragment newInstance(String userId) {
        Bundle args = new Bundle();
        args.putString(USER_ID_KEY, userId);
        FriendsFragment fragment = new FriendsFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateFragment(R.layout.fragment_friends_list, inflater, container);
    }

    /**
     * Add a new friend to a user's friends list
     *
     * @param userId the id of the user
     * @param position the array position of the user who is to be added
     */
    private void addFriend(String userId, final int position) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("friendId", getItems().get(position).getObjectId());

        ParseCloud.callFunctionInBackground("createFriendship", params, new FunctionCallback<String>() {
            @Override
            public void done(String message, ParseException e) {
                if (e == null) {
                    getItems().remove(position);
                    getBaseAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Remove a friend from the user's friends list
     *
     * @param position the array position of the friend who is to removed
     */
    private void removeFriend(final int position) {
        String userId = getItems().get(position).getUser().getObjectId();
        String friendId = getItems().get(position).getFriend().getObjectId();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("friendId", friendId);

        ParseCloud.callFunctionInBackground("deleteFriendship", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if (e == null) {
                    getItems().remove(position);
                    getBaseAdapter().notifyItemRemoved(position);
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        String userId = getArguments().getString(USER_ID_KEY);

        ParseQuery<Friend> query = Friend.getQuery();
        query.whereEqualTo("user", ParseObject.createWithoutData("_User", userId))
                .whereNotEqualTo("friend", ParseUser.getCurrentUser())
                .whereEqualTo("pending", false)
                .include("friend")
                .setSkip(getNextPage())
                .setLimit(getLimit());

        query.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(List<Friend> friends, ParseException e) {
                if (e == null) {
                    if (!friends.isEmpty()) {
                        getItems().addAll(friends);
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

    @Override
    protected FriendAdapter buildAdapter() {
        return new FriendAdapter(getContext(), getItems(), new FriendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mListener.onFriendClicked(getItems().get(position).getFriend());
            }
        });
    }
}

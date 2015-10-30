package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;

/**
 * A fragment that displays a list of the user's friends
 *
 * @author Steven Briggs
 * @version 2015.10.29
 */
public class MyFriendsFragment extends FriendListFragment {

    private static final String TAG = MyFriendsFragment.class.getSimpleName();

    /**
     * A factory method to return a new MyFriendsFragment that has been configured
     *
     * @return a new MyFriendsFragment that has been configured
     */
    public static MyFriendsFragment newInstance() {
        return new MyFriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflateFragment(R.layout.fragment_friends_list, inflater, container);

        getRecyclerView().setAdapter(new FriendAdapter(getFriends(), new FriendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // TODO: Transition to a Profile screen
            }

            @Override
            public void onRemoveFriendClicked(int position) {
                removeFriend(position);
            }
        }));

        refreshFriends();
        return view;
    }

    /**
     * Refresh the user's list of friends
     */
    private void refreshFriends() {
        ParseQuery<Friend> query = Friend.getQuery().whereEqualTo("user", ParseUser.getCurrentUser()).include("friend");
        query.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(List<Friend> friends, ParseException e) {
                if (e == null) {
                    for (Friend f : friends) {
                        getFriends().add(f);
                    }

                    getRecyclerView().getAdapter().notifyDataSetChanged();
                } else {
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
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", getFriends().get(position).getUser().getObjectId());
        params.put("friendId", getFriends().get(position).getFriend().getObjectId());

        ParseCloud.callFunctionInBackground("deleteFriendship", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if (e == null) {
                    getFriends().remove(position);
                    getRecyclerView().getAdapter().notifyItemRemoved(position);
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

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
import com.rockerhieu.rvadapter.endless.EndlessRecyclerViewAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * A fragment that displays a list of the user's friends
 *
 * @author Steven Briggs
 * @version 2015.10.31
 */
public class MyFriendsFragment extends ListFragment<Friend> implements EndlessRecyclerViewAdapter.RequestToLoadMoreListener {

    private static final String TAG = MyFriendsFragment.class.getSimpleName();

    private EndlessRecyclerViewAdapter mEndlessAdapter;
    private FriendAdapter mFriendAdapter;

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

        mFriendAdapter = new FriendAdapter(getItems(), new FriendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // Transition to a profile screen
            }

            @Override
            public void onRemoveFriendClicked(int position) {
                removeFriend(position);
            }
        });

        mEndlessAdapter = new EndlessRecyclerViewAdapter(getContext(), mFriendAdapter, this);
        getRecyclerView().setAdapter(mEndlessAdapter);

        return view;
    }

    /**
     * Remove a friend from the user's friends list
     *
     * @param position the array position of the friend who is to removed
     */
    private void removeFriend(final int position) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", getItems().get(position).getUser().getObjectId());
        params.put("friendId", getItems().get(position).getFriend().getObjectId());

        ParseCloud.callFunctionInBackground("deleteFriendship", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if (e == null) {
                    getItems().remove(position);
                    mEndlessAdapter.notifyItemRemoved(position);
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        ParseQuery<Friend> query = Friend.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser())
                .include("friend")
                .setSkip(getNextPage())
                .setLimit(getLimit());

        query.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(List<Friend> friends, ParseException e) {
                if (e == null) {
                    if (!friends.isEmpty()) {
                        getItems().addAll(friends);
                        mFriendAdapter.notifyDataSetChanged();
                        mEndlessAdapter.onDataReady(true);
                    }

                    else {
                        mEndlessAdapter.onDataReady(false);
                    }

                    setPage(getPage() + 1);
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

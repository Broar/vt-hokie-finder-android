package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;

/**
 * A fragment that displays potential friends to the user
 *
 * @author Steven Briggs
 * @version 2015.10.31
 */
public class FindFriendsFragment extends ListFragment<ParseUser> {

    public static final String TAG = FindFriendsFragment.class.getSimpleName();

    /**
     * A factory method to return a new FindFriendsFragment that has been configured
     *
     * @return a new FindFriendsFragment that has been configured
     */
    public static FindFriendsFragment newInstance() {
        return new FindFriendsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflateFragment(R.layout.fragment_friends_list, inflater, container);

        getRecyclerView().setAdapter(new UserAdapter(getItems(), new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                openProfile(getItems().get(position).getObjectId());
            }

            @Override
            public void onAddFriendClicked(int position) {
                addFriend(position);
            }
        }));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_find_friends, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView sv = new SearchView(((FindFriendsActivity) getActivity()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW |
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, sv);


        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        sv.setLayoutParams(params);
        MenuItemCompat.expandActionView(item);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    protected void populate() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());

        ParseCloud.callFunctionInBackground("getPotentialFriends", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    getItems().addAll(users);
                    getRecyclerView().getAdapter().notifyDataSetChanged();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Add a new friend to the user's friends list
     *
     * @param position the array position of the user who is to added
     */
    private void addFriend(final int position) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        ParseCloud.callFunctionInBackground("createFriendship", params, new FunctionCallback<String>() {
            @Override
            public void done(String message, ParseException e) {
                if (e == null) {
                    getItems().remove(position);
                    getRecyclerView().getAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void openProfile(String id) {

    }
}

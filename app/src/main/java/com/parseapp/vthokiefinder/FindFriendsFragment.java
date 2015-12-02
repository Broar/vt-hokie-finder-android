package com.parseapp.vthokiefinder;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment that displays potential friends to the user
 *
 * @author Steven Briggs
 * @version 2015.11.02
 */
public class FindFriendsFragment extends RecyclerFragment<ParseUser, UserAdapter> implements
        SearchView.OnQueryTextListener{

    public static final String TAG = FindFriendsFragment.class.getSimpleName();

    private List<ParseUser> mOriginal;

    /**
     * A factory method to return a new FindFriendsFragment that has been configured
     *
     * @return a new FindFriendsFragment that has been configured
     */
    public static FindFriendsFragment newInstance() {
        return new FindFriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mOriginal = new ArrayList<ParseUser>();
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateFragment(R.layout.fragment_friends_list, inflater, container);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) MenuItemCompat.getActionView(item);
        sv.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        getBaseAdapter().setUsers(filter(query));
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        getBaseAdapter().setUsers(filter(newText));
        return true;
    }

    /**
     * Filter the list of users by the prefix
     *
     * @param prefix the prefix to filter by
     * @return a filtered list of users
     */
    private List<ParseUser> filter(String prefix) {
        List<ParseUser> filtered = new ArrayList<ParseUser>(getItems().size());

        if (prefix.isEmpty()) {
            filtered.addAll(mOriginal);
        }

        else {
            for (ParseUser user : getItems()) {
                if (user.getUsername().toLowerCase().startsWith(prefix.toLowerCase())) {
                    filtered.add(user);
                }
            }
        }

        return filtered;
    }

    @Override
    public void onLoadMoreRequested() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("skip", getNextPage());
        params.put("limit", getLimit());

        ParseCloud.callFunctionInBackground("getPotentialFriends", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    if (!users.isEmpty()) {
                        mOriginal.addAll(users);
                        getItems().addAll(users);
                        getAdapter().onDataReady(true);
                    } else {
                        getAdapter().onDataReady(false);
                    }

                    nextPage();
                } else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected UserAdapter buildAdapter() {
        return new UserAdapter(getItems(), new UserAdapter.OnItemClickedListener() {
            @Override
            public void onItemClicked(int position) {
                openProfile(getItems().get(position).getObjectId());
            }

            @Override
            public boolean onItemLongClicked(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("Send request to " + getItems().get(position).getUsername() + "?")
                        .setCancelable(true)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendFriendRequest(position);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.create().show();

                return true;
            }
        });
    }

    private void openProfile(String id) {

    }

    /**
     * Send a friend request to the user at position
     *
     * @param position the array position of the user who is being sent the request
     */
    private void sendFriendRequest(final int position) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("friendId", getItems().get(position).getObjectId());

        ParseCloud.callFunctionInBackground("createFriendRequest", params, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean requestSent, ParseException e) {
                if (e == null) {
                    if (requestSent) {
                        mOriginal.remove(getItems().get(position));
                        getItems().remove(position);
                        getBaseAdapter().notifyItemRemoved(position);
                        Toast.makeText(getContext(), "Request sent!", Toast.LENGTH_LONG).show();
                    }

                    else {
                        Toast.makeText(getContext(), "Couldn't send request", Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

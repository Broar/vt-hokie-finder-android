package com.parseapp.vthokiefinder;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment that displays the user's friend invites and handles their management. The invites
 * can be either incoming or outgoing but only one type can displayed per fragment instance
 *
 * @author Steven Briggs
 * @version 2015.11.24
 */
public class FriendInvitesFragment extends RecyclerFragment<ParseUser, UserAdapter> {

    public static final String TAG = FriendInvitesFragment.class.getSimpleName();
    public static final int INCOMING_INVITES = 0;
    public static final int OUTGOING_INVITES = 1;

    private static final String INVITE_TYPE_KEY = "inviteType";
    private static final int ACCEPT_INVITE = 0;
    private static final int DECLINE_INVITE = 1;
    private static final int CANCEL_INVITE = 0;

    private SwipeRefreshLayout mSwipeLayout;

    /**
     * A factory method to return a new FriendInvitesFragment that has been configured
     *
     * @param inviteType the invite type to display
     * @return a new FriendInvitesFragment that has been configured
     */
    public static FriendInvitesFragment newInstance(int inviteType) {
        Bundle args = new Bundle();
        args.putInt(INVITE_TYPE_KEY, inviteType);
        FriendInvitesFragment fragment = new FriendInvitesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflateFragment(R.layout.fragment_friend_invites, inflater, container);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getItems().clear();
                setPage(0);
                getAdapter().onDataReady(true);
            }
        });
        return view;
    }

    @Override
    protected UserAdapter buildAdapter() {
        return new UserAdapter(getItems(), new UserAdapter.OnItemClickedListener() {
            @Override
            public void onItemClicked(int position) {
                // Do nothing
            }

            @Override
            public boolean onItemLongClicked(int position) {
                buildInviteActionsDialog(position);
                return true;
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        if (getArguments().getInt(INVITE_TYPE_KEY) == INCOMING_INVITES) {
            loadInvites("getPendingIncomingFriendRequests");
        }

        else {
            loadInvites("getPendingOutgoingFriendRequests");
        }
    }

    /**
     * Load friend invites into the data set
     *
     * @param cloudFunctionName the name of the cloud code function to run
     */
    private void loadInvites(String cloudFunctionName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("skip", getNextPage());
        params.put("limit", getLimit());

        ParseCloud.callFunctionInBackground(cloudFunctionName, params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> invites, ParseException e) {
                if (e == null) {
                    if (!invites.isEmpty()) {
                        getItems().addAll(invites);
                        getAdapter().onDataReady(true);
                    }

                    else {
                        mSwipeLayout.setRefreshing(false);
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

    /**
     * Build the alert dialog to display actions to manage a user's friend invites/requests
     *
     * @param position the position of the user with a pending friendship
     */
    private void buildInviteActionsDialog(final int position) {
        final ParseUser user = getItems().get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (getArguments().getInt(INVITE_TYPE_KEY) == INCOMING_INVITES) {
            builder.setTitle(user.getUsername())
                    .setItems(R.array.actions_incoming_friend_invites, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case ACCEPT_INVITE:
                                    acceptFriendInvite(position);
                                    break;
                                case DECLINE_INVITE:
                                    deleteFriendInvite(user, ParseUser.getCurrentUser(), position);
                                    break;
                            }
                        }
                    });
        }

        else {
            builder.setTitle(user.getUsername())
                    .setItems(R.array.actions_outgoing_friend_invites, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case CANCEL_INVITE:
                                    deleteFriendInvite(ParseUser.getCurrentUser(), user, position);
                                    break;
                            }
                        }
                    });
        }

        builder.create().show();
    }

    /**
     * Accept the invitation of the user at position to be a friend
     *
     * @param position the position of the user
     */
    private void acceptFriendInvite(final int position) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("friendId", getItems().get(position).getObjectId());
        
        ParseCloud.callFunctionInBackground("acceptFriendRequest", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if (e == null) {
                    getItems().remove(position);
                    getBaseAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Delete the friend invite from user to friend
     *
     * @param user the user who requested the friendship
     * @param friend the user who received the friend request
     * @param position the position of the declined friend
     */
    private void deleteFriendInvite(ParseUser user, ParseUser friend, final int position) {
        ParseQuery<Friend> query = Friend.getQuery();
        query.whereEqualTo("user", user)
                .whereEqualTo("friend", friend);

        query.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(List<Friend> invites, ParseException e) {
                if (e == null) {
                    if (!invites.isEmpty()) {
                        invites.get(0).deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
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

                    else {
                        Toast.makeText(getContext(), "Couldn't deny request!", Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

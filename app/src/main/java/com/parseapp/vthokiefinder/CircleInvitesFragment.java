package com.parseapp.vthokiefinder;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment that displays the user's circle invites and handles their management. The invites
 * can be either incoming or outgoing but only one type can displayed per fragment instance
 *
 * @author Steven Briggs
 * @version 2015.11.24
 */
public class CircleInvitesFragment extends RecyclerFragment<UserCircle, UserCircleAdapter> {

    public static final String TAG = CircleInvitesFragment.class.getSimpleName();

    public static final int TYPE_INVITES = 0;
    public static final int TYPE_REQUESTS = 1;
    public static final int TYPE_MEMBERSHIP = 2;

    private static final String TYPE_KEY = "typeKey";

    private static final int ACCEPT_INVITE = 0;
    private static final int DECLINE_INVITE = 1;
    private static final int APPROVE_REQUEST = 0;
    private static final int DENY_REQUEST = 1;

    /**
     * A factory method to return a new FriendInvitesFragment that has been configured
     *
     * @param type the invite type to display
     * @return a new FriendInvitesFragment that has been configured
     */
    public static CircleInvitesFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(TYPE_KEY, type);
        CircleInvitesFragment fragment = new CircleInvitesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateFragment(R.layout.fragment_circle_invites, inflater, container);
    }

    @Override
    protected UserCircleAdapter buildAdapter() {
        return new UserCircleAdapter(getItems(), getArguments().getInt(TYPE_KEY), new UserCircleAdapter.OnItemClickedListener() {
            @Override
            public void onItemClicked(int position) {
                // Do nothing
            }

            @Override
            public boolean onItemLongClicked(int position) {
                buildActionsDialog(position);
                return true;
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        switch (getArguments().getInt(TYPE_KEY)) {
            case TYPE_INVITES:
                loadInvites();
                break;

            case TYPE_REQUESTS:
                loadRequests();
                break;

            case TYPE_MEMBERSHIP:
                loadMembershipRequests();
                break;
        }
    }

    /**
     * Load the user's invitations to circles
     */
    private void loadInvites() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser())
                .whereEqualTo("pending", true)
                .whereEqualTo("isInvite", true)
                .setLimit(getLimit())
                .setSkip(getNextPage())
                .include("user")
                .include("circle")
                .include("friend");

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> circleInvites, ParseException e) {
                if (e == null) {
                    if (!circleInvites.isEmpty()) {
                        getItems().addAll(circleInvites);
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

    /**
     * Load the user's requests to join circles
     */
    private void loadRequests() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser())
                .whereEqualTo("pending", true)
                .whereEqualTo("isInvite", false)
                .setLimit(getLimit())
                .setSkip(getNextPage())
                .include("user")
                .include("circle");

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> circleInvites, ParseException e) {
                if (e == null) {
                    if (!circleInvites.isEmpty()) {
                        getItems().addAll(circleInvites);
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

    /**
     * Load the membership requests of other users to join circles the current user belongs to
     */
    private void loadMembershipRequests() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());

        ParseCloud.callFunctionInBackground("getIncomingMembershipRequests", params, new FunctionCallback<List<UserCircle>>() {
            @Override
            public void done(List<UserCircle> membershipRequests, ParseException e) {
                if (e == null) {
                    if (!membershipRequests.isEmpty()) {
                        getItems().addAll(membershipRequests);
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

    /**
     * Build the alert dialog to display actions to manage a user's circle invites/requests
     *
     * @param position the position of the user-circle relationship
     */
    private void buildActionsDialog(final int position) {
        final UserCircle uc = getItems().get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (getArguments().getInt(TYPE_KEY) == TYPE_INVITES) {
            builder.setTitle("Invite to " + uc.getCircle().getName() + " from " + uc.getFriend().getUsername())
                    .setCancelable(true)
                    .setItems(R.array.actions_circle_invites, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case ACCEPT_INVITE:
                                    acceptCircleInvite(position);
                                    break;
                                case DECLINE_INVITE:
                                    declineCircleInvite(position);
                                    break;
                            }
                        }
                    });
        }

        else if (getArguments().getInt(TYPE_KEY) == TYPE_REQUESTS) {
            builder.setTitle("Delete request to join " + uc.getCircle().getName() + "?")
                    .setCancelable(true)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteCircleRequest(position);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }

        else {
            builder.setTitle("Request to join " + uc.getCircle().getName() + " from " + uc.getUser().getUsername())
                    .setCancelable(true)
                    .setItems(R.array.actions_circle_membership_requests, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case APPROVE_REQUEST:
                                    approveMembershipRequest(position);
                                    break;
                                case DENY_REQUEST:
                                    deleteCircleRequest(position);
                                    break;
                            }
                        }
                    });
        }

        builder.create().show();
    }

    /**
     * Accept the invitation to join the circle located at position
     *
     * @param position the position of the invitation
     */
    private void acceptCircleInvite(final int position) {
        UserCircle uc = getItems().get(position);
        uc.setIsPending(false);

        uc.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    getItems().remove(position);
                    getBaseAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Invite accepted!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Decline the invitation to join the circle located at position
     *
     * @param position the position of the invitation
     */
    private void declineCircleInvite(final int position) {
        getItems().get(position).deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    getItems().remove(position);
                    getBaseAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Invite declined!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Delete the outgoing request to join the circle located at position
     *
     * @param position the position of the outgoing invitation
     */
    private void deleteCircleRequest(final int position) {
        getItems().get(position).deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    getItems().remove(position);
                    getBaseAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Request deleted!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void approveMembershipRequest(final int position) {
        UserCircle uc = getItems().get(position);
        uc.setIsPending(false);

        uc.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    getItems().remove(position);
                    getBaseAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Membership approved!", Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

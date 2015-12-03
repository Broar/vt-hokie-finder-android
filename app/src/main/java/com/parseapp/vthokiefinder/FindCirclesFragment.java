package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
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
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment that displays the circles / communities the current user can possibly join
 *
 * @author Steven Briggs
 * @version 2015.12.01
 */
public class FindCirclesFragment extends RecyclerFragment<Circle, CircleAdapter> implements
        SearchView.OnQueryTextListener {

    public static final String TAG = FindCirclesFragment.class.getSimpleName();
    public static final int FIND_CIRCLES = 0;
    public static final int FIND_COMMUNITIES = 1;

    private static final String FIND_KEY = "find";
    private static final String CURRENT_LOCATION_ERROR = "Couldn't fetch current location. Please try again later";

    private Callbacks mListener;
    private List<Circle> mOriginal;

    public interface Callbacks {
        void onCircleClicked(Circle circle);
        void onCurrentLocationRequested(GoogleApiManagerFragment.OnLocationFoundListener listener);
    }

    /**
     * A factory method to return a new FindCirclesFragment that has been configured
     *
     * @param find the type of circle to search for
     * @return a new FindCirclesFragment that has been configured
     */
    public static FindCirclesFragment newInstance(int find) {
        Bundle args = new Bundle();
        args.putInt(FIND_KEY, find);
        FindCirclesFragment fragment = new FindCirclesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mOriginal = new ArrayList<Circle>();
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateFragment(R.layout.fragment_circles_list, inflater, container);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) MenuItemCompat.getActionView(item);
        sv.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        getBaseAdapter().setCircles(filter(query));
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        getBaseAdapter().setCircles(filter(newText));
        return true;
    }

    /**
     * Filter the list of circles by the prefix
     *
     * @param prefix the prefix to filter by
     * @return a filtered list of circles
     */
    private List<Circle> filter(String prefix) {
        List<Circle> filtered = new ArrayList<Circle>(getItems().size());

        if (prefix.isEmpty()) {
            filtered.addAll(mOriginal);
        }

        else {
            for (Circle circle : getItems()) {
                if (circle.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                    filtered.add(circle);
                }
            }
        }

        return filtered;
    }

    @Override
    public void onLoadMoreRequested() {
        if (getArguments().getInt(FIND_KEY) == FIND_CIRCLES) {
            loadCircles();
        }

        else {
            loadCommunities();
        }
    }

    /**
     * Load circles the user can join
     */
    private void loadCircles() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());

        ParseCloud.callFunctionInBackground("getCirclesToJoin", params, new FunctionCallback<List<Circle>>() {
            @Override
            public void done(List<Circle> circles, ParseException e) {
                if (e == null) {
                    mOriginal.addAll(circles);
                    getItems().addAll(circles);
                    getAdapter().onDataReady(false);
                } else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Load communities the user can join
     */
    private void loadCommunities() {
        mListener.onCurrentLocationRequested(new GoogleApiManagerFragment.OnLocationFoundListener() {
            @Override
            public void onLocationFound(Location location) {
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("userId", ParseUser.getCurrentUser().getObjectId());
                params.put("latitude", location.getLatitude());
                params.put("longitude", location.getLongitude());

                ParseCloud.callFunctionInBackground("getCommunitiesToJoin", params, new FunctionCallback<List<Circle>>() {
                    @Override
                    public void done(List<Circle> communities, ParseException e) {
                        if (e == null) {
                            mOriginal.addAll(communities);
                            getItems().addAll(communities);
                            getAdapter().onDataReady(false);
                        }

                        else {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onLocationNotFound() {
                Toast.makeText(getContext(), CURRENT_LOCATION_ERROR, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Display a dialog that allows the user to send a join request to a circle
     *
     * @param circle the community to display the dialog for
     * @param position the index position of the community
     */
    private void showSendJoinRequestDialog(final Circle circle, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Send join request to " + circle.getName() + "?")
                .setCancelable(true)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendJoinRequest(circle, position);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
        builder.create().show();
    }

    /**
     * Display a dialog that allows the user to join a community
     *
     * @param community the community to display the dialog for
     * @param position the index position of the community
     */
    private void showJoinDialog(final Circle community, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Join " + community.getName() + "?")
                .setCancelable(true)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        joinCommunity(community, position);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
        builder.create().show();
    }

    /**
     * Create a request for the user to join the specified circle
     *
     * @param circle the circle to send the request to
     * @param position the index position of the circle
     */
    private void sendJoinRequest(Circle circle, final int position) {
        UserCircle uc = new UserCircle();
        uc.setCircle(circle);
        uc.setUser(ParseUser.getCurrentUser());
        uc.setIsBroadcasting(false);
        uc.setIsPending(true);
        uc.setIsAccepted(false);
        uc.setIsInvite(false);
        uc.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mOriginal.remove(getItems().get(position));
                    getItems().remove(position);
                    getBaseAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Sent request!", Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Add the user as a new member of the specified community
     *
     * @param community the community the user is joining
     * @param position the index position of the community
     */
    private void joinCommunity(Circle community, final int position) {
        UserCircle uc = new UserCircle();
        uc.setCircle(community);
        uc.setUser(ParseUser.getCurrentUser());
        uc.setIsBroadcasting(false);
        uc.setIsPending(false);
        uc.setIsAccepted(true);
        uc.setIsInvite(false);
        uc.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mOriginal.remove(getItems().get(position));
                    getItems().remove(position);
                    getBaseAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Joined community!", Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected CircleAdapter buildAdapter() {
        return new CircleAdapter(getItems(), new CircleAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                mListener.onCircleClicked(getItems().get(position));
            }

            @Override
            public boolean onItemLongClicked(int position) {
                if (getArguments().getInt(FIND_KEY) == FIND_CIRCLES) {
                    showSendJoinRequestDialog(getItems().get(position), position);
                }

                else {
                    showJoinDialog(getItems().get(position), position);
                }

                return true;
            }
        });
    }
}

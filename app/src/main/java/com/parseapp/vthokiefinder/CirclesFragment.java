package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * A fragment that displays the circles a user is a part of
 *
 * @author Steven Briggs
 * @version 2015.11.12
 */
public class CirclesFragment extends RecyclerFragment<Circle, CircleAdapter> {

    public static final String TAG = CirclesFragment.class.getSimpleName();
    public static final String USER_ID_KEY = "userId";

    private Callbacks mListener;

    public interface Callbacks {
        void onCircleClicked(Circle circle);
    }

    /**
     * A factory method to return a new CirclesFragment that has been configured
     *
     * @param userId the id of the user
     * @return a new CirclesFragment that has been configured
     */
    public static CirclesFragment newInstance(String userId) {
        Bundle args = new Bundle();
        args.putString(USER_ID_KEY, userId);
        CirclesFragment fragment = new CirclesFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateFragment(R.layout.fragment_circles_list, inflater, container);
    }

    @Override
    public void onLoadMoreRequested() {
        String userId = getArguments().getString(USER_ID_KEY);

        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseObject.createWithoutData("_User", userId))
                .whereEqualTo("pending", false)
                .include("circle")
                .setSkip(getNextPage())
                .setLimit(getLimit());

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null) {
                    if (!userCircles.isEmpty()) {
                        for (UserCircle uc : userCircles) {
                            getItems().add(uc.getCircle());
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

    @Override
    protected CircleAdapter buildAdapter() {
        return new CircleAdapter(getContext(), getItems(), new CircleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mListener.onCircleClicked(getItems().get(position));
            }
        });
    }
}

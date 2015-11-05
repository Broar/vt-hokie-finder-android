package com.parseapp.vthokiefinder;

import android.content.Intent;
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
 * @version 2015.11.03
 */
public class CirclesFragment extends ListFragment<Circle, CircleAdapter> {

    public static final String TAG = CirclesFragment.class.getSimpleName();
    public static final String USER_ID_KEY = "userId";

    public interface Callbacks {
        void onCircleClick(Circle circle);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateFragment(R.layout.fragment_circles_list, inflater, container);
    }

    /**
     * Open a detailed view of the circle specified by id
     *
     * @param id the id of the circle to be opened
     * @param isMember determines whether the current user is member of the circle
     */
    private void openCircle(String id, boolean isMember) {
        Intent intent = new Intent(getContext(), CircleDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CircleDetailActivity.CIRCLE_ID_KEY, id);
        bundle.putBoolean(CircleDetailActivity.IS_MEMBER_KEY, isMember);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    @Override
    public void onLoadMoreRequested() {
        String userId = getArguments().getString(USER_ID_KEY);

        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseObject.createWithoutData("_User", userId))
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
        return new CircleAdapter(getItems(), new CircleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openCircle(getItems().get(position).getObjectId(), true);
                //mListener.onCircleClick(getItems().get(position));
            }
        });
    }
}

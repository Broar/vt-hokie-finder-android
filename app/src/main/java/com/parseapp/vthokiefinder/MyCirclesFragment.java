package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * A fragment that displays the circles the current user is a part of
 *
 * @author Steven Briggs
 * @version 2015.11.02
 */
public class MyCirclesFragment extends ListFragment<Circle, CircleAdapter> {

    public static final String TAG = MyCirclesFragment.class.getSimpleName();

    /**
     * A factory method to return a new MyCirclesFragment that has been configured
     *
     * @return a new MyCirclesFragment that has been configured
     */
    public static MyCirclesFragment newInstance() {
        return new MyCirclesFragment();
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
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser())
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

                    setPage(getPage() + 1);
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

                setRefresh(false);
            }
        });
    }

    @Override
    protected CircleAdapter buildAdapter() {
        return new CircleAdapter(getItems(), new CircleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Circle circle = getItems().get(position);
                openCircle(circle.getObjectId(), true);
            }
        });
    }
}

package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.view.View;
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
 * @version 2015.09.16
 */
public class MyCirclesFragment extends CircleListFragment {

    public static final String TAG = MyCirclesFragment.class.getSimpleName();


    /**
     * A factory method to return a new FindCirclesFragment that has been configured
     *
     * @return a new FindCirclesFragment that has been configured
     */
    public static MyCirclesFragment newInstance() {
        return new MyCirclesFragment();
    }

    @Override
    protected void setCircleAdapter() {
        getRecyclerView().setAdapter(new CircleAdapter(mCircles, new CircleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                openCircleDetails(getCircles().get(position).getObjectId(), true);
            }
        }));
    }

    @Override
    protected void refreshCircles() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser()).include("circle");
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null) {
                    getCircles().clear();

                    for (UserCircle uc : userCircles) {
                        getCircles().add(uc.getCircle());
                    }

                    getRecyclerView().getAdapter().notifyDataSetChanged();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

                stopRefresh();
            }
        });
    }
}

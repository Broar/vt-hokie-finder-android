package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
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
 * A fragment that displays the circles the current user can possibly join
 *
 * @author Steven Briggs
 * @version 2015.11.02
 */
public class FindCirclesFragment extends RecyclerFragment<Circle, CircleAdapter> {

    public static final String TAG = FindCirclesFragment.class.getSimpleName();

    private CirclesFragment.Callbacks mListener;

    /**
     * A factory method to return a new FindCirclesFragment that has been configured
     *
     * @return a new FindCirclesFragment that has been configured
     */
    public static FindCirclesFragment newInstance() {
        return new FindCirclesFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            mListener = (CirclesFragment.Callbacks) activity;
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
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("skip", getNextPage());
        params.put("limit", getLimit());

        ParseCloud.callFunctionInBackground("getCirclesToJoin", params, new FunctionCallback<List<Circle>>() {
            @Override
            public void done(List<Circle> circles, ParseException e) {
                if (e == null) {
                    if (!circles.isEmpty()) {
                        getItems().addAll(circles);
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

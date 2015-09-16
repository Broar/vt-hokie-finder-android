package com.parseapp.vthokiefinder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * An adapter that determines how to display information about circles onscreen
 *
 * @author Steven Briggs
 * @version 2015.09.15
 */
public class CirclesAdapter extends RecyclerView.Adapter<CirclesAdapter.ViewHolder> {

    private List<String> mCircles;

    /**
     * Create a new CirclesAdapter object.
     *
     * @param circles a list of circle names
     */
    public CirclesAdapter(List<String> circles) {
        mCircles = circles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCircleName.setText(mCircles.get(position));
    }

    @Override
    public int getItemCount() {
        return mCircles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView mCircleName;
        public ViewHolder(View view) {
            super(view);
            mCircleName = (TextView) itemView.findViewById(R.id.circleName);
        }
    }
}
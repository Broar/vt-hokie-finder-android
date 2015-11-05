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
 * @version 2015.11.03
 */
public class CircleAdapter extends RecyclerView.Adapter<CircleAdapter.ViewHolder> {

    private List<Circle> mCircles;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * Create a new CircleAdapter object.
     *
     * @param circles the dataset of circles
     * @param listener the item listener
     */
    public CircleAdapter(List<Circle> circles, OnItemClickListener listener) {
        mCircles = circles;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCircleName.setText(mCircles.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mCircles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnItemClickListener mListener;
        private TextView mCircleName;

        /**
         * Create a new ViewHolder object.
         *
         * @param view the item view
         * @param listener listener to callback when this holder is clicked
         */
        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);
            mListener = listener;
            view.setOnClickListener(this);
            mCircleName = (TextView) itemView.findViewById(R.id.circleName);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(getLayoutPosition());
            }
        }
    }
}
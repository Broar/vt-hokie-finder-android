package com.parseapp.vthokiefinder;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flipview.FlipView;

/**
 * An adapter for a RecyclerView that displays the broadcast status of user's circles
 *
 * @author Steven Briggs
 * @version 2015.11.04
 */
public class BroadcastAdapter extends RecyclerView.Adapter<BroadcastAdapter.ViewHolder> {

    private List<UserCircle> mUserCircles;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    /**
     * Create a new BroadcastAdapter object.
     *
     * @param userCircles the dataset of UserCircles
     * @param listener the item listener
     */
    public BroadcastAdapter(List<UserCircle> userCircles, OnItemClickListener listener) {
        mUserCircles = userCircles;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_broadcast, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mName.setText(mUserCircles.get(position).getCircle().getName());
        holder.mBroadcastStatus.flipSilently(mUserCircles.get(position).isBroadcasting());
    }

    @Override
    public int getItemCount() {
        return mUserCircles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private OnItemClickListener mListener;
        private TextView mName;
        private FlipView mBroadcastStatus;

        /**
         * Create a new ViewHolder object.
         *
         * @param itemView the item view
         * @param listener listener to callback when this holder is clicked
         */
        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mListener = listener;
            mName = (TextView) itemView.findViewById(R.id.name);
            mBroadcastStatus = (FlipView) itemView.findViewById(R.id.broadcast_status);
            mBroadcastStatus.setChildBackgroundColor(FlipView.REAR_VIEW_INDEX,
                    ContextCompat.getColor(itemView.getContext(), R.color.primary));
            mBroadcastStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBroadcastStatus.showNext();
                    mListener.onItemClick(v, getLayoutPosition());
                }
            });
        }
    }

}

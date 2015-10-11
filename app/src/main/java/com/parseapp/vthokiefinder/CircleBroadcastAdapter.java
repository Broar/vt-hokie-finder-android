package com.parseapp.vthokiefinder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import java.util.List;

/**
 * An adapter for a RecylcerView that displays the broadcast status of user's circles
 *
 * @author Steven Briggs
 * @version 2015.10.10
 */
public class CircleBroadcastAdapter extends RecyclerView.Adapter<CircleBroadcastAdapter.ViewHolder> {

    private List<UserCircle> mUserCircles;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
        boolean isUserBroadcasting();
    }

    /**
     * Create a new CircleBroadcastAdapter object.
     *
     * @param userCircles a list of UserCircles
     * @param listener the item listener
     */
    public CircleBroadcastAdapter(List<UserCircle> userCircles, OnItemClickListener listener) {
        mUserCircles = userCircles;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle_broadcast, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mBroadcastStatus.setText(mUserCircles.get(position).getCircle().getName());
        holder.mBroadcastStatus.setChecked(mUserCircles.get(position).isBroadcasting());
    }

    @Override
    public int getItemCount() {
        return mUserCircles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnItemClickListener mListener;
        private CheckedTextView mBroadcastStatus;

        /**
         * Create a new ViewHolder object.
         *
         * @param itemView the item view
         * @param listener listener to callback when this holder is clicked
         */
        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mListener = listener;
            mBroadcastStatus = (CheckedTextView) itemView.findViewById(R.id.broadcastStatus);
            mBroadcastStatus.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener.isUserBroadcasting() && v.getId() == R.id.broadcastStatus) {
                mBroadcastStatus.toggle();
                mListener.onItemClick(v, getLayoutPosition());
            }
        }
    }

}

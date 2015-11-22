package com.parseapp.vthokiefinder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.franlopez.flipcheckbox.FlipCheckBox;

import java.util.List;

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
        holder.mCircleName.setText(mUserCircles.get(position).getCircle().getName());
        holder.mBroadcastStatus.setChecked(mUserCircles.get(position).isBroadcasting());
    }

    @Override
    public int getItemCount() {
        return mUserCircles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnItemClickListener mListener;
        private FlipCheckBox mBroadcastStatus;
        private TextView mCircleName;

        /**
         * Create a new ViewHolder object.
         *
         * @param itemView the item view
         * @param listener listener to callback when this holder is clicked
         */
        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mListener = listener;
            mBroadcastStatus = (FlipCheckBox) itemView.findViewById(R.id.broadcastStatus);
            mBroadcastStatus.setOnClickListener(this);
            mCircleName = (TextView) itemView.findViewById(R.id.name);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.broadcastStatus) {
                mBroadcastStatus.switchChecked();
                mListener.onItemClick(v, getLayoutPosition());
            }
        }
    }

}

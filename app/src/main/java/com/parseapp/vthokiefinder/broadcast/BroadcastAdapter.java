package com.parseapp.vthokiefinder.broadcast;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parseapp.vthokiefinder.R;
import com.parseapp.vthokiefinder.model.UserCircle;

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
    private SparseBooleanArray mIsViewingStates;
    private OnItemClickedListener mListener;
    private FlipView mPrevious;
    private int mPreviousPos;

    public interface OnItemClickedListener {
        void onBroadcastClicked(int position);
        void onIsViewingClicked(int position, boolean isViewing);
    }

    /**
     * Create a new BroadcastAdapter object.
     *
     * @param userCircles the dataset of UserCircles
     * @param listener the item listener
     */
    public BroadcastAdapter(List<UserCircle> userCircles, OnItemClickedListener listener) {
        mUserCircles = userCircles;
        mIsViewingStates = new SparseBooleanArray(mUserCircles.size());
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_broadcast, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mName.setText(mUserCircles.get(position).getCircle().getName());
        holder.mBroadcastStatus.flipSilently(mUserCircles.get(position).isBroadcasting());
        holder.mIsViewing.flipSilently(mIsViewingStates.get(position));

        // Handle logic to ensure only a single checkbox is flipped at a time
        holder.mIsViewing.setOnFlippingListener(new FlipView.OnFlippingListener() {
            @Override
            public void onFlipped(FlipView flipView, boolean checked) {
                if (checked) {
                    if (mPrevious != null) {
                        mPrevious.flipSilently(false);
                        mIsViewingStates.put(mPreviousPos, false);
                    }

                    mIsViewingStates.put(position, true);
                    mPrevious = flipView;
                    mPreviousPos = position;
                }

                else {
                    mIsViewingStates.put(mPreviousPos, false);
                    mPrevious = null;
                    mPreviousPos = -1;
                }

                mListener.onIsViewingClicked(position, checked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserCircles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private OnItemClickedListener mListener;
        private TextView mName;
        private FlipView mBroadcastStatus;
        private FlipView mIsViewing;

        /**
         * Create a new ViewHolder object.
         *
         * @param itemView the item view
         * @param listener listener to callback when this holder is clicked
         */
        public ViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            mListener = listener;
            mName = (TextView) itemView.findViewById(R.id.name);
            mBroadcastStatus = (FlipView) itemView.findViewById(R.id.broadcast_status);
            mIsViewing = (FlipView) itemView.findViewById(R.id.is_viewing);

            mBroadcastStatus.setChildBackgroundColor(FlipView.REAR_VIEW_INDEX,
                    ContextCompat.getColor(itemView.getContext(), R.color.primary));
            mBroadcastStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBroadcastStatus.showNext();
                    mListener.onBroadcastClicked(getLayoutPosition());
                }
            });

            mIsViewing.setChildBackgroundColor(FlipView.REAR_VIEW_INDEX,
                    ContextCompat.getColor(itemView.getContext(), R.color.primary));
//            mIsViewing.setOnFlippingListener(new FlipView.OnFlippingListener() {
//                @Override
//                public void onFlipped(FlipView flipView, boolean checked) {
//                    mListener.onIsViewingClicked(getLayoutPosition(), checked);
//                }
//            });
        }
    }

}

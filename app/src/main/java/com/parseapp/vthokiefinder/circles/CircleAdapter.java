package com.parseapp.vthokiefinder.circles;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseFile;
import com.parseapp.vthokiefinder.R;
import com.parseapp.vthokiefinder.model.Circle;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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
        void onItemClicked(int position);
        boolean onItemLongClicked(int position);
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
        ParseFile imageFile = mCircles.get(position).getIcon();

        // Load the circle's icon if it exists
        if (imageFile != null) {
            Uri imageUri = Uri.parse(imageFile.getUrl());
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mIcon);
        }

        // Otherwise, just load the default Fightin' Gobblers icon
        else {
            Glide.with(holder.itemView.getContext())
                    .fromResource()
                    .load(R.drawable.fighting_gobblers_medium)
                    .into(holder.mIcon);
        }

        holder.mName.setText(mCircles.get(position).getName());
        holder.mDescription.setText(mCircles.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return mCircles.size();
    }

    public void setCircles(List<Circle> circles) {
        mCircles.clear();
        mCircles.addAll(circles);
        notifyDataSetChanged();
    }

    /**
     * A class to display information about circles
     *
     * @author Steven Briggs
     * @version 2015.12.01
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        private OnItemClickListener mListener;
        private CircleImageView mIcon;
        private TextView mName;
        private TextView mDescription;

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
            view.setOnLongClickListener(this);
            mIcon = (CircleImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
            mDescription = (TextView) itemView.findViewById(R.id.description);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClicked(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null) {
                return mListener.onItemLongClicked(getLayoutPosition());
            }

            return false;
        }
    }
}
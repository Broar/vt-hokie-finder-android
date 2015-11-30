package com.parseapp.vthokiefinder;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * An adapter that determines how to display information about users onscreen
 *
 * @author Steven Briggs
 * @version 2015.10.31
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<ParseUser> mUsers;
    private OnItemClickedListener mListener;

    public interface OnItemClickedListener {
        void onItemClicked(int position);
        boolean onItemLongClicked(int position);
    }

    /**
     * Create a new UserAdapter object.
     *
     * @param users the dataset of users
     * @param listener the object listening to item click events
     */
    public UserAdapter(List<ParseUser> users, OnItemClickedListener listener) {
        mUsers = users;
        mListener = listener;
    }

    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(UserAdapter.ViewHolder holder, int position) {
        ParseUser user = mUsers.get(position);

        // Load the user's avatar if it exists
        ParseFile imageFile = user.getParseFile("avatar");
        if (imageFile != null) {
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(imageFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mAvatar);
        }

        // Otherwise, just load the default Fightin' Gobblers icon
        else {
            Glide.with(holder.itemView.getContext())
                    .fromResource()
                    .load(R.drawable.fighting_gobblers_medium)
                    .into(holder.mAvatar);
        }

        holder.mUsername.setText(user.getString("username"));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnLongClickListener {

        private OnItemClickedListener mListener;
        private TextView mUsername;
        private CircleImageView mAvatar;

        /**
         * Create a new ViewHolder object.
         *
         * @param itemView the item view
         * @param listener the object listening for item click events
         */
        public ViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            mListener = listener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mAvatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            mUsername = (TextView) itemView.findViewById(R.id.username);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClicked(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return mListener.onItemLongClicked(getLayoutPosition());
        }
    }
}

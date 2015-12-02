package com.parseapp.vthokiefinder;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * An adapter that determines how to display information about friends onscreen
 *
 * @author Steven Briggs
 * @version 2015.10.30
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private Context mContext;
    private List<Friend> mFriends;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onAddFriendClicked(int position);
        void onRemoveFriendClicked(int position);
    }

    /**
     * Create a new FriendAdapter object.
     *
     * @param friends the dataset of friends
     * @param listener the item listener
     */
    public FriendAdapter(Context context, List<Friend> friends, OnItemClickListener listener) {
        mContext = context;
        mFriends = friends;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ParseUser friend = mFriends.get(position).getFriend();

        // Load the user's avatar if it exists
        ParseFile imageFile = friend.getParseFile("avatar");
        if (imageFile != null) {
            Glide.with(mContext)
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

        holder.mUsername.setText(friend.getUsername());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("friendId", friend.getObjectId());

        ParseCloud.callFunctionInBackground("areFriends", params, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean areFriends, ParseException e) {
                if (e == null) {
                    if (areFriends) {
                        holder.isFriendsWithCurrentUser = true;
                    }

                    else {
                        holder.isFriendsWithCurrentUser = false;
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        private static final int ADD = 0;
        private static final int REMOVE = 0;

        private OnItemClickListener mListener;
        private TextView mUsername;
        private CircleImageView mAvatar;
        private boolean isFriendsWithCurrentUser;

        /**
         * Create a new ViewHolder object.
         *
         * @param view the item view
         * @param listener listener to callback when this holder is clicked
         */
        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            mListener = listener;
            mUsername = (TextView) view.findViewById(R.id.username);
            mAvatar = (CircleImageView) view.findViewById(R.id.avatar);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(mUsername.getText().toString());

            if (isFriendsWithCurrentUser) {
                builder.setItems(R.array.actions_friends, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case REMOVE:
                                mListener.onRemoveFriendClicked(getLayoutPosition());
                                break;
                        }
                    }
                });
            }

            else {
                builder.setItems(R.array.actions_nonfriends, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case ADD:
                                mListener.onAddFriendClicked(getLayoutPosition());
                                break;
                        }
                    }
                });
            }

            builder.create().show();

            return true;
        }
    }
}

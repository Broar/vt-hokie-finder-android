package com.parseapp.vthokiefinder;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * An adapter that determines how to display information about friends onscreen
 *
 * @author Steven Briggs
 * @version 2015.10.30
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<Friend> mFriends;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
        void onRemoveFriendClicked(int position);
    }

    /**
     * Create a new FriendAdapter object.
     *
     * @param friends a list of Friends
     * @param listener the item listener
     */
    public FriendAdapter(List<Friend> friends, OnItemClickListener listener) {
        mFriends = friends;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mUsername.setText(mFriends.get(position).getFriend().getUsername());
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        private static final int REMOVE = 0;

        private OnItemClickListener mListener;
        private TextView mUsername;
        private CircleImageView mAvatar;

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
                mListener.onItemClick(v, getLayoutPosition());
            }
        }


        @Override
        public boolean onLongClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(mUsername.getText().toString())
                    .setItems(R.array.actions_friends, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case REMOVE:
                                    mListener.onRemoveFriendClicked(getLayoutPosition());
                                    break;
                            }
                        }
                    });

            builder.create().show();

            return true;
        }
    }
}

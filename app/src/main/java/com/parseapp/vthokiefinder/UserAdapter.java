package com.parseapp.vthokiefinder;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
        void onAddFriendClicked(int position);
    }

    /**
     * Create a new UserAdapter object.
     *
     * @param users the dataset of users
     * @param listener the object listening to item click events
     */
    public UserAdapter(List<ParseUser> users, OnItemClickListener listener) {
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
        holder.mUsername.setText(mUsers.get(position).getString("username"));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        private static final int ADD = 0;

        private OnItemClickListener mListener;
        private TextView mUsername;
        private CircleImageView mAvatar;

        /**
         * Create a new ViewHolder object.
         *
         * @param view the item view
         * @param listener the object listening for item click events
         */
        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            mUsername = (TextView) view.findViewById(R.id.username);
            mAvatar = (CircleImageView) view.findViewById(R.id.avatar);
            mListener = listener;
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
                    .setItems(R.array.actions_users, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case ADD:
                                    mListener.onAddFriendClicked(getLayoutPosition());
                                    break;
                            }
                        }
                    });

            builder.create().show();
            return false;
        }
    }
}

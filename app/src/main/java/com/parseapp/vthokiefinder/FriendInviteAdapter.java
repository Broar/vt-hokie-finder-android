package com.parseapp.vthokiefinder;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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
 * An adapter that displays a user's friend incoming and outgoing friend invites
 *
 * @author Steven Briggs
 * @version 2015.11.23
 */
public class FriendInviteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_INVITE = 0;
    public static final int TYPE_REQUEST = 1;

    private List<ParseUser> mInvites;
    private int mViewType;
    private OnItemClickedListener mListener;

    public interface OnItemClickedListener {
        void onItemClicked(int position);
        void onAcceptFriendInviteClicked(int position);
        void onDenyFriendInviteClicked(int position);
        void onCancelFriendInviteClicked(int position);
    }

    /**
     * Create a new FriendInviteAdapter object
     *
     * @param invites the list of invites
     * @param listener the object listening to item clicks
     */
    public FriendInviteAdapter(List<ParseUser> invites, int viewType, OnItemClickedListener listener) {
        mInvites = invites;
        mViewType = viewType;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mViewType == TYPE_INVITE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new IncomingInvitesViewHolder(view, mListener);
        }

        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new OutgoingInvitesViewHolder(view, mListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BaseViewHolder) {
            BaseViewHolder base = (BaseViewHolder) holder;
            ParseUser pendingFriend = mInvites.get(position);

            ParseFile imageFile = pendingFriend.getParseFile("avatar");
            if (imageFile != null) {
                Glide.with(base.itemView.getContext())
                        .load(imageFile.getUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(base.mAvatar);
            }

            base.mUsername.setText(pendingFriend.getUsername());
        }
    }

    @Override
    public int getItemCount() {
        return mInvites.size();
    }

    public abstract class BaseViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        protected CircleImageView mAvatar;
        protected TextView mUsername;

        public BaseViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mAvatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            mUsername = (TextView) itemView.findViewById(R.id.username);
        }
    }

    public class IncomingInvitesViewHolder extends BaseViewHolder {

        private static final int ACCEPT_INVITE = 0;
        private static final int DENY_INVITE = 1;

        private OnItemClickedListener mListener;

        /**
         * Create a new IncomingInvitesViewHolder object.
         *
         * @param itemView the item view
         * @param listener listener to callback when this holder is clicked
         */
        public IncomingInvitesViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClicked(getLayoutPosition());
            }
        }


        @Override
        public boolean onLongClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(mUsername.getText().toString());

            builder.setItems(R.array.actions_incoming_friend_invites, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case ACCEPT_INVITE:
                            mListener.onAcceptFriendInviteClicked(getLayoutPosition());
                            break;
                        case DENY_INVITE:
                            mListener.onDenyFriendInviteClicked(getLayoutPosition());
                            break;
                    }
                }
            });

            builder.create().show();

            return true;
        }
    }

    public class OutgoingInvitesViewHolder extends BaseViewHolder {

        private static final int CANCEL_INVITE = 0;

        private OnItemClickedListener mListener;

        /**
         * Create a new OutgoingInvitesViewHolder object.
         *
         * @param itemView the item view
         * @param listener listener to callback when this holder is clicked
         */
        public OutgoingInvitesViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClicked(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(mUsername.getText().toString());

            builder.setItems(R.array.actions_outgoing_friend_invites, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case CANCEL_INVITE:
                            mListener.onCancelFriendInviteClicked(getLayoutPosition());
                            break;
                    }
                }
            });

            builder.create().show();

            return true;
        }
    }
}

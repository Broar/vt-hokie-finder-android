package com.parseapp.vthokiefinder;

import android.net.Uri;
import android.support.v4.content.ContextCompat;
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
import eu.davidea.flipview.FlipView;

/**
 * An adapter that determines how to display information about users onscreen
 *
 * @author Steven Briggs
 * @version 2015.10.31
 */
public class InvitesAdapter extends RecyclerView.Adapter<InvitesAdapter.ViewHolder> {

    private List<ParseUser> mUsers;
    private OnItemClickedListener mListener;

    public interface OnItemClickedListener {
        void onInviteClicked(int position, boolean isInvited);
        boolean onIsInvitedRequested(int position);
    }

    /**
     * Create a new UserAdapter object.
     *
     * @param users the dataset of users
     * @param listener the object listening to item click events
     */
    public InvitesAdapter(List<ParseUser> users, OnItemClickedListener listener) {
        mUsers = users;
        mListener = listener;
    }

    @Override
    public InvitesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite_friend, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(InvitesAdapter.ViewHolder holder, int position) {
        ParseUser user = mUsers.get(position);
        holder.mUsername.setText(user.getString("username"));

        // Load the user's avatar if it exists
        ParseFile imageFile = user.getParseFile("avatar");
        if (imageFile != null) {
            CircleImageView avatar = (CircleImageView) holder.mIsInvited.getFrontLayout().findViewById(R.id.avatar);
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(imageFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(avatar);
        }

        // Otherwise, just load the default Fightin' Gobblers icon
        else {
            CircleImageView avatar = (CircleImageView) holder.mIsInvited.getFrontLayout().findViewById(R.id.avatar);
            Glide.with(holder.itemView.getContext())
                    .fromResource()
                    .load(R.drawable.fighting_gobblers_medium)
                    .into(avatar);
        }

        holder.mIsInvited.flipSilently(mListener.onIsInvitedRequested(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnItemClickedListener mListener;
        private TextView mUsername;
        private FlipView mIsInvited;

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
            mUsername = (TextView) itemView.findViewById(R.id.username);
            mIsInvited = (FlipView) itemView.findViewById(R.id.invite);
            mIsInvited.setChildBackgroundColor(FlipView.REAR_VIEW_INDEX,
                    ContextCompat.getColor(itemView.getContext(), R.color.primary));

            mIsInvited.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsInvited.showNext();
                }
            });

            // We must provide the FlipView with a listener because a FlipView is not
            // considered flipped until the animation has completed. This means the
            // OnItemClickedListener cannot be informed about the invite change until we
            // get a callback from the FlipView itself
            mIsInvited.setOnFlippingListener(new FlipView.OnFlippingListener() {
                @Override
                public void onFlipped(FlipView flipView, boolean checked) {
                    mListener.onInviteClicked(getLayoutPosition(), checked);
                }
            });
        }

        @Override
        public void onClick(View v) {
            // Defer informing the OnItemClickedListener about the flip change to the
            // FlipView's own listener
            mIsInvited.performClick();
        }
    }
}

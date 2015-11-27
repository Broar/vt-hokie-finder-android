package com.parseapp.vthokiefinder;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

        ParseFile imageFile = user.getParseFile("avatar");
        if (imageFile != null) {
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(imageFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mAvatar);
        }


        holder.mUsername.setText(user.getString("username"));
        holder.mIsInvited.setChecked(mListener.onIsInvitedRequested(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnItemClickedListener mListener;
        private TextView mUsername;
        private CircleImageView mAvatar;
        private CheckBox mIsInvited;

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
            mAvatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            mUsername = (TextView) itemView.findViewById(R.id.username);
            mIsInvited = (CheckBox) itemView.findViewById(R.id.invite);

            mIsInvited.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mListener.onInviteClicked(getLayoutPosition(), isChecked);
                }
            });
        }

        @Override
        public void onClick(View v) {
            // Defer informing the OnItemClickedListener about the checked change
            // to the CheckBox's own listener
            mIsInvited.toggle();
        }
    }
}

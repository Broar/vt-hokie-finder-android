package com.parseapp.vthokiefinder;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * An adapter that displays information about a user's relationship to a circle
 *
 * @author Steven Briggs
 * @version 2015.11.24
 */
public class UserCircleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INVITES = 0;
    private static final int TYPE_REQUESTS = 1;
    private static final int TYPE_MEMBERSHIP = 2;

    private List<UserCircle> mUserCircles;
    private int mViewType;
    private OnItemClickedListener mListener;

    public interface OnItemClickedListener {
        void onItemClicked(int position);
        boolean onItemLongClicked(int position);
    }

    /**
     * Create a new UserCircleAdapter object.
     *
     * @param userCircles the data set of user to circle relationships
     * @param listener the object listening to item click events
     */
    public UserCircleAdapter(List<UserCircle> userCircles, int viewType, OnItemClickedListener listener) {
        mUserCircles = userCircles;
        mViewType = viewType;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (mViewType) {
            case TYPE_INVITES:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle_invite, parent, false);
                return new InvitesViewHolder(view, mListener);

            case TYPE_REQUESTS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle, parent, false);
                return new RequestsViewHolder(view, mListener);

            case TYPE_MEMBERSHIP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle_invite, parent, false);
                return new MembershipRequestsViewHolder(view, mListener);

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ParseUser user = mUserCircles.get(position).getUser();
        ParseUser friend = mUserCircles.get(position).getFriend();
        Circle circle = mUserCircles.get(position).getCircle();

        // Bind the UserCircle at position to an InvitesViewHolder
        if (holder instanceof InvitesViewHolder) {
            InvitesViewHolder invitesHolder = (InvitesViewHolder) holder;
            loadImage(friend.getParseFile("avatar"), invitesHolder.mUserAvatar);
            loadImage(circle.getIcon(), invitesHolder.mCircleAvatar);
            invitesHolder.mCircleName.setText(circle.getName());
            invitesHolder.mUsername.setText(friend.getUsername());
        }

        // Bind the UserCircle at position to a RequestsViewHolder
        else if (holder instanceof RequestsViewHolder) {
            RequestsViewHolder requestsHolder = (RequestsViewHolder) holder;
            loadImage(circle.getIcon(), requestsHolder.mAvatar);
            requestsHolder.mName.setText(circle.getName());
            requestsHolder.mDescription.setText(circle.getDescription());
        }

        // Bind the UserCircle at position to an MembershipRequestsViewHolder
        else if (holder instanceof MembershipRequestsViewHolder) {
            MembershipRequestsViewHolder membershipHolder = (MembershipRequestsViewHolder) holder;
            loadImage(user.getParseFile("avatar"), membershipHolder.mUserAvatar);
            loadImage(circle.getIcon(), membershipHolder.mCircleAvatar);
            membershipHolder.mCircleName.setText(circle.getName());
            membershipHolder.mUsername.setText(user.getUsername());
        }
    }

    @Override
    public int getItemCount() {
        return mUserCircles.size();
    }

    /**
     * Load an image in a ParseFile into the specified ImageView
     *
     * @param imageFile the file containing the image to load
     * @param iv the ImageView to load the image into
     */
    private void loadImage(ParseFile imageFile, ImageView iv) {
        if (imageFile != null) {
            Glide.with(iv.getContext())
                    .load(Uri.parse(imageFile.getUrl()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iv);
        }

        else {
            Glide.with(iv.getContext())
                    .fromResource()
                    .load(R.drawable.fighting_gobblers_medium)
                    .into(iv);
        }
    }

    public static class InvitesViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        private OnItemClickedListener mListener;
        private CircleImageView mUserAvatar;
        private CircleImageView mCircleAvatar;
        private TextView mCircleName;
        private TextView mUsername;

        public InvitesViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mListener = listener;
            mUserAvatar = (CircleImageView) itemView.findViewById(R.id.user_avatar);
            mCircleAvatar = (CircleImageView) itemView.findViewById(R.id.circle_avatar);
            mCircleName = (TextView) itemView.findViewById(R.id.circle_name);
            mUsername = (TextView) itemView.findViewById(R.id.username);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClicked(getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return mListener.onItemLongClicked(getLayoutPosition());
        }
    }

    public static class MembershipRequestsViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        private OnItemClickedListener mListener;
        private CircleImageView mUserAvatar;
        private CircleImageView mCircleAvatar;
        private TextView mCircleName;
        private TextView mUsername;

        public MembershipRequestsViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mListener = listener;
            mUserAvatar = (CircleImageView) itemView.findViewById(R.id.user_avatar);
            mCircleAvatar = (CircleImageView) itemView.findViewById(R.id.circle_avatar);
            mCircleName = (TextView) itemView.findViewById(R.id.circle_name);
            mUsername = (TextView) itemView.findViewById(R.id.username);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClicked(getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return mListener.onItemLongClicked(getLayoutPosition());
        }
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        private OnItemClickedListener mListener;
        private CircleImageView mAvatar;
        private TextView mName;
        private TextView mDescription;

        public RequestsViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mListener = listener;
            mAvatar = (CircleImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
            mDescription = (TextView) itemView.findViewById(R.id.description);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClicked(getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return mListener.onItemLongClicked(getLayoutPosition());
        }
    }
}

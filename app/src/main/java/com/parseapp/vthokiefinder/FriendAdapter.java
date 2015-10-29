package com.parseapp.vthokiefinder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * An adapter that determines how to display information about friends onscreen
 *
 * @author Steven Briggs
 * @version 2015.10.29
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<Friend> mFriends;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_friend, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mName.setText(mFriends.get(position).getFriend().getUsername());
        holder.mRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("userId", mFriends.get(position).getUser().getObjectId());
                params.put("friendId", mFriends.get(position).getFriend().getObjectId());

                ParseCloud.callFunctionInBackground("deleteFriendship", params, new FunctionCallback<String>() {
                    @Override
                    public void done(String result, ParseException e) {
                        if (e == null) {
                            mFriends.remove(position);
                            notifyItemRemoved(position);
                        }

                        else {
                            // Do stuff
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnItemClickListener mListener;
        private TextView mName;
        private CircleImageView mAvatar;
        private ImageButton mRemove;

        /**
         * Create a new ViewHolder object.
         *
         * @param view the item view
         * @param listener listener to callback when this holder is clicked
         */
        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);
            view.setOnClickListener(this);
            mListener = listener;
            mName = (TextView) view.findViewById(R.id.name);
            mAvatar = (CircleImageView) view.findViewById(R.id.avatar);
            mRemove = (ImageButton) view.findViewById(R.id.remove);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getLayoutPosition());
            }
        }
    }
}

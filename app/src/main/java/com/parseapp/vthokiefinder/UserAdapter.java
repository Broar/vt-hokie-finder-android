package com.parseapp.vthokiefinder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;

/**
 * An adapter that determines how to display information about users onscreen
 *
 * @author Steven Briggs
 * @version 2015.10.31
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<ParseUser> mMembers;

    /**
     * Create a new UserAdapter object.
     *
     * @param members the users to be displayed
     */
    public UserAdapter(List<ParseUser> members) {
        mMembers = members;
    }

    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserAdapter.ViewHolder holder, int position) {
        holder.mUsername.setText(mMembers.get(position).getString("username"));
    }

    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsername;

        /**
         * Create a new ViewHolder object.
         *
         * @param view the item view
         */
        public ViewHolder(View view) {
            super(view);
            mUsername = (TextView) itemView.findViewById(R.id.username);
        }
    }
}
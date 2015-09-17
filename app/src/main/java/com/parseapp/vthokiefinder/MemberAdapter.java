package com.parseapp.vthokiefinder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * An adapter that determines how to display information about users onscreen
 *
 * @author Steven Briggs
 * @version 2015.09.17
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private List<String> mMembers;

    /**
     * Create a new MemberAdapter object.
     *
     * @param members the users to be displayed
     */
    public MemberAdapter(List<String> members) {
        mMembers = members;
    }

    @Override
    public MemberAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberAdapter.ViewHolder holder, int position) {
        holder.mUsername.setText(mMembers.get(position));
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

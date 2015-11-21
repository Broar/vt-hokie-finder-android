package com.parseapp.vthokiefinder;

import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A POJO representing a friendship relationship from one ParseUser to another
 *
 * @author Steven Briggs
 * @version 2015.10.29
 */
@ParseClassName("Friend")
public class Friend extends ParseObject {

    public static final int FRIENDS = 0;
    public static final int NOT_FRIENDS = 1;
    public static final int INCOMING = 2;
    public static final int OUTGOING = 3;

    public Friend() {
        // Required default constructor
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public ParseUser getFriend() {
        return getParseUser("friend");
    }

    public void setFriend(ParseUser friend) {
        put("friend", friend);
    }

    public boolean isPending() {
        return getBoolean("pending");
    }

    public void setIsPending(boolean pending) {
        put("pending", pending);
    }

    public boolean hasAccepted() {
        return getBoolean("accepted");
    }

    public void setHasAccepted(boolean accepted) {
        put("accepted", accepted);
    }

    /**
     * Determine the friendship status of a pair of users
     *
     * @param user the user
     * @param friend the friend
     * @param listener the listener to return the results to
     */
    public static void findFriendshipStatus(ParseUser user, ParseUser friend, final OnFriendshipFoundListener listener) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", user.getObjectId());
        params.put("friendId", friend.getObjectId());

        // Determine the friendship status
        ParseCloud.callFunctionInBackground("getFriendshipStatus", params, new FunctionCallback<String>() {
            @Override
            public void done(String friendStatus, ParseException e) {
                if (e == null) {
                    // Report the friendship status to the listener
                    switch (friendStatus) {
                        case "friend":
                            listener.onFriendshipFound(FRIENDS);
                            break;
                        case "outgoing":
                            listener.onFriendshipFound(INCOMING);
                            break;
                        case "incoming":
                            listener.onFriendshipFound(OUTGOING);
                            break;
                        case "not_friend":
                            listener.onFriendshipFound(NOT_FRIENDS);
                            break;
                    }
                }

            }
        });
    }

    public static ParseQuery<Friend> getQuery() {
        return new ParseQuery<Friend>(Friend.class);
    }

    public interface OnFriendshipFoundListener {
        void onFriendshipFound(int friendStatus);
    }
}

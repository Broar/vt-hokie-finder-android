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

/**
 * A POJO representing a friendship relationship from one ParseUser to another
 *
 * @author Steven Briggs
 * @version 2015.10.29
 */
@ParseClassName("Friend")
public class Friend extends ParseObject {

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

    public static ParseQuery<Friend> getQuery() {
        return new ParseQuery<Friend>(Friend.class);
    }
}

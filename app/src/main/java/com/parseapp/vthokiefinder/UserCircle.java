package com.parseapp.vthokiefinder;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A POJO representing a relationship between a Circle and ParseUser
 *
 * @author Steven Briggs
 * @version 2015.10.03
 */
@ParseClassName("UserCircle")
public class UserCircle extends ParseObject {

    public UserCircle() {
        // Required default constructor
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public Circle getCircle() {
        return (Circle) getParseObject("circle");
    }

    public void setCircle(Circle circle) {
        put("circle", circle);
    }

    public boolean isBroadcasting() {
        return getBoolean("isBroadcasting");
    }

    public void setIsBroadcasting(boolean isBroadcasting) {
        put("isBroadcasting", isBroadcasting);
    }

    public boolean isPending() {
        return getBoolean("pending");
    }

    public void setIsPending(boolean pending) {
        put("pending", pending);
    }

    public static ParseQuery<UserCircle> getQuery() {
        return new ParseQuery<UserCircle>(UserCircle.class);
    }
}

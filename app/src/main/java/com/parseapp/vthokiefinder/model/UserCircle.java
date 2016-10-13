package com.parseapp.vthokiefinder.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A POJO representing a relationship between a Circle and ParseUser
 *
 * @author Steven Briggs
 * @version 2015.11.24
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

    public boolean isAccepted() {
        return getBoolean("accepted");
    }

    public void setIsAccepted(boolean accepted) {
        put("accepted", accepted);
    }

    public ParseUser getFriend() {
        return getParseUser("friend");
    }

    public void setFriend(ParseUser user) {
        put("friend", user);
    }

    public boolean isInvite() {
        return getBoolean("isInvite");
    }

    public void setIsInvite(boolean isInvite) {
        put("isInvite", isInvite);
    }

    public static ParseQuery<UserCircle> getQuery() {
        return new ParseQuery<UserCircle>(UserCircle.class);
    }
}

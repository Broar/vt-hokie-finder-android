package com.parseapp.vthokiefinder;

import android.content.Context;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


/**
 * A POJO representing a HokieFinder Circle
 *
 * @author Steven Briggs
 * @version 2015.09.16
 */
@ParseClassName("Circle")
public class Circle extends ParseObject {

    public Circle() {
        // Default constructor is required
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public ParseFile getIcon() {
        return getParseFile("icon");
    }

    public void setIcon(ParseFile icon) {
        put("icon", icon);
    }

    /**
     * Determine if the specified user is a member of this circle
     *
     * @param user the ParseUser whose membership is to be tested
     * @param listener a callback for when the user's membership has been determined
     */
    public void isMember(ParseUser user, final OnMembershipFoundListener listener) {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("circle", this).whereEqualTo("user", user);
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null && !userCircles.isEmpty()) {
                    listener.onMembershipFound(true);
                }

                else if (e == null) {
                    listener.onMembershipFound(false);
                }
            }
        });
    }

    public static ParseQuery<Circle> getQuery() {
        return new ParseQuery<Circle>(Circle.class);
    }

    public interface OnMembershipFoundListener {
        void onMembershipFound(boolean isMember);
    }
}

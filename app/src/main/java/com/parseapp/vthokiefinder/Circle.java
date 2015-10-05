package com.parseapp.vthokiefinder;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;


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

    public static ParseQuery<Circle> getQuery() {
        return new ParseQuery<Circle>(Circle.class);
    }
}

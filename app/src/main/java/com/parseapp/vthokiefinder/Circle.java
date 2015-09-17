package com.parseapp.vthokiefinder;

/**
 * A POJO representing a HokieFinder Circle
 *
 * @author Steven Briggs
 * @version 2015.09.16
 */
public class Circle {
    private String mObjectId;
    private String mName;

    public Circle(String objectId, String name) {
        mObjectId = objectId;
        mName = name;
    }

    public String getObjectId() {
        return mObjectId;
    }

    public void setObjectId(String mObjectId) {
        this.mObjectId = mObjectId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }
}

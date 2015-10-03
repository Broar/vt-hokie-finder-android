package com.parseapp.vthokiefinder;

import com.parse.ParseClassName;

import java.util.ArrayList;

/**
 * A POJO representing a HokieFinder Circle
 *
 * @author Steven Briggs
 * @version 2015.09.16
 */
public class Circle {
    private String mObjectId;
    private String mName;
    private ArrayList<String> mMembers;

    public Circle(String objectId, String name) {
        mObjectId = objectId;
        mName = name;
        mMembers = new ArrayList<String>();
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

    public ArrayList<String> getMembers() {
        return mMembers;
    }

    public void setMembers(ArrayList<String> members) {
        mMembers = members;
    }
}

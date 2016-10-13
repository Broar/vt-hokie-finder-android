package com.parseapp.vthokiefinder;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parseapp.vthokiefinder.model.Circle;
import com.parseapp.vthokiefinder.model.UserCircle;
import com.parseapp.vthokiefinder.model.Friend;

/**
 * Application subclass required by Parse framework to initialize global state
 *
 * @author Steven Briggs
 * @version 2015.09.11
 */
public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(Circle.class);
        ParseObject.registerSubclass(UserCircle.class);
        ParseObject.registerSubclass(Friend.class);
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(this, "Bugsei9yjtBNjH51JSerSEniB895r1zLA3NwFDUO", "ilswrJMAc26LQCRptGaMn5XiH5uVTIHrkfLP82W3");
        ParseFacebookUtils.initialize(this);
    }
}

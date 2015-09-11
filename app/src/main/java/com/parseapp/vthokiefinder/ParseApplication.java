package com.parseapp.vthokiefinder;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

/**
 * Application subclass required by Parse framework to initialize global state
 *
 * @author Steven Briggs
 * @version 2015.09.09
 */
public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(this, "Bugsei9yjtBNjH51JSerSEniB895r1zLA3NwFDUO", "ilswrJMAc26LQCRptGaMn5XiH5uVTIHrkfLP82W3");

        //ParseUser.enableAutomaticUser();
        //ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        //ParseACL.setDefaultACL(defaultACL, true);
    }
}

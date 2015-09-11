package com.parseapp.vthokiefinder;

import android.app.Application;

import com.parse.Parse;

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
        Parse.initialize(this, "Bugsei9yjtBNjH51JSerSEniB895r1zLA3NwFDUO", "ilswrJMAc26LQCRptGaMn5XiH5uVTIHrkfLP82W3");
    }
}

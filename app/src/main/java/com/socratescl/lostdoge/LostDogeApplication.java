package com.socratescl.lostdoge;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class LostDogeApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "BNecrbcvzsmcN1EnFi6GM8uvPE7xzYrL96UrUuWy", "Q3fe40lRopUyS1EPxhjeVxUxEzdnKCvE9wnUCfw7");
    }
    public static void updateParseInstallation(ParseUser user){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
        installation.saveInBackground();
    }
}

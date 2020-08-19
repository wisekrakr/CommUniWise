package com.wisekrakr.communiwise.phone;

import java.util.prefs.Preferences;

public class PreferencesUtil {

    private Preferences preferences;

    public void setPreference() {
        // This will define a node in which the preferences can be stored
        preferences = Preferences.userRoot().node(this.getClass().getName());
        String ID1 = "Test1";
        String ID2 = "Test2";
        String ID3 = "Test3";

        // First we will get the values
        // Define a boolean value
        System.out.println(preferences.getBoolean(ID1, true));
        // Define a string with default "Hello World
        System.out.println(preferences.get(ID2, "Hello World"));
        // Define a integer with default 50
        System.out.println(preferences.getInt(ID3, 50));

        // now set the values
        preferences.putBoolean(ID1, false);
        preferences.put(ID2, "Hello Europa");
        preferences.putInt(ID3, 45);

        // Delete the preference settings for the first value
        preferences.remove(ID1);

    }

}

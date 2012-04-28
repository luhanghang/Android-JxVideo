package com.bsht;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by IntelliJ IDEA.
 * User: luhang
 * Date: Apr 28, 2010
 * Time: 10:06:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class JxVideoPreferences extends PreferenceActivity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}

package org.kinjeng.apmpilot.activities;

import android.view.View;

import org.kinjeng.apmpilot.classes.CustomDrone;
import org.kinjeng.apmpilot.classes.KinjengDrone;

/**
 * Created by sblaksono on 02/11/2016.
 */

public class KinjengActivity extends MainActivity {

    @Override
    protected CustomDrone createDrone() {
        return new KinjengDrone(getApplicationContext());
    }

    @Override
    protected void updateConnectionState() {
        super.updateConnectionState();
        if (drone.isConnected()) {

        } else {

        }
    }
}

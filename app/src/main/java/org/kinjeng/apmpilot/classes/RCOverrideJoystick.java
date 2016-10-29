package org.kinjeng.apmpilot.classes;

import android.content.Context;

import com.o3dr.android.client.Drone;

/**
 * Created by sblaksono on 24/10/2016.
 */

public class RCOverrideJoystick extends BaseJoystick {

    public RCOverrideJoystick(Context _context) {
        super(_context);
    }

    @Override
    protected void processJoystickInput(Drone drone, float throttle, float roll, float pitch, float yaw, float ltrigger, float rtrigger) {

    }
}

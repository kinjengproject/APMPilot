package org.kinjeng.apmpilot.classes;

import android.content.Context;

/**
 * Created by sblaksono on 24/10/2016.
 */

public class RCOverrideJoystick extends BaseJoystick {

    // hardcoded - todo : get these mavlink parameters from drone
    protected int throttleMinRC = 1000;
    protected int throttleMaxRC = 2000;
    protected int rollMinRC = 1000;
    protected int rollMaxRC = 2000;
    protected int pitchMinRC = 1000;
    protected int pitchMaxRC = 2000;
    protected int yawMinRC = 1000;
    protected int yawMaxRC = 2000;

    public RCOverrideJoystick(Context _context) {
        super(_context);
    }

    @Override
    protected void processJoystickInput(CustomDrone drone) {
        int[] rcOutputs = new int[CustomDrone.RC_OUTPUT_COUNT];

        // roll
        rcOutputs[0] = rollMinRC + ((int) (drone.getRoll() * (rollMaxRC - rollMinRC)));
        // pitch
        rcOutputs[1] = pitchMinRC + ((int) (drone.getPitch() * (pitchMaxRC - pitchMinRC)));
        // throttle
        rcOutputs[2] = throttleMinRC + ((int) (drone.getThrottle() * (throttleMaxRC - throttleMinRC)));
        // yaw
        rcOutputs[3] = yawMinRC + ((int) (drone.getYaw() * (yawMaxRC - yawMinRC)));

        rcOutputs[4] = 0;
        rcOutputs[5] = 0;
        rcOutputs[6] = 0;
        rcOutputs[7] = 0;

        drone.sendRcOverrideMsg(rcOutputs);
    }
}

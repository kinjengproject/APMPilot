package org.kinjeng.apmpilot.classes;

import android.content.Context;

/**
 * Created by sblaksono on 24/10/2016.
 */

public class RCOverrideJoystick extends BaseJoystick {

    // hardcoded - todo : get these mavlink parameters from drone
    protected int throttleMinRC = 1100;
    protected int throttleMaxRC = 1900;
    protected int rollMinRC = 1100;
    protected int rollMaxRC = 1900;
    protected int pitchMinRC = 1100;
    protected int pitchMaxRC = 1900;
    protected int yawMinRC = 1100;
    protected int yawMaxRC = 1900;

    public RCOverrideJoystick(Context context, CustomTower tower, CustomDrone drone) {
        super(context, tower, drone);
    }

    @Override
    protected void processJoystickInput() {
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

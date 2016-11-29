package org.kinjeng.apmpilot.classes;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.Calendar;

/**
 * Created by sblaksono on 24/10/2016.
 */

public abstract class BaseJoystick {

    protected CustomTower tower;
    protected CustomDrone drone;

    protected float x = 0.0f;
    protected float y = 0.0f;
    protected float z = 0.0f;
    protected float rz = 0.0f;
    protected float throttleRate = 0.2f;
    protected float rpyRate = 0.5f;

    protected float hx = 0;
    protected float hy = 0;
    protected float gimbalRate = 30;

    public BaseJoystick(CustomTower tower, CustomDrone drone) {
        this.tower = tower;
        this.drone = drone;
    }

    public abstract boolean processMotionEvent(MotionEvent event);

    public void processJoystickHat1() {
        if (drone.isGimbalActive()) {
            long d = Calendar.getInstance().getTimeInMillis() - drone.getLastGimbalUpdate();
            float gimbalPitch = drone.getGimbalPitch();
            float gimbalYaw = drone.getGimbalYaw();

            if (Float.compare(hx, -1.0f) == 0) {
                // LEFT
                gimbalYaw += (gimbalRate * d) / 1000;
            } else if (Float.compare(hx, 1.0f) == 0) {
                // RIGHT
                gimbalYaw -= (gimbalRate * d) / 1000;
            } else if (Float.compare(hy, -1.0f) == 0) {
                // UP
                gimbalPitch += (gimbalRate * d) / 1000;
            } else if (Float.compare(hy, 1.0f) == 0) {
                // DOWN
                gimbalPitch -= (gimbalRate * d) / 1000;
            }
            drone.setGimbalOrientation(gimbalPitch, 0, gimbalYaw);
        }
    }

    // process and convert current joystick inputs into 0.0f - 1.0f
    public void processJoystickInput1() {
        // throttle
        float throttle = drone.getThrottle();
        if (Settings.getBoolean("pref_throttle_mode_gamepad", true)) {
            // gamepad mode - process throttle only when inputs interval <= 500 ms, otherwise do not change
            long d = Calendar.getInstance().getTimeInMillis() - drone.getLastTRPYUpdate();
            if (d <= 500) {
                throttle = throttle + ((-y * d * throttleRate) / 1000);
                if (throttle < 0.0f) throttle = 0.0f;
                else if (throttle > 1.0f) throttle = 1.0f;
            }
        } else {
            // other mode - using joystick throttle control, not tested yet
            if (!Settings.getBoolean("pref_throttle_center_zero", true)) {
                throttle = (y + 1.0f) / 2.0f;
            } else {
                throttle = y;
            }
        }

        // roll
        float roll = drone.getRoll();
        float roll2 = (z + 1.0f) / 2.0f;
        long d = Calendar.getInstance().getTimeInMillis() - drone.getLastTRPYUpdate();
        if (d <= 500) {
            if (roll2 >= 0.45f && roll2 <= 0.55f) {
                roll = roll2;
            }
            else if (roll > roll2) {
                roll = roll - ((d * rpyRate) / 1000);
                if (roll < roll2) roll = roll2;
            }
            else if (roll < roll2) {
                roll = roll + ((d * rpyRate) / 1000);
                if (roll > roll2) roll = roll2;
            }
            if (roll < 0.0f) roll = 0.0f;
            else if (roll > 1.0f) roll = 1.0f;
        }

        // pitch
        float pitch = drone.getPitch();
        float pitch2 = (rz + 1.0f) / 2.0f;
        d = Calendar.getInstance().getTimeInMillis() - drone.getLastTRPYUpdate();
        if (d <= 500) {
            if (pitch2 >= 0.45f && pitch2 <= 0.55f) {
                pitch = pitch2;
            }
            else if (pitch > pitch2) {
                pitch = pitch - ((d * rpyRate) / 1000);
                if (pitch < pitch2) pitch = pitch2;
            }
            else if (pitch < pitch2) {
                pitch = pitch + ((d * rpyRate) / 1000);
                if (pitch > pitch2) pitch = pitch2;
            }
            if (pitch < 0.0f) pitch = 0.0f;
            else if (pitch > 1.0f) pitch = 1.0f;
        }

        // yaw
        float yaw = drone.getYaw();
        float yaw2 = (x + 1.0f) / 2.0f;
        d = Calendar.getInstance().getTimeInMillis() - drone.getLastTRPYUpdate();
        if (d <= 500) {
            if (yaw2 >= 0.45f && yaw2 <= 0.55f) {
                yaw = yaw2;
            }
            else if (yaw > yaw2) {
                yaw = yaw - ((d * rpyRate) / 1000);
                if (yaw < yaw2) yaw = yaw2;
            }
            else if (yaw < yaw2) {
                yaw = yaw + ((d * rpyRate) / 1000);
                if (yaw > yaw2) yaw = yaw2;
            }
            if (yaw < 0.0f) yaw = 0.0f;
            else if (yaw > 1.0f) yaw = 1.0f;
        }

        drone.setTRPY(throttle, roll, pitch, yaw);
    }

    public abstract boolean processKeyEvent(KeyEvent event);

}

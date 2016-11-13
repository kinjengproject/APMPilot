package org.kinjeng.apmpilot.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.Calendar;

/**
 * Created by sblaksono on 24/10/2016.
 */

public abstract class BaseJoystick {

    protected Context context;
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

    public BaseJoystick(Context context, CustomTower tower, CustomDrone drone) {
        this.context = context;
        this.tower = tower;
        this.drone = drone;
    }

    public boolean processMotionEvent(MotionEvent event, SharedPreferences preferences) {

        // Check if this event if from a D-pad and process accordingly.
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {

            float xaxis = event.getAxisValue(MotionEvent.AXIS_HAT_X);
            float yaxis = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

            return true;
        }

        // Check if this event is from a joystick movement and process accordingly.
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                processJoystickInput0(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput0(event, -1);
            return true;
        }

        return false;
    }

    private float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
        if (range != null) {
            //final float flat = range.getFlat();
            final float value = historyPos < 0 ? event.getAxisValue(axis): event.getHistoricalAxisValue(axis, historyPos);
            //if (Math.abs(value) > flat) {
                return value;
            //}
        }
        return 0;
    }

    // translate inputs from joystick (from -1.0f to 1.0f)
    private void processJoystickInput0(MotionEvent event, int historyPos) {
        InputDevice mInputDevice = event.getDevice();

        hx = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_X, historyPos);
        hy = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_Y, historyPos);

        processJoystickHat1();

        y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Y, historyPos);
        z = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);
        rz = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RZ, historyPos);
        x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X, historyPos);

        processJoystickInput1();
    }

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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // throttle
        float throttle = drone.getThrottle();
        if (preferences.getBoolean("pref_throttle_mode_gamepad", true)) {
            // gamepad mode - process throttle only when inputs interval <= 500 ms, otherwise do not change
            long d = Calendar.getInstance().getTimeInMillis() - drone.getLastThrottleUpdate();
            if (d <= 500) {
                throttle = throttle + ((-y * d * throttleRate) / 1000);
                if (throttle < 0.0f) throttle = 0.0f;
                else if (throttle > 1.0f) throttle = 1.0f;
            }
        } else {
            // other mode - using joystick throttle control, not tested yet
            if (!preferences.getBoolean("pref_throttle_center_zero", true)) {
                throttle = (y + 1.0f) / 2.0f;
            } else {
                throttle = y;
            }
        }
        drone.setThrottle(throttle);

        // roll
        float roll = drone.getRoll();
        float roll2 = (z + 1.0f) / 2.0f;
        long d = Calendar.getInstance().getTimeInMillis() - drone.getLastRollUpdate();
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
        drone.setRoll(roll);

        // pitch
        float pitch = drone.getPitch();
        float pitch2 = (rz + 1.0f) / 2.0f;
        d = Calendar.getInstance().getTimeInMillis() - drone.getLastPitchUpdate();
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
        drone.setPitch(pitch);

        // yaw
        float yaw = drone.getYaw();
        float yaw2 = (x + 1.0f) / 2.0f;
        d = Calendar.getInstance().getTimeInMillis() - drone.getLastYawUpdate();
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
        drone.setYaw(yaw);

        processJoystickInput();
    }

    // do some manual control
    protected abstract void processJoystickInput();

    public boolean processKeyEvent(KeyEvent event, SharedPreferences preferences) {
        try {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                drone.setVehicleMode(preferences.getString("pref_joystick_button_a", "0"));
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B) {
                drone.setVehicleMode(preferences.getString("pref_joystick_button_b", "0"));
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                drone.setVehicleMode(preferences.getString("pref_joystick_button_x", "0"));
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
                drone.setVehicleMode(preferences.getString("pref_joystick_button_y", "0"));
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_L1) {
                drone.setGimbalOrientation(0, 0, 0);
                tower.setRefOrientation();
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_L2) {
                drone.triggerCamera();
                return true;
            }
            if (event.isLongPress()) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R2) {
                    drone.arm();
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R1) {
                    drone.disarm();
                }
            }

        } catch (Exception e) {

        }

        return false;
    }


}

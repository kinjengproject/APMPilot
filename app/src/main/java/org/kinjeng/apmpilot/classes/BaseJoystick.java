package org.kinjeng.apmpilot.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

/**
 * Created by sblaksono on 24/10/2016.
 */

public abstract class BaseJoystick {

    protected Context context;
    protected float currentThrottle = 0;

    public BaseJoystick(Context _context) {
        context = _context;
    }

    public boolean processMotionEvent(Drone drone, MotionEvent event, SharedPreferences preferences) {

        // Check if this event if from a D-pad and process accordingly.
        if ((event.getSource() & InputDevice.SOURCE_DPAD) != InputDevice.SOURCE_DPAD) {

            float xaxis = event.getAxisValue(MotionEvent.AXIS_HAT_X);
            float yaxis = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

            if (Float.compare(xaxis, -1.0f) == 0) {
                // LEFT

            }
            else if (Float.compare(xaxis, 1.0f) == 0) {
                // RIGHT

            }
            else if (Float.compare(yaxis, -1.0f) == 0) {
                // UP

            }
            else if (Float.compare(yaxis, 1.0f) == 0) {
                // DOWN

            }
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
                processJoystickInput0(drone, event, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput0(drone, event, -1);
            return true;
        }

        return false;
    }

    private float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value = historyPos < 0 ? event.getAxisValue(axis):
                    event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    private void processJoystickInput0(Drone drone, MotionEvent event, int historyPos) {
        InputDevice mInputDevice = event.getDevice();

        float throttle = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Y, historyPos);
        float roll = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);
        float pitch = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RZ, historyPos) * -1;
        float yaw = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X, historyPos);
        float ltrigger = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_LTRIGGER, historyPos);
        float rtrigger = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RTRIGGER, historyPos);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (!preferences.getBoolean("pref_throttle_center_zero", true)) {
            throttle = (throttle * 2.0f) / 1.0f;
        }

        if (preferences.getBoolean("pref_throttle_mode_gamepad", true)) {
            currentThrottle = currentThrottle + (throttle / 10);
            if (currentThrottle < -1.0f) currentThrottle = -1.0f;
            else if (currentThrottle > 1.0f) currentThrottle = 1.0f;
        }
        else {
            currentThrottle = throttle;
        }

        processJoystickInput(drone, currentThrottle, roll, pitch, yaw, ltrigger, rtrigger);

    }

    protected abstract void processJoystickInput(Drone drone, float throttle, float roll,
                                        float pitch, float yaw, float ltrigger, float rtrigger);

    protected VehicleMode getVehicleMode(int prefValue) {
        for (VehicleMode vehicleMode : VehicleMode.getVehicleModePerDroneType(Type.TYPE_COPTER)) {
            if (vehicleMode.getMode() == prefValue) {
                return vehicleMode;
            }
        }
        return VehicleMode.COPTER_STABILIZE;
    }

    public boolean processKeyEvent(Drone drone, KeyEvent event, SharedPreferences preferences) {
        try {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                VehicleApi.getApi(drone).setVehicleMode(getVehicleMode(Integer.parseInt(preferences.getString("pref_joystick_button_a", "0"))));
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B) {
                VehicleApi.getApi(drone).setVehicleMode(getVehicleMode(Integer.parseInt(preferences.getString("pref_joystick_button_b", "0"))));
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                VehicleApi.getApi(drone).setVehicleMode(getVehicleMode(Integer.parseInt(preferences.getString("pref_joystick_button_x", "0"))));
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
                VehicleApi.getApi(drone).setVehicleMode(getVehicleMode(Integer.parseInt(preferences.getString("pref_joystick_button_y", "0"))));
                return true;
            }

            if (event.isLongPress()) {
                if (Integer.parseInt(preferences.getString("pref_drone_arm", "0")) == event.getKeyCode()) {
                    VehicleApi.getApi(drone).arm(true);
                }
                if (Integer.parseInt(preferences.getString("pref_drone_disarm", "0")) == event.getKeyCode()) {
                    VehicleApi.getApi(drone).arm(false);
                }
                if (Integer.parseInt(preferences.getString("pref_drone_land", "0")) == event.getKeyCode()) {
                    VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_LAND);

                }
            }

        } catch (Exception e) {

        }

        return false;
    }


}

package org.kinjeng.apmpilot.classes;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by sblaksono on 19/11/2016.
 */

public class PhysicalJoystick extends BaseJoystick {

    public PhysicalJoystick(CustomTower tower, CustomDrone drone) {
        super(tower, drone);
    }

    public boolean processMotionEvent(MotionEvent event) {

        // Check if this event if from a D-pad and process accordingly.
        /*if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {

            float xaxis = event.getAxisValue(MotionEvent.AXIS_HAT_X);
            float yaxis = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

            return true;
        }*/

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

    public boolean processKeyEvent(KeyEvent event) {
        try {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                drone.setVehicleMode(Settings.getString("pref_joystick_button_a", "0"));
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B) {
                drone.setVehicleMode(Settings.getString("pref_joystick_button_b", "0"));
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                drone.setVehicleMode(Settings.getString("pref_joystick_button_x", "0"));
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
                drone.setVehicleMode(Settings.getString("pref_joystick_button_y", "0"));
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

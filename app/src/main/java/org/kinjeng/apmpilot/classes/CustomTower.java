package org.kinjeng.apmpilot.classes;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

import java.util.Calendar;

/**
 * Created by sblaksono on 07/11/2016.
 */

public class CustomTower extends ControlTower implements SensorEventListener, DroneListener {

    protected float[] gravityValues;
    protected float[] magneticValues;
    protected float[] gyroValues;
    protected float[] orientationValues;
    protected float[] refOrientationValues;
    protected long lastGimbalUpdate;

    protected float gimbalRate = 180;

    protected CustomDrone drone = null;

    protected Context context;
    protected SensorManager sensorManager;

    protected final Handler handler = new Handler();

    public CustomTower(Context context) {
        super(context);
        this.context = context;
        refOrientationValues = new float[3];

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = event.values;
        }
        if (gravityValues != null && magneticValues != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);
            if (success) {
                orientationValues = new float[3];
                SensorManager.getOrientation(R, orientationValues);
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroValues = event.values;
        }
        updateGimbal();
    }

    public void updateGimbal() {
        if (Settings.getBoolean("pref_gimbal_accelerometer", true)) {
            if (orientationValues != null && refOrientationValues != null) {
                float gimbalPitch = drone.getGimbalPitch();
                float gimbalRoll = drone.getGimbalRoll();
                float gimbalYaw = drone.getGimbalYaw();
                long d = Calendar.getInstance().getTimeInMillis() - lastGimbalUpdate;

                float yaw = (float) Math.toDegrees(orientationValues[0] - refOrientationValues[0]) * -2;
                if (gimbalYaw < yaw) {
                    gimbalYaw += (gimbalRate * d) / 1000;
                } else if (gimbalYaw > yaw) {
                    gimbalYaw -= (gimbalRate * d) / 1000;
                }

                float pitch = ((float) Math.toDegrees(orientationValues[2] - refOrientationValues[2]) * -2) - 45;
                if (gimbalPitch < pitch) {
                    gimbalPitch += (gimbalRate * d) / 1000;
                } else if (gimbalPitch > pitch) {
                    gimbalPitch -= (gimbalRate * d) / 1000;
                }

                float roll = (float) Math.toDegrees(orientationValues[1] - refOrientationValues[1]) * -2;
                if (gimbalRoll < roll) {
                    gimbalRoll += (gimbalRate * d) / 1000;
                } else if (gimbalRoll > roll) {
                    gimbalRoll -= (gimbalRate * d) / 1000;
                }
                drone.setGimbalOrientation(gimbalPitch, gimbalRoll, gimbalYaw);
            }
            lastGimbalUpdate = Calendar.getInstance().getTimeInMillis();
        }
    }

    public void setRefOrientation() {
        refOrientationValues = orientationValues;
    }

    public void registerDrone(Drone drone) {
        if ((this.drone == null) && (drone instanceof CustomDrone)) {
            registerDrone(drone, handler);
            this.drone = (CustomDrone) drone;
            drone.registerDroneListener(this);
        }
    }

    public void unregisterDrone() {
        if (this.drone != null) {
            drone.unregisterDroneListener(this);
            unregisterDrone(drone);
            drone = null;
        }
    }

    public void startMotionSensor() {
        if (Settings.getBoolean("pref_gimbal_accelerometer", true)) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void stopMotionSensor() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
            case AttributeEvent.STATE_DISCONNECTED:

                break;

            case AttributeEvent.STATE_VEHICLE_MODE:
            case AttributeEvent.STATE_ARMING:
            case AttributeEvent.STATE_UPDATED:

                break;

            case AttributeEvent.TYPE_UPDATED:
            case AttributeEvent.ALTITUDE_UPDATED:
            case AttributeEvent.SPEED_UPDATED:
            case AttributeEvent.BATTERY_UPDATED:

                break;

            case AttributeEvent.ATTITUDE_UPDATED:

                break;

            case AttributeEvent.GPS_FIX:
            case AttributeEvent.GPS_POSITION:
            case AttributeEvent.GPS_COUNT:
            case AttributeEvent.WARNING_NO_GPS:

                break;

            case AttributeEvent.AUTOPILOT_ERROR:
            case AttributeEvent.AUTOPILOT_MESSAGE:

                break;

            default:
                break;
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

}

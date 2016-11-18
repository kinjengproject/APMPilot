package org.kinjeng.apmpilot.classes;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by sblaksono on 07/11/2016.
 */

public class CustomTower extends ControlTower implements SensorEventListener {

    protected float[] gravityValues;
    protected float[] magneticValues;
    protected float[] gyroValues;
    protected float[] orientationValues;
    protected float[] refOrientationValues;
    protected long lastGimbalUpdate;

    protected float gimbalRate = 180;

    protected ArrayList<CustomDrone> drones;

    protected Context context;
    protected SensorManager sensorManager;

    public CustomTower(Context context) {
        super(context);
        this.context = context;
        refOrientationValues = new float[3];
        drones = new ArrayList<>();

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
                for (CustomDrone drone : drones) {
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
    }

    public void setRefOrientation() {
        refOrientationValues = orientationValues;
    }

    @Override
    public void registerDrone(Drone drone, Handler handler) {
        super.registerDrone(drone, handler);
        drones.add((CustomDrone) drone);
    }

    @Override
    public void unregisterDrone(Drone drone) {
        super.unregisterDrone(drone);
        drones.remove(drone);
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

}

package org.kinjeng.apmpilot.classes;

import android.content.Context;

import com.MAVLink.common.msg_rc_channels_override;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ExperimentalApi;
import com.o3dr.android.client.apis.GimbalApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;

import java.util.Calendar;

/**
 * Created by sblaksono on 30/10/2016.
 */

public class CustomDrone extends Drone implements GimbalApi.GimbalOrientationListener {

    public static  int RC_OUTPUT_COUNT = 8;

    /**
     * Creates a Drone instance.
     *
     * @param context Application context
     */
    public CustomDrone(Context context) {
        super(context);
    }

    protected float throttle = 0.0f;
    protected long lastThrottleUpdate = 0;
    protected float roll = 0.5f;
    protected long lastRollUpdate = 0;
    protected float pitch = 0.5f;
    protected long lastPitchUpdate = 0;
    protected float yaw = 0.5f;
    protected long lastYawUpdate = 0;
    protected boolean gimbalActive = false;
    protected long minGimbalPitch = -45;
    protected long maxGimbalPitch = 45;
    protected long minGimbalRoll = -45;
    protected long maxGimbalRoll = 45;
    protected long minGimbalYaw = -45;
    protected long maxGimbalYaw = 45;
    protected long lastGimbalUpdate = 0;

    public float getThrottle() {
        return throttle;
    }

    public void setThrottle(float throttle) {
        this.throttle = throttle;
        lastThrottleUpdate = Calendar.getInstance().getTimeInMillis();
    }

    public long getLastThrottleUpdate() {
        return lastThrottleUpdate;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
        lastRollUpdate = Calendar.getInstance().getTimeInMillis();
    }

    public long getLastRollUpdate() {
        return lastRollUpdate;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        lastPitchUpdate = Calendar.getInstance().getTimeInMillis();
    }

    public long getLastPitchUpdate() {
        return lastPitchUpdate;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        lastYawUpdate = Calendar.getInstance().getTimeInMillis();
    }

    public boolean isGimbalActive() {
        return gimbalActive;
    }

    public long getMinGimbalPitch() {
        return minGimbalPitch;
    }

    public long getMaxGimbalPitch() {
        return maxGimbalPitch;
    }

    public long getMinGimbalRoll() {
        return minGimbalRoll;
    }

    public long getMaxGimbalRoll() {
        return maxGimbalRoll;
    }

    public long getMinGimbalYaw() {
        return minGimbalYaw;
    }

    public long getMaxGimbalYaw() {
        return maxGimbalYaw;
    }

    public long getLastYawUpdate() {
        return lastYawUpdate;
    }

    public void sendRcOverrideMsg(int[] rcOutputs) {

        msg_rc_channels_override msg = new msg_rc_channels_override();
        msg.chan1_raw = (short)rcOutputs[0];
        msg.chan2_raw = (short)rcOutputs[1];
        msg.chan3_raw = (short)rcOutputs[2];
        msg.chan4_raw = (short)rcOutputs[3];
        msg.chan5_raw = (short)rcOutputs[4];
        msg.chan6_raw = (short)rcOutputs[5];
        msg.chan7_raw = (short)rcOutputs[6];
        msg.chan8_raw = (short)rcOutputs[7];
        msg.target_system = 0;
        msg.target_component = 0;

        ExperimentalApi.getApi(this).sendMavlinkMessage(new MavlinkMessageWrapper(msg));
    }

    // get vehicle mode
    protected VehicleMode getVehicleMode(int mode) {
        for (VehicleMode vehicleMode : VehicleMode.getVehicleModePerDroneType(Type.TYPE_COPTER)) {
            if (vehicleMode.getMode() == mode) {
                return vehicleMode;
            }
        }
        return VehicleMode.COPTER_STABILIZE;
    }

    public void setVehicleMode(String mode) {
        setVehicleMode(Integer.parseInt(mode));
    }

    public void setVehicleMode(int mode) {
        VehicleApi.getApi(this).setVehicleMode(getVehicleMode(mode));
    }

    public void arm() {
        State vehicleState = getAttribute(AttributeType.STATE);
        if (!vehicleState.isArmed()) {
            setThrottle(0.0f);
            setRoll(0.5f);
            setPitch(0.5f);
            setYaw(0.5f);
            VehicleApi.getApi(this).arm(true);
        }
    }

    public void disarm() {
        State vehicleState = getAttribute(AttributeType.STATE);
        if (!vehicleState.isFlying()) {
            VehicleApi.getApi(this).arm(false);
        }
    }

    public long getLastGimbalUpdate() {
        return lastGimbalUpdate;
    }

    public void setGimbalOrientation(float pitch, float roll, float yaw) {
        GimbalApi.getApi(this).updateGimbalOrientation(pitch, roll, yaw, this);
        lastGimbalUpdate = Calendar.getInstance().getTimeInMillis();
    }

    public void triggerCamera() {
        // to be implemented
    }

    public void startGimbalControl() {
        GimbalApi.getApi(this).startGimbalControl(this);
        gimbalActive = true;
    }

    public void stopGimbalControl() {
        GimbalApi.getApi(this).stopGimbalControl(this);
        gimbalActive = false;
    }

    @Override
    public void onGimbalOrientationUpdate(GimbalApi.GimbalOrientation orientation) {

    }

    @Override
    public void onGimbalOrientationCommandError(int error) {

    }
}

package org.kinjeng.apmpilot.classes;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.MAVLink.common.msg_rc_channels_override;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.ExperimentalApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;

import org.kinjeng.apmpilot.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by sblaksono on 30/10/2016.
 */

public class CustomDrone extends Drone {

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

    public void setVehicleModeRTL() {
        VehicleApi.getApi(this).setVehicleMode(VehicleMode.COPTER_RTL);
    }

    public void setVehicleModeLand() {
        VehicleApi.getApi(this).setVehicleMode(VehicleMode.COPTER_LAND);
    }

}

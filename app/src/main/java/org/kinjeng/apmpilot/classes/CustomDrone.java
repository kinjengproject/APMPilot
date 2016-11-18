package org.kinjeng.apmpilot.classes;

import android.content.Context;

import com.MAVLink.common.msg_rc_channels_override;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.MavlinkObserver;
import com.o3dr.android.client.apis.ExperimentalApi;
import com.o3dr.android.client.apis.GimbalApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
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

    private Context context;

    /**
     * Creates a Drone instance.
     *
     * @param context Application context
     */
    public CustomDrone(Context context) {
        super(context);
        this.context = context;
    }

    protected short rcThrottleMin = 1100;
    protected short rcThrottleMax = 1900;
    protected short rcRollMin = 1100;
    protected short rcRollMax = 1900;
    protected short rcPitchMin = 1100;
    protected short rcPitchMax = 1900;
    protected short rcYawMin = 1100;
    protected short rcYawMax = 1900;
    protected short rcChannel5 = 0;
    protected short rcChannel6 = 0;
    protected short rcChannel7 = 0;
    protected short rcChannel8 = 0;

    protected float throttle = 0.0f;
    protected float roll = 0.5f;
    protected float pitch = 0.5f;
    protected float yaw = 0.5f;
    protected long lastTRPYUpdate = 0;

    protected boolean gimbalActive = false;
    protected float minGimbalPitch = -45;
    protected float maxGimbalPitch = 45;
    protected float minGimbalRoll = -45;
    protected float maxGimbalRoll = 45;
    protected float minGimbalYaw = -45;
    protected float maxGimbalYaw = 45;
    protected float gimbalRoll = 0;
    protected float gimbalPitch = 0;
    protected float gimbalYaw = 0;
    protected long lastGimbalUpdate = 0;

    public float getThrottle() {
        return throttle;
    }

    public float getRoll() {
        return roll;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public long getLastTRPYUpdate() {
        return lastTRPYUpdate;
    }

    public void setTRPY(float throttle, float roll, float pitch, float yaw) {
        if ((this.throttle != throttle) || (this.roll != roll) || (this.pitch != pitch) || (this.yaw != yaw)) {
            this.throttle = throttle;
            this.roll = roll;
            this.pitch = pitch;
            this.yaw = yaw;

            if (Settings.getInt("pref_manual_mode", 1) == 1) {

                msg_rc_channels_override msg = new msg_rc_channels_override();
                msg.chan1_raw = rcRollMin + ((short) (roll * (rcRollMax - rcRollMin)));;
                msg.chan2_raw = rcPitchMin + ((int) (pitch * (rcPitchMax - rcPitchMin)));
                msg.chan3_raw = rcThrottleMin + ((int) (throttle * (rcThrottleMax - rcThrottleMin)));
                msg.chan4_raw = rcYawMin + ((int) (yaw * (rcYawMax - rcYawMin)));
                msg.chan5_raw = rcChannel5;
                msg.chan6_raw = rcChannel6;
                msg.chan7_raw = rcChannel7;
                msg.chan8_raw = rcChannel8;
                msg.target_system = 0;
                msg.target_component = 0;

                ExperimentalApi.getApi(this).sendMavlinkMessage(new MavlinkMessageWrapper(msg));
            }

        }
        lastTRPYUpdate = Calendar.getInstance().getTimeInMillis();
    }

    public void sendRcOverrideMsg(int[] rcOutputs) {

    }

    public boolean isGimbalActive() {
        return gimbalActive;
    }

    public float getMinGimbalPitch() {
        return minGimbalPitch;
    }

    public float getMaxGimbalPitch() {
        return maxGimbalPitch;
    }

    public float getMinGimbalRoll() {
        return minGimbalRoll;
    }

    public float getMaxGimbalRoll() {
        return maxGimbalRoll;
    }

    public float getMinGimbalYaw() {
        return minGimbalYaw;
    }

    public float getMaxGimbalYaw() {
        return maxGimbalYaw;
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
            setTRPY(0.0f, 0.5f, 0.5f, 0.5f);
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
        if (pitch < getMinGimbalPitch()) pitch = getMinGimbalPitch();
        if (pitch > getMaxGimbalPitch()) pitch = getMaxGimbalPitch();
        if (roll < getMinGimbalRoll()) roll = getMinGimbalRoll();
        if (roll > getMaxGimbalRoll()) roll = getMaxGimbalRoll();
        if (yaw < getMinGimbalYaw()) yaw = getMinGimbalYaw();
        if (yaw > getMaxGimbalYaw()) yaw = getMaxGimbalYaw();
        if ((gimbalPitch != pitch) || (gimbalRoll != roll) || (gimbalYaw != yaw)) {
            GimbalApi.getApi(this).updateGimbalOrientation(pitch, roll, yaw, this);
            gimbalRoll = roll;
            gimbalPitch = pitch;
            gimbalYaw = yaw;
        }
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
        gimbalActive = false;
        GimbalApi.getApi(this).stopGimbalControl(this);
    }

    public float getGimbalRoll() {
        return gimbalRoll;
    }

    public float getGimbalPitch() {
        return gimbalPitch;
    }

    public float getGimbalYaw() {
        return gimbalYaw;
    }

    @Override
    public void onGimbalOrientationUpdate(GimbalApi.GimbalOrientation orientation) {
        gimbalRoll = orientation.getRoll();
        gimbalPitch = orientation.getPitch();
        gimbalYaw = orientation.getYaw();
    }

    @Override
    public void onGimbalOrientationCommandError(int error) {
        if (gimbalActive) {
            stopGimbalControl();
            startGimbalControl();
        }
        else {
            stopGimbalControl();
        }
    }

    @Override
    public void disconnect() {
        if (gimbalActive) {
            stopGimbalControl();
        }
        super.disconnect();
    }

    public void connect() {
        String prefProtocol = Settings.getString("pref_protocol", "");
        if (prefProtocol.equals("UDP")) {
            int udpPort = ConnectionType.DEFAULT_UDP_SERVER_PORT;
            try {
                udpPort = Integer.parseInt(Settings.getString("pref_udp_port", ""));
            }
            catch (Exception e) {
            }
            ConnectionParameter connectionParams = ConnectionParameter.newUdpConnection(udpPort, null);
            connect(connectionParams);
        }
        else if (prefProtocol.equals("TCP")) {
            String tcpHost = Settings.getString("pref_tcp_host", "");
            int tcpPort = ConnectionType.DEFAULT_TCP_SERVER_PORT;
            try {
                tcpPort = Settings.getInt("pref_tcp_port", 0);
            }
            catch (Exception e) {
            }
            ConnectionParameter connectionParams = ConnectionParameter.newTcpConnection(tcpHost, tcpPort, null);
            connect(connectionParams);
        }
        else if (prefProtocol.equals("USB")) {
            int usbBaudRate = ConnectionType.DEFAULT_USB_BAUD_RATE;
            try {
                usbBaudRate = Settings.getInt("pref_usb_baud_rate", 0);
            }
            catch (Exception e) {
            }
            ConnectionParameter connectionParams = ConnectionParameter.newUsbConnection(usbBaudRate, null);
            connect(connectionParams);
        }
    }

}

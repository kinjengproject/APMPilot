package org.kinjeng.apmpilot.classes;

import android.content.Context;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_rc_channels_override;
import com.MAVLink.common.msg_rc_channels_raw;
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

    protected Context context;

    protected int rcThrottleMin = 1100;
    protected int rcThrottleMax = 1900;
    protected int rcRollMin = 1100;
    protected int rcRollMax = 1900;
    protected int rcPitchMin = 1100;
    protected int rcPitchMax = 1900;
    protected int rcYawMin = 1100;
    protected int rcYawMax = 1900;
    protected int rcChannel1 = 1500;
    protected int rcChannel2 = 1500;
    protected int rcChannel3 = 1100;
    protected int rcChannel4 = 1500;
    protected int rcChannel5 = 0;
    protected int rcChannel6 = 0;
    protected int rcChannel7 = 0;
    protected int rcChannel8 = 0;

    protected float throttle = 0.0f;
    protected float roll = 0.5f;
    protected float pitch = 0.5f;
    protected float yaw = 0.5f;
    protected long lastTRPYUpdate = 0;
    protected long lastTRPYSend = 0;

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

    protected MavlinkObserver observer;

    /**
     * Creates a Drone instance.
     *
     * @param context Application context
     */
    public CustomDrone(Context context) {
        super(context);
        this.context = context;

        observer = new MavlinkObserver() {
            @Override
            public void onMavlinkMessageReceived(MavlinkMessageWrapper mavlinkMessageWrapper) {
                processMavlinkMessage(mavlinkMessageWrapper);
            }
        };
    }

    protected void processMavlinkMessage(MavlinkMessageWrapper mavlinkMessageWrapper) {
        MAVLinkMessage mavLinkMessage = mavlinkMessageWrapper.getMavLinkMessage();
        if (mavLinkMessage instanceof msg_rc_channels_raw) {
            msg_rc_channels_raw msg = (msg_rc_channels_raw) mavLinkMessage;
            rcChannel1 = msg.chan1_raw;
            rcChannel2 = msg.chan2_raw;
            rcChannel3 = msg.chan3_raw;
            rcChannel4 = msg.chan4_raw;
            rcChannel5 = msg.chan5_raw;
            rcChannel6 = msg.chan6_raw;
            rcChannel7 = msg.chan7_raw;
            rcChannel8 = msg.chan8_raw;
            // sync
            if (Calendar.getInstance().getTimeInMillis() - lastTRPYSend > 500) {
                syncTRPY();
            }
        }

    }

    protected void syncTRPY() {
        roll = ((float) (rcChannel1 - rcRollMin)) / ((float) (rcRollMax - rcRollMin));
        pitch = ((float) (rcChannel2 - rcPitchMin)) / ((float) (rcPitchMax - rcPitchMin));
        throttle = ((float) (rcChannel3 - rcThrottleMin)) / ((float) (rcThrottleMax - rcThrottleMin));
        yaw = ((float) (rcChannel4 - rcYawMin)) / ((float) (rcYawMax - rcYawMin));
    }

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

    protected boolean isEqual(float a, float b) {
        return (Math.abs(a - b) < 0.0001);
    }

    public void setTRPY(float throttle, float roll, float pitch, float yaw) {
        if (!isEqual(this.throttle, throttle) || !isEqual(this.roll, roll) || !isEqual(this.pitch, pitch) || !isEqual(this.yaw, yaw)) {
            this.throttle = throttle;
            this.roll = roll;
            this.pitch = pitch;
            this.yaw = yaw;

            if (Settings.getInt("pref_manual_mode", 1) == 1) {

                msg_rc_channels_override msg = new msg_rc_channels_override();
                msg.chan1_raw = rcRollMin + ((int) (roll * (rcRollMax - rcRollMin)));;
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
                lastTRPYSend = Calendar.getInstance().getTimeInMillis();
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
        removeMavlinkObserver(observer);
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
            addMavlinkObserver(observer);
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
            addMavlinkObserver(observer);
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
            addMavlinkObserver(observer);
        }
    }

}

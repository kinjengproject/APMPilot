package org.kinjeng.apmpilot.views;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;

import org.kinjeng.apmpilot.classes.CustomDrone;

import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by sblaksono on 21/11/2016.
 */

public abstract class FlightDataView extends SurfaceView implements SurfaceHolder.Callback, DroneListener {

    protected CustomDrone drone;

    protected class DroneMessage {
        String message;
        long timestamp;
    }

    protected ArrayBlockingQueue<DroneMessage> dms;
    protected String lastMessage;

    public FlightDataView(Context context) {
        super(context);
        getHolder().addCallback(this);
        dms = new ArrayBlockingQueue<DroneMessage>(255);
        addMessage("READY");
    }

    public FlightDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        dms = new ArrayBlockingQueue<DroneMessage>(255);
        addMessage("READY");
    }

    public void setDrone(CustomDrone drone) {
        if (this.drone != drone) {
            if (this.drone != null) {
                this.drone.unregisterDroneListener(this);
            }
            this.drone = drone;
            this.drone.registerDroneListener(this);
        }
    }

    protected int dp2px(int dps) {
        return (int) (dps * getResources().getDisplayMetrics().density);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    protected void addMessage(String message) {
        if (lastMessage != message) {
            if (dms.remainingCapacity() < 1) {
                dms.poll();
                if (dms.isEmpty()) lastMessage = null;
            }
            DroneMessage dm = new DroneMessage();
            dm.message = message;
            dm.timestamp = Calendar.getInstance().getTimeInMillis();
            dms.add(dm);
            lastMessage = message;
        }
    }

    protected String getMessage() {
        try {
            DroneMessage dm = dms.peek();
            if (dm != null) {
                if (Calendar.getInstance().getTimeInMillis() - dm.timestamp > 2000) {
                    dms.poll();
                    if (dms.isEmpty()) lastMessage = null;
                }
                return dm.message;
            }
        } catch (Exception e) {

        }
        return "";
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        addMessage(errorMsg);
        postInvalidate();
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        if (getVisibility() == VISIBLE) {
            switch (event) {
                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.STATE_DISCONNECTED:
                case AttributeEvent.STATE_VEHICLE_MODE:
                case AttributeEvent.STATE_ARMING:
                case AttributeEvent.STATE_UPDATED:
                case AttributeEvent.TYPE_UPDATED:
                case AttributeEvent.ALTITUDE_UPDATED:
                case AttributeEvent.SPEED_UPDATED:
                case AttributeEvent.BATTERY_UPDATED:
                case AttributeEvent.ATTITUDE_UPDATED:
                case AttributeEvent.GPS_FIX:
                case AttributeEvent.GPS_POSITION:
                case AttributeEvent.GPS_COUNT:
                    postInvalidate();
                    break;

                case AttributeEvent.WARNING_NO_GPS:
                    addMessage("WARNING: NO GPS");
                    postInvalidate();
                    break;

                case AttributeEvent.AUTOPILOT_ERROR:
                    String errid = drone.getAttribute(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    if (errid != null) {
                        addMessage("AUTOPILOT ERROR: " + errid);
                        postInvalidate();
                    }
                    break;

                case AttributeEvent.AUTOPILOT_MESSAGE:
                    String message = drone.getAttribute(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE);
                    addMessage(message);
                    postInvalidate();
                    break;

                default:
                    break;
            }
        }
    }

}

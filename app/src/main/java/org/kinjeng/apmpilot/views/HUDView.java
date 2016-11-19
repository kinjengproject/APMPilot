package org.kinjeng.apmpilot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;

import org.kinjeng.apmpilot.classes.CustomDrone;

import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by sblaksono on 29/10/2016.
 */

public class HUDView extends SurfaceView implements SurfaceHolder.Callback, DroneListener {

    protected CustomDrone drone;

    protected TextPaint textPaint;
    protected Paint linePaint;
    protected int textSize = 16;
    protected float hudAngle = 180;

    protected class DroneMessage {
        String message;
        long timestamp;
    }

    protected ArrayBlockingQueue<DroneMessage> dms;
    protected String lastMessage;

    public HUDView(Context context) {
        super(context);
        getHolder().addCallback(this);
        dms = new ArrayBlockingQueue<DroneMessage>(255);
        addMessage("READY");
    }

    public HUDView(Context context, AttributeSet attrs) {
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
    public void surfaceCreated(SurfaceHolder holder) {
        setWillNotDraw(false);
        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp2px(textSize));
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp2px(1));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    protected PointF rpTransform(float roll, float x, float y, float pitch, float h) {
        float ph = ((float) Math.sin(Math.toRadians(pitch)) * h);
        float a = (float) Math.atan2(y - ph, x);
        float r = (float) Math.sqrt((x * x) + ((y - ph) * (y - ph)));
        return new PointF(((float) Math.sin(Math.toRadians(-roll + 90) - a) * r),
                ((float) Math.cos(Math.toRadians(-roll + 90) - a) * -r));
    }

    protected void drawLabel1(Canvas canvas, float x, float y, String s) {
        float x1 = x + dp2px(8);
        float x2 = x + textPaint.measureText("000") + dp2px(16);
        float y1 = y - dp2px(16);
        float y2 = y + dp2px(16);
        canvas.drawLine(x, y, x1, y1, linePaint);
        canvas.drawLine(x1, y1, x2, y1, linePaint);
        canvas.drawLine(x2, y1, x2, y2, linePaint);
        canvas.drawLine(x2, y2, x1, y2, linePaint);
        canvas.drawLine(x1, y2, x, y, linePaint);
        canvas.drawText(s, x + dp2px(8), y + (dp2px(textSize) * 0.6f / 2), textPaint);
    }

    protected void drawLabel2(Canvas canvas, float x, float y, String s) {
        float x1 = x - (textPaint.measureText("000") / 2) - dp2px(8);
        float x2 = x + (textPaint.measureText("000") / 2) + dp2px(8);
        float y1 = y - dp2px(8);
        float y2 = y - dp2px(textSize + 20);
        canvas.drawLine(x, y, x1, y1, linePaint);
        canvas.drawLine(x1, y1, x1, y2, linePaint);
        canvas.drawLine(x1, y2, x2, y2, linePaint);
        canvas.drawLine(x2, y2, x2, y1, linePaint);
        canvas.drawLine(x2, y1, x, y, linePaint);
        canvas.drawText(s, x - (textPaint.measureText(s) / 2), y - 28, textPaint);
    }

    protected void drawHUD(Canvas canvas, float x, float y, float w, float h) {
        float cx = w / 2;
        float cy = h / 2;
        float r = (w / 4) < (h / 4) ? (w / 4) : (h / 4);
        float xx = 0;
        float yy = 0;
        String s = "";

        // target circle
        canvas.drawCircle(x + cx, y + cy, dp2px(8), linePaint);
        canvas.drawLine(x + cx - dp2px(16), y + cy, x + cx - dp2px(8), y + cy, linePaint);
        canvas.drawLine(x + cx, y + cy - dp2px(16), x + cx, y + cy - dp2px(8), linePaint);
        canvas.drawLine(x + cx + dp2px(8), y + cy, x + cx + dp2px(16), y + cy, linePaint);

        if (drone != null) {

            // draw attitude indicator
            Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);

            // yaw
            canvas.drawLine(x, y + dp2px(textSize + 24), x + w, y + dp2px(textSize + 24), linePaint);
            yy = y + dp2px(textSize + 8);
            float yaw = (float) attitude.getYaw();
            float a1 = yaw - (hudAngle / 2);
            float a2 = yaw + (hudAngle / 2);
            float a = ((int) (a1 / 15)) * 15;
            while (a < a1) a += 15;
            while (a <= a2) {
                int hh = (int) (a < 0 ? 360 + a : a);
                if (hh == 0) s = "N";
                else if (hh == 90) s = "E";
                else if (hh == 180) s = "S";
                else if (hh == 270) s = "W";
                else s = String.valueOf(hh);
                xx = x + (((a - a1) / (hudAngle / 2)) * (w / 2));
                canvas.drawText(s, xx - (int) (textPaint.measureText(s) / 2), yy, textPaint);
                canvas.drawLine(xx, y + dp2px(textSize + 16), xx, y + dp2px(textSize + 24), linePaint);
                a += 15;
            }

            yy += dp2px(textSize + 32);
            s = String.valueOf((int) (yaw < 0 ? 360 + yaw : yaw));
            xx = x + cx;
            canvas.drawLine(xx, y + dp2px(textSize + 24), xx, y + dp2px(textSize + 32), linePaint);
            canvas.drawText(s, xx - (textPaint.measureText(s) / 2), yy, textPaint);

            // draw messages
            yy += dp2px(textSize * 2);
            s = getMessage().toUpperCase();
            canvas.drawText(s, x + cx - (textPaint.measureText(s) / 2), yy, textPaint);

            // draw roll and pitch
            float roll = (float) attitude.getRoll();
            float pitch = (float) attitude.getPitch();

            for (float pd = -90; pd <= 90; pd += 10) {
                float r1 = r - dp2px(32);
                float r2 = pd == 0 ? r + dp2px(64) : r + dp2px(16);
                PointF pt1 = rpTransform(roll, r1, 0, pitch + pd, h);
                PointF pt2 = rpTransform(roll, r2, 0, pitch + pd, h);
                if ((pt1.y >= -cy * 0.75f) && (pt1.y <= cy * 0.75f)) {
                    canvas.drawLine(pt1.x + cx, pt1.y + cy, pt2.x + cx, pt2.y + cy, linePaint);
                    if (pd != 0) {
                        canvas.drawText(String.format("%3.0f", -pd), pt2.x + cx + dp2px(8), pt2.y + cy + dp2px(4), textPaint);
                    }
                }
                pt1 = rpTransform(roll, -r1, 0, pitch + pd, h);
                pt2 = rpTransform(roll, -r2, 0, pitch + pd, h);
                if ((pt1.y >= -cy * 0.75f) && (pt1.y <= cy * 0.75f)) {
                    canvas.drawLine(pt1.x + cx, pt1.y + cy, pt2.x + cx, pt2.y + cy, linePaint);
                }
            }
            drawLabel1(canvas, cx + r + dp2px(64), cy, String.format("%3.0f", pitch));

            // roll
            s = String.valueOf((int) attitude.getRoll());
            drawLabel2(canvas, x + cx, y + h - dp2px(textSize + 48), s);

            // draw vehicle mode
            xx = x + dp2px(8);
            yy = y + dp2px((textSize * 3) + 24);
            State vehicleState = drone.getAttribute(AttributeType.STATE);
            canvas.drawText(vehicleState.getVehicleMode().getLabel(), xx, yy, textPaint);

            // draw altitude
            yy += dp2px(textSize * 2);
            Altitude altitude = drone.getAttribute(AttributeType.ALTITUDE);
            canvas.drawText("ALT: " + String.format("%3.1f", altitude.getAltitude()) + " m", xx, yy, textPaint);

            // draw speed
            yy += dp2px(textSize);
            Speed speed = drone.getAttribute(AttributeType.SPEED);
            canvas.drawText("SPD: " + String.format("%3.1f", speed.getGroundSpeed() * 3600 / 1000) + " km/h", xx, yy, textPaint);

            // draw battery status
            yy += dp2px(textSize * 2);
            Battery battery = drone.getAttribute(AttributeType.BATTERY);
            canvas.drawText("BAT: " + String.format("%3.0f", battery.getBatteryRemain()) + "%", xx, yy, textPaint);
            yy += dp2px(textSize);
            canvas.drawText(String.format("%3.1f", battery.getBatteryVoltage()) + " V / " + String.format("%3.1f", battery.getBatteryCurrent()) + " A", xx, yy, textPaint);

            // draw gps info
            yy += dp2px(textSize * 2);
            Gps gps = drone.getAttribute(AttributeType.GPS);
            canvas.drawText("GPS: " + gps.getFixStatus().toUpperCase(), xx, yy, textPaint);
            yy += dp2px(textSize);
            canvas.drawText("SAT: " + gps.getSatellitesCount(), xx, yy, textPaint);
            yy += dp2px(textSize);
            LatLong latLong = gps.getPosition();
            if (latLong != null) {
                canvas.drawText(String.format("%3.3f", latLong.getLatitude()) + " " + String.format("%3.3f", latLong.getLongitude()), xx, yy, textPaint);
            }

            // draw drone status
            s = "";
            if (drone.isConnected()) {
                if (vehicleState.isArmed()) {
                    s = "Armed";
                } else {
                    s = "Disarmed";
                }
            } else {
                s = "Disconnected";
            }
            xx = x + w - (int) (textPaint.measureText(s) + dp2px(8));
            yy = y + dp2px((textSize * 3) + 24);
            canvas.drawText(s, xx, yy, textPaint);

            // draw TRPY
            xx = x + w - (int) (textPaint.measureText("X: 0.00") + dp2px(8));
            yy += dp2px(textSize * 2);
            canvas.drawText("T: " + String.format("%3.2f", drone.getThrottle()), xx, yy, textPaint);
            yy += dp2px(textSize);
            canvas.drawText("R: " + String.format("%3.2f", drone.getRoll()), xx, yy, textPaint);
            yy += dp2px(textSize);
            canvas.drawText("P: " + String.format("%3.2f", drone.getPitch()), xx, yy, textPaint);
            yy += dp2px(textSize);
            canvas.drawText("Y: " + String.format("%3.2f", drone.getYaw()), xx, yy, textPaint);

        }

    }

    @Override
    protected void onDraw(Canvas canvas){
        drawHUD(canvas, 0, 0, canvas.getWidth(), canvas.getHeight());
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

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        addMessage(errorMsg);
        postInvalidate();
    }

}

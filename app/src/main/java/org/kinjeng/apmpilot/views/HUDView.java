package org.kinjeng.apmpilot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;

import org.kinjeng.apmpilot.classes.CustomDrone;

/**
 * Created by sblaksono on 29/10/2016.
 */

public class HUDView extends SurfaceView implements SurfaceHolder.Callback {

    protected CustomDrone drone;

    protected TextPaint textPaint;
    protected Paint linePaint;
    protected int dpTextSize = 16;
    protected float hudAngleH = 90;

    public HUDView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public HUDView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void setDrone(CustomDrone drone) {
        this.drone = drone;
    }

    protected int dp2px(int dps) {
        return (int) (dps * getResources().getDisplayMetrics().density);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setWillNotDraw(false);
        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp2px(dpTextSize));
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
        canvas.drawText(s, x + dp2px(8), y + (dp2px(dpTextSize) * 0.6f / 2), textPaint);
    }

    protected void drawPichBar1() {

    }

    @Override
    protected void onDraw(Canvas canvas){
        String s = "";
        float x = 0;
        float y = 0;
        float cw = canvas.getWidth();
        float ch = canvas.getHeight();
        float a = canvas.getWidth() / 4;
        float cx = cw / 2;
        float b = canvas.getHeight() / 4;
        float cy = ch / 2;
        float r = a < b ? a : b;
        canvas.drawLine(a, dp2px(dpTextSize + 24), a * 3, dp2px(dpTextSize + 24), linePaint);
        canvas.drawCircle(cx, cy, dp2px(8), linePaint);
        canvas.drawLine(cx - dp2px(16), cy, cx - dp2px(8), cy, linePaint);
        canvas.drawLine(cx, cy - dp2px(16), cx, cy - dp2px(8), linePaint);
        canvas.drawLine(cx + dp2px(8), cy, cx + dp2px(16), cy, linePaint);

        if (drone != null) {

            // draw attitude indicator
            Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);

            // yaw
            y = dp2px(dpTextSize + 8);
            float yaw = (float) attitude.getYaw();
            float h1 = yaw - (hudAngleH / 4);
            float h2 = yaw + (hudAngleH / 4);
            float h = ((int) (h1 / 15)) * 15;
            while (h < h1) h += 15;
            while (h <= h2) {
                int hh = (int) (h < 0 ? 360 + h : h);
                if (hh == 0) s = "N";
                else if (hh == 90) s = "E";
                else if (hh == 180) s = "S";
                else if (hh == 270) s = "W";
                else s = String.valueOf(hh);
                x = a + (((h - h1) / (hudAngleH / 4)) * a);
                canvas.drawText(s, x - (int) (textPaint.measureText(s) / 2), y, textPaint);
                canvas.drawLine(x, dp2px(dpTextSize + 16), x, dp2px(dpTextSize + 24), linePaint);
                h += 15;
            }

            y += dp2px(dpTextSize + 32);
            s = String.valueOf((int) (yaw < 0 ? 360 + yaw : yaw));
            x = cx;
            canvas.drawLine(x, dp2px(dpTextSize + 24), x, dp2px(dpTextSize + 32), linePaint);
            canvas.drawText(s, x - (textPaint.measureText(s) / 2), y, textPaint);

            // draw roll and pitch
            float roll = (float) attitude.getRoll();
            float pitch = (float) attitude.getPitch();

            for (float pd = -90; pd <= 90; pd += 10) {
                float r1 = b - dp2px(32);
                float r2 = pd == 0 ? b + dp2px(64) : b + dp2px(16);
                PointF pt1 = rpTransform(roll, r1, 0, pitch + pd, ch);
                PointF pt2 = rpTransform(roll, r2, 0, pitch + pd, ch);
                if ((pt1.y >= -cy * 0.75f) && (pt1.y <= cy * 0.75f)) {
                    canvas.drawLine(pt1.x + cx, pt1.y + cy, pt2.x + cx, pt2.y + cy, linePaint);
                    if (pd != 0) {
                        canvas.drawText(String.format("%3.0f", -pd), pt2.x + cx + dp2px(8), pt2.y + cy + dp2px(4), textPaint);
                    }
                }
                pt1 = rpTransform(roll, -r1, 0, pitch + pd, ch);
                pt2 = rpTransform(roll, -r2, 0, pitch + pd, ch);
                if ((pt1.y >= -cy * 0.75f) && (pt1.y <= cy * 0.75f)) {
                    canvas.drawLine(pt1.x + cx, pt1.y + cy, pt2.x + cx, pt2.y + cy, linePaint);
                }
            }
            drawLabel1(canvas, cx + b + dp2px(64), cy, String.format("%3.0f", pitch));

            // roll
            s = String.valueOf((int) attitude.getRoll());
            x = (a * 2) - (textPaint.measureText(s) / 2);
            y = (b * 2) + r + dp2px(dpTextSize + 8);
            canvas.drawText(s, x, y, textPaint);

            // draw vehicle mode
            x = dp2px(8);
            y = dp2px(dpTextSize + 8);
            State vehicleState = drone.getAttribute(AttributeType.STATE);
            canvas.drawText(vehicleState.getVehicleMode().getLabel(), x, y, textPaint);

            // draw altitude
            y += dp2px(dpTextSize * 2);
            Altitude altitude = drone.getAttribute(AttributeType.ALTITUDE);
            canvas.drawText("Alt: " + String.format("%3.1f", altitude.getAltitude()) + " m", x, y, textPaint);

            // draw speed
            y += dp2px(dpTextSize);
            Speed speed = drone.getAttribute(AttributeType.SPEED);
            canvas.drawText("Spd: " + String.format("%3.1f", speed.getGroundSpeed() * 3600 / 1000) + " km/h", x, y, textPaint);

            // drat battery status
            y += dp2px(dpTextSize * 2);
            Battery battery = drone.getAttribute(AttributeType.BATTERY);
            canvas.drawText("Bat: " + String.format("%3.0f", battery.getBatteryRemain()) + "%", x, y, textPaint);
            y += dp2px(dpTextSize);
            canvas.drawText(String.format("%3.1f", battery.getBatteryVoltage()) + " V / " + String.format("%3.1f", battery.getBatteryCurrent()) + " A", x, y, textPaint);

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
            x = canvas.getWidth() - (int) (textPaint.measureText(s) + dp2px(8));
            y = dp2px(dpTextSize + 8);
            canvas.drawText(s, x, y, textPaint);

            // draw TRPY
            x = canvas.getWidth() - (int) (textPaint.measureText("X: 0.00") + dp2px(8));
            y += dp2px(dpTextSize * 2);
            canvas.drawText("T: " + String.format("%3.2f", drone.getThrottle()), x, y, textPaint);
            y += dp2px(dpTextSize);
            canvas.drawText("R: " + String.format("%3.2f", drone.getRoll()), x, y, textPaint);
            y += dp2px(dpTextSize);
            canvas.drawText("P: " + String.format("%3.2f", drone.getPitch()), x, y, textPaint);
            y += dp2px(dpTextSize);
            canvas.drawText("Y: " + String.format("%3.2f", drone.getYaw()), x, y, textPaint);
        }
    }

}

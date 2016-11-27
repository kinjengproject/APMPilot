package org.kinjeng.apmpilot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;

import org.kinjeng.apmpilot.R;

/**
 * Created by sblaksono on 21/11/2016.
 */

public class MapOverlayView extends FlightDataView {

    protected TextPaint textPaintB;
    protected TextPaint textPaintW;
    protected Paint linePaintB;
    protected Paint linePaintW;
    protected int textSize = 16;

    public MapOverlayView(Context context) {
        super(context);
    }

    public MapOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setWillNotDraw(false);
        setZOrderOnTop(true);
        SurfaceHolder mapOverlayHolder = getHolder();
        mapOverlayHolder.setFormat(PixelFormat.TRANSPARENT);
        textPaintB = new TextPaint();
        textPaintB.setColor(Color.BLACK);
        textPaintB.setTextSize(dp2px(textSize));
        textPaintW = new TextPaint();
        textPaintW.setColor(Color.WHITE);
        textPaintW.setTextSize(dp2px(textSize));
        linePaintB = new Paint();
        linePaintB.setColor(Color.BLACK);
        linePaintB.setStyle(Paint.Style.STROKE);
        linePaintB.setStrokeWidth(dp2px(1));
        linePaintW = new Paint();
        linePaintW.setColor(Color.WHITE);
        linePaintW.setStyle(Paint.Style.STROKE);
        linePaintW.setStrokeWidth(dp2px(1));
    }

    protected void drawHUD(Canvas canvas, float x, float y, float w, float h, String msg, TextPaint textPaint) {
        float cx = w / 2;
        float xx = 0;
        float yy = 0;
        String s = "";

        if (drone != null) {

            // draw attitude indicator
            Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);

            // draw messages
            yy = y + dp2px(textSize + 8);
            canvas.drawText(msg, x + cx - (textPaint.measureText(msg) / 2), yy, textPaint);

            // draw vehicle mode
            xx = x + dp2px(8);
            yy = y + dp2px(textSize + 8);
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
            yy = y + dp2px(textSize + 8);
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
    protected void onDraw(Canvas canvas) {
        int cx = (canvas.getWidth() / 2);
        int cy = (canvas.getHeight() / 2);

        if (drone != null) {

            Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
            float yaw = (float) attitude.getYaw();

            // draw arrow
            canvas.save();
            canvas.rotate(yaw, cx, cy);
            Drawable d = getResources().getDrawable(R.drawable.arrow_rw_32);
            d.setBounds(cx - dp2px(16), cy - dp2px(16), cx + dp2px(16), cy + dp2px(16));
            d.draw(canvas);
            canvas.restore();

            String msg = getMessage().toUpperCase();
            drawHUD(canvas, dp2px(1), dp2px(1), canvas.getWidth(), canvas.getHeight(), msg, textPaintB);
            drawHUD(canvas, 0, 0, canvas.getWidth(), canvas.getHeight(), msg, textPaintW);

        }
    }
}

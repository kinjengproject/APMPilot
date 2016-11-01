package org.kinjeng.apmpilot.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.o3dr.android.client.apis.CameraApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.kinjeng.apmpilot.R;
import org.kinjeng.apmpilot.classes.CustomDrone;

/**
 * Created by sblaksono on 29/10/2016.
 */

public class HUDView extends SurfaceView implements SurfaceHolder.Callback {

    protected CustomDrone drone;

    protected TextPaint textPaint;
    protected int dpTextSize = 16;
    protected int dpMargin = 8;
    protected float hudAngleH = 90;

    protected String videoTag = "VIDEO";

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
        textPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onDraw(Canvas canvas){
        String s = "";
        float x = 0;
        float y = 0;
        float a = canvas.getWidth() / 4;
        float b = canvas.getHeight() / 4;
        float r = a < b ? a : b;
        canvas.drawLine(a, dp2px(dpMargin + dpTextSize + dpMargin), a * 3, dp2px(dpMargin + dpTextSize + dpMargin), textPaint);
        canvas.drawCircle(a * 2, b * 2, r, textPaint);

        float hudAngleV = hudAngleH * b / a;

        if (drone != null) {
            Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
            y = dp2px(dpMargin + dpTextSize);
            float yaw = (float) attitude.getYaw();
            float h1 = yaw - (hudAngleH / 4);
            float h2 = yaw + (hudAngleH / 4);
            float h = ((int) (h1 / 15)) * 15;
            while (h < h1) h += 15;
            while (h <= h2) {
                s = String.valueOf((int) (h < 0 ? 360 + h : h));
                x = a + (((h - h1) / (hudAngleH / 4)) * a) - (int) (textPaint.measureText(s) / 2);
                canvas.drawText(s, x, y, textPaint);
                h += 15;
            }

            y += dp2px(dpTextSize * 2);
            s = String.valueOf((int) (yaw < 0 ? 360 + yaw : yaw));
            x = (a * 2) - (textPaint.measureText(s) / 2);
            canvas.drawText(s, x, y, textPaint);


            s = String.valueOf((int) attitude.getRoll());
            x = (a * 2) - (textPaint.measureText(s) / 2);
            y = (b * 2) + r + dp2px(dpMargin + dpTextSize);
            canvas.drawText(s, x, y, textPaint);

            s = String.valueOf((int) attitude.getPitch());
            x = (a * 2) + r + dp2px(dpMargin);
            y = (b * 2) + (dp2px(dpTextSize) / 2);
            canvas.drawText(s, x, y, textPaint);

            x = dp2px(dpMargin);
            y = dp2px(dpMargin + dpTextSize);
            State vehicleState = drone.getAttribute(AttributeType.STATE);
            canvas.drawText(vehicleState.getVehicleMode().getLabel(), x, y, textPaint);

            y += dp2px(dpTextSize * 2);
            Altitude altitude = drone.getAttribute(AttributeType.ALTITUDE);
            canvas.drawText("Alt: " + String.format("%3.1f", altitude.getAltitude()) + " m", x, y, textPaint);

            y += dp2px(dpTextSize);
            Speed speed = drone.getAttribute(AttributeType.SPEED);
            canvas.drawText("Speed: " + String.format("%3.1f", speed.getGroundSpeed() * 3600 / 1000) + " km/h", x, y, textPaint);

            y += dp2px(dpTextSize * 2);
            Battery battery = drone.getAttribute(AttributeType.BATTERY);
            canvas.drawText("Bat Vol: " + String.format("%3.1f", battery.getBatteryVoltage()) + " V", x, y, textPaint);
            y += dp2px(dpTextSize);
            canvas.drawText("Bat Cur: " + String.format("%3.1f", battery.getBatteryCurrent()) + " A", x, y, textPaint);

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
            x = canvas.getWidth() - (int) (textPaint.measureText(s) + dp2px(dpMargin));
            y = dp2px(dpMargin + dpTextSize);
            canvas.drawText(s, x, y, textPaint);

            x = canvas.getWidth() - (int) (textPaint.measureText("X: 0.00") + dp2px(dpMargin));
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

    public void startVideo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        try {
            int prefUDPVideoPort = Integer.parseInt(preferences.getString("pref_udp_video_port", ""));
            Bundle bundle = new Bundle();
            bundle.putInt(CameraApi.VIDEO_PROPS_UDP_PORT, prefUDPVideoPort);
            CameraApi.getApi(drone).startVideoStream(getHolder().getSurface(), videoTag, bundle,
                    new SimpleCommandListener());
        }
        catch (Exception e) {

        }
    }

    public void stopVideo() {
        CameraApi.getApi(drone).stopVideoStream(videoTag, new SimpleCommandListener());
    }

}

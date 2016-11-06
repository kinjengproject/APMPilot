package org.kinjeng.apmpilot.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import org.kinjeng.apmpilot.R;
import org.kinjeng.apmpilot.classes.BaseJoystick;
import org.kinjeng.apmpilot.classes.CustomDrone;
import org.kinjeng.apmpilot.classes.RCOverrideJoystick;
import org.kinjeng.apmpilot.views.HUDView;
import org.kinjeng.apmpilot.views.VideoView;

public class MainActivity extends Activity implements TowerListener, DroneListener {

    protected BaseJoystick joystick;
    protected ControlTower controlTower;
    protected CustomDrone drone;
    protected final Handler handler = new Handler();

    protected PowerManager.WakeLock mWakeLock;
    protected ImageButton preferenceButton;
    protected ImageButton connectButton;
    protected ImageButton rtlButton;
    protected ImageButton landButton;
    protected HUDView hudView;
    protected VideoView videoView;

    // Thread for controlling input and hud
    protected class ControlThread extends Thread {
        private boolean running;

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            while (running) {
                joystick.processJoystickHat1(drone);
                joystick.processJoystickInput1(drone);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected ControlThread controlThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MainActivity mainActivity = this;

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "APMPilot");
        mWakeLock.acquire();

        preferenceButton = (ImageButton) findViewById(R.id.button_preference);
        preferenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mainActivity, SettingsActivity.class));
            }
        });

        connectButton = (ImageButton) findViewById(R.id.button_connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drone.isConnected()) {
                    drone.disconnect();
                }
                else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String prefProtocol = preferences.getString("pref_protocol", "");
                    if (prefProtocol.equals("UDP")) {
                        int udpPort = ConnectionType.DEFAULT_UDP_SERVER_PORT;
                        try {
                            udpPort = Integer.parseInt(preferences.getString("pref_udp_port", ""));
                        }
                        catch (Exception e) {
                        }
                        ConnectionParameter connectionParams = ConnectionParameter.newUdpConnection(udpPort, null);
                        drone.connect(connectionParams);
                    }
                    else if (prefProtocol.equals("TCP")) {
                        String tcpHost = preferences.getString("pref_tcp_host", "");
                        int tcpPort = ConnectionType.DEFAULT_TCP_SERVER_PORT;
                        try {
                            tcpPort = Integer.parseInt(preferences.getString("pref_tcp_port", ""));
                        }
                        catch (Exception e) {
                        }
                        ConnectionParameter connectionParams = ConnectionParameter.newTcpConnection(tcpHost, tcpPort, null);
                        drone.connect(connectionParams);
                    }
                    else if (prefProtocol.equals("USB")) {
                        int usbBaudRate = ConnectionType.DEFAULT_USB_BAUD_RATE;
                        try {
                            usbBaudRate = Integer.parseInt(preferences.getString("pref_usb_baud_rate", ""));
                        }
                        catch (Exception e) {
                        }
                        ConnectionParameter connectionParams = ConnectionParameter.newUsbConnection(usbBaudRate, null);
                        drone.connect(connectionParams);
                    }
                }
            }
        });

        controlTower = new ControlTower(getApplicationContext());
        drone = createDrone();
        createHUD();

        rtlButton = (ImageButton) findViewById(R.id.button_rtl);
        rtlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drone.setVehicleMode(VehicleMode.COPTER_RTL.getMode());
            }
        });

        landButton = (ImageButton) findViewById(R.id.button_land);
        landButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drone.setVehicleMode(VehicleMode.COPTER_LAND.getMode());
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int prefManualMode = 1;
        try {
            prefManualMode = Integer.parseInt(preferences.getString("pref_manual_mode", "1"));
        }
        catch (Exception e) {
        }
        if (prefManualMode == 1) {
            joystick = new RCOverrideJoystick(getApplicationContext());
        }
        else {

        }

        updateConnectionState();
        updateHUD();

        controlThread = new ControlThread();
        controlThread.setRunning(true);
        controlThread.start();
    }

    protected CustomDrone createDrone() {
        return new CustomDrone(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        controlThread.setRunning(false);
        try {
            controlThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        if (joystick.processMotionEvent(drone, ev, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()))) return true;
        updateHUD();
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (joystick.processKeyEvent(drone, event, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()))) return true;
        updateHUD();
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        controlTower.connect(this);
    }

    @Override
    protected void onStop() {
        if (drone.isConnected()) {
            drone.disconnect();
        }
        controlTower.unregisterDrone(drone);
        controlTower.disconnect();
        super.onStop();
    }

    @Override
    public void onTowerConnected() {
        controlTower.registerDrone(drone, handler);
        drone.registerDroneListener(this);
    }

    @Override
    public void onTowerDisconnected() {

    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
            case AttributeEvent.STATE_DISCONNECTED:
                updateConnectionState();
                updateHUD();
            case AttributeEvent.STATE_VEHICLE_MODE:
            case AttributeEvent.STATE_ARMING:
            case AttributeEvent.STATE_UPDATED:
                updateHUD();
                break;

            case AttributeEvent.TYPE_UPDATED:
            case AttributeEvent.ALTITUDE_UPDATED:
            case AttributeEvent.SPEED_UPDATED:
            case AttributeEvent.BATTERY_UPDATED:
                updateHUD();
                break;

            case AttributeEvent.ATTITUDE_UPDATED:
                updateHUD();
                break;

            case AttributeEvent.GPS_FIX:
            case AttributeEvent.GPS_POSITION:
            case AttributeEvent.GPS_COUNT:
            case AttributeEvent.WARNING_NO_GPS:
                updateHUD();
                break;

            case AttributeEvent.AUTOPILOT_ERROR:
            case AttributeEvent.AUTOPILOT_MESSAGE:
                updateHUD();
                break;

            default:
                break;
        }
    }

    protected void createHUD() {
        videoView = (VideoView) findViewById(R.id.view_video);
        videoView.setDrone(drone);
        hudView = (HUDView) findViewById(R.id.view_hud);
        hudView.setDrone(drone);
    }

    protected void updateHUD() {
        hudView.postInvalidate();
    }

    protected void updateConnectionState() {
        connectButton.setVisibility(View.VISIBLE);
        if (drone.isConnected()) {
            preferenceButton.setVisibility(View.INVISIBLE);
            drone.startGimbalControl();
            videoView.startVideo();
        } else {
            videoView.stopVideo();
            drone.stopGimbalControl();
            preferenceButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

}

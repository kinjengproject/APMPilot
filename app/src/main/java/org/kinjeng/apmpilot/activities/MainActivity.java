package org.kinjeng.apmpilot.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import org.kinjeng.apmpilot.R;
import org.kinjeng.apmpilot.classes.BaseJoystick;
import org.kinjeng.apmpilot.classes.CustomDrone;
import org.kinjeng.apmpilot.classes.CustomTower;
import org.kinjeng.apmpilot.classes.PhysicalJoystick;
import org.kinjeng.apmpilot.classes.Settings;
import org.kinjeng.apmpilot.views.HUDView;
import org.kinjeng.apmpilot.views.MapOverlayView;
import org.kinjeng.apmpilot.views.VideoView;

public class MainActivity extends Activity implements TowerListener, DroneListener, OnMapReadyCallback {

    protected BaseJoystick joystick;
    protected CustomTower tower;
    protected CustomDrone drone;
    protected GoogleMap map;

    protected PowerManager.WakeLock mWakeLock;
    protected ImageButton preferenceButton;
    protected ImageButton connectButton;
    protected ImageButton rtlButton;
    protected ImageButton landButton;
    protected HUDView hudView;
    protected VideoView videoView;
    protected MapFragment mapFragment;
    protected MapOverlayView mapOverlayView;
    protected View videoContainer;
    protected View mapContainer;

    // Thread for controlling input and hud
    protected class ControlThread extends Thread {
        private boolean running;

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            while (running) {
                joystick.processJoystickHat1();
                joystick.processJoystickInput1();
                tower.updateGimbal();
                updateDisplay();
                try {
                    Thread.sleep(30);
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
        Settings.init(getApplicationContext());
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
                } else {
                    drone.connect();
                }
            }
        });

        tower = new CustomTower(getApplicationContext());
        drone = createDrone();
        createDisplay(savedInstanceState);

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

        joystick = new PhysicalJoystick(tower, drone);

        updateConnectionState();
        updateDisplay();

        controlThread = new ControlThread();
        controlThread.setRunning(true);
        controlThread.start();
    }

    protected CustomDrone createDrone() {
        return new CustomDrone(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controlThread.setRunning(false);
        try {
            controlThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWakeLock.release();
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
        if (joystick.processMotionEvent(ev)) {
            updateDisplay();
            return true;
        }
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (joystick.processKeyEvent(event)) {
            updateDisplay();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        tower.connect(this);
    }

    @Override
    protected void onStop() {
        if (drone.isConnected()) {
            drone.disconnect();
        }
        tower.unregisterDrone();
        tower.disconnect();
        super.onStop();
    }

    @Override
    public void onTowerConnected() {
        tower.registerDrone(drone);
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
                updateDisplay();
                break;

            case AttributeEvent.STATE_VEHICLE_MODE:
            case AttributeEvent.STATE_ARMING:
            case AttributeEvent.STATE_UPDATED:
                updateDisplay();
                break;

            case AttributeEvent.TYPE_UPDATED:
            case AttributeEvent.ALTITUDE_UPDATED:
            case AttributeEvent.SPEED_UPDATED:
            case AttributeEvent.BATTERY_UPDATED:
                updateDisplay();
                break;

            case AttributeEvent.ATTITUDE_UPDATED:
                updateDisplay();
                break;

            case AttributeEvent.GPS_FIX:
            case AttributeEvent.GPS_POSITION:
            case AttributeEvent.GPS_COUNT:
            case AttributeEvent.WARNING_NO_GPS:
                updateDisplay();
                break;

            case AttributeEvent.AUTOPILOT_ERROR:
            case AttributeEvent.AUTOPILOT_MESSAGE:
                updateDisplay();
                break;

            default:
                break;
        }
    }

    protected void createDisplay(Bundle savedInstanceState) {
        videoView = (VideoView) findViewById(R.id.view_video);
        videoView.setDrone(drone);
        hudView = (HUDView) findViewById(R.id.view_hud);
        hudView.setDrone(drone);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_flight_map);
        mapFragment.getMapAsync(this);
        //mapOverlayView = (MapOverlayView) findViewById(R.id.view_map_overlay);
        //mapOverlayView.setDrone(drone);

        videoContainer = findViewById(R.id.container_video);
        mapContainer = findViewById(R.id.container_map);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-6.175387, 106.827131), Settings.getInt("pref_map_zoom", 18)));
    }

    protected void updateDisplay() {
        if (Settings.getInt("pref_display_mode", 1) == 2) {
            // 2 : Video
            if (videoContainer.getVisibility() != View.VISIBLE) {
                videoContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        videoContainer.setVisibility(View.VISIBLE);
                    }
                });
            }
            if (mapContainer.getVisibility() != View.INVISIBLE) {
                mapContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        mapContainer.setVisibility(View.INVISIBLE);
                    }
                });
            }
            hudView.postInvalidate();
        }
        else {
            // 1 or default : Google Map
            if (mapContainer.getVisibility() != View.VISIBLE) {
                mapContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        mapContainer.setVisibility(View.VISIBLE);
                    }
                });
            }
            if (videoContainer.getVisibility() != View.INVISIBLE) {
                videoContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        videoView.stopVideo();
                        videoContainer.setVisibility(View.INVISIBLE);
                    }
                });
            }
            mapContainer.post(new Runnable() {
                @Override
                public void run() {
                    Gps gps = drone.getAttribute(AttributeType.GPS);
                    LatLong latLong = gps.getPosition();
                    if (latLong != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latLong.getLatitude(), latLong.getLongitude())));
                    }
                }
            });
        }
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

    @Override
    protected void onResume() {
        tower.startMotionSensor();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        tower.stopMotionSensor();
    }

}

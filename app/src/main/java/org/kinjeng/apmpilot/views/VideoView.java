package org.kinjeng.apmpilot.views;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.o3dr.android.client.apis.CameraApi;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.kinjeng.apmpilot.classes.CustomDrone;
import org.kinjeng.apmpilot.classes.Settings;

/**
 * Created by sblaksono on 05/11/2016.
 */

public class VideoView extends SurfaceView {

    protected CustomDrone drone;

    protected String videoTag = "VIDEO";

    public VideoView(Context context) {
        super(context);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDrone(CustomDrone drone) {
        this.drone = drone;
    }

    public void startVideo() {
        try {
            int prefUDPVideoPort = Settings.getInt("pref_udp_video_port", 0);
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

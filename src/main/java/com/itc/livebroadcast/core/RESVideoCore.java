package com.itc.livebroadcast.core;
import android.graphics.SurfaceTexture;

import com.itc.livebroadcast.core.listener.RESScreenShotListener;
import com.itc.livebroadcast.core.listener.RESVideoChangeListener;
import com.itc.livebroadcast.encoder.IMediaMuxerListener;
import com.itc.livebroadcast.encoder.MediaVideoEncoder;
import com.itc.livebroadcast.model.RESConfig;
import com.itc.livebroadcast.model.RESCoreParameters;
import com.itc.livebroadcast.rtmp.RESFlvDataCollecter;

public interface RESVideoCore {
    int OVERWATCH_TEXTURE_ID = 10;
    boolean prepare(RESConfig resConfig);

    void updateCamTexture(SurfaceTexture camTex);

    void startPreview(SurfaceTexture surfaceTexture, int visualWidth, int visualHeight);

    void updatePreview(int visualWidth, int visualHeight);

    void stopPreview(boolean releaseTexture);

    boolean startStreaming(RESFlvDataCollecter flvDataCollecter, IMediaMuxerListener mediaMuxerListener);

    boolean stopStreaming();

    boolean destroy();

    void reSetVideoBitrate(int bitrate);

    int getVideoBitrate();

    void reSetVideoFPS(int fps);

    void reSetVideoSize(RESCoreParameters newParameters);

    //设置前后摄像头方向
    void setCurrentCamera(int cameraIndex);

    //获取前后摄像头方向
    int getCurrentCamera();

    void takeScreenShot(RESScreenShotListener listener);

    void setVideoChangeListener(RESVideoChangeListener listener);

    float getDrawFrameRate();

    void setVideoEncoder(final MediaVideoEncoder encoder);

    void setMirror(boolean isEnableMirror, boolean isEnablePreviewMirror, boolean isEnableStreamMirror);
    void setNeedResetEglContext(boolean bol);


}

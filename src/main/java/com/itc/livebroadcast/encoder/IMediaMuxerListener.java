package com.itc.livebroadcast.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * 混合mp4回调接口
 */
public interface IMediaMuxerListener {

    /**
     * 开始混合编码
     */
    boolean start();

    /**
     * 停止混合编码
     */
    void stop();

    /**
     * 添加MediaFormat
     */
    int addTrack(MediaFormat format);

    /**
     * 是否已经开始混合编码了
     */
    boolean isStarted();

    /**
     * 写入混合编码器
     */
    void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo);

    /**
     * 获取保存至本地混合视频的地址
     */
    String getSaveVideoPath(String outputPath, String afterStamp);

}

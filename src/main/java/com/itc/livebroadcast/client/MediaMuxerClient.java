package com.itc.livebroadcast.client;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.itc.livebroadcast.encoder.IMediaMuxerListener;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * * @Author xiaosq
 * 2020/10/30
 * h264和aac混合成mp4工具类
 */
public class MediaMuxerClient implements IMediaMuxerListener {

    //默认存放保存视频的目录
    private static final String DIR_NAME = "WSLive";

    private MediaMuxer mMuxer;
    //定义混合编码的视频和音频两个数量， 第二个是调用开始函数start()的数量
    private int mEncoderCount, mStartedCount;
    //是否已经开始合成
    private boolean isStarted;
    //是否有视频编码
    private boolean hasVideoEncoder;
    //是否有视频编码
    private boolean hasAudioEncoder;

    public MediaMuxerClient(String outputDir, String afterStamp) {
        try {
            //输出视频的绝对路径
            String tempPath = getSaveVideoPath(outputDir, afterStamp);
            Log.i("jfiejsoutpestess", "=====" + tempPath);
            mMuxer = new MediaMuxer(tempPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (final NullPointerException e) {
            throw new RuntimeException("This app has no permission of writing external storage");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        isStarted = false;
        mEncoderCount = mStartedCount = 0;
    }

    /**
     * 开始混合编码
     */
    @Override
    public synchronized boolean start() {
        mStartedCount++;
        Log.i("MediaMuxerClient","=====start====mStartedCount: "+mStartedCount+"===mEncoderCount: "+mEncoderCount);
        if ((mEncoderCount > 0) && (mStartedCount == mEncoderCount)) {
            mMuxer.start();
            isStarted = true;
            notifyAll();
            Log.i("MediaMuxerClient","==MediaMuxer===start:");
        }
        return isStarted;
    }

    /**
     * 添加音频或者视频的MediaFormat
     */
    @Override
    public synchronized int addTrack(MediaFormat format) {
        //如果MediaMuxer开始编码则不能执行addTrack方法
        if (isStarted)
            throw new IllegalStateException("muxer already started");
        final int trackIx = mMuxer.addTrack(format);
        Log.i("MediaMuxerClient", "addTrack=====mStartedCount"+mStartedCount+"======mEncoderCount="
                + mEncoderCount + ",trackIx=" + trackIx + ",format=" + format);
        return trackIx;
    }

    /**
     * 写入混合编码器
     */
    @Override
    public synchronized void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        if (mStartedCount > 0 && isStarted)
            mMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }

    /**
     * 停止混合编码
     */
    @Override
    public synchronized void stop() {
        mStartedCount--;
        Log.i("MediaMuxerClient","=====stop====mStartedCount: "+mStartedCount+"===mEncoderCount: "+mEncoderCount);
        if ((mEncoderCount > 0) && (mStartedCount <= 0) && isStarted()) {
            mMuxer.stop();
            mMuxer.release();
            isStarted = false;
            Log.i("MediaMuxerClient","==MediaMuxer===stop:");
        }
    }

    /**
     * 获取文件路径
     * @param outputDir 视频存放目录
     * @param afterStamp 视频文件的后缀如：.mp4
     */
    @Override
    public String getSaveVideoPath(String outputDir, String afterStamp) {
        File dir;
        if (TextUtils.isEmpty(outputDir)) {
            //固定目录WSLive
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), DIR_NAME);
        } else {
            //自定义目录
            dir = new File(outputDir);
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Log.d("MediaMuxerClient", "path=" + dir.toString());
        final GregorianCalendar now = new GregorianCalendar();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
        dateTimeFormat.format(now.getTime());

        if (dir.canWrite()) {
            return new File(dir, dateTimeFormat.format(now.getTime()) + afterStamp).getAbsolutePath();
        }
        return null;
    }

    /**
     * 是否已经开始混合编码了
     */
    @Override
    public boolean isStarted() {
        return isStarted;
    }

    public boolean isHasVideoEncoder() {
        return hasVideoEncoder;
    }

    /**
     * 设置混合mp4是否合并h264数据
     * @param hasVideoEncoder 是否混合h264
     */
    public void setHasVideoEncoder(boolean hasVideoEncoder) {
        this.hasVideoEncoder = hasVideoEncoder;
        //设置记录编码的个数
        mEncoderCount = (hasAudioEncoder ? 1:0) + (this.hasVideoEncoder ? 1:0);
    }

    public boolean isHasAudioEncoder() {
        return hasAudioEncoder;
    }

    /**
     * 设置混合mp4是否合并aac数据
     * @param hasAudioEncoder 是否混合aac
     */
    public void setHasAudioEncoder(boolean hasAudioEncoder) {
        this.hasAudioEncoder = hasAudioEncoder;
        //设置记录编码的个数
        mEncoderCount = (hasAudioEncoder ? 1:0) + (this.hasVideoEncoder ? 1:0);
    }

}

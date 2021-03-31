package com.itc.livebroadcast.core;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.itc.livebroadcast.encoder.IMediaMuxerListener;
import com.itc.livebroadcast.rtmp.RESFlvData;
import com.itc.livebroadcast.rtmp.RESFlvDataCollecter;
import com.itc.livebroadcast.rtmp.RESRtmpSender;
import com.itc.livebroadcast.tools.LogTools;

import java.nio.ByteBuffer;

public class AudioSenderThread extends Thread {
    private static final long WAIT_TIME = 5000;//1ms;
    private MediaCodec.BufferInfo eInfo;
    private long startTime = 0;
    private MediaCodec dstAudioEncoder;
    private RESFlvDataCollecter dataCollecter;
    //混合mp4监听
    private IMediaMuxerListener mMediaMuxerListener;
    //轨道
    private int mTrackIndex;

    AudioSenderThread(String name, MediaCodec encoder, RESFlvDataCollecter flvDataCollecter, IMediaMuxerListener mediaMuxerListener) {
        super(name);
        eInfo = new MediaCodec.BufferInfo();
        startTime = 0;
        dstAudioEncoder = encoder;
        dataCollecter = flvDataCollecter;
        mMediaMuxerListener = mediaMuxerListener;
    }

    private boolean shouldQuit = false;

    void quit() {
        shouldQuit = true;
        this.interrupt();
    }

    @Override
    public void run() {
        while (!shouldQuit) {
            int eobIndex = dstAudioEncoder.dequeueOutputBuffer(eInfo, WAIT_TIME);
            switch (eobIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    LogTools.d("AudioSenderThread,MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
//                        LogTools.d("AudioSenderThread,MediaCodec.INFO_TRY_AGAIN_LATER");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    LogTools.d("AudioSenderThread,MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:" +
                            dstAudioEncoder.getOutputFormat().toString());

                    MediaFormat outputFormat = dstAudioEncoder.getOutputFormat();
                    //开始混合视频编码
                    if (mMediaMuxerListener != null) {
                        mTrackIndex = mMediaMuxerListener.addTrack(outputFormat);
                        mMediaMuxerListener.start();
                    }

                    ByteBuffer csd0 = outputFormat.getByteBuffer("csd-0");
                    sendAudioSpecificConfig(0, csd0);
                    break;
                default:
                    LogTools.d("AudioSenderThread,MediaCode,eobIndex=" + eobIndex);
                    if (startTime == 0) {
                        startTime = eInfo.presentationTimeUs / 1000;
                    }
                    /**
                     * we send audio SpecificConfig already in INFO_OUTPUT_FORMAT_CHANGED
                     * so we ignore MediaCodec.BUFFER_FLAG_CODEC_CONFIG
                     */
                    if (eInfo.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG && eInfo.size != 0) {
                        ByteBuffer realData = dstAudioEncoder.getOutputBuffers()[eobIndex];
                        realData.position(eInfo.offset);
                        realData.limit(eInfo.offset + eInfo.size);
                        sendRealData((eInfo.presentationTimeUs / 1000) - startTime, realData);
                    }
                    dstAudioEncoder.releaseOutputBuffer(eobIndex, false);
                    break;
            }
        }
        eInfo = null;
    }

    private void sendAudioSpecificConfig(long tms, ByteBuffer realData) {
        //写入混合器
        if (mMediaMuxerListener != null) {
            mMediaMuxerListener.writeSampleData(mTrackIndex, realData, eInfo);
        }

        int packetLen = Packager.FLVPackager.FLV_AUDIO_TAG_LENGTH +
                realData.remaining();
        byte[] finalBuff = new byte[packetLen];
        realData.get(finalBuff, Packager.FLVPackager.FLV_AUDIO_TAG_LENGTH,
                realData.remaining());
        Packager.FLVPackager.fillFlvAudioTag(finalBuff,
                0,
                true);
        RESFlvData resFlvData = new RESFlvData();
        resFlvData.droppable = false;
        resFlvData.byteBuffer = finalBuff;
        resFlvData.size = finalBuff.length;
        resFlvData.dts = (int) tms;
        resFlvData.flvTagType = RESFlvData.FLV_RTMP_PACKET_TYPE_AUDIO;
        dataCollecter.collect(resFlvData, RESRtmpSender.FROM_AUDIO);
    }

    private void sendRealData(long tms, ByteBuffer realData) {
        //写入混合器
        if (mMediaMuxerListener != null) {
            mMediaMuxerListener.writeSampleData(mTrackIndex, realData, eInfo);
        }

        int packetLen = Packager.FLVPackager.FLV_AUDIO_TAG_LENGTH +
                realData.remaining();
        byte[] finalBuff = new byte[packetLen];
        realData.get(finalBuff, Packager.FLVPackager.FLV_AUDIO_TAG_LENGTH,
                realData.remaining());
        Packager.FLVPackager.fillFlvAudioTag(finalBuff,
                0,
                false);
        RESFlvData resFlvData = new RESFlvData();
        resFlvData.droppable = true;
        resFlvData.byteBuffer = finalBuff;
        resFlvData.size = finalBuff.length;
        resFlvData.dts = (int) tms;
        resFlvData.flvTagType = RESFlvData.FLV_RTMP_PACKET_TYPE_AUDIO;

        //推流flv格式的数据
        dataCollecter.collect(resFlvData, RESRtmpSender.FROM_AUDIO);
    }
}

package com.itc.livebroadcast.tools;

import android.util.Log;

/**
 * @author xiaosq
 * 创建日期：2021/3/16
 * 描述：
 */
public class ProgramTimeUtil {

    private static long videoLastClickTime = 0;
    private static long audioLastClickTime = 0;

    //获取程序执行间隔
    public static long getVideoIntervalTime() {
        long time = System.currentTimeMillis();
        long timeD = time - videoLastClickTime;
        Log.i("getVideoIntervalTime","==time: "+timeD+"==videoLastClickTime: "+videoLastClickTime);
        return timeD;
    }

    //获取程序执行间隔
    public static long getAudioIntervalTime() {
        long time = System.currentTimeMillis();
        long timeD = time - audioLastClickTime;
        Log.i("getAudioIntervalTime","==time: "+timeD+"==audioLastClickTime: "+audioLastClickTime);
        return timeD;
    }

    public static void setVideoStartIntervalTime() {
        videoLastClickTime = System.currentTimeMillis();
    }

    public static void setAudioStartIntervalTime() {
        audioLastClickTime = System.currentTimeMillis();
    }

}

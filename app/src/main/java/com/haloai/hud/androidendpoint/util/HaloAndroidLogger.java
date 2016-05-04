package com.haloai.hud.androidendpoint.util;

import android.util.Log;

import com.haloai.hud.utils.HaloLoggerConsole;
import com.haloai.hud.utils.IHaloLogger;

/**
 * Created by wangshengxing on 16/4/12.
 */
public class HaloAndroidLogger {
    public static final IHaloLogger logger  = new HaloLoggerConsole();

    public static void logI(String tag, String message) {
        if (logger != null)
            logger.logI(tag, message);
    }
    public static void logD(String tag, String message) {
        if (logger != null)
            logger.logD(tag, message);
    }
    public static void logW(String tag, String message) {
        if (logger != null)
            logger.logW(tag, message);
    }
    public static void logE(String tag, String message) {
        if (logger != null)
            logger.logE(tag, message);
    }

}

package com.haloai.hud.androidendpoint.util;

import com.haloai.hud.androidendpoint.bean.HudAmapNaviInfo;

/**
 * Created by wangshengxing on 16/4/27.
 */
public class HudNaviInfoLab {


    private static HudAmapNaviInfo hudAmapNaviInfo;

    public static HudAmapNaviInfo getHudAmapNaviInfo() {
        if (hudAmapNaviInfo == null){
            hudAmapNaviInfo = new HudAmapNaviInfo();
        }
        return hudAmapNaviInfo;
    }

    public static void releaseHudAmapNaviInfo() {
        HudNaviInfoLab.hudAmapNaviInfo = null;
    }



}

package com.haloai.hud.androidendpoint.controllers.navi;

import android.content.Context;

import com.amap.api.navi.AMapNavi;
import com.haloai.hud.navigation.AMapNavigationSDKAdapter;
import com.haloai.hud.navigation.NavigationSDKAdapter;
import com.haloai.hud.proto.v2.Phone2HudMessagesProtoDef;

/**
 * Created by wangshengxing on 16/4/27.
 */
public abstract class NavigationController {

    public static class Strategy{
        public static int DrivingDefault = AMapNavi.DrivingDefault;
        public static int DrivingShortDistance = AMapNavi.DrivingShortDistance;
        public static int DrivingFastestTime = AMapNavi.DrivingFastestTime;

        public static Phone2HudMessagesProtoDef.NaviDrivingStrategyProto getStrategy(int strategy){
            Phone2HudMessagesProtoDef.NaviDrivingStrategyProto strategyProto = Phone2HudMessagesProtoDef.NaviDrivingStrategyProto.DEFAULT;
            if (strategy == DrivingDefault){
                strategyProto = Phone2HudMessagesProtoDef.NaviDrivingStrategyProto.DEFAULT;
            }else if (strategy == DrivingShortDistance){
                strategyProto = Phone2HudMessagesProtoDef.NaviDrivingStrategyProto.SHORT_DISTANCE;
            }else if (strategy == DrivingFastestTime){
                strategyProto = Phone2HudMessagesProtoDef.NaviDrivingStrategyProto.FASTEST_TIME;
            }
            return strategyProto;
        }
    }

    public static void launchNavigationSDK(String navigationSdkName,
                                           Context appContext) {
        if (navigationSdkName.equalsIgnoreCase(NavigationSDKAdapter.NAVIGATION_SDK_BAIDU)) {
            // currentNavigationAdapter = new BaiduNavigationSDKAdapter();
        } else if (navigationSdkName.equalsIgnoreCase(NavigationSDKAdapter.NAVIGATION_SDK_CARELAND)) {
            // currentNavigationAdapter = new CarelandNavigationSDKAdapter();
        } else if (navigationSdkName.equalsIgnoreCase(NavigationSDKAdapter.NAVIGATION_SDK_AMAP)) {

        } else if (navigationSdkName
                .equalsIgnoreCase(NavigationSDKAdapter.NAVIGATION_SDK_BAIDU_INNER)) {
            // currentNavigationAdapter = new InnerBaiduNavigationSDKAdapter();
        } else {
            throw new IllegalArgumentException(String.format(
                    "Doesn't support '%s' SDK", navigationSdkName));
        }
    }


}

package com.haloai.hud.androidendpoint.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import com.haloai.hud.model.v2.PhoneInfoDeclaim;
import com.haloai.hud.proto.v2.Phone2HudMessagesProtoDef;

/**
 * Created by wangshengxing on 16/4/20.
 */
public class PhoneInfoDeclaimUtils {
    private Context mContext;

    public PhoneInfoDeclaimUtils(Context context){
        mContext = context;
    }

    //1、DeviceId(imei)		测试结果：DeviceId:null
    public String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
//        return Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
        return tm.getDeviceId();
    }


    public String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "error";
        }
    }

    public int getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getProtocolVersionCode(){
        return 1;
    }

    public String getPhonePlatformVersion(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String softVerStr = tm.getDeviceSoftwareVersion();
        if(softVerStr == null){
            softVerStr = "unknown";
        }
        return softVerStr;
    }


    public String getPhoneNumber(Context context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String number = tm.getLine1Number();
        if (number == null) {
            number = "12345678";
        }
        return number;
    }


    public PhoneInfoDeclaim getPhoneInfoDeclaim(){
        PhoneInfoDeclaim phoneInfoDeclaim = new PhoneInfoDeclaim();
        phoneInfoDeclaim.setProtocolVersionCode(this.getProtocolVersionCode());
        phoneInfoDeclaim.setPhonePlatform(Phone2HudMessagesProtoDef.PhonePlatformProto.ANROID);
        phoneInfoDeclaim.setPhonePlatformVersion(this.getPhonePlatformVersion(mContext));
        phoneInfoDeclaim.setPhoneAppVersionCode(this.getVersionCode(mContext));
        phoneInfoDeclaim.setPhoneAppVersionStr(this.getVersionName(mContext));
        phoneInfoDeclaim.setPhoneUniqueId(this.getDeviceId(mContext));
        phoneInfoDeclaim.setPhoneNumber(this.getPhoneNumber(mContext));
        return phoneInfoDeclaim;
    }
}

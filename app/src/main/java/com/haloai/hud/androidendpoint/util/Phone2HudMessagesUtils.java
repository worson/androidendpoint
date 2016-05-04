package com.haloai.hud.androidendpoint.util;

import android.content.Context;

import com.haloai.hud.model.v2.Hud2PhoneMessages;
import com.haloai.hud.model.v2.HudResponseId;
import com.haloai.hud.model.v2.Phone2HudMessages;
import com.haloai.hud.model.v2.PhoneRequestId;

/**
 * Created by wangshengxing on 16/4/20.
 */
public class Phone2HudMessagesUtils {

    public static Phone2HudMessages preparePhoneInfoDeclaim(Phone2HudMessages phone2HudMessages ,Context context){
        if (phone2HudMessages == null) {
            phone2HudMessages = new Phone2HudMessages();
        }
        PhoneInfoDeclaimUtils phoneInfoDeclaimUtils = new PhoneInfoDeclaimUtils(context);
        phone2HudMessages.setPhoneInfoDeclaim(phoneInfoDeclaimUtils.getPhoneInfoDeclaim());
        return phone2HudMessages;
    }

    public static Phone2HudMessages addPhoneRequestId(Phone2HudMessages phone2HudMessages){
        if (phone2HudMessages != null) {
            PhoneRequestId phoneRequestId = new PhoneRequestId();
            phoneRequestId.setPhoneRequestSerialNumber(getPhoneRequestIdStr());
            phone2HudMessages.setPhoneRequestId(phoneRequestId);
        }
        return phone2HudMessages;
    }

    public static Phone2HudMessages addPhoneRequestId(Phone2HudMessages phone2HudMessages ,String phoneRequestIdStr){
        if (phone2HudMessages != null) {
            PhoneRequestId phoneRequestId = new PhoneRequestId();
            phoneRequestId.setPhoneRequestSerialNumber(phoneRequestIdStr);
            phone2HudMessages.setPhoneRequestId(phoneRequestId);
        }
        return phone2HudMessages;
    }

    public static String getPhoneRequestIdFromMessages(Phone2HudMessages Phone2HudMessages){
        PhoneRequestId phoneRequestId = Phone2HudMessages.getPhoneRequestId();
        return phoneRequestId.getPhoneRequestSerialNumber();
    }
    public static String getHudResponseSerialNumberStr(Hud2PhoneMessages hud2PhoneMessages){
        HudResponseId phoneRequestId = hud2PhoneMessages.getHudResponseId();
        return phoneRequestId.getHudResponseSerialNumber();
    }

    public static String getPhoneRequestIdStr(){
        return ""+System.currentTimeMillis();
    }


}

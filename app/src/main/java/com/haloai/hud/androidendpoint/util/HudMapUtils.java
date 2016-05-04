package com.haloai.hud.androidendpoint.util;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.haloai.hud.androidendpoint.R;

/**
 * Created by wangshengxing on 16/4/22.
 */
public class HudMapUtils {

    public static LatLng getLatLng(LatLonPoint latLonPoint){
        return new LatLng(latLonPoint.getLatitude(),latLonPoint.getLongitude());
    }
}

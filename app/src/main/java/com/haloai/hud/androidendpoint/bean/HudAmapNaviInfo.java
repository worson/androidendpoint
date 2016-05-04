package com.haloai.hud.androidendpoint.bean;

import com.amap.api.services.core.PoiItem;

import java.util.List;

/**
 * Created by wangshengxing on 16/4/27.
 */
public class HudAmapNaviInfo {
    private List<PoiItem> poiList = null; //搜索到的poi
    private List<PoiItem> markPoiList = null;//用于显示mark点的
    private String searchKeyword; //搜索的关键词
    private PoiItem destination; //目的地

    private int lastClickPosition = 0;

    public HudAmapNaviInfo(){}

    public List<PoiItem> getPoiList() {
        return poiList;
    }

    public void setPoiList(List<PoiItem> poiList) {
        this.poiList = poiList;
    }

    public List<PoiItem> getMarkPoiList() {
        return markPoiList;
    }

    public void setMarkPoiList(List<PoiItem> markPoiList) {
        this.markPoiList = markPoiList;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public PoiItem getDestination() {
        return destination;
    }

    public void setDestination(PoiItem destination) {
        this.destination = destination;
    }
}

package com.haloai.hud.androidendpoint.views.navi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.overlay.DrivingRouteOverlay;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.haloai.hud.androidendpoint.bean.HudAmapNaviInfo;
import com.haloai.hud.androidendpoint.connmanager.HudConnectionManager;
import com.haloai.hud.androidendpoint.R;
import com.haloai.hud.androidendpoint.controllers.navi.NavigationController;
import com.haloai.hud.androidendpoint.util.HaloAndroidLogger;
import com.haloai.hud.androidendpoint.util.HudMapUtils;
import com.haloai.hud.androidendpoint.util.HudNaviInfoLab;
import com.haloai.hud.androidendpoint.util.Phone2HudMessagesUtils;
import com.haloai.hud.androidendpoint.views.view.WrapSlidingDrawer;
import com.haloai.hud.model.v2.Hud2PhoneMessages;
import com.haloai.hud.model.v2.NaviRouteInfo;
import com.haloai.hud.model.v2.Phone2HudMessages;
import com.haloai.hud.model.v2.PhoneRequestId;
import com.haloai.hud.navigation.AMapNavigationSDKAdapter;
import com.haloai.hud.navigation.AMapNavigationSDKAdapter.RouteSearchData;
import com.haloai.hud.navigation.NavigationSDKAdapter;
import com.haloai.hud.proto.v2.Phone2HudMessagesProtoDef;

import java.nio.ByteBuffer;
import java.util.List;

/*
*
* 地图相关接口
*
* */
public class HudMapPointActivity extends AppCompatActivity implements View.OnClickListener, AMap.OnPOIClickListener, AMap.OnMarkerClickListener, AMap.OnMapLongClickListener {
    private final static String TAG = HudMapPointActivity.class.getName();

    private AMapNavigationSDKAdapter mNavigationSDKAdapter;

    private Context mContext;


    private String mToolbarTitle = "导航";
    //VIEW
    private Toolbar mToolbar;
    private RelativeLayout mPoiListLayout;
    private Button mPlanButton;

    private EditText mLocationText;
    private ListView mSearchPosListView;

    private WrapSlidingDrawer mSlidingDrawer;
    private ImageView mSlidingDrawerHandler;

    private RelativeLayout mNaviRouteDisplayGroupview;


    private SearchedPoiListAdapter mSearchPoiListAdapter;

    private RouteSearchData mCurrentRouteSearchData;

    private SearchedRouteViewHolder shortestRouteView;
    private SearchedRouteViewHolder fastestRouteView;
    private SearchedRouteViewHolder defaultRouteView;

    private SearchedRouteViewHolder[] mRouteViewGroups;
    private SearchedRouteViewGroup searchedRouteViewGroup;

    private TextView mTrafficlightsTextView;
    private TextView mChargeTextView;

    private Button mStartNaviButton;
    private RelativeLayout mStartNaviButtonViewgroup;
    private TextView mStartNaviTimerTextview;

    private HudNaviStartPopupWindow mHudNaviStartPopupWindow;

    private static final int MAP_ZOOM_LEVEL = 15;
    //导航相关
    private MapView mapView;
    private AMap mAMap;
    private Marker mFocusPoiMarker;

    private HudAmapNaviInfo hudAmapNaviInfo = HudNaviInfoLab.getHudAmapNaviInfo();



    private DriveRouteResult mDriveRouteResult;
    private SearchedRouteInfo mSearchedInfo;

    private int mNavigationStrategy;
    private PoiItem mDestinationPoiItem;

    private enum Display_STS {
        Display_POI_LIST,
        Display_ROUTE
    };

    //传输相关
    private HudConnectionManager mHudConnectionManager;
    private HudConnectionManager.IDataDispatcher mDataDispatcher;

    private PhoneRequestId mCurrentPhoneRequestId ;

    private Display_STS mViewState;

    private class SearchedRouteInfo {
        public AMapNavigationSDKAdapter.RouteSearchData shortestDistance = new AMapNavigationSDKAdapter.RouteSearchData();
        //最短路径
        public AMapNavigationSDKAdapter.RouteSearchData fastestTime = new AMapNavigationSDKAdapter.RouteSearchData();
        //最快时间,躲避拥堵
        public AMapNavigationSDKAdapter.RouteSearchData defaultStrategy = new AMapNavigationSDKAdapter.RouteSearchData();
        //推荐路线
    }

    private class SearchedRouteViewGroup {
        public LinearLayout shortestDistance;
        public LinearLayout fastestTime;
        public LinearLayout defaultStrategy;
    }

    private class SearchedRouteViewHolder {
        public LinearLayout viewGroup;
        public TextView routeType;
        public TextView time;
        public TextView distance;
    }

    private void initConnection(){
        mDataDispatcher = new HudConnectionManager.IDataDispatcher() {
            @Override
            public void dispatchH2PData(Hud2PhoneMessages hud2PhoneMessages) {
                Log.i(TAG, "收到HUD Rsp");
                if(mCurrentPhoneRequestId != null && hud2PhoneMessages != null){

                    if(mCurrentPhoneRequestId.getPhoneRequestSerialNumber()
                            .equals(Phone2HudMessagesUtils.getHudResponseSerialNumberStr(hud2PhoneMessages))){
                        Toast.makeText(mContext,"导航数据已发送到HUD",Toast.LENGTH_SHORT).show();
                    }else{
                        Log.i(TAG, "Msg 流水号不一致");
                    }
                }else{
                    Log.i(TAG, "Msg或流水号为空");
                }

            }
        };

        mHudConnectionManager = HudConnectionManager.getInstance(this.getApplicationContext());
        mHudConnectionManager.addDataDispatcher(mDataDispatcher);
    }

    private void initDefaultData() {
        mSearchedInfo = new SearchedRouteInfo();
        mViewState = Display_STS.Display_POI_LIST;
        initConnection();

    }

    private void initNavigationSDKAdapter() {
//        mNavigationSDKAdapter = AMapNavigationController.getAMapNavigationSDKAdapter(this.getApplicationContext());
        mNavigationSDKAdapter = (AMapNavigationSDKAdapter) NavigationSDKAdapter
                .getCurrentNavigationAdapter();
        //TODO 空指针崩溃
        mNavigationSDKAdapter.setNaviNotifier(mNavigationNotifier);
        mNavigationSDKAdapter.setChooseNaviStrategyCallback(new AMapNavigationSDKAdapter.ChooseNaviStrategyCallback() {
            @Override
            public void chooseNaviStrategy() {
                Log.i(TAG, "路径规划成功");
                mNavigationSDKAdapter.getRouteSearchResult(mSearchedInfo.shortestDistance, mSearchedInfo.fastestTime, mSearchedInfo.defaultStrategy);
                updateMapRouteView(NavigationController.Strategy.DrivingDefault);
                SelectViewGroup(Display_STS.Display_ROUTE);
                updateRouteView();


            }
        });

    }

    private void updateRouteView() {
        Log.i(TAG, "路径规划成功，更新路径列表数据");
        String distanceSymbol = "km";
        String timeSymbol = "min";
        int timeScale = 60;
        int distanceScale = 100;
        defaultRouteView.time.setText(String.valueOf(mSearchedInfo.defaultStrategy.duration / timeScale) + timeSymbol);
        defaultRouteView.distance.setText(String.valueOf(mSearchedInfo.defaultStrategy.duration / distanceScale) + distanceSymbol);

        fastestRouteView.time.setText(String.valueOf(mSearchedInfo.fastestTime.duration / timeScale) + timeSymbol);
        fastestRouteView.distance.setText(String.valueOf(mSearchedInfo.fastestTime.duration / distanceScale) + distanceSymbol);

        shortestRouteView.time.setText(String.valueOf(mSearchedInfo.shortestDistance.duration / timeScale) + timeSymbol);
        shortestRouteView.distance.setText(String.valueOf(mSearchedInfo.shortestDistance.duration / distanceScale) + distanceSymbol);

        updateMoreRouteView(NavigationController.Strategy.DrivingDefault);
        updateRouteViewGroup(0);

    }

    private void updateMoreRouteView(int strategy) {
        Log.i(TAG, "路径规划成功，更新红绿灯、收费信息");
        if (strategy == NavigationController.Strategy.DrivingDefault) {
            mCurrentRouteSearchData = mSearchedInfo.defaultStrategy;
        } else if (strategy == NavigationController.Strategy.DrivingFastestTime) {
            mCurrentRouteSearchData = mSearchedInfo.fastestTime;
        } else if (strategy == NavigationController.Strategy.DrivingShortDistance) {
            mCurrentRouteSearchData = mSearchedInfo.shortestDistance;
        } else {
            return;
        }

        String charge = "过路费" + Math.round(mCurrentRouteSearchData.charge) + "元";
        String trafficLight = "红绿灯" + mCurrentRouteSearchData.trafficLights + "个";

        mTrafficlightsTextView.setText(trafficLight);
        mChargeTextView.setText(charge);

    }

    private void updateRouteViewGroup(int index){
        int colorWite =  mContext.getApplicationContext().getResources().getColor(R.color.white);
        int colorTimeText =  mContext.getApplicationContext().getResources().getColor(R.color.color_plan_route_info_time_text);
        int colorDefault =  mContext.getApplicationContext().getResources().getColor(R.color.color_plan_route_info_default);
        for(int i=0;i<3;i++){
            if (i == index){

                mRouteViewGroups[i].viewGroup.setBackgroundResource(R.drawable.navi_route_info_background_point_9);
                mRouteViewGroups[i].routeType.setTextColor(colorWite);
                mRouteViewGroups[i].distance.setTextColor(colorWite);
                mRouteViewGroups[i].time.setTextColor(colorWite);
            }else{
                mRouteViewGroups[i].viewGroup.setBackgroundResource(R.color.white);
                mRouteViewGroups[i].routeType.setTextColor(colorDefault);
                mRouteViewGroups[i].distance.setTextColor(colorDefault);
                mRouteViewGroups[i].time.setTextColor(colorTimeText);
            }
        }
    }

    private void initRouteView() {
        //
        shortestRouteView = new SearchedRouteViewHolder();
        fastestRouteView = new SearchedRouteViewHolder();
        defaultRouteView = new SearchedRouteViewHolder();

        searchedRouteViewGroup = new SearchedRouteViewGroup();
        searchedRouteViewGroup.defaultStrategy = (LinearLayout) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_groupview1);
        searchedRouteViewGroup.fastestTime = (LinearLayout) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_groupview2);
        searchedRouteViewGroup.shortestDistance = (LinearLayout) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_groupview3);

        mRouteViewGroups = new SearchedRouteViewHolder[]{defaultRouteView,fastestRouteView,shortestRouteView};

        searchedRouteViewGroup.defaultStrategy.setOnClickListener(this);
        searchedRouteViewGroup.fastestTime.setOnClickListener(this);
        searchedRouteViewGroup.shortestDistance.setOnClickListener(this);


        defaultRouteView.viewGroup = searchedRouteViewGroup.defaultStrategy;
        defaultRouteView.routeType = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_type_textview1);
        defaultRouteView.time = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_time1);
        defaultRouteView.distance = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_distance1);

        fastestRouteView.viewGroup = searchedRouteViewGroup.fastestTime;
        fastestRouteView.routeType = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_type_textview2);
        fastestRouteView.time = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_time2);
        fastestRouteView.distance = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_distance2);

        shortestRouteView.viewGroup = searchedRouteViewGroup.shortestDistance;
        shortestRouteView.routeType = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_type_textview3);
        shortestRouteView.time = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_time3);
        shortestRouteView.distance = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.plan_route_info_route_distance3);


        mTrafficlightsTextView = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.anvi_route_trafficlights_textView);
        mChargeTextView = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.anvi_route_charge_textView);

        mStartNaviTimerTextview = (TextView) mNaviRouteDisplayGroupview.findViewById(R.id.anvi_route_start_timer_textview);
        mStartNaviButton = (Button) mNaviRouteDisplayGroupview.findViewById(R.id.anvi_route_start_button);
        mStartNaviButtonViewgroup = (RelativeLayout) mNaviRouteDisplayGroupview.findViewById(R.id.anvi_route_start_viewgroup);

        mStartNaviButton.setOnClickListener(this);
        mStartNaviButtonViewgroup.setOnClickListener(this);


    }


    private void initViewGroup() {
        mNaviRouteDisplayGroupview = (RelativeLayout) findViewById(R.id.navi_route_display_viewgroup);
        mNaviRouteDisplayGroupview.setVisibility(ViewGroup.GONE);
    }


    private void SelectViewGroup(Display_STS slt) {
        mViewState = slt;
        switch (slt) {
            case Display_POI_LIST:
                mSlidingDrawer.setVisibility(View.VISIBLE);
                mNaviRouteDisplayGroupview.setVisibility(View.GONE);
                break;
            case Display_ROUTE:
                mSlidingDrawer.close();
                mSlidingDrawer.setVisibility(View.GONE);
                mNaviRouteDisplayGroupview.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

    }

    private void initDefaultView() {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    private void initPoiListView() {
        mSearchPosListView = (ListView) findViewById(R.id.searchPoiListView);
        mSearchPosListView.setClickable(true);
        mSearchPosListView.setItemsCanFocus(true);
        mSearchPosListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mSearchPoiListAdapter = new SearchedPoiListAdapter(mContext, hudAmapNaviInfo.getPoiList());
        mSearchPosListView.setAdapter(mSearchPoiListAdapter);
        mSearchPosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiItem poiItem = (PoiItem)mSearchPoiListAdapter.getItem(position);
                LatLng latLng = HudMapUtils.getLatLng(poiItem.getLatLonPoint());
                updateFocusMarker(latLng);
            }
        });
        mSearchPoiListAdapter.notifyDataSetChanged();
        if (mSearchPosListView.getChildCount()>0){
            View childView = mSearchPosListView.getChildAt(0);
            childView.measure(0,0);
            int totalHeight = Math.min(mSearchPoiListAdapter.getCount(),4) * childView.getMeasuredHeight()+mSearchPosListView.getDividerHeight();
            ViewGroup.LayoutParams params = mSearchPosListView.getLayoutParams();
            params.height = totalHeight;
            mSearchPosListView.setLayoutParams(params);
        }

    }

    void updateFocusMarker(LatLng latLng){
        if (mFocusPoiMarker != null) {
            mFocusPoiMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.map_poi_default_mark));
        mFocusPoiMarker  = mAMap.addMarker(markerOptions);
        mFocusPoiMarker.hideInfoWindow();
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL));
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.common_tool_bar);
        TextView toolTitle = (TextView)findViewById(R.id.toolbar_title);
        toolTitle.setText(mToolbarTitle);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                OnViewBack();
            }
        });

        ImageView clearInputView = (ImageView) findViewById(R.id.iv_searchbox_search_clean);
        clearInputView.setVisibility(View.INVISIBLE);
    }

    private void initSearchBar() {
        String keyword = hudAmapNaviInfo.getSearchKeyword();

        mLocationText = (EditText) findViewById(R.id.edittext_searchbox_search_input);
        mLocationText.clearFocus();
        mLocationText.setFocusable(false);
        if (keyword != null) {
            mLocationText.setHint(keyword);
        }
        mLocationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, HudPoiTipsActivity.class));
            }
        });
        mLocationText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mLocationText.hasFocus()) {
                    Log.i(TAG, "搜索框拿到焦点");
                }
            }
        });
        mLocationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void initNaviStartPopupWindow(){

        mHudNaviStartPopupWindow = new HudNaviStartPopupWindow(HudMapPointActivity.this, mOnPopupWindowClickLister);
        mHudNaviStartPopupWindow.showAtLocation(HudMapPointActivity.this.findViewById(R.id.activity_hdu_map_point), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }
    private void initView() {

        initToolbar();
        initDefaultView();
        initSearchBar();
        initViewGroup();
        initPoiListView();
        initSlidingDrawer();
        initRouteView();

    }

    private void updatePoiListView() {
        mSearchPoiListAdapter = new SearchedPoiListAdapter(mContext, hudAmapNaviInfo.getPoiList());
        mSearchPosListView.setAdapter(mSearchPoiListAdapter);
    }

    public void updateMapRouteView(int strategy) {
        Log.i(TAG, "路径规划成功，更新红绿灯、收费信息");

        mNavigationStrategy = strategy;
        if (strategy == NavigationController.Strategy.DrivingDefault) {
            mDriveRouteResult = mSearchedInfo.defaultStrategy.driveRouteResult;
        } else if (strategy == NavigationController.Strategy.DrivingFastestTime) {
            mDriveRouteResult = mSearchedInfo.fastestTime.driveRouteResult;
        } else if (strategy == NavigationController.Strategy.DrivingShortDistance) {
            mDriveRouteResult = mSearchedInfo.shortestDistance.driveRouteResult;
        } else {
            return;
        }

        Log.i(TAG, "路径规划成功，在地图上描绘路径");
        mAMap.clear();// 清理之前的图标

        final DrivePath drivePath = mDriveRouteResult.getPaths()
                .get(0);
        DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                mContext, mAMap, drivePath,
                mDriveRouteResult.getStartPos(),
                mDriveRouteResult.getTargetPos());
        drivingRouteOverlay.removeFromMap();
        drivingRouteOverlay.addToMap();
        drivingRouteOverlay.zoomToSpan();
    }


    private void updatePioOverlay() {
        List<PoiItem> poiItems = hudAmapNaviInfo.getPoiList();// 取得第一页的poiitem数据，页数从数字0开始
        if (poiItems != null && poiItems.size() > 0) {
            Log.d(TAG, "正在更新overlay");
            mAMap.clear();// 清理之前的图标

            LatLonPoint latLonPoint = poiItems.get(0).getLatLonPoint();
            LatLng latLng = new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
            mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            PoiOverlay poiOverlay = new PoiOverlay(mAMap, poiItems);
            poiOverlay.removeFromMap();
            poiOverlay.addToMap();
            poiOverlay.zoomToSpan();
        }
    }

    private void initSlidingDrawer() {
        mSlidingDrawer = (WrapSlidingDrawer) findViewById(R.id.poi_list_WrapSlidingDrawer);
        mSlidingDrawerHandler = (ImageView) findViewById(R.id.poi_list_WrapSlidingDrawer_hold);
        mSlidingDrawer.open();
        mSlidingDrawer.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return false;
            }
        });
    }

    private boolean OnViewBack() {
        if (Display_STS.Display_ROUTE == mViewState) {
            Log.i(TAG, "按下返回键，切换到POI列表");
            SelectViewGroup(Display_STS.Display_POI_LIST);
            mSlidingDrawer.open();
            updatePioOverlay();
            return true;
        } else {
            Log.i(TAG, "按下返回键，直接退出");
            finish();
            return false;
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.anvi_route_start_button:
                Log.i(TAG, "开始导航键下");
                startNavigation();
                break;
            case R.id.anvi_route_start_viewgroup:
                Log.i(TAG, "开始导航键下");
                startNavigation();
                break;
            case R.id.plan_route_info_route_groupview1:
                updateRouteViewGroup(0);
                updateMapRouteView(NavigationController.Strategy.DrivingDefault);
                updateMoreRouteView(NavigationController.Strategy.DrivingDefault);
                break;
            case R.id.plan_route_info_route_groupview2:
                updateRouteViewGroup(1);
                updateMapRouteView(NavigationController.Strategy.DrivingFastestTime);
                updateMoreRouteView(NavigationController.Strategy.DrivingFastestTime);
                break;
            case R.id.plan_route_info_route_groupview3:
                updateRouteViewGroup(2);
                updateMapRouteView(NavigationController.Strategy.DrivingShortDistance);
                updateMoreRouteView(NavigationController.Strategy.DrivingShortDistance);
                break;
            default:
                break;
        }
    }

    public void startNavigation(){
        Log.i(TAG, "发送导航数据到HUD");
        if(mHudConnectionManager.isConnected()){
            Toast.makeText(mContext,"正在发送导航数据",Toast.LENGTH_SHORT).show();
            mHudConnectionManager.send(ByteBuffer.wrap(perapareNaviInfo()));
            startActivity(new Intent(mContext,HudStartNaviActivity.class));
        }else {
            initNaviStartPopupWindow();
            //Toast.makeText(mContext,"未连接到HUD,请先连接HUD或通过云端规划",Toast.LENGTH_SHORT).show();
            //Log.i(TAG, "未连接到HUD,请连接HUD或发送导航数据到云端");
        }

    }

    public void connectForNavigation(){

    }

    private View.OnClickListener mOnPopupWindowClickLister = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.navi_start_popup_send_hud_vuewgroup:
                    connectForNavigation();
                    break;
                case R.id.navi_start_popup_send_cloud_textview:
                    Log.i(TAG, "发送导航数据到云");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "检测返回键...");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return OnViewBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * event handler
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hud_map_point);
        mContext = this;

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        initDefaultData();
        initAMap();
        initNavigationSDKAdapter();
        initMapClick();

        updatePioOverlay();
        initView();


    }

    private void initMapClick() {
        mAMap.setOnPOIClickListener(this);
        mAMap.setOnMarkerClickListener(this);

        mAMap.setOnMapLongClickListener(this);// 对amap添加长按地图事件监听器
    }

    //底图poi点击回调
    @Override
    public void onPOIClick(Poi poi) {
//        mAMap.clear();
//        MarkerOptions markOptiopns = new MarkerOptions();
//        markOptiopns.position(poi.getCoordinate());
//        TextView textView = new TextView(getApplicationContext());
//        textView.setText("到" + poi.getName() + "去");
//        textView.setGravity(Gravity.CENTER);
//        textView.setTextColor(Color.BLACK);
//        textView.setBackgroundResource(R.drawable.custom_info_bubble);
//        markOptiopns.icon(BitmapDescriptorFactory.fromView(textView));
//        mAMap.addMarker(markOptiopns);


    }

    @Override
    public boolean onMarkerClick(Marker marker) {

//        marker.p
//        mSearchPosListView.
        updateFocusMarker(marker.getPosition());
        mSearchPosListView.setSelection(mSearchPosListView.getCount()-1);
        mSearchPosListView.callOnClick();
        return false;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d(TAG, "long pressed, point=" + latLng);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
//        mAMap.setLocationSource(this);// 设置定位监听
//        mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
//        mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
//        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
//        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    private void initAMap() {
        if (mAMap == null) {
            mAMap = mapView.getMap();


        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        initNavigationSDKAdapter();
        mapView.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    public class SearchedPoiListAdapter extends BaseAdapter {
        private Context ctx;
        private List<PoiItem> list;

        public SearchedPoiListAdapter(Context context, List<PoiItem> poiList) {
            this.ctx = context;
            this.list = poiList;
        }

        public void setListData(List<PoiItem> poiList) {
            this.list = poiList;
        }

        public List<PoiItem> getList() {
            return list;
        }

        public void setList(List<PoiItem> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
//            return list.size();
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(ctx, R.layout.searched_poi_list_item_new, null);
//                convertView = ctx.getActivity().getLayoutInflater()
//                        .inflate(R.layout.list_item_crime, null);
                holder.nuberTitle = (TextView) convertView
                        .findViewById(R.id.sort_nubmer_textview);
                holder.poititle = (TextView) convertView
                        .findViewById(R.id.poititle);
                holder.subtitle = (TextView) convertView
                        .findViewById(R.id.subpoititle);
                holder.startNavi = (TextView) convertView
                        .findViewById(R.id.searched_poi_item_start_navi);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final PoiItem item = list.get(position);
            holder.startNavi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "触发发起导航");
                    mNavigationSDKAdapter.startNavigate(position);
                    mDestinationPoiItem = item;
                }
            });

            if (item != null) {
                String subtitle = item.getCityName() + item.getAdName() + item.getSnippet();
                holder.poititle.setText(item.getTitle());
                holder.nuberTitle.setText(String.valueOf(position + 1) + "、");
                holder.subtitle.setText(subtitle);
            }

//            if (item.getSubPois().size() > 0) {
//                List<SubPoiItem> subPoiItems = item.getSubPois();
//                SubPoiAdapter subpoiAdapter=new SubPoiAdapter(ctx, subPoiItems);
//                holder.subpois.setAdapter(subpoiAdapter);
//            }


            return convertView;
        }

        private class ViewHolder {
            TextView nuberTitle;
            TextView poititle;
            TextView subtitle;
            TextView startNavi;
        }

    }

    private byte[] perapareNaviInfo(){
        Phone2HudMessages phone2HudMessages = new Phone2HudMessages();
        NaviRouteInfo naviRouteInfo = new NaviRouteInfo();
        String destination = mDestinationPoiItem.getTitle();
        double naviDestinationLng = mDestinationPoiItem.getLatLonPoint().getLongitude();
        double naviDestinationLat = mDestinationPoiItem.getLatLonPoint().getLatitude();
        Phone2HudMessagesProtoDef.NaviDrivingStrategyProto strategy = NavigationController.Strategy.getStrategy(mNavigationStrategy);

        HaloAndroidLogger.logD(TAG,"目的地为："+destination+", Lng "+naviDestinationLng+",  Lat  "+naviDestinationLat+",  strategy  "+strategy.ordinal());

        naviRouteInfo.setNaviDestinationPoi(destination);
        naviRouteInfo.setNaviDestinationLng(naviDestinationLng);
        naviRouteInfo.setNaviDestinationLat(naviDestinationLat);
//        naviRouteInfo.setNaviDrivingStrategy(Phone2HudMessagesProtoDef.NaviDrivingStrategyProto.DEFAULT);
        naviRouteInfo.setNaviDrivingStrategy(strategy);

        phone2HudMessages.setNaviRouteInfo(naviRouteInfo);

        PhoneRequestId phoneRequestId = new PhoneRequestId();
        phoneRequestId.setPhoneRequestSerialNumber(Phone2HudMessagesUtils.getPhoneRequestIdStr());
        phone2HudMessages.setPhoneRequestId(phoneRequestId);


        mCurrentPhoneRequestId = phoneRequestId;

        return phone2HudMessages.encapsulateHudP2HData();

    }


    private NavigationSDKAdapter.NavigationNotifier mNavigationNotifier = new NavigationSDKAdapter.NavigationNotifier() {

        @Override
        public void onDetailPOIListData(String poiNameToSearch, List<PoiItem> poiList) {


//            mNavigationSDKAdapter.startNavigate(0);
        }

        @Override
        public void onPOIListData(String poiNameToSearch, List<String> poiList) {


        }


        @Override
        public void onPOISearchFailed(String poiNameToSearch, String error) {
            Log.e(TAG, "搜索POI失败");
        }

        @Override
        public void onCalculateRouteFailure(int errorInfo) {

        }

        @Override
        public void onNaviStarted() {

        }

        @Override
        public void onNaviStopped() {

        }

        @Override
        public void onNaviText(NaviTextType textType, String text) {

        }

        @Override
        public void onYawStart() {

        }

        @Override
        public void onYawEnd() {

        }

        @Override
        public void onReCalculateRouteForTrafficJam() {

        }

        @Override
        public void onCrossShow(Bitmap cross) {

        }

        @Override
        public void onCrossHide() {

        }

        @Override
        public void onNaviInfo(NaviInfo info) {

        }

        @Override
        public void onSpeedUpgraded(float speed) {

        }

        @Override
        public void onLocationChanged(Object location) {

        }

        @Override
        public void onPathCalculated(Object aMapNavi) {
            Log.i(TAG, "路径规划成功");
        }

        @Override
        public void onSatelliteFound(int satelliteNum) {

        }

        @Override
        public void onArriveDestination() {

        }

        @Override
        public void onEndEmulatorNavi() {

        }
    };
}

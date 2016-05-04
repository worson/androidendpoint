package com.haloai.hud.androidendpoint.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.haloai.hud.androidendpoint.R;
import com.haloai.hud.androidendpoint.connmanager.HudConnectionManager;
import com.haloai.hud.androidendpoint.util.PhoneInfoDeclaimUtils;
import com.haloai.hud.model.v2.Hud2PhoneMessages;
import com.haloai.hud.model.v2.HudQueryRequest;
import com.haloai.hud.model.v2.NaviRouteInfo;
import com.haloai.hud.model.v2.Phone2HudMessages;
import com.haloai.hud.proto.v2.Phone2HudMessagesProtoDef;

import java.nio.ByteBuffer;

public class TestPhone2HudConnection extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = TestPhone2HudConnection.class.getName();

    private HudConnectionManager mHudConnectionManager;
    private HudConnectionManager.IDataDispatcher mDataDispatcher;

    private Button sendNaviInfoButton;

    private void initConnection(){
        mDataDispatcher = new HudConnectionManager.IDataDispatcher() {
            @Override
            public void dispatchH2PData(Hud2PhoneMessages hud2PhoneMessages) {

                Log.i(TAG, "收到HUD Rsp");
            }
        };

        mHudConnectionManager = HudConnectionManager.getInstance(this.getApplicationContext());
        mHudConnectionManager.addDataDispatcher(mDataDispatcher);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_phone2_hud_connection);
        initConnection();
        sendNaviInfoButton = (Button)findViewById(R.id.test_send_naviinfo_textview);
        sendNaviInfoButton.setOnClickListener(this);




    }

    @Override
    protected void onDestroy() {
        mHudConnectionManager.removeDataDispatcher(mDataDispatcher);
        super.onDestroy();
    }

    private byte[] perapareNaviInfo(){
        Phone2HudMessages phone2HudMessages = new Phone2HudMessages();
      /*  HudQueryRequest hudQueryRequest=new HudQueryRequest();
        hudQueryRequest.setPhoneRequestCode("naviinfo");
        hudQueryRequest.setPhoneBindHudKey("dgshg");*/
        NaviRouteInfo naviRouteInfo = new NaviRouteInfo();
        naviRouteInfo.setNaviDestinationPoi("西乡");
        naviRouteInfo.setNaviDestinationLng(1.0);
        naviRouteInfo.setNaviDestinationLat(2.0);
        naviRouteInfo.setNaviDrivingStrategy(Phone2HudMessagesProtoDef.NaviDrivingStrategyProto.DEFAULT);

        PhoneInfoDeclaimUtils phoneInfoDeclaimUtils = new PhoneInfoDeclaimUtils(this);
        phone2HudMessages.setNaviRouteInfo(naviRouteInfo);
        phone2HudMessages.setPhoneInfoDeclaim(phoneInfoDeclaimUtils.getPhoneInfoDeclaim());
       // phone2HudMessages.setHudQueryRequest(hudQueryRequest);
        return phone2HudMessages.encapsulateHudP2HData();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.test_send_naviinfo_textview:
                Log.i(TAG, "发送导航数据到HUD");
                PhoneInfoDeclaimUtils phoneInfoDeclaimUtils = new PhoneInfoDeclaimUtils(TestPhone2HudConnection.this);
                phoneInfoDeclaimUtils.getPhoneInfoDeclaim();
                if(mHudConnectionManager.isConnected()){
                    mHudConnectionManager.send(ByteBuffer.wrap(perapareNaviInfo()));
                }else {
                    Log.i(TAG, "未连接到HUD");
                }
                break;
            default:
                break;
        }
    }
}

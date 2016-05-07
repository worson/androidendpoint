package com.haloai.hud.androidendpoint.views.carcorder;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.haloai.hud.androidendpoint.R;
import com.haloai.hud.androidendpoint.connmanager.HudConnectionManager;
import com.haloai.hud.androidendpoint.util.Phone2HudMessagesUtils;
import com.haloai.hud.model.v2.Hud2PhoneMessages;
import com.haloai.hud.model.v2.HudCarcorderInfoQueryResp;
import com.haloai.hud.model.v2.HudInfoDeclaim;
import com.haloai.hud.model.v2.HudQueryRequest;
import com.haloai.hud.model.v2.HudQueryResponse;
import com.haloai.hud.model.v2.Phone2HudMessages;
import com.haloai.hud.model.v2.PhoneRequestId;
import com.haloai.hud.model.v2.RecorderVideoQuery;
import com.haloai.hud.model.v2.RecorderVideoQueryResp;
import com.haloai.hud.model.v2.ThumbnailTransferRequest;
import com.haloai.hud.model.v2.ThumbnailTransferResp;
import com.haloai.hud.proto.v2.CommonMessagesProtoDef;
import com.haloai.hud.utils.HaloLogger;
import com.haloai.hud.utils.IHaloLogger;

import java.util.ArrayList;
import java.util.List;

public class HudCarcorderActivity extends AppCompatActivity {
    private final static String TAG = HudCarcorderActivity.class.getName();
    private Context mContext;

    private String mToolbarTitle = "行车记录仪";
    //VIEW
    private Toolbar mToolbar;

    private HudConnectionManager mHudConnectionManager;
    private String mCurrentPhoneRequestIdStr;

    private static IHaloLogger mIHaloLogger = HaloLogger.logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carcorder);
        mContext = this;
        initView();
        initHudConnection();
        sendRecorderVideoQuery();
    }

    private void initView(){
        initToolbar();
    }
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.common_tool_bar);
        TextView toolTitle = (TextView)findViewById(R.id.toolbar_title);
        toolTitle.setText(mToolbarTitle);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initHudConnection() {
        mHudConnectionManager = HudConnectionManager.getInstance(this.getApplicationContext());
        mHudConnectionManager.addConnecttionListener(new HudConnectionManager.ConnecttionListener() {
            @Override
            public void ConnectResult(boolean result) {

            }
        });
        mHudConnectionManager.setConnectStrategy(HudConnectionManager.HudConnectionStrategy.HUD_CONNECTION_WIFI_FIRST);
        HudConnectionManager.IDataDispatcher dataDispatcher = new HudConnectionManager.IDataDispatcher() {
            @Override
            public void dispatchH2PData(Hud2PhoneMessages hud2PhoneMessages) {
                Log.i(TAG, "收到HUD Rsp");
                if (mCurrentPhoneRequestIdStr != null && hud2PhoneMessages != null) {

                    if (mCurrentPhoneRequestIdStr
                            .equals(Phone2HudMessagesUtils.getHudResponseSerialNumberStr(hud2PhoneMessages))) {

                        HudQueryResponse queryResponse = hud2PhoneMessages.getHudQueryResponse();
                        List<RecorderVideoQueryResp> videoQueryRespList = hud2PhoneMessages.getRecorderVideoQueryRespList();
                        List<ThumbnailTransferResp> thumbnailTransferRespList = hud2PhoneMessages.getThumbnailTransferRespList();

                        if (thumbnailTransferRespList != null && thumbnailTransferRespList.size()>0) {
                            for (ThumbnailTransferResp thumbnailTransferResp:thumbnailTransferRespList) {
                                LogI("行车记录仪缩略图信息为："+thumbnailTransferResp);
                            }
                        }
                        if (videoQueryRespList != null && videoQueryRespList.size()>0) {
                            for (RecorderVideoQueryResp videoQueryResp:videoQueryRespList) {
                                LogI("行车记录仪视频信息为："+videoQueryResp);
                            }
                        }
                        if (queryResponse != null) {
                            HudCarcorderInfoQueryResp carcorderInfoQueryResp = queryResponse.getCarcorderInfoQueryResp();
                            if (carcorderInfoQueryResp != null) {
//                                LogI("行车记录仪信息为："+" 总存储:"+carcorderInfoQueryResp.getHudTotalMemory()+" 已用存储:"+" 清晰度:"+" 循环视频:"+" 加锁视频:"+" 精彩视频:"+" 紧急视频:");
                                LogI("行车记录仪信息为："+ carcorderInfoQueryResp);
                            }
                        }

                    } else {
                        Log.i(TAG, "Msg 流水号不一致");
                    }
                } else {
                    Log.i(TAG, "Msg或流水号为空");
                }






            }
        };
        mHudConnectionManager.addDataDispatcher(dataDispatcher);



    }
    public void onClickSend(View v){
        sendRecorderVideoQuery();
    }
    private void sendRecorderVideoQuery(){
        Phone2HudMessages phone2HudMessages = new Phone2HudMessages();

        HudQueryRequest hudQueryRequest = new HudQueryRequest();
        hudQueryRequest.setFeatureCarcorderInfo(0x0001);//查询行车记录仪信息
        phone2HudMessages.setHudQueryRequest(hudQueryRequest);


        ArrayList<RecorderVideoQuery> videoQueryArrayList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            RecorderVideoQuery videoQuery = new RecorderVideoQuery();
            videoQuery.setBeginNewBatch(true);
            videoQuery.setVideoIndex(1);
            videoQuery.setVideoNumber(5);
            videoQuery.setRecorderType(CommonMessagesProtoDef.RecorderTypeProto.valueOf(i));
            videoQueryArrayList.add(videoQuery);
        }
        phone2HudMessages.setRecorderVideoQueryList(videoQueryArrayList);

        ArrayList<ThumbnailTransferRequest> thumbnailTransferRequestList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ThumbnailTransferRequest thumbnailTransferRequest = new ThumbnailTransferRequest();
            thumbnailTransferRequest.setThumbnailIndex(1);
            thumbnailTransferRequest.setThumbnailNumber(5);
            thumbnailTransferRequest.setRecorderType(CommonMessagesProtoDef.RecorderTypeProto.valueOf(i));
            thumbnailTransferRequestList.add(thumbnailTransferRequest);

        }
        phone2HudMessages.setThumbnailTransferRequestList(thumbnailTransferRequestList);

        PhoneRequestId requestId = new PhoneRequestId();
        mCurrentPhoneRequestIdStr = Phone2HudMessagesUtils.getPhoneRequestIdStr();
        requestId.setPhoneRequestSerialNumber(mCurrentPhoneRequestIdStr);
        phone2HudMessages.setPhoneRequestId(requestId);
        if(mHudConnectionManager.isConnected()){
            mHudConnectionManager.send(phone2HudMessages.encapsulateHudP2HData());
            LogI("发送行车记录仪数据请求");
        }
    }
    private void LogI(String msg){
        mIHaloLogger.logI(TAG,msg);
    }
}

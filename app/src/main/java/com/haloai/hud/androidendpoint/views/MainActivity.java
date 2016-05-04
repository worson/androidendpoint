package com.haloai.hud.androidendpoint.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.haloai.hud.androidendpoint.connmanager.HudConnectionManager;
import com.haloai.hud.androidendpoint.demo.TestPhone2HudConnection;
import com.haloai.hud.androidendpoint.util.Phone2HudMessagesUtils;
import com.haloai.hud.androidendpoint.views.navi.HudPoiTipsActivity;
import com.haloai.hud.androidendpoint.R;
import com.haloai.hud.androidendpoint.views.settings.HudSettingsActivity;
import com.haloai.hud.model.v2.Hud2PhoneMessages;
import com.haloai.hud.model.v2.HudInfoDeclaim;

import java.util.Arrays;
import java.util.List;

/*
* 主导航类
* */
public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();
    private Context mContext;

    private HudConnectionManager mHudConnectionManager;
    //View
    ProgressDialog mProgressDialog;


    private ImageView mHudBackgroundImageView;
    private ImageView mHudconnectStatesImageview;
    private ImageView mHudColorBlockImageview;
    private TextView mConnectHudView;

    private ListView mModuleListView;

    //    private static final ModuleDetails[] demos = {
//            new ModuleDetails(0,0,HudPoiTipsActivity.class),
//            new ModuleDetails(0,0,udDriveRouteActivity.class),
//            new ModuleDetails(0,0,HudMapPointActivity.class),
//            new ModuleDetails(0,0,TestToolBarActivity.class),
//            new ModuleDetails(0,0,TestMapViewActivity.class),
//            new ModuleDetails(0,0,TestReloadMapViewActivity.class),
//    };
    private static final ModuleDetails[] demos = {
//            new ModuleDetails(0, 0, HudNaviAcitity.class),
            new ModuleDetails(0, 0, HudPoiTipsActivity.class),
            new ModuleDetails(0, 0, TestPhone2HudConnection.class),
//            new ModuleDetails(0, 0, HudCarcorderActivity.class),
            new ModuleDetails(0, 0, HudSettingsActivity.class),
    };

    private static String[] moduleNames;
    private static String[] moduleDetailNames;
    private static int[] modulePictures = new int[]{R.drawable.main_item_navi, R.drawable.main_item_carcorder, R.drawable.main_item_setting};


    private static class ModuleDetails {
        private final int titleId;
        private final int descriptionId;
        private final Class<? extends android.app.Activity> activityClass;

        public ModuleDetails(int titleId, int descriptionId,
                             Class<? extends android.app.Activity> activityClass) {
            super();
            this.titleId = titleId;
            this.descriptionId = descriptionId;
            this.activityClass = activityClass;
        }
    }

    private class ViewHolder {
        ImageView icon;
        TextView tittle;
        TextView subtittle;
    }

    private final static int[] MainModuleIds = new int[]{R.id.main_item_navi, R.id.main_item_carcorder, R.id.main_item_settings};
    private final static int[] MainModuleItmeIds = new int[]{R.id.title_picture_imageview, R.id.moduletitle, R.id.submoduletitle};

    private RelativeLayout mModuleViewGroup;
    private ViewHolder mModuleViewHolder = new ViewHolder();

    public void initIncludeMainItem() {
        for (int i = 0; i < MainModuleIds.length; i++) {

            final int clickIndex = i;
            mModuleViewGroup = (RelativeLayout) findViewById(MainModuleIds[i]);
            mModuleViewGroup.setClickable(true);
            mModuleViewGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ModuleDetails demo = demos[clickIndex];
                    startActivity(new Intent(mContext.getApplicationContext(),
                            demo.activityClass));
                }
            });
            mModuleViewHolder.icon = (ImageView) mModuleViewGroup.findViewById(MainModuleItmeIds[0]);
            mModuleViewHolder.icon.setImageResource(modulePictures[i]);
            mModuleViewHolder.tittle = (TextView) mModuleViewGroup.findViewById(MainModuleItmeIds[1]);
            mModuleViewHolder.tittle.setText(moduleNames[i]);
            mModuleViewHolder.subtittle = (TextView) mModuleViewGroup.findViewById(MainModuleItmeIds[2]);
            mModuleViewHolder.subtittle.setText(moduleDetailNames[i]);

        }

    }

    public class ModuleArrayAdapter extends BaseAdapter {
        private Context ctx;
        private List<ModuleDetails> list;

        public ModuleArrayAdapter(Context context, List<ModuleDetails> poiList) {
            this.ctx = context;
            this.list = poiList;
        }

        public void setListData(List<ModuleDetails> poiList) {
            this.list = poiList;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(ctx, R.layout.main_navi_item, null);

                holder.icon = (ImageView) convertView.findViewById(R.id.title_picture_imageview);
                holder.tittle = (TextView) convertView
                        .findViewById(R.id.moduletitle);
                holder.subtittle = (TextView) convertView.findViewById(R.id.submoduletitle);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ModuleDetails item = list.get(position);
            holder.tittle.setText(moduleNames[position]);
            holder.subtittle.setText(moduleDetailNames[position]);
//            holder.icon.setImageDrawable;
            holder.icon.setImageResource(modulePictures[position]);
            return convertView;
        }

        private class ViewHolder {
            ImageView icon;
            TextView tittle;
            TextView subtittle;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        initResource();
        initView();


    }

    @Override
    protected void onResume() {
        super.onResume();
        updateConectionView(mHudConnectionManager.isConnected());
    }



    private void initResource() {
        moduleNames = this.getApplicationContext().getResources().getStringArray(R.array.main_modules);
        moduleDetailNames = this.getApplicationContext().getResources().getStringArray(R.array.main_modules_detail);
    }

    String mCurrentPhoneRequestIdStr;

    private void initHudConnectionView() {
        mHudConnectionManager = HudConnectionManager.getInstance(this.getApplicationContext());
        mHudConnectionManager.addConnecttionListener(new HudConnectionManager.ConnecttionListener() {
            @Override
            public void ConnectResult(boolean result) {
                updateConectionView(result);
                ConnectionToast(result);
            }
        });
        mHudConnectionManager.setConnectStrategy(HudConnectionManager.HudConnectionStrategy.HUD_CONNECTION_WIFI_FIRST);
        HudConnectionManager.IDataDispatcher dataDispatcher = new HudConnectionManager.IDataDispatcher() {
            @Override
            public void dispatchH2PData(Hud2PhoneMessages hud2PhoneMessages) {
                Log.i(TAG, "收到HUD Rsp");
                mCurrentPhoneRequestIdStr = mHudConnectionManager.getLastPhoneRequestIdStr();
                if (mCurrentPhoneRequestIdStr != null && hud2PhoneMessages != null) {

                    if (mCurrentPhoneRequestIdStr
                            .equals(Phone2HudMessagesUtils.getHudResponseSerialNumberStr(hud2PhoneMessages))) {
                        HudInfoDeclaim hudInfoDeclaim = hud2PhoneMessages.getHudInfoDeclaim();
                        String uniqueId = null;
                        if (hudInfoDeclaim != null) {
                            uniqueId = hudInfoDeclaim.getHudUniqueId();
                        }
                        //uniqueId 保存到本地，并上传到服务器
                        if (uniqueId != null) {

                            updateConectionView(mHudConnectionManager.isConnected());
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


        mHudColorBlockImageview = (ImageView) findViewById(R.id.main_color_block_imageview);
        mHudBackgroundImageView = (ImageView) findViewById(R.id.main_hud_background_Imageview);
        mHudconnectStatesImageview = (ImageView) findViewById(R.id.main_hud_connect_states_imageview);
        mConnectHudView = (TextView) findViewById(R.id.main_connect_hud_view);
        updateConectionView(mHudConnectionManager.isConnected());
        mConnectHudView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHudConnectionManager.isConnected()) {
                    mHudConnectionManager.connectServer();//EndpointDeviceInfo deviceInfo
//                    mProgressDialog = ProgressDialog.show(mContext, "请稍等...", "获取数据中...", true);
                    Toast.makeText(mContext, "正在请求连接HUD...", Toast.LENGTH_SHORT).show();
                } else {
                    mHudConnectionManager.disconnect();
                }

               /* if(mHudConnectionManager.isConnected()){
                    Log.i(TAG, "连接HUD成功");
                }else {
                    Log.i(TAG, "连接HUD不成功");
                }*/
            }
        });

        if (mHudConnectionManager.isConnected()) {
            Log.i(TAG, "打开APP自动连接HUD");
            mHudConnectionManager.connectServer();
        }
    }

    private void updateConectionView(boolean isConnected) {
        if (isConnected) {
            mConnectHudView.setText("已连接HUD");
            mHudColorBlockImageview.setVisibility(View.INVISIBLE);
            mHudBackgroundImageView.setBackgroundResource(R.drawable.main_hud_connected_backgroud);
            mHudconnectStatesImageview.setImageResource(R.drawable.main_hud_connected);
        } else {
            mConnectHudView.setText("点击连接HUD");
            mHudColorBlockImageview.setVisibility(View.VISIBLE);
            mHudBackgroundImageView.setBackgroundResource(R.drawable.main_hud_unconnected_backgroud);
            mHudconnectStatesImageview.setImageResource(R.drawable.main_hud_unconnected);
        }
    }

    private void ConnectionToast(boolean isConnected) {
        if (isConnected) {
            Toast.makeText(mContext, "HUD连接成功", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, "已断开连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void initModuleView() {
        mModuleListView = (ListView) findViewById(R.id.moduleListView);

        ModuleArrayAdapter adapter = new ModuleArrayAdapter(
                this.getApplicationContext(), Arrays.asList(demos));
        mModuleListView.setAdapter(adapter);
        mModuleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ModuleDetails demo = demos[position];
                startActivity(new Intent(mContext.getApplicationContext(),
                        demo.activityClass));
            }
        });
    }

    private void initView() {
        initModuleView();
        initIncludeMainItem();
        initHudConnectionView();

    }
}
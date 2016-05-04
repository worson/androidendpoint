package com.haloai.hud.androidendpoint.views.navi;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.navi.model.NaviInfo;
import com.amap.api.services.core.PoiItem;
import com.haloai.hud.androidendpoint.bean.HudAmapNaviInfo;
import com.haloai.hud.androidendpoint.controllers.navi.AMapNavigationController;
import com.haloai.hud.androidendpoint.R;
import com.haloai.hud.androidendpoint.controllers.navi.NavigationController;
import com.haloai.hud.androidendpoint.util.HudNaviInfoLab;
import com.haloai.hud.navigation.AMapNavigationSDKAdapter;
import com.haloai.hud.navigation.NavigationSDKAdapter;
import com.haloai.hud.androidendpoint.views.HudBaseActivity;

import java.util.LinkedList;
import java.util.List;

public class HudPoiTipsActivity extends HudBaseActivity {
    private final static String TAG = HudPoiTipsActivity.class.getName();
    private AMapNavigationSDKAdapter mNavigationSDKAdapter;

    private Context mContext;
    //VIEW
    private Toolbar mToolbar;
    private ActionBar mActionBar;
    private EditText mLocationText;
    private ListView mLocationListView;
    private ImageView clearInputView;
    private LinearLayout mSearchHelpPanel;
    private ImageView mModifyAddImageView;

    private TIPS_VIEW_STS mViewstate;

    //
    private String mTitle = "搜索目的地";

    private HudAmapNaviInfo hudAmapNaviInfo = HudNaviInfoLab.getHudAmapNaviInfo();

    private PoiListAdapter mPoiListAdapter;
    private List<PoiItem> mPoiList;

    private boolean mClickPoiListSearch = false;    //点击poi 搜索列表时置true
    private enum TIPS_VIEW_STS {
        DSP_COLLECT_VIEW,
        DSP_POI_LISTVIEW,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hud_poi_tips);

        mContext = this;
        initView();
        startAMapNaviContent();


    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mNavigationSDKAdapter.stopNavigate();
        HudNaviInfoLab.releaseHudAmapNaviInfo();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initNavigationSDKAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void startAMapNaviContent() {
        NavigationSDKAdapter
                .launchNavigationSDK(NavigationSDKAdapter.NAVIGATION_SDK_AMAP, getApplicationContext());
        NavigationController.launchNavigationSDK(NavigationSDKAdapter.NAVIGATION_SDK_AMAP, getApplicationContext());


    }

    private void initView() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mViewstate = TIPS_VIEW_STS.DSP_COLLECT_VIEW;
        mToolbar = (Toolbar) findViewById(R.id.common_tool_bar);
        TextView toolTitle = (TextView)findViewById(R.id.toolbar_title);
        toolTitle.setText(mTitle);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mLocationText = (EditText) findViewById(R.id.edittext_searchbox_search_input);
        mLocationText.addTextChangedListener(mLocationTextWatcher);

        mLocationListView = (ListView) findViewById(R.id.locationList);
        mLocationListView.setClickable(true);
        mPoiListAdapter = new PoiListAdapter(mContext, new LinkedList<PoiItem>());
        mLocationListView.setEmptyView(new View(mContext));
        mLocationListView.setAdapter(mPoiListAdapter);


        clearInputView = (ImageView) findViewById(R.id.iv_searchbox_search_clean);
        clearInputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationText.setText("");
            }
        });

        mSearchHelpPanel = (LinearLayout) findViewById(R.id.searchHelpPanel);

        mModifyAddImageView = (ImageView) findViewById(R.id.common_used_add_imageview);

        mModifyAddImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, HudAddCollectionActivity.class));
                mClickPoiListSearch = true;



//                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.fragment_container, newFragment);
//                transaction.addToBackStack(null);
//                transaction.commit();
            }
        });

    }

    private void switchView(TIPS_VIEW_STS sts) {
        mViewstate = sts;
    }

    /*
    * 只负责刷新界面，不进行选择切换
    * */
    private void updateSwitchViews() {
        if (mViewstate == TIPS_VIEW_STS.DSP_COLLECT_VIEW) {
            if (View.GONE == mSearchHelpPanel.getVisibility()) {
                Log.i(TAG, "重新加载快捷搜索View");
                //mLocationListView.removeAllViews(); //无效，崩溃
                mSearchHelpPanel.setVisibility(View.VISIBLE);

                //mPoiListAdapter.notifyDataSetInvalidated();
            }
            mLocationListView.removeAllViewsInLayout();//能用，但是会保留布局
            mPoiListAdapter.clearViews();
            if (View.GONE == mSearchHelpPanel.getVisibility()) {
                mLocationListView.setVisibility(View.GONE);
            }


        } else if (mViewstate == TIPS_VIEW_STS.DSP_POI_LISTVIEW) {
            if (View.GONE != mSearchHelpPanel.getVisibility()) {
                mSearchHelpPanel.setVisibility(View.GONE);
            }
            if (View.GONE == mLocationListView.getVisibility()) {
                mLocationListView.setVisibility(View.VISIBLE);
            }

        }
    }

    private void initNavigationSDKAdapter() {
//        mNavigationSDKAdapter = AMapNavigationController.getAMapNavigationSDKAdapter(this.getApplicationContext());
        mNavigationSDKAdapter = (AMapNavigationSDKAdapter) NavigationSDKAdapter
                .getCurrentNavigationAdapter();
        //TODO 空指针崩溃
        mNavigationSDKAdapter.setNaviNotifier(mNavigationNotifier);

    }


    private AdapterView.OnItemClickListener mPoiClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i(TAG, "pio列表点击响应");


            mClickPoiListSearch = true;
            PoiItem poiItem =  mPoiList.get(position);
//            AMapNavigationController.setLastClickPosition(position);
            hudAmapNaviInfo.setDestination(poiItem);
            String title = poiItem.getTitle();
            hudAmapNaviInfo.setSearchKeyword(title);
            mNavigationSDKAdapter.setLocationNameAndSearchPOI(title);
//            AMapNavigationController.setSearchKeyword(mLocationText.getText());

        }
    };


    private TextWatcher mLocationTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.i(TAG, "beforeTextChanged");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.i(TAG, "onTextChanged");
            if (s == null || s.toString() == null || s.toString().length() <= 0) {//count <= 0 ||
                switchView(TIPS_VIEW_STS.DSP_COLLECT_VIEW);
                updateSwitchViews();
                return;
            }
            String name = s.toString();
            if (name != null) {
                hudAmapNaviInfo.setSearchKeyword(name);
                Log.d(TAG, "输入了：" + name.toString() + "start=" + start + "before=" + before + "count=" + count);
                switchView(TIPS_VIEW_STS.DSP_POI_LISTVIEW);
                updateSwitchViews();
                if (count > 0) {
                    mLocationListView.removeAllViewsInLayout();
                }
                mNavigationSDKAdapter.setLocationNameAndSearchPOI(name);
            } else {

            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.i(TAG, "afterTextChanged");
        }
    };
    private NavigationSDKAdapter.NavigationNotifier mNavigationNotifier = new NavigationSDKAdapter.NavigationNotifier() {

        @Override
        public void onDetailPOIListData(String poiNameToSearch, List<PoiItem> poiList) {
            String searchName;
            for (int i = 0; i < poiList.size(); i++) {
                searchName = poiList.get(i).getAdName();
                if (searchName != null) {
//                    Log.i(TAG, "搜索到POI:" + searchName);
                }


            }
            //如果是点击进入搜索POI
            if (mClickPoiListSearch){
                mClickPoiListSearch=false;
                hudAmapNaviInfo.setPoiList(poiList);
                startActivity(new Intent(mContext.getApplicationContext(),
                        HudMapPointActivity.class));
                return;
            }
            mPoiList = poiList;
            //收藏视图不更新
            if (mViewstate != TIPS_VIEW_STS.DSP_POI_LISTVIEW) {
                return;
            }

            //更新Listview
            hudAmapNaviInfo.setPoiList(poiList);

            List<PoiItem> poiItems = poiList;

            mPoiListAdapter.setListData(poiItems);
//            mPoiListAdapter = new PoiListAdapter(mContext, poiItems);
            mPoiListAdapter.notifyDataSetChanged();
            mLocationListView.setOnItemClickListener(mPoiClickListener);

            if (poiItems.size() > 0) {
                Log.i(TAG, "更新ListView成功");
            } else {
                Log.e(TAG, "更新ListView失败");
            }


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

    public class PoiListAdapter extends BaseAdapter {
        private Context ctx;
        private List<PoiItem> list;

        public PoiListAdapter(Context context, List<PoiItem> poiList) {
            this.ctx = context;
            this.list = poiList;
        }

        public void setListData(List<PoiItem> poiList) {
            this.list = poiList;
        }

        public void clearViews() {
            if (list == null) {
                return;
            }
            if (list.size() > 0) {
                list.clear();
                notifyDataSetChanged();
            }
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
                convertView = View.inflate(ctx, R.layout.poi_listview_item, null);
//                convertView = ctx.getActivity().getLayoutInflater()
//                        .inflate(R.layout.list_item_crime, null);
                holder.poititle = (TextView) convertView
                        .findViewById(R.id.poititle);
                holder.subtitle = (TextView) convertView
                        .findViewById(R.id.subpoititle);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            PoiItem item = list.get(position);
            String keyword = hudAmapNaviInfo.getSearchKeyword();
            String title = item.getTitle();
            int startIndex = title.indexOf(keyword);
            if (startIndex != -1){
                SpannableStringBuilder spanTitle = new SpannableStringBuilder(title);
                int focusColor =  mContext.getApplicationContext().getResources().getColor(R.color.color_poi_listitem__title_focus);
                spanTitle.setSpan(new ForegroundColorSpan(focusColor),startIndex,startIndex+keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.poititle.setText(spanTitle);
            }else {
                holder.poititle.setText(title);
            }

//            title.indexOf();
            String subtitle = item.getCityName()+item.getAdName()+item.getSnippet();
            //TODO 显示具体地址
            holder.subtitle.setText(subtitle);

//            if (item.getSubPois().size() > 0) {
//                List<SubPoiItem> subPoiItems = item.getSubPois();
//                SubPoiAdapter subpoiAdapter=new SubPoiAdapter(ctx, subPoiItems);
//                holder.subpois.setAdapter(subpoiAdapter);
//            }


            return convertView;
        }

        private class ViewHolder {
            TextView poititle;
            TextView subtitle;
        }

    }
}

package com.haloai.hud.androidendpoint.views.navi;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.haloai.hud.androidendpoint.R;

/**
 * Created by wangshengxing on 16/4/17.
 */
public class HudNaviStartPopupWindow extends PopupWindow {
    private Button btn_cancel;
    private Button btnSendHud;
    private LinearLayout viewGroupSendHud;
    private Button btnSendCloud;
    private View mMenuView;

    public HudNaviStartPopupWindow(Activity context, View.OnClickListener itemsOnClick){
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.navi_start_popup_window, null);
        this.setContentView(mMenuView);

        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);

        viewGroupSendHud = (LinearLayout) mMenuView.findViewById(R.id.navi_start_popup_send_hud_vuewgroup);
        btnSendCloud = (Button) mMenuView.findViewById(R.id.navi_start_popup_send_cloud_textview);
        viewGroupSendHud.setOnClickListener(itemsOnClick);
        btnSendCloud.setOnClickListener(itemsOnClick);

        btn_cancel = (Button) mMenuView.findViewById(R.id.navi_start_popup_return_textview);
        //取消按钮
        btn_cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //销毁弹出框
                dismiss();
            }
        });

        mMenuView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.navi_start_popup_windwow).getTop();
                int y=(int) event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(y<height){
                        dismiss();
                    }
                }
                return true;
            }
        });
    }
}

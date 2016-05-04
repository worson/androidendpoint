package com.haloai.hud.androidendpoint.views.navi;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.haloai.hud.androidendpoint.R;
import com.haloai.hud.androidendpoint.views.MainActivity;

public class HudStartNaviActivity extends AppCompatActivity {

    private Context mContext;
    Button mBack2MainButton;

    private String mToolbarTitle = "发起导航";
    //VIEW
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hud_start_navi);
        mContext = this;
        initView();

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


    private void initView(){
        mBack2MainButton = (Button)findViewById(R.id.start_navi_back_main_button);
        mBack2MainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, MainActivity.class));
            }
        });
        initToolbar();
    }
}

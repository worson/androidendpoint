package com.haloai.hud.androidendpoint.views.navi;

import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.haloai.hud.androidendpoint.R;

public class HudAddCollectionActivity extends AppCompatActivity  implements HudAddCollectionFragment.OnFragmentInteractionListener{
    private final static String TAG = HudAddCollectionActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hud_add_collection);
        FragmentManager manager = getSupportFragmentManager();

        HudAddCollectionFragment newFragment = (HudAddCollectionFragment)manager.
                findFragmentById(R.id.add_collection_fragment_fragmentContainer);
        if (newFragment == null) {
            newFragment = HudAddCollectionFragment.newInstance("1","2");;
            manager.beginTransaction()
                    .add(R.id.add_collection_fragment_fragmentContainer, newFragment)
                    .commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

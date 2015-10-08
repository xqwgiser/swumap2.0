package com.example.xqw.swumap;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
/**
 * Created by xqw on 2015/9/22.
 */
public class OtherActivity extends Activity {
    int FRAGMENT_TAG=0;
    final static int ROUT_PLAN=1;
    final static int POI_SEARCH=2;
    final static int SETTING=3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity);
        int tag=getIntent().getIntExtra("fragment_tag",0);
        //fragment事务操作
        FragmentManager fragmentManager=getFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        switch (tag) {
            case ROUT_PLAN:
                fragmentTransaction.replace(R.id.id_content, new RoutePlan(), "RoutePlan");
                fragmentTransaction.commit();
                break;
            case POI_SEARCH:
                fragmentTransaction.replace(R.id.id_content,new POISearch(),"POI_search");
                fragmentTransaction.commit();
                break;
            case SETTING:
                fragmentTransaction.replace(R.id.id_content,new Setting(),"setting");
                fragmentTransaction.commit();
                break;
        }
    }

}

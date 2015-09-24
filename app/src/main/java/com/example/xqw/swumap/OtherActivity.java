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
    Fragment routPlan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity);
        //fragment事务操作
        FragmentManager fragmentManager=getFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.id_content, new RoutePlan(), "RoutePlan");
        fragmentTransaction.commit();
        routPlan=fragmentManager.findFragmentByTag("RoutePlan");
    }

}

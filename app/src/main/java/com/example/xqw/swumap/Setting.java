package com.example.xqw.swumap;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by xqw on 2015/9/30.
 */
public class Setting extends Fragment implements View.OnClickListener,CompoundButton.OnCheckedChangeListener {
    ImageButton Back;
    LinearLayout screenLight;
    Switch screenLightBtn;
    LinearLayout romate;
    Switch romateBtn;
    LinearLayout overlook;
    Switch overlookBtn;
    LinearLayout heatMap;
    LinearLayout positionRemind;
    SharedPreferences sharedPreferences;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.setting,container,false);
        Back=(ImageButton)view.findViewById(R.id.back);
        screenLight=(LinearLayout)view.findViewById(R.id.screen_light);
        screenLightBtn=(Switch)view.findViewById(R.id.screen_light_btn);
        romate=(LinearLayout)view.findViewById(R.id.romate);
        romateBtn=(Switch)view.findViewById(R.id.romate_btn);
        overlook=(LinearLayout)view.findViewById(R.id.overlook);
        overlookBtn=(Switch)view.findViewById(R.id.overlook_btn);
        heatMap=(LinearLayout)view.findViewById(R.id.heat_map);
        positionRemind=(LinearLayout)view.findViewById(R.id.position_remind);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Back.setOnClickListener(this);
        screenLight.setOnClickListener(this);
        romate.setOnClickListener(this);
        overlook.setOnClickListener(this);
        heatMap.setOnClickListener(this);
        positionRemind.setOnClickListener(this);
        screenLightBtn.setOnCheckedChangeListener(this);
        romateBtn.setOnCheckedChangeListener(this);
        overlookBtn.setOnCheckedChangeListener(this);
        sharedPreferences=getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        screenLightBtn.setChecked(sharedPreferences.getBoolean("screenlight",false));
        romateBtn.setChecked(sharedPreferences.getBoolean("romate",true));
        overlookBtn.setChecked(sharedPreferences.getBoolean("overlook",true));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                Intent intent=new Intent(getActivity(),MainActivity.class);
                startActivity(intent);
                break;
            case R.id.screen_light:
                Toast.makeText(getActivity(),"开启后保持屏幕常亮",Toast.LENGTH_SHORT).show();
                break;
            case R.id.romate:
                Toast.makeText(getActivity(),"双指旋转切换地图方向",Toast.LENGTH_SHORT).show();
                break;
            case R.id.overlook:
                Toast.makeText(getActivity(),"双指下滑切换3d视图",Toast.LENGTH_SHORT).show();
                break;
            case R.id.heat_map:
                break;
            case R.id.position_remind:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor =sharedPreferences.edit();
        switch (buttonView.getId()){
            case R.id.screen_light_btn:
                editor.putBoolean("screenlight",isChecked);
                editor.commit();
                break;
            case R.id.romate_btn:
                editor.putBoolean("romate",isChecked);
                editor.commit();
                break;
            case R.id.overlook_btn:
                editor.putBoolean("overlook",isChecked);
                editor.commit();
                break;


        }

    }
}

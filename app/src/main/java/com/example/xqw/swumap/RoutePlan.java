package com.example.xqw.swumap;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by xqw on 2015/9/22.
 */
public class RoutePlan extends Fragment implements View.OnClickListener,RadioGroup.OnCheckedChangeListener {
    ImageButton btnBack;
    RadioGroup chooseBtns;
    Button btnConfirm;
    MaterialEditText startPoint;
    MaterialEditText stopPoint;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.route_plan,container,false);
        btnBack=(ImageButton)view.findViewById(R.id.back);
        chooseBtns=(RadioGroup)view.findViewById(R.id.switch_btns);
        btnConfirm=(Button)view.findViewById(R.id.confirm);
        startPoint=(MaterialEditText)view.findViewById(R.id.start);
        stopPoint=(MaterialEditText)view.findViewById(R.id.end);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                Intent intent=new Intent(getActivity(),MainActivity.class);
                startActivity(intent);
                break;
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getCheckedRadioButtonId()){
            case R.id.btn_bus:
                Toast.makeText(getActivity(),"点击了公交",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_drive:
                Toast.makeText(getActivity(),"点击了驾驶",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_walk:
                Toast.makeText(getActivity(),"点击了步行",Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnBack.setOnClickListener(this);
        chooseBtns.setOnCheckedChangeListener(this);
    }
}

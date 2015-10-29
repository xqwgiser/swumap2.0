package com.example.xqw.swumap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

public class MainActivity extends Activity {
    MapView mMapView;                                        //创建地图容器
    BaiduMap baiduMap;                                      //创建百度地图实例
    public LocationClient mLocationClient;
    public MyLocationListenner myListener = new MyLocationListenner();//创建定位监听器
    private MyLocationConfiguration.LocationMode mCurrentMode;//创建当前定位模式
    BitmapDescriptor mCurrentMarker=null;
    ImageButton requestLocButton;//定位按钮
    boolean isFirstLoc = true;// 是否首次定位
    FloatingActionsMenu menuMultipleActions;//悬浮菜单
    BDLocation myLocation=null;
    MapStatus ms=null;
    MapStatusUpdate u=null;
    SharedPreferences sharedPreferences;
    UiSettings mUiSettings;
    PowerManager powerManager = null;
    PowerManager.WakeLock wakeLock = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//SDK初始化，建议放置于Application类中完成
        setContentView(R.layout.activity_main);
        //创建设置sharedpreferences,保存设置内容
        sharedPreferences=getSharedPreferences("setting", MODE_PRIVATE);
        //屏幕常亮设置
        powerManager = (PowerManager)this.getSystemService(this.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Lock");
        requestLocButton = (ImageButton) findViewById(R.id.locButton);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;//定位模式默认为普通
        //创建监听器，实现通过按钮切换定位模式
        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    //切换为跟随
                    case NORMAL:
                        requestLocButton.setImageResource(R.mipmap.main_icon_follow);
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        baiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                    //切换为普通
                    case COMPASS:
                        requestLocButton.setImageResource(R.mipmap.main_icon_location);
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        baiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                    //切换为罗盘
                    case FOLLOWING:
                        requestLocButton.setImageResource(R.mipmap.main_icon_compass);
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        baiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);
        //初始化地图组件
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);
        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        ms=new MapStatus.Builder(baiduMap.getMapStatus()).zoom(17).build();
        u = MapStatusUpdateFactory.newMapStatus(ms);
        baiduMap.animateMapStatus(u);
        initUISetting();//初始化UI设置
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(myListener);  //注册LocationClient的监听事件
        LocationClientOption option = new LocationClientOption();//创建定位配置
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        //setMyLocationConfigeration方法放在最后，因为其中所需参数需要预先实例化
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
        //悬浮菜单操作及其内部组件选择对话框的操作，可重构
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        //悬浮按钮之切换图层按钮操作
        FloatingActionButton switchAction=(FloatingActionButton)findViewById(R.id.action_switch);
        switchAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogPlus dialogPlus=DialogPlus.newDialog(MainActivity.this).setContentHolder(new ViewHolder(R.layout.radiogroup))
                        .setGravity(Gravity.CENTER).setCancelable(true).setInAnimation(com.orhanobut.dialogplus.R.anim.fade_in_center)
                        .setOutAnimation(com.orhanobut.dialogplus.R.anim.fade_out_center).create();
                dialogPlus.show();
                RadioGroup chooseMap=(RadioGroup)dialogPlus.getHolderView();
                chooseMap.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        switch (group.getCheckedRadioButtonId()) {
                            case R.id.map_plain:
                                baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                                ms = new MapStatus.Builder(baiduMap.getMapStatus()).overlook(0).build();
                                u = MapStatusUpdateFactory.newMapStatus(ms);
                                baiduMap.animateMapStatus(u);
                                break;
                            case R.id.map_3d:
                                ms = new MapStatus.Builder(baiduMap.getMapStatus()).overlook(-45).build();
                                u = MapStatusUpdateFactory.newMapStatus(ms);
                                baiduMap.animateMapStatus(u);
                                break;
                            case R.id.map_sat:
                                baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                                break;
                            default:
                                break;
                        }
                    }
                });
                menuMultipleActions.collapseImmediately();
            }


    });
        //悬浮按钮之路线查找功能，在此将其余需要新界面的功能统一置于OtherActivity类中，以fragment方式实现
        FloatingActionButton routePlanBtn=(FloatingActionButton)findViewById(R.id.action_search);
        routePlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,OtherActivity.class);
                intent.putExtra("location_data",myLocation);
                intent.putExtra("fragment_tag",OtherActivity.ROUT_PLAN);
                startActivity(intent);

            }
        });
        FloatingActionButton POISearch=(FloatingActionButton)findViewById(R.id.action_nearby);
        POISearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,OtherActivity.class);
                intent.putExtra("location_data",myLocation);
                intent.putExtra("fragment_tag",OtherActivity.POI_SEARCH);
                startActivity(intent);
            }
        });
        FloatingActionButton Mine=(FloatingActionButton)findViewById(R.id.action_setting);
        Mine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OtherActivity.class);
                intent.putExtra("fragment_tag", OtherActivity.SETTING);
                startActivity(intent);
            }
        });

    }
    private void initUISetting(){
        mUiSettings=baiduMap.getUiSettings();
        mUiSettings.setRotateGesturesEnabled(sharedPreferences.getBoolean("romate",true));
        mUiSettings.setOverlookingGesturesEnabled(sharedPreferences.getBoolean("overlook",true));
        //是否开启热力图
        baiduMap.setBaiduHeatMapEnabled(sharedPreferences.getBoolean("heatmap",false));
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        // 关闭定位图层
        baiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        //判断屏幕是否设置常亮
        if(sharedPreferences.getBoolean("screenlight",false)){
            wakeLock.acquire();
        }
        initUISetting();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
        if(sharedPreferences.getBoolean("screenlight",false)){
            wakeLock.release();
        }
    }

    @Override
    public void onBackPressed() {
        if(menuMultipleActions.isExpanded()){
            menuMultipleActions.collapse();
        }else
        super.onBackPressed();
    }

    public class MyLocationListenner implements BDLocationListener {
        //创建MyLocationListenner类实现BDLocationListener接口，处理回调数据
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不再处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                baiduMap.animateMapStatus(u);
            }
            myLocation=location;
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}

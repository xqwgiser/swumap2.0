package com.example.xqw.swumap;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by xqw on 2015/9/22.
 */
public class RoutePlan extends Fragment implements View.OnClickListener,RadioGroup.OnCheckedChangeListener,BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener {
    ImageButton btnBack;
    RadioGroup chooseBtns;
    Button btnConfirm;
    MaterialEditText startPoint;
    MaterialEditText stopPoint;
    PlanNode stNode=null;//起点
    PlanNode enNode=null;//终点
    Button mBtnPre = null;//上一个节点
    Button mBtnNext = null;//下一个节点
    int nodeIndex = -1;//节点索引,供浏览节点时使用
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    private TextView popupText = null;//提示view
    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;
    //搜索相关
    RoutePlanSearch mSearch = null;
    BDLocation myLocation;
    int METHOD=1;//规划路线行进方法，默认使用公交
    final int BUS=1;
    final int DRIVE=2;
    final int WALK=3;
    boolean useDefaultIcon = true;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.route_plan,container,false);
        btnBack=(ImageButton)view.findViewById(R.id.back);
        chooseBtns=(RadioGroup)view.findViewById(R.id.switch_btns);
        btnConfirm=(Button)view.findViewById(R.id.confirm);
        startPoint=(MaterialEditText)view.findViewById(R.id.start);
        stopPoint=(MaterialEditText)view.findViewById(R.id.end);
        //初始化地图
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.showZoomControls(false);
        mBaidumap = mMapView.getMap();
        mBaidumap.setMyLocationEnabled(true);
        mBtnPre = (Button) view.findViewById(R.id.pre);
        mBtnNext = (Button) view.findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                Intent intent=new Intent(getActivity(),MainActivity.class);
                startActivity(intent);
                break;
            case R.id.confirm:
                route = null;
                mBtnPre.setVisibility(View.INVISIBLE);
                mBtnNext.setVisibility(View.INVISIBLE);
                mBaidumap.clear();
                // 处理搜索按钮响应
                stNode=PlanNode.withCityNameAndPlaceName("重庆","重庆"+startPoint.getText().toString());
                enNode=PlanNode.withCityNameAndPlaceName("重庆","重庆"+stopPoint.getText().toString());
                if(stNode==null||myLocation!=null){
                    LatLng ll = new LatLng(myLocation.getLatitude(),
                            myLocation.getLongitude());
                    stNode=PlanNode.withLocation(ll);
                }
                switch (METHOD){
                    case DRIVE:
                        mSearch.drivingSearch((new DrivingRoutePlanOption())
                                .from(stNode)
                                .to(enNode));
                        break;
                    case BUS:
                        mSearch.transitSearch((new TransitRoutePlanOption())
                                .from(stNode)
                                .city("重庆")
                                .to(enNode));
                        break;
                    case WALK:
                        mSearch.walkingSearch((new WalkingRoutePlanOption())
                                .from(stNode)
                                .to(enNode));
                        break;
                }
                break;
            case R.id.pre:
            case R.id.next:
                if (route == null ||
                        route.getAllStep() == null) {
                    return;
                }
                if (nodeIndex == -1 && v.getId() == R.id.pre) {
                    return;
                }
                //设置节点索引
                if (v.getId() == R.id.next) {
                    if (nodeIndex < route.getAllStep().size() - 1) {
                        nodeIndex++;
                    } else {
                        return;
                    }
                } else if (v.getId() == R.id.pre) {
                    if (nodeIndex > 0) {
                        nodeIndex--;
                    } else {
                        return;
                    }

                }
                //获取节结果信息
                LatLng nodeLocation = null;
                String nodeTitle = null;
                Object step = route.getAllStep().get(nodeIndex);
                if (step instanceof DrivingRouteLine.DrivingStep) {
                    nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
                    nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
                } else if (step instanceof WalkingRouteLine.WalkingStep) {
                    nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
                    nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
                } else if (step instanceof TransitRouteLine.TransitStep) {
                    nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
                    nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
                }

                if (nodeLocation == null || nodeTitle == null) {
                    return;
                }
                //移动节点至中心
                mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
                // show popup
                popupText = new TextView(getActivity());
                popupText.setBackgroundResource(R.drawable.popup);
                popupText.setTextColor(0xFF000000);
                popupText.setText(nodeTitle);
                mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));

                break;
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (group.getCheckedRadioButtonId()){
            case R.id.btn_bus:
                METHOD=BUS;
                break;
            case R.id.btn_drive:
                METHOD=DRIVE;
                break;
            case R.id.btn_walk:
                METHOD=WALK;
                break;
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        btnBack.setOnClickListener(this);
        chooseBtns.setOnCheckedChangeListener(this);
        btnConfirm.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnPre.setOnClickListener(this);
        myLocation=(BDLocation)getActivity().getIntent().getParcelableExtra("location_data");
        if(myLocation!=null){
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(myLocation.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(myLocation.getLatitude())
                    .longitude(myLocation.getLongitude()).build();
            mBaidumap.setMyLocationData(locData);
            LatLng ll = new LatLng(myLocation.getLatitude(),
                    myLocation.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaidumap.animateMapStatus(u);
        }
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            Toast.makeText(getActivity(), "检索词", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.KEY_ERROR) {
            Toast.makeText(getActivity(), "KEY", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.NETWORK_ERROR) {
            Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.NETWORK_TIME_OUT) {
            Toast.makeText(getActivity(), "网络超时", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.NOT_SUPPORT_BUS) {
            Toast.makeText(getActivity(), "不支持", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.PERMISSION_UNFINISHED) {
            Toast.makeText(getActivity(), "授权未完成", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(getActivity(), "没有结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.ST_EN_TOO_NEAR) {
            Toast.makeText(getActivity(), "太近", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
            routeOverlay = overlay;
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }
    //定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_track_navi_end);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_track_navi_start);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_track_navi_end);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_track_navi_start);
            }
            return null;
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_track_navi_end);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_track_navi_start);
            }
            return null;
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        mBaidumap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mSearch.destroy();
        mMapView.onDestroy();
        super.onDestroy();
    }
}

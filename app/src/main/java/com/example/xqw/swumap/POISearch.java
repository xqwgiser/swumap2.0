package com.example.xqw.swumap;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by xqw on 2015/9/25.
 */
public class POISearch extends Fragment implements View.OnClickListener,OnGetPoiSearchResultListener {
    ImageButton btnBack;
    MaterialEditText btnContainer;
    Button btnSearch;
    PoiSearch mPoiSearch = null;
    MapView mapView=null;
    BaiduMap mBaiduMap = null;
    MapStatus ms=null;
    MapStatusUpdate u=null;
    int load_Index = 0;
    BDLocation myLocation;
    TextView POIName;
    TextView POIAddress;
    TextView POIPhone;
    TextView POIRating;
    TextView POITag;
    TextView POITime;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.poi_search,container,false);
        btnBack=(ImageButton)view.findViewById(R.id.back);
        btnContainer=(MaterialEditText)view.findViewById(R.id.ads_poi);
        btnSearch=(Button)view.findViewById(R.id.search);
        mapView=(MapView)view.findViewById(R.id.map);
        mapView.showZoomControls(false);
        POIName=(TextView)view.findViewById(R.id.POI_title);
        POIAddress=(TextView)view.findViewById(R.id.POI_address);
        POIPhone=(TextView)view.findViewById(R.id.POI_phonenumber);
        POIRating=(TextView)view.findViewById(R.id.POI_ratingnumber);
        POITag=(TextView)view.findViewById(R.id.POI_tag);
        POITime=(TextView)view.findViewById(R.id.POI_time);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnBack.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        mBaiduMap=mapView.getMap();
        ms=new MapStatus.Builder(mBaiduMap.getMapStatus()).zoom(17).build();
        u = MapStatusUpdateFactory.newMapStatus(ms);
        mBaiduMap.animateMapStatus(u);
        mBaiduMap.setMyLocationEnabled(true);
        myLocation=(BDLocation)getActivity().getIntent().getParcelableExtra("location_data");
        if(myLocation!=null) {
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(myLocation.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(myLocation.getLatitude())
                    .longitude(myLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            LatLng ll = new LatLng(myLocation.getLatitude(),
                    myLocation.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                Intent intent=new Intent(getActivity(),MainActivity.class);
                startActivity(intent);
                break;
            case R.id.search:
                InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                nearbySearch();
                break;
        }
    }
    //附近搜索方法
    private void nearbySearch(){
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
        //设置地理位置
        nearbySearchOption.location(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        nearbySearchOption.keyword(btnContainer.getText().toString());
        nearbySearchOption.radius(2000);// 检索半径，单位是米
        nearbySearchOption.pageNum(1);//搜索结果分页
        mPoiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null
                || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(getActivity(), "未找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(getActivity(), strInfo, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        } else {
            POIName.setText(result.getName());
            POIAddress.setText(result.getAddress());
            POIPhone.setText(result.getTelephone());
            POIRating.setText(Double.toString(result.getOverallRating()));
            POITag.setText(result.getTag());
            POITime.setText(result.getShopHours());
        }
    }

    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                    .poiUid(poi.uid));
            return true;
        }
    }

    @Override
    public void onDestroy() {
        mPoiSearch.destroy();
        super.onDestroy();
    }
}

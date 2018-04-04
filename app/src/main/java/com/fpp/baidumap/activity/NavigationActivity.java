package com.fpp.baidumap.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.fpp.baidumap.R;


/**
 * @author fupengpeng
 * @description 描述
 * @data 2018/3/15 0015 9:03
 */

public class NavigationActivity extends Activity
//        implements BaiduMap.OnMapClickListener
//        , OnGetRoutePlanResultListener
{
    // 浏览路线节点相关

    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    RouteLine route = null;
    private String loaclcity = null;
    private Button requestLocButton, go;
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private TextView popupText = null, driver_city; // 泡泡view

    boolean isFirstLoc = true; // 是否首次定位

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    // 地图控件
    private TextureMapView mMapView = null;
    private BaiduMap mBaidumap;
    // 搜索相关
//    RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private Marker marker;
    private RelativeLayout mRlPoiMessage;
    private TextView mTvPoiName;
    private TextView mTvPoiAddress;
    private TextView mTvPoiDistance;
    private TextView mTvPoiGoToHere;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 状态栏标识颜色变黑
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext
        // 注意该方法要再setContentView方法之前实现
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置标题栏不可用
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_navigation);
        requestLocButton = (Button) findViewById(R.id.driver_change);

        mCurrentMode = LocationMode.COMPASS;
        requestLocButton.setText("罗盘");
        requestLocButton.setOnClickListener(btnClickListener);

        // 初始化地图
        inintmap();
        initview();
        initEvent();

    }

    private void initEvent() {
        // TODO: 2018/3/27 0027 地图单击事件
        mBaidumap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            /**
             * 地图单击事件回调函数
             * @param point 点击的地理坐标
             */
            public void onMapClick(LatLng point) {
                Log.e("onMapClick", "获取到点击坐标 = " + point);
            }

            /**
             * 地图内 Poi 单击事件回调函数
             * @param poi 点击的 poi 信息
             */
            public boolean onMapPoiClick(MapPoi poi) {
                Log.e("onMapPoiClick", " name = " + poi.getName()
                        + "   uid = " + poi.getUid() + "   latlng = " + poi.getPosition());
                mBaidumap.clear();
                // 添加地图标点
                // 定义Maker坐标点
                LatLng point0 = poi.getPosition();
                OverlayOptions ooa = new MarkerOptions()
                        .position(point0)         // 位置
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_green))  // 图标
                        .zIndex(1)        // 设置Marker所在层级
                        .draggable(true)  // 是否可拖拽
                        .alpha(1.0f)      // 透明度
                        .perspective(true)  //   是否开启近大远小效果
                        .visible(true)      // 是否显示
                        .title(poi.getName())  // 标题
                        .animateType(MarkerOptions.MarkerAnimateType.grow);  // 添加冒出动画

                marker = (Marker) mBaidumap.addOverlay(ooa);
                // 地图标注物信息设置
                if (mRlPoiMessage.getVisibility() == View.GONE){
                    mRlPoiMessage.setVisibility(View.VISIBLE);
                }

                mTvPoiName.setText(poi.getName());
                // TODO: 2018/4/4 0004 点击地图标识物显示其信息。 

                return true;
            }
        });

    }

    public void inintmap() {
        // 地图初始化
        mMapView = (TextureMapView) findViewById(R.id.driver_mTexturemap);
        mBaidumap = mMapView.getMap();

        // 定位相关
        locationCorrelation();

    }

    private void locationCorrelation() {

        // 开启定位图层
        mBaidumap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        // 定位监听
        mLocClient.registerLocationListener(myListener);
        // 定位参数配置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
        option.setIsNeedLocationPoiList(true);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        // 设置定位参数
        mLocClient.setLocOption(option);
        // 开启定位
        mLocClient.start();

    }

    public void initview() {
        driver_city = (TextView) findViewById(R.id.driver_city);
        mRlPoiMessage = (RelativeLayout) findViewById(R.id.rl_poi_message);
        mTvPoiName = (TextView) findViewById(R.id.tv_poi_name);
        mTvPoiAddress = (TextView) findViewById(R.id.tv_poi_address);
        mTvPoiDistance = (TextView) findViewById(R.id.tv_poi_distance);
        mTvPoiGoToHere = (TextView) findViewById(R.id.tv_poi_gotohere);
        mTvPoiGoToHere.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2018/4/4 0004 点击进行路径设置
            }
        });
    }



    /**
     * 切换路线图标，刷新地图使其生效 注意： 起终点图标使用中心对齐.
     */

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }




    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    OnClickListener btnClickListener = new OnClickListener() {
        public void onClick(View v) {
            switch (mCurrentMode) {
                case NORMAL:
                    requestLocButton.setText("跟随");
                    mCurrentMode = LocationMode.FOLLOWING;
                    mBaidumap.setMyLocationConfigeration(
                            new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
                    break;
                case COMPASS:
                    requestLocButton.setText("普通");
                    mCurrentMode = LocationMode.NORMAL;
                    mBaidumap.setMyLocationConfigeration(
                            new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
                    break;
                case FOLLOWING:
                    requestLocButton.setText("罗盘");
                    mCurrentMode = LocationMode.COMPASS;
                    mBaidumap.setMyLocationConfigeration(
                            new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            mBaidumap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaidumap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                Toast.makeText(NavigationActivity.this,"当前所在位置：" + location.getAddrStr(),Toast.LENGTH_LONG).show();
                driver_city.setText(location.getCity());
                loaclcity = location.getCity();
            }
        }


    }

}

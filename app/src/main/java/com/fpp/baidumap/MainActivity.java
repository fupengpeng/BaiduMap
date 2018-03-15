package com.fpp.baidumap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.fpp.baidumap.activity.NavigationActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements BDLocationListener {

    private static final String TAG = "MainActivity";



    @BindView(R.id.tv_atvt_main_common)
    CheckBox tvAtvtMainCommon;
    @BindView(R.id.tv_atvt_main_satellite)
    CheckBox tvAtvtMainSatellite;
    @BindView(R.id.tv_atvt_main_empty)
    CheckBox tvAtvtMainEmpty;
    @BindView(R.id.tv_atvt_main_traffic)
    CheckBox tvAtvtMainTraffic;
    @BindView(R.id.tv_atvt_main_heating_power)
    CheckBox tvAtvtMainHeatingPower;


    @BindView(R.id.tv_atvt_main_normal)
    CheckBox tvAtvtMainNormal;
    @BindView(R.id.tv_atvt_main_following)
    CheckBox tvAtvtMainFollowing;
    @BindView(R.id.tv_atvt_main_compass)
    CheckBox tvAtvtMainCompass;
    @BindView(R.id.tv_atvt_main_traffic1)
    CheckBox tvAtvtMainTraffic1;
    @BindView(R.id.tv_atvt_main_heating_power1)
    TextView tvAtvtMainHeatingPower1;

    private TextureMapView mMapView = null;
    /**
     * 地图控制器对象
     */
    private BaiduMap mBaiduMap;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private BitmapDescriptor mCurrentMarker;
    private BDLocation location;


    // 定位相关
    LocationClient mLocClient;
    // 定位监听
    MyLocationListener myLocListener = new MyLocationListener();
    // 定位参数
    private LocationClientOption option;
    /**
     * 经纬度
     */
    private double latitude;
    private double longitude;

    private boolean isFirstLoc = true;
    //
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现，一般情况下放置在application中
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // 初始化View
        initView();
        // LocationClientOption 配置
        initOption();
        // 初始化地图及相关设置
        initMap();

//        initMap();

//        locationView();
    }

    private void initMap() {

        //获取地图控制器对象
        mBaiduMap = mMapView.getMap();

        // 设置地图图层
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
        // 设置定位跟随状态
        mBaiduMap.setMyLocationConfigeration(
                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker));

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        // 定位监听
        mLocClient.registerLocationListener(myLocListener);

        // 设置定位参数
        mLocClient.setLocOption(option);
        // 开启定位
        mLocClient.start();


    }



    private void initOption() {
        // 定位参数配置
        option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
        option.setIsNeedLocationPoiList(true);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
    }


    private void initView() {
        //获取地图控件引用
        mMapView = (TextureMapView) findViewById(R.id.bmapView);
        Log.e(TAG, "onViewClicked: 点击定位");
        tvAtvtMainCommon.setOnCheckedChangeListener(onCheckedChangeListener);
        tvAtvtMainSatellite.setOnCheckedChangeListener(onCheckedChangeListener);
        tvAtvtMainEmpty.setOnCheckedChangeListener(onCheckedChangeListener);
        tvAtvtMainTraffic.setOnCheckedChangeListener(onCheckedChangeListener);
        tvAtvtMainHeatingPower.setOnCheckedChangeListener(onCheckedChangeListener);
        tvAtvtMainNormal.setOnCheckedChangeListener(onCheckedChangeListener);
        tvAtvtMainFollowing.setOnCheckedChangeListener(onCheckedChangeListener);
        tvAtvtMainCompass.setOnCheckedChangeListener(onCheckedChangeListener);
        tvAtvtMainTraffic1.setOnCheckedChangeListener(onCheckedChangeListener);


    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.tv_atvt_main_common:
                    // 是否展示2D地图
                    if (isChecked) {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    } else {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
                    }
                    break;
                case R.id.tv_atvt_main_satellite:
                    // 是否展示卫星图
                    if (isChecked) {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    } else {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    }
                    break;
                case R.id.tv_atvt_main_empty:
                    // 是否展示交通图
                    if (isChecked) {
                        mBaiduMap.setTrafficEnabled(true);
                    } else {
                        mBaiduMap.setTrafficEnabled(false);
                    }
                    break;
                case R.id.tv_atvt_main_traffic:
                    // 是否展示热力图
                    if (isChecked) {
                        mBaiduMap.setBaiduHeatMapEnabled(true);
                    } else {
                        mBaiduMap.setBaiduHeatMapEnabled(false);
                    }
                    break;
                case R.id.tv_atvt_main_heating_power:

                    break;
                case R.id.tv_atvt_main_normal:
                    // 是否跟随定位
                    if (isChecked) {
                        mBaiduMap.setMyLocationConfigeration(
                                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, mCurrentMarker));

                    } else {
                        mBaiduMap.setMyLocationConfigeration(
                                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker));
                    }

                    break;
                case R.id.tv_atvt_main_following:
                    // 是否罗盘定位
                    if (isChecked) {

                        mBaiduMap.setMyLocationConfigeration(
                                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, mCurrentMarker));
                    } else {
                        mBaiduMap.setMyLocationConfigeration(
                                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker));

                    }
                    break;
                case R.id.tv_atvt_main_compass:

                    break;
                case R.id.tv_atvt_main_traffic1:

                    break;

            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }



    @OnClick({ R.id.tv_atvt_main_heating_power1})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_atvt_main_heating_power1:
                Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                startActivity(intent);
                break;
        }
    }




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
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                // 经纬度获取
                latitude = location.getLatitude();
                longitude = location.getLongitude();


                LatLng ll = new LatLng(latitude, longitude);
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                Toast.makeText(MainActivity.this, "当前所在位置：" + location.getAddrStr(), Toast.LENGTH_LONG).show();
                tvAtvtMainHeatingPower1.setText(location.getCity());
            }
        }


    }


    private void initM() {
        //获取地图控制器对象
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocationClient = new LocationClient(this);
        // 设定定位状态
        locationState();
        // 定位参数配置
        locationConfig();
        // 设置定位参数
        mLocationClient.setLocOption(option);
        // 定位监听
        locationListener();
        // 开启定位
        mLocationClient.start();

        // 构造定位数据
//        MyLocationData locData = new MyLocationData.Builder()
//                .accuracy(location.getRadius())
//                // 此处设置开发者获取到的方向信息，顺时针0-360
//                .direction(100).latitude(location.getLatitude())
//                .longitude(location.getLongitude()).build();
//        // 设置定位数据
//        mBaiduMap.setMyLocationData(locData);

        // 当不需要定位图层时关闭定位图层
//                mBaiduMap.setMyLocationEnabled(false);


    }

    private void locationListener() {
        // 定位监听
        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                // 获取精度纬度
                latitude = bdLocation.getLatitude();
                longitude = bdLocation.getLongitude();

                LatLng ll = new LatLng(latitude, longitude);
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//地图移动到当前经纬                  度


                //事情做完了关闭定位
                mLocationClient.stop();

            }
        });
    }

    private void locationState() {
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//        mCurrentMarker = BitmapDescriptorFactory
//                .fromResource(R.drawable.ic_gps_not_fixed_black_24dp);
        //定位跟随态
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        final MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
        mBaiduMap.setMyLocationConfiguration(config);

    }


    private void locationView() {

        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.ic_gps_not_fixed_black_24dp);
        int accuracyCircleFillColor = 0xAAFFFF88;//自定义精度圈填充颜色
        int accuracyCircleStrokeColor = 0xAA00FF00;//自定义精度圈边框颜色
        mBaiduMap.setMyLocationConfiguration(new
                MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker,
                accuracyCircleFillColor, accuracyCircleStrokeColor));


        /*

        用于设置定位的属性，包括定位模式、是否开启方向、设置自定义定位图标、精度圈填充颜色，精度圈边框颜色。更详细信息，请检索类参考。


        1.定位模式
            地图SDK支持三种定位模式：NORMAL（普通态）, FOLLOWING（跟随态）, COMPASS（罗盘态）

        mCurrentMode = LocationMode.FOLLOWING;//定位跟随态
        mCurrentMode = LocationMode.NORMAL;   //默认为 LocationMode.NORMAL 普通态
        mCurrentMode = LocationMode.COMPASS;  //定位罗盘态

        2.自定义定位图标
            支持自定义定位图标样式，
            替换定位icon
                mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
            自定义精度圈填充颜色
                accuracyCircleFillColor = 0xAAFFFF88;//自定义精度圈填充颜色
            自定义精度圈边框颜色
                accuracyCircleStrokeColor = 0xAA00FF00;//自定义精度圈边框颜色
            定位精度圈大小
            定位精度圈大小 ，是根据当前定位精度自动控制的，无法手动控制大小。精度圈越小，代表当前定位精度越高；反之圈越大，代表当前定位精度越低。

            定位指针方向
            定位指针朝向，是通过获取手机系统陀螺仪数据，控制定位指针的方向，需要开发者自己实现，并不在地图实现范畴。

            在定义了以上属性之后，需要通过下面方法设置：

            mBaiduMap.setMyLocationConfiguration(new
                MyLocationConfiguration(
                mCurrentMode, true,mCurrentMarker,
                accuracyCircleFillColor, accuracyCircleStrokeColor));
            */


    }

    public void initLocationClient() {

    }

    public void initLocationClientOption() {

    }


    private void locationConfig() {

        // 定位参数配置
        option = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd09ll");
        int span = 1000;
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才有效的
        option.setScanSpan(span);

        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.            getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiLi            st里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是            否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置高精度定位定位模式


    }

    private GeoCoder mSearch;

    //通过地址获取经纬度
    public void checkAddPosition() {

        if (mSearch == null) {
            mSearch = GeoCoder.newInstance();
            mSearch.setOnGetGeoCodeResultListener(listener);
        }
        mSearch.geocode(new GeoCodeOption()
                .city("XX")
                .address("xx村"));

    }

    //计算地理的 编码与反编码
    OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
        public void onGetGeoCodeResult(GeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有检索到结果
                Toast.makeText(MainActivity.this, "当前城市无法进行定位！", Toast.LENGTH_LONG).show();
            } else {

                //获取地理编码结果
                LatLng location = result.getLocation();
                double latitude1 = location.latitude;
                double longitude1 = location.longitude;
                getLine(latitude, longitude, latitude1, longitude1);
                Log.i("TAG", "latitude1:" + latitude1 + "  longitude1" + longitude1);
            }

        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有找到检索结果
            }
            //获取反向地理编码结果
        }
    };

    public void getLine(double latitude, double longitude, double latitude1, double longitude1) {
        //------------添加覆盖物--d--------
        BitmapDescriptor bdA = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_openmap_focuse_mark);
        BitmapDescriptor bdB = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_openmap_mark);
        LatLng llA = new LatLng(latitude, longitude);
        LatLng llB = new LatLng(latitude1, longitude1);
        MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA)
                .zIndex(9).draggable(true);
        Marker mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
        MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdB)
                .zIndex(5);
        Marker mMarkerB = (Marker) (mBaiduMap.addOverlay(ooB));
        //--------------------处理连线------------------
        Log.i("TAG", "latitude=" + latitude + "  longitude" + longitude);
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(llA);
        points.add(llB);
        OverlayOptions ooPolyline = new PolylineOptions().width(5)
                .color(0xAAFF0000).points(points);
        mBaiduMap.addOverlay(ooPolyline);

        //----------------
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);
        if (isFirstLoc) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(ll, 16);//设置地图中心及缩放级别
            mBaiduMap.animateMapStatus(update);
            isFirstLoc = false;
            Toast.makeText(getApplicationContext(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
        }
    }


    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


}

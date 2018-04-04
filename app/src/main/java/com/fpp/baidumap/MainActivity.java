package com.fpp.baidumap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiAddrInfo;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorInfo;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.fpp.baidumap.activity.NavigationActivity;
import com.fpp.baidumap.activity.OverlayDemoActivity;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.paddingBottom;
import static android.R.attr.paddingRight;
import static android.R.attr.paddingTop;

public class MainActivity extends AppCompatActivity
//        implements BDLocationListener
{

    private static final String TAG = "MainActivity";
    @BindView(R.id.cb_atvt_main_common)
    CheckBox cbAtvtMainCommon;
    @BindView(R.id.cb_atvt_main_satellite)
    CheckBox cbAtvtMainSatellite;
    @BindView(R.id.cb_atvt_main_empty)
    CheckBox cbAtvtMainEmpty;
    @BindView(R.id.cb_atvt_main_traffic)
    CheckBox cbAtvtMainTraffic;
    @BindView(R.id.tv_atvt_main_heating_power)
    TextView tvAtvtMainHeatingPower;
    @BindView(R.id.cb_atvt_main_normal)
    CheckBox cbAtvtMainNormal;
    @BindView(R.id.cb_atvt_main_following)
    CheckBox cbAtvtMainFollowing;
    @BindView(R.id.cb_atvt_main_compass)
    CheckBox cbAtvtMainCompass;
    @BindView(R.id.tv_atvt_main_traffic1)
    TextView tvAtvtMainTraffic1;
    @BindView(R.id.tv_atvt_main_one)
    TextView tvAtvtMainOne;
    @BindView(R.id.tv_atvt_main_two)
    TextView tvAtvtMainTwo;
    @BindView(R.id.tv_atvt_main_three)
    TextView tvAtvtMainThree;
    @BindView(R.id.tv_atvt_main_four)
    TextView tvAtvtMainFour;
    @BindView(R.id.tv_atvt_main_heating_five)
    TextView tvAtvtMainHeatingFive;
    @BindView(R.id.bmapView)
    TextureMapView bmapView;
    @BindView(R.id.iv_atvt_main_location)
    ImageView ivAtvtMainLocation;
    @BindView(R.id.cb_atvt_main_location)
    CheckBox cbAtvtMainLocation;
    @BindView(R.id.iv_atvt_main_assign_location)
    ImageView ivAtvtMainAssignLocation;
    @BindView(R.id.et_atvt_main_city)
    EditText etAtvtMainCity;
    @BindView(R.id.et_atvt_main_text)
    EditText etAtvtMainText;
    @BindView(R.id.tv_atvt_main_search)
    TextView tvAtvtMainSearch;


    private TextureMapView mMapView = null;
    /**
     * 地图控制器对象
     */
    private BaiduMap mBaiduMap;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private BitmapDescriptor mCurrentMarker;


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

    private ImageView mIvAtvtMain;
    private MapStatus.Builder builder;
    private LatLng latLng;
    private LatLng latLngLocation;
    private Marker marker;
    private ReverseGeoCodeOption reverseGeoCodeOption;
    private OnGetGeoCoderResultListener onGetGeoCoderResultListener;


    private boolean mPopupWindowState = false;
    private static final int MAP_STATE_ALL = 1;
    private static final int MAP_STATE_NORMAL = 2;
    private static final int MAP_STATE_POPUP_WINDOW_SHOU = 3;
    private PoiSearch mPoiSearch;
    private OnGetPoiSearchResultListener poiListener;
    private SuggestionSearch mSuggestionSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现，一般情况下放置在application中
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        // 状态栏标识颜色变黑，布局填充状态栏，设置透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;  // 状态栏标识颜色变黑
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);//透明状态栏
        }

        // 初始化View
        initView();
        // 显示地图
        showMap();
        // 定位相关（定位到当前位置）
        locationMap();
        // 室内地图相关
        indoorMap();
        // 地图上绘制点标记
        dotMark();
        // 地图事件
        mapEvent();
        // 地图上绘制线
        drawLine();
        // 地图上绘制面
        drawPlane();
        // 地图上绘制文字
        addText();
        // 地图poi检索配置
        poiSearchSet();

    }

    // 地图poi检索配置
    private void poiSearchSet(){
        // 1.创建POI检索实例
        mPoiSearch = PoiSearch.newInstance();

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();

        // 2.创建POI检索监听者；
        //获取POI检索结果

        //获取Place详情页检索结果
        poiListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                //获取POI检索结果
                Log.e("onGetPoiResult", "POI检索结果  poiResult = " + poiResult);

                List<PoiInfo> poiInfoList = poiResult.getAllPoi();
                Log.e("onGetPoiResult", "POI检索结果  poiInfoList = " + poiInfoList);
                if (poiInfoList != null && poiInfoList.size() > 0) {
                    PoiInfo poiinfo = poiInfoList.get(0);
                    Log.e("onGetPoiResult", "POI检索结果  describeContents = " + poiinfo.describeContents() +
                            "  name =  " + poiinfo.name + "  address = " + poiinfo.address + "  city =  " + poiinfo.city + "   " +
                            "  phoneNum =   " + poiinfo.phoneNum + "  postCode =  " + poiinfo.postCode + "  uid =  " + poiinfo.uid +
                            "  hasCaterDetails = " + poiinfo.hasCaterDetails + "  isPano = " + poiinfo.isPano + "  location = " + poiinfo.location +
                            "  type = " + poiinfo.type + "   ");

                    for (int i = 0; i < poiInfoList.size(); i++) {
                        PoiInfo poiInfo = (PoiInfo) poiInfoList.get(i);
                        Log.e("poiInfo ", " poiInfo  = " + poiInfo.address + "   " + poiInfo.name);
                    }
                }



                List<PoiAddrInfo> poiAddrInfoList = poiResult.getAllAddr();
                Log.e("onGetPoiResult", "POI检索结果  poiAddrInfoList = " + poiAddrInfoList);
                if (poiAddrInfoList != null && poiAddrInfoList.size() > 0) {
                    for (int i = 0; i < poiAddrInfoList.size(); i++) {
                        PoiAddrInfo poiAddrInfo = poiAddrInfoList.get(i);
                        Log.e("onGetPoiResult", "POI检索结果  location = " + poiAddrInfo.location +
                                "  name =  " + poiAddrInfo.name +
                                "  address =  " + poiAddrInfo.name );
                    }
                }
                List<CityInfo> suggestCityList = poiResult.getSuggestCityList();
                Log.e("onGetPoiResult", "POI检索结果  suggestCityList = " + suggestCityList);
                if (suggestCityList != null && suggestCityList.size() > 0) {
                    for (int i = 0; i < suggestCityList.size(); i++) {
                        CityInfo cityInfo = suggestCityList.get(i);
                        Log.e("onGetPoiResult", "POI检索结果  describeContents = " + cityInfo.describeContents() +
                                "  num =  " + cityInfo.num +
                                "  city =  " + cityInfo.city );
                    }
                }


            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                //获取Place详情页检索结果
                Log.e("onGetPoiDetailResult", "Place详情页检索结果  poiDetailResult = " + poiDetailResult.toString());
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                Log.e("onGetPoiIndoorResult", "Indoor POI检索结果  poiIndoorResult = " + poiIndoorResult.toString());
                List<PoiIndoorInfo> poiIndoorInfoList = poiIndoorResult.getmArrayPoiInfo();
                if (poiIndoorInfoList != null && poiIndoorInfoList.size() > 0) {
                    for (int i = 0; i < poiIndoorInfoList.size(); i++) {
                        PoiIndoorInfo poiIndoorInfo = (PoiIndoorInfo) poiIndoorInfoList.get(i);
                        Log.e("onGetPoiResult", "POI检索结果  describeContents = " + poiIndoorInfo.latLng +
                                "  name =  " + poiIndoorInfo.name + "  address = " + poiIndoorInfo.address +
                                "  bid =  " + poiIndoorInfo.bid + "   " + "  uid =   " + poiIndoorInfo.uid +
                                "  cid =  " + poiIndoorInfo.cid + "  floor =  " + poiIndoorInfo.floor +
                                "  tag = " + poiIndoorInfo.tag + "  isGroup = " + poiIndoorInfo.isGroup +
                                "  discount = " + poiIndoorInfo.discount + "  groupNum = " + poiIndoorInfo.groupNum +
                                "  phone =  " + poiIndoorInfo.phone + "  floor =  " + poiIndoorInfo.floor +
                                "  isTakeOut =  " + poiIndoorInfo.isTakeOut + "  isWaited =  " + poiIndoorInfo.isWaited +
                                "  price = " + poiIndoorInfo.price + "  starLevel = " + poiIndoorInfo.starLevel );
                    }
                }
            }



        };

        OnGetSuggestionResultListener onGetSuggestionResultListener = new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {

            }
        };



        // 3.设置POI检索监听者；
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        mSuggestionSearch.setOnGetSuggestionResultListener(onGetSuggestionResultListener);

    }
    // 条件搜索
    private void poiConditionSearch() {
        // 4.发起检索请求；
        if (!TextUtils.isEmpty(etAtvtMainCity.getText().toString().trim()) && !TextUtils.isEmpty(etAtvtMainText.getText().toString().trim())) {
            mPoiSearch.searchInCity((new PoiCitySearchOption())
                    .city(etAtvtMainCity.getText().toString().trim())
                    .keyword(etAtvtMainText.getText().toString().trim())
                    .pageNum(10));
            /**
             * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
             */
            mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                    .keyword(etAtvtMainText.getText().toString())        // 搜索关键字
                    .city(etAtvtMainCity.getText().toString()));  // 搜索城市

        }


    }
    // 周边搜索
    private void poiAmbitusSearch() {

        // 4.发起检索请求；
        if (!TextUtils.isEmpty(etAtvtMainCity.getText().toString().trim()) && !TextUtils.isEmpty(etAtvtMainText.getText().toString().trim())) {
            LatLng center = new LatLng(34.29923570837169,  108.95409189492081);
            mPoiSearch.searchNearby(new PoiNearbySearchOption()
                    .keyword(etAtvtMainText.getText().toString().trim())
                    .sortType(PoiSortType.distance_from_near_to_far)
                    .location(center)
                    .radius(100)
                    .pageNum(10));

        }

    }
    // 矩形搜索
    private void poiRectangleSearch() {
        // 4.发起检索请求；
        LatLng southwest = new LatLng( 39.92235, 116.380338 );
        LatLng northeast = new LatLng( 39.947246, 116.414977);
        LatLngBounds searchbound = new LatLngBounds.Builder()
                .include(southwest).include(northeast)
                .build();
        mPoiSearch.searchInBound(new PoiBoundSearchOption().bound(searchbound)
                .keyword("餐厅"));

    }

    // 地图上绘制文字
    private void addText() {

        //定义文字所显示的坐标点
        LatLng llText = new LatLng(39.86923, 116.397428);

        //构建文字Option对象，用于在地图上添加文字
        OverlayOptions textOption = new TextOptions()
                .bgColor(0xAAFFFF00)  // 背景色
                .fontSize(24)         // 文字大小
                .fontColor(0xFFFF00FF)  // 文字颜色
                .text("绘制文字")  // 文字内容
                .rotate(-30)          // 文字旋转角度
                .position(llText);    // 文字显示位置

        //在地图上添加该文字对象并显示
        mBaiduMap.addOverlay(textOption);

        // 添加信息窗（弹窗覆盖物InfoWindow）
        // 弹出窗覆盖物的实现方式如下，开发者可利用此接口，构建具有更强交互性的地图页面。

        //创建InfoWindow展示的view
        Button button = new Button(getApplicationContext());
        button.setBackgroundResource(R.color.colorAccent);
        button.setText("popup");

        //定义用于显示该InfoWindow的坐标点
        LatLng pt = new LatLng(35.86923, 116.397428);

        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, pt, -47);

        //显示InfoWindow
        mBaiduMap.showInfoWindow(mInfoWindow);


    }

    // 地图上绘制面
    private void drawPlane() {
        // TODO: 2018/3/27 0027 绘制圆
        LatLng llCircle = new LatLng(39.90923, 116.447428);
        Stroke stroke = new Stroke(5, 0xAA000000);
        OverlayOptions ooCircle = new CircleOptions().fillColor(0x000000FF).center(llCircle).stroke(stroke).radius(1400);
        // 设置颜色和透明度,均使用16进制显示,0xAARRGGBB，如 0xAA000000 其中AA是透明度,000000为颜色
        mBaiduMap.addOverlay(ooCircle);


        // TODO: 2018/3/27 0027 绘制多边形 
        //定义多边形的五个顶点
        LatLng pt1 = new LatLng(39.93923, 116.357428);
        LatLng pt2 = new LatLng(39.91923, 116.327428);
        LatLng pt3 = new LatLng(39.89923, 116.347428);
        LatLng pt4 = new LatLng(39.89923, 116.367428);
        LatLng pt5 = new LatLng(39.91923, 116.387428);
        List<LatLng> pts = new ArrayList<LatLng>();
        pts.add(pt1);
        pts.add(pt2);
        pts.add(pt3);
        pts.add(pt4);
        pts.add(pt5);

        //构建用户绘制多边形的Option对象
        OverlayOptions polygonOption = new PolygonOptions()
                .points(pts)
                .stroke(new Stroke(5, 0xAA00FF00))
                .fillColor(0xAAFFFF00);

        //在地图上添加多边形Option，用于显示
        mBaiduMap.addOverlay(polygonOption);


    }

    // 地图上绘制线
    private void drawLine() {

        LatLng p1 = new LatLng(39.97923, 116.357428);
        LatLng p2 = new LatLng(39.94923, 116.397428);
        LatLng p3 = new LatLng(39.97923, 116.437428);
        OverlayOptions ooArc = new ArcOptions()
                .color(0xAA00FF00)
                .width(4)//设置颜色和透明度，均使用16进制显示，0xAARRGGBB，如 0xAA00FF00 其中AA是透明度，00FF00为颜色
                .points(p1, p2, p3);
        mBaiduMap.addOverlay(ooArc);


        // TODO: 2018/3/27 0027 绘制多段线 
        //构建折线点坐标
        LatLng p4 = new LatLng(39.97923, 116.357428);
        LatLng p5 = new LatLng(36.94923, 113.397428);
        LatLng p6 = new LatLng(33.97923, 119.437428);
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(p4);
        points.add(p5);
        points.add(p6);

        //绘制折线
        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                .color(0xAAFF0000).points(points);
        Polyline mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);

//        mPolyline.setDottedLine(true);   //设置是否虚线绘制


        // TODO: 2018/3/27 0027 绘制多段分色线 
        // 构造折线点坐标
        List<LatLng> pointss = new ArrayList<LatLng>();
        pointss.add(new LatLng(39.965, 116.404));
        pointss.add(new LatLng(39.925, 116.454));
        pointss.add(new LatLng(39.955, 116.494));
        pointss.add(new LatLng(39.905, 116.554));
        pointss.add(new LatLng(39.965, 116.604));

        //构建分段颜色索引数组
        List<Integer> colors = new ArrayList<>();
        colors.add(Integer.valueOf(Color.BLUE));
        colors.add(Integer.valueOf(Color.RED));
        colors.add(Integer.valueOf(Color.YELLOW));
        colors.add(Integer.valueOf(Color.GREEN));

        OverlayOptions oooPolyline = new PolylineOptions().width(10)
                .colorsValues(colors).points(pointss);

        // 添加在地图中
        mPolyline = (Polyline) mBaiduMap.addOverlay(oooPolyline);


    }

    // 地图事件
    private void mapEvent() {


        // TODO: 2018/3/27 0027 地图单击事件 
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            /**
             * 地图单击事件回调函数
             * @param point 点击的地理坐标
             */
            public void onMapClick(LatLng point) {
                Log.e("onMapClick", "获取到点击坐标 = " + point);

//                // 添加地图标点
//                // 定义Maker坐标点
//                LatLng point0 = new LatLng(32.29505308631754, 105.95935596520927);
//                OverlayOptions ooa = new MarkerOptions()
//                        .position(point0)         // 位置
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_blue))  // 图标
//                        .zIndex(1)        // 设置Marker所在层级
//                        .draggable(true)  // 是否可拖拽
//                        .alpha(0.5f)      // 透明度
//                        .perspective(true)  //   是否开启近大远小效果
//                        .visible(true)      // 是否显示
//                        .title("动态定位")  // 标题
//                        .animateType(MarkerOptions.MarkerAnimateType.drop);  // 添加掉下动画
//
//                marker = (Marker) mBaiduMap.addOverlay(ooa);
//                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(point0, 17.0f);
//                mBaiduMap.animateMapStatus(u);

            }

            /**
             * 地图内 Poi 单击事件回调函数
             * @param poi 点击的 poi 信息
             */
            public boolean onMapPoiClick(MapPoi poi) {
                Log.e("onMapPoiClick", " name = " + poi.getName()
                        + "   uid = " + poi.getUid() + "   latlng = " + poi.getPosition());
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

                marker = (Marker) mBaiduMap.addOverlay(ooa);


                return true;
            }
        });


        // TODO: 2018/3/27 0027 覆盖物单击事件 
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            /**
             * 地图 Marker 覆盖物点击事件监听函数
             * @param marker 被点击的 marker
             */
            public boolean onMarkerClick(Marker marker) {
                Log.e("onMarkerClick", "覆盖物点击事件监听 = " + marker);
                return false;
            }
        });
        // TODO: 2018/3/27 0027 地图定位点点击事件
        mBaiduMap.setOnMyLocationClickListener(new BaiduMap.OnMyLocationClickListener() {
            /**
             * 地图定位图标点击事件监听函数
             */
            public boolean onMyLocationClick() {
                Log.e("onMyLocationClick", "定位图标点击事件");
                return false;
            }
        });

        BaiduMap.SnapshotReadyCallback callback = new BaiduMap.SnapshotReadyCallback() {
            /**
             * 地图截屏回调接口
             * @param snapshot 截屏返回的 bitmap 数据
             */
            public void onSnapshotReady(Bitmap snapshot) {
                Log.e("onSnapshotReady", "地图截屏回调接口 = " + snapshot);
            }
        };


        mBaiduMap.setOnMapDrawFrameCallback(new BaiduMap.OnMapDrawFrameCallback() {
            @Override
            public void onMapDrawFrame(GL10 gl10, MapStatus mapStatus) {
                Log.e("onMapDrawFrame", "jianting =  gl10 = " + gl10 + "  mapStatus  = " + mapStatus);
            }

            @Override
            public void onMapDrawFrame(MapStatus mapStatus) {
//                Log.e("onMapDrawFrame","???? = " + mapStatus );
            }
        });

        // TODO: 2018/3/27 0027 地图触摸事件 
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            /**
             * 当用户触摸地图时回调函数
             * @param event 触摸事件
             */
            public void onTouch(MotionEvent event) {
//                Log.e("onTouch","触摸事件 event = " + event );
            }
        });

        // TODO: 2018/3/27 0027 地图长按事件 
        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.e("onMapLongClick", "长按事件  latLng = " + latLng);
            }
        });

        // TODO: 2018/3/27 0027 ？？？ 
        mBaiduMap.setOnPolylineClickListener(new BaiduMap.OnPolylineClickListener() {
            @Override
            public boolean onPolylineClick(Polyline polyline) {
                Log.e("onPolylineClick", "yiwen 事件  polyline = " + polyline);
                return false;
            }
        });
        // TODO: 2018/3/27 0027 地图滑动监听 
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
                Log.e("onMapStatusChangeStart", "地图滑动开始 + 2    mapStatus = " + mapStatus + "    i = " + i);
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                GeoCoder geoCoder = GeoCoder.newInstance();
                marker.setPosition(mapStatus.target);
                reverseGeoCodeOption = new ReverseGeoCodeOption().location(mapStatus.target);
                geoCoder.reverseGeoCode(reverseGeoCodeOption);
                onGetGeoCoderResultListener = new OnGetGeoCoderResultListener() {
                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

                    }

                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                        String address = reverseGeoCodeResult.getAddress();
                        if (!TextUtils.isEmpty(address)) {
                            latLng = reverseGeoCodeResult.getLocation();
                            String sendAddress = address;
                            Log.e("onGetReverseGeoCodeResult", "地图滑动完成    sendLatLng = " + latLng + "      address = " + address);

                            return;
                        } else {
                            Log.e("onGetReverseGeoCodeResult", "地图滑动完成    获取地址失败");
                        }
                    }
                };
                geoCoder.setOnGetGeoCodeResultListener(onGetGeoCoderResultListener);
                Log.e("onMapStatusChangeFinish", "地图滑动完成    mapStatus = " + mapStatus);
            }
        });


        // TODO: 2018/3/27 0027 地图上覆盖物拖拽的监听
        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
                //拖拽中
                Log.e("onMarkerDrag", "拖拽中  marker = " + marker);
            }

            public void onMarkerDragEnd(Marker marker) {
                //拖拽结束
                Log.e("onMarkerDragEnd", "拖拽结束  marker = " + marker);
            }

            public void onMarkerDragStart(Marker marker) {
                //开始拖拽
                Log.e("onMarkerDragStart", "开始拖拽  marker = " + marker);
            }
        });


    }

    // 地图上绘制点标记
    private void dotMark() {
        //定义Maker坐标点
        LatLng point = new LatLng(33.95670159951895, 107.76964913084957);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_location_red);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .title("太白山");
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);


        //定义Maker坐标点
        point = new LatLng(34.48498948836416, 110.09315226462795);
        //构建Marker图标
        bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_location_red);
        //构建MarkerOption，用于在地图上添加Marker
        option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .title("华山");
        mBaiduMap.addOverlay(option);


        // 添加地图标点
        // 定义Maker坐标点
        LatLng point0 = new LatLng(32.29505308631754, 105.95935596520927);
        OverlayOptions ooa = new MarkerOptions()
                .position(point0)         // 位置
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_blue))  // 图标
                .zIndex(1)        // 设置Marker所在层级
                .draggable(true)  // 是否可拖拽
                .alpha(0.5f)      // 透明度
                .perspective(true)  //   是否开启近大远小效果
                .visible(true)      // 是否显示
                .title("动态定位")  // 标题
                .animateType(MarkerOptions.MarkerAnimateType.drop);  // 添加掉下动画

        marker = (Marker) mBaiduMap.addOverlay(ooa);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(point0, 17.0f);
        mBaiduMap.animateMapStatus(u);


        // 批量添加地图标点
        //创建OverlayOptions的集合
        List<OverlayOptions> options = new ArrayList<OverlayOptions>();
        //设置坐标点
        LatLng point1 = new LatLng(39.92235, 116.380338);
        LatLng point2 = new LatLng(39.947246, 116.414977);

        //创建OverlayOptions属性
        OverlayOptions option1 = new MarkerOptions()
                .position(point1)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_red));
        OverlayOptions option2 = new MarkerOptions()
                .position(point2)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_green));
        //将OverlayOptions添加到list
        options.add(option1);
        options.add(option2);
        //在地图上批量添加
        mBaiduMap.addOverlays(options);

        // TODO: 2018/3/27 0027 为marker添加动画
        // 通过Marker的icons设置一组图片，再通过period设置多少帧刷新一次图片资源
        ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();

        giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_green));
        giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_red));
        giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_blue));

        OverlayOptions ooD = new MarkerOptions().position(point).icons(giflist)
                .zIndex(0).period(10);

        marker = (Marker) (mBaiduMap.addOverlay(ooD));


        // TODO: 2018/3/27 0027 为marker添加动画 
        MarkerOptions markerOptions = new MarkerOptions().position(point1).icons(giflist)
                .zIndex(0).period(10);
        // 生长动画
        markerOptions.animateType(MarkerOptions.MarkerAnimateType.grow);

        Marker mMarkerD = (Marker) (mBaiduMap.addOverlay(markerOptions));


    }

    // 室内地图相关
    private void indoorMap() {
        // 设置监听事件来监听进入和移出室内图：

        mBaiduMap.setOnBaseIndoorMapListener(new BaiduMap.OnBaseIndoorMapListener() {
            @Override
            public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {
                if (b) {
                    // 进入室内图
                    // 通过获取回调参数 mapBaseIndoorMapInfo 便可获取室内图信息，包含楼层信息，室内ID等
                } else {
                    // 移除室内图
                }
            }
        });
    }

    // 初始化定位并定位到当前位置
    private void locationMap() {

        // LocationClientOption 配置
        initOption();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);


        // 自定义定位图标
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);

        // 自定义精度圈填充颜色
        int accuracyCircleFillColor = 0x556EE1F4;//自定义精度圈填充颜色

        // 自定义精度圈边框颜色
        int accuracyCircleStrokeColor = 0xFF116DDD;//自定义精度圈边框颜色

        // 定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        MyLocationConfiguration myLocationConfiguration = new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker,
                accuracyCircleFillColor, accuracyCircleStrokeColor);

        // 设定定位图层配置
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration);


//        // 当不需要定位图层时关闭定位图层
//        mBaiduMap.setMyLocationEnabled(false);


//        // 设置定位跟随状态(使用默认图标)
//        mBaiduMap.setMyLocationConfigeration(
//                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker));

        // 定位初始化
        mLocClient = new LocationClient(this);

        // 定位监听
        mLocClient.registerLocationListener(myLocListener);

        // 设置定位参数
        mLocClient.setLocOption(option);

        // 开启定位
        mLocClient.start();


    }

    // 显示地图
    private void showMap() {
        // 设置地图图层
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);  // 默认地图（2D，3D ）
    }

    // LocationClientOption 配置
    private void initOption() {
        // 定位参数配置
        option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000); // 定位次数
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
        option.setIsNeedLocationPoiList(true);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
    }

    // 初始化View
    private void initView() {
        //获取地图控件引用
        mMapView = (TextureMapView) findViewById(R.id.bmapView);

        mIvAtvtMain = (ImageView) findViewById(R.id.iv_atvt_main_location);

        //获取地图控制器对象
        mBaiduMap = mMapView.getMap();
        Log.e(TAG, "onViewClicked: 点击定位");

        cbAtvtMainCommon.setOnCheckedChangeListener(onCheckedChangeListener);
        cbAtvtMainSatellite.setOnCheckedChangeListener(onCheckedChangeListener);
        cbAtvtMainEmpty.setOnCheckedChangeListener(onCheckedChangeListener);
        cbAtvtMainTraffic.setOnCheckedChangeListener(onCheckedChangeListener);
        cbAtvtMainNormal.setOnCheckedChangeListener(onCheckedChangeListener);
        cbAtvtMainFollowing.setOnCheckedChangeListener(onCheckedChangeListener);
        cbAtvtMainCompass.setOnCheckedChangeListener(onCheckedChangeListener);
        cbAtvtMainLocation.setOnCheckedChangeListener(onCheckedChangeListener);

        mMapView.setLogoPosition(LogoPosition.logoPostionleftBottom);
        // 地图Logo不允许遮挡，可通过以下方法可以设置地图边界区域，来避免UI遮挡。

        mBaiduMap.setPadding(10, paddingTop, paddingRight, paddingBottom);
        // 其中参数paddingLeft、paddingTop、paddingRight、paddingBottom参数表示距离屏幕边框的左、上、
        // 右、下边距的距离，单位为屏幕坐标的像素密度。

        // 指南针

        //指南针默认为开启状态，可以关闭显示 。设置方法如下：

        UiSettings mUiSettings = mBaiduMap.getUiSettings();
        //  实例化UiSettings类对象 mUiSettings.setCompassEnabled(enable);
        mUiSettings.setCompassEnabled(false);

        // 比例尺

        // 比例尺默认为开启状态，可以关闭显示。设置方法如下：

        mMapView. showScaleControl(true);
        // 同时支持设置MaxZoomLevel和minZoomLevel，方法为：

//        mBaiduMap.setMaxAndMinZoomLevel(float max, float min);
        // 另外，可通过mMapView.getMapLevel获取当前地图级别下比例尺所表示的距离大小。

      //  缩放按钮

        // 通过如下方式控制缩放按钮是否显示：
        //mMapView. showZoomControls(enable)；



    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.cb_atvt_main_common:
                    // 是否展示空地图
                    if (isChecked) {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
                    } else {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    }
                    break;
                case R.id.cb_atvt_main_satellite:
                    // 是否展示卫星图
                    if (isChecked) {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    } else {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    }
                    break;
                case R.id.cb_atvt_main_empty:
                    // 是否展示交通图
                    if (isChecked) {
                        mBaiduMap.setTrafficEnabled(true);
                    } else {
                        mBaiduMap.setTrafficEnabled(false);
                    }
                    break;
                case R.id.cb_atvt_main_traffic:
                    // 是否展示热力图
                    if (isChecked) {
                        mBaiduMap.setBaiduHeatMapEnabled(true);
                    } else {
                        mBaiduMap.setBaiduHeatMapEnabled(false);
                    }
                    break;

                case R.id.cb_atvt_main_normal:
                    // 是否跟随定位
                    if (isChecked) {
                        mBaiduMap.setMyLocationConfigeration(
                                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, mCurrentMarker));

                    } else {
                        mBaiduMap.setMyLocationConfigeration(
                                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker));
                    }

                    break;
                case R.id.cb_atvt_main_following:
                    // 是否罗盘定位
                    if (isChecked) {

                        mBaiduMap.setMyLocationConfigeration(
                                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, mCurrentMarker));
                    } else {
                        mBaiduMap.setMyLocationConfigeration(
                                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker));

                    }
                    break;
                case R.id.cb_atvt_main_compass:
                    // 是否打开室内图，默认为关闭状态
                    if (isChecked) {
                        mBaiduMap.setIndoorEnable(true);
                    } else {
                        mBaiduMap.setIndoorEnable(false);
                    }
                    break;
                case R.id.cb_atvt_main_location:
                    // 是否打开定位图层，默认打开
                    if (isChecked) {
                        mBaiduMap.setMyLocationEnabled(true);
                    } else {
                        mBaiduMap.setMyLocationEnabled(false);
                    }
                    break;


            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 5.释放POI检索实例；
        mPoiSearch.destroy();
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


    @OnClick({R.id.tv_atvt_main_heating_power, R.id.iv_atvt_main_location,
            R.id.tv_atvt_main_traffic1, R.id.tv_atvt_main_one, R.id.tv_atvt_main_two,
            R.id.tv_atvt_main_three,
            R.id.tv_atvt_main_four, R.id.tv_atvt_main_heating_five, R.id.tv_atvt_main_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_atvt_main_heating_power:  // 点及跳转
                Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_atvt_main_location:  // 重新定位点击事件
                // 移动定位图标至定位点上
                builder = new MapStatus.Builder();
                builder.target(latLngLocation).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                break;


            case R.id.tv_atvt_main_traffic1:
                // 移动定位图标至定位点上
                latLng = new LatLng(33.95670159951895, 107.76964913084957);
                builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                break;
            case R.id.tv_atvt_main_one:
                // 移动定位图标至定位点上
                latLng = new LatLng(39.86923, 116.397428);
                builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                break;
            case R.id.tv_atvt_main_two:
                // 移动定位图标至定位点上
                latLng = new LatLng(34.48498948836416, 110.09315226462795);
                builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                break;
            case R.id.tv_atvt_main_three:

                // 移动定位图标至定位点上
                latLng = new LatLng(32.29505308631754, 105.95935596520927);
                builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                break;
            case R.id.tv_atvt_main_four:

                // 移动定位图标至定位点上
                latLng = new LatLng(32.29505308631754, 105.95935596520927);
                builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));


                break;
            case R.id.tv_atvt_main_heating_five:
                intent = new Intent(MainActivity.this, OverlayDemoActivity.class);
                startActivity(intent);
                break;

            case R.id.tv_atvt_main_search:
                poiConditionSearch();
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
            // 此处设置开发者获取到的方向信息，顺时针0-360
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                // 经纬度获取
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                // 移动定位图标至定位点上
                latLngLocation = new LatLng(latitude, longitude);
                builder = new MapStatus.Builder();
                builder.target(latLngLocation).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                Toast.makeText(MainActivity.this, "当前所在位置：" + location.getAddrStr(), Toast.LENGTH_LONG).show();
                tvAtvtMainHeatingPower.setText(location.getCity());
            }
        }


    }


//    private void initM() {
//        //获取地图控制器对象
//        mBaiduMap = mMapView.getMap();
//        // 开启定位图层
//        mBaiduMap.setMyLocationEnabled(true);
//        // 定位初始化
//        mLocationClient = new LocationClient(this);
//        // 设定定位状态
//        locationState();
//        // 定位参数配置
//        locationConfig();
//        // 设置定位参数
//        mLocationClient.setLocOption(option);
//        // 定位监听
//        locationListener();
//        // 开启定位
//        mLocationClient.start();
//
//        // 当不需要定位图层时关闭定位图层
////                mBaiduMap.setMyLocationEnabled(false);
//
//
//    }
//
//    private void locationListener() {
//        // 定位监听
//        mLocationClient.registerLocationListener(new BDLocationListener() {
//            @Override
//            public void onReceiveLocation(BDLocation bdLocation) {
//                // 获取精度纬度
//                latitude = bdLocation.getLatitude();
//                longitude = bdLocation.getLongitude();
//
//                LatLng ll = new LatLng(latitude, longitude);
//                MapStatus.Builder builder = new MapStatus.Builder();
//                builder.target(ll).zoom(18.0f);
//                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//地图移动到当前经纬                  度
//
//
//                //事情做完了关闭定位
//                mLocationClient.stop();
//
//            }
//        });
//    }
//
//    private void locationState() {
//        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//        // 定位显示图标
//        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.ic_gps_not_fixed_black_24dp);
//        // 定位显示方式：跟随，罗盘，默认
//        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
//        int accuracyCircleFillColor = 0xAAFFFF88;//自定义精度圈填充颜色
//        int accuracyCircleStrokeColor = 0xAA00FF00;//自定义精度圈边框颜色
//        final MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true,
//                mCurrentMarker,accuracyCircleFillColor, accuracyCircleStrokeColor);
//        mBaiduMap.setMyLocationConfiguration(config);
//
//
//        /*
//
//        用于设置定位的属性，包括定位模式、是否开启方向、设置自定义定位图标、精度圈填充颜色，精度圈边框颜色。更详细信息，请检索类参考。
//
//        1.定位模式
//            地图SDK支持三种定位模式：NORMAL（普通态）, FOLLOWING（跟随态）, COMPASS（罗盘态）
//
//        mCurrentMode = LocationMode.FOLLOWING;//定位跟随态
//        mCurrentMode = LocationMode.NORMAL;   //默认为 LocationMode.NORMAL 普通态
//        mCurrentMode = LocationMode.COMPASS;  //定位罗盘态
//
//        2.自定义定位图标
//            支持自定义定位图标样式，
//            替换定位icon
//                mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
//            自定义精度圈填充颜色
//                accuracyCircleFillColor = 0xAAFFFF88;//自定义精度圈填充颜色
//            自定义精度圈边框颜色
//                accuracyCircleStrokeColor = 0xAA00FF00;//自定义精度圈边框颜色
//            定位精度圈大小
//            定位精度圈大小 ，是根据当前定位精度自动控制的，无法手动控制大小。精度圈越小，代表当前定位精度越高；反之圈越大，代表当前定位精度越低。
//
//            定位指针方向
//            定位指针朝向，是通过获取手机系统陀螺仪数据，控制定位指针的方向，需要开发者自己实现，并不在地图实现范畴。
//
//            在定义了以上属性之后，需要通过下面方法设置：
//
//            mBaiduMap.setMyLocationConfiguration(new
//                MyLocationConfiguration(
//                mCurrentMode, true,mCurrentMarker,
//                accuracyCircleFillColor, accuracyCircleStrokeColor));
//            */
//        //定位跟随态
//
//
//
//    }
//
//
//
//    private void locationConfig() {
//
//        // 定位参数配置
//        option = new LocationClientOption();
//        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//        //可选，默认gcj02，设置返回的定位结果坐标系
//        option.setCoorType("bd09ll");
//        int span = 1000;
//        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才有效的
//        option.setScanSpan(span);
//
//        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
//        option.setOpenGps(true);//可选，默认false,设置是否使用gps
//        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.            getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiLi            st里得到
//        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是            否在stop的时候杀死这个进程，默认不杀死
//        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
//        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
//        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置高精度定位定位模式
//
//
//    }


//    private GeoCoder mSearch;
//
//    //通过地址获取经纬度
//    public void checkAddPosition() {
//
//        if (mSearch == null) {
//            mSearch = GeoCoder.newInstance();
//            mSearch.setOnGetGeoCodeResultListener(listener);
//        }
//        mSearch.geocode(new GeoCodeOption()
//                .city("XX")
//                .address("xx村"));
//
//    }
//
//    //计算地理的 编码与反编码
//    OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
//        public void onGetGeoCodeResult(GeoCodeResult result) {
//            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//                //没有检索到结果
//                Toast.makeText(MainActivity.this, "当前城市无法进行定位！", Toast.LENGTH_LONG).show();
//            } else {
//
//                //获取地理编码结果
//                LatLng location = result.getLocation();
//                double latitude1 = location.latitude;
//                double longitude1 = location.longitude;
//                getLine(latitude, longitude, latitude1, longitude1);
//                Log.i("TAG", "latitude1:" + latitude1 + "  longitude1" + longitude1);
//            }
//
//        }
//
//        @Override
//        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
//            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//                //没有找到检索结果
//            }
//            //获取反向地理编码结果
//        }
//    };
//
//    public void getLine(double latitude, double longitude, double latitude1, double longitude1) {
//        //------------添加覆盖物--d--------
//        BitmapDescriptor bdA = BitmapDescriptorFactory
//                .fromResource(R.drawable.icon_openmap_focuse_mark);
//        BitmapDescriptor bdB = BitmapDescriptorFactory
//                .fromResource(R.drawable.icon_openmap_mark);
//        LatLng llA = new LatLng(latitude, longitude);
//        LatLng llB = new LatLng(latitude1, longitude1);
//        MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA)
//                .zIndex(9).draggable(true);
//        Marker mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
//        MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdB)
//                .zIndex(5);
//        Marker mMarkerB = (Marker) (mBaiduMap.addOverlay(ooB));
//        //--------------------处理连线------------------
//        Log.i("TAG", "latitude=" + latitude + "  longitude" + longitude);
//        List<LatLng> points = new ArrayList<LatLng>();
//        points.add(llA);
//        points.add(llB);
//        OverlayOptions ooPolyline = new PolylineOptions().width(5)
//                .color(0xAAFF0000).points(points);
//        mBaiduMap.addOverlay(ooPolyline);
//
//        //----------------
//    }
//
//    @Override
//    public void onReceiveLocation(BDLocation bdLocation) {
//        MyLocationData locData = new MyLocationData.Builder()
//                .accuracy(bdLocation.getRadius())
//                // 此处设置开发者获取到的方向信息，顺时针0-360
//                .direction(100).latitude(bdLocation.getLatitude())
//                .longitude(bdLocation.getLongitude()).build();
//        // 设置定位数据
//        mBaiduMap.setMyLocationData(locData);
//        if (isFirstLoc) {
//            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
//            MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(ll, 16);//设置地图中心及缩放级别
//            mBaiduMap.animateMapStatus(update);
//            isFirstLoc = false;
//            Toast.makeText(getApplicationContext(), bdLocation.getAddrStr(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//    private void initLocation() {
//        LocationClientOption option = new LocationClientOption();
//        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
//        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
//        int span = 1000;
//        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
//        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
//        option.setOpenGps(true);//可选，默认false,设置是否使用gps
//        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
//        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
//        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
//        mLocationClient.setLocOption(option);
//    }


}

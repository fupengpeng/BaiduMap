package com.fpp.baidumap.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.fpp.baidumap.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


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
    Button mBtnPre = null;
    Button mBtnNext = null;
    String startNodeStr = "高新区创意大厦";
    String endNodeStr = "绿色家园小区";
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    int nowSearchType = -1;

    WalkingRouteResult nowResultwalk = null;
    BikingRouteResult nowResultbike = null;
    TransitRouteResult nowResultransit = null;
    DrivingRouteResult nowResultdrive = null;
    MassTransitRouteResult nowResultmass = null;

    boolean hasShownDialogue = false;
    boolean useDefaultIcon = false;

    OverlayManager routeOverlay = null;

    MassTransitRouteLine massroute = null;

    RouteLine route = null;


    @BindView(R.id.driver_mTexturemap)
    TextureMapView driverMTexturemap;
    @BindView(R.id.driver_city)
    TextView driverCity;
    @BindView(R.id.driverb_layout)
    RelativeLayout driverbLayout;
    @BindView(R.id.et_start_address)
    EditText etStartAddress;
    @BindView(R.id.id_finish_green)
    EditText idFinishGreen;
    @BindView(R.id.iv_swap_calls)
    ImageView ivSwapCalls;
    @BindView(R.id.tv_search)
    TextView tvSearch;
    @BindView(R.id.iv_car)
    ImageView ivCar;
    @BindView(R.id.iv_bus)
    ImageView ivBus;
    @BindView(R.id.iv_walk)
    ImageView ivWalk;
    @BindView(R.id.iv_bike)
    ImageView ivBike;
    @BindView(R.id.driver_change)
    Button driverChange;
    @BindView(R.id.pre)
    Button pre;
    @BindView(R.id.next)
    Button next;
    @BindView(R.id.tv_poi_name)
    TextView tvPoiName;
    @BindView(R.id.tv_poi_distance)
    TextView tvPoiDistance;
    @BindView(R.id.tv_poi_address)
    TextView tvPoiAddress;
    @BindView(R.id.tv_poi_gotohere)
    TextView tvPoiGotohere;
    @BindView(R.id.rl_poi_message)
    RelativeLayout rlPoiMessage;


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
    RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private Marker marker;
    private RelativeLayout mRlPoiMessage;
    private TextView mTvPoiName;
    private TextView mTvPoiAddress;
    private TextView mTvPoiDistance;
    private TextView mTvPoiGoToHere;
    private OnGetRoutePlanResultListener onGetRoutePlanResultListener;

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
        ButterKnife.bind(this);
        requestLocButton = (Button) findViewById(R.id.driver_change);

        mCurrentMode = LocationMode.COMPASS;
        requestLocButton.setText("罗盘");
        requestLocButton.setOnClickListener(btnClickListener);

        // 初始化地图
        inintmap();
        initview();
        initEvent();

    }

    /**
     * 节点浏览示例
     *
     * @param v
     */
    public void nodeClick(View v) {
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = null;

        if (nowSearchType != 0 && nowSearchType != -1) {
            // 非跨城综合交通
            if (route == null || route.getAllStep() == null) {
                return;
            }
            if (nodeIndex == -1 && v.getId() == R.id.pre) {
                return;
            }
            // 设置节点索引
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
            // 获取节结果信息
            step = route.getAllStep().get(nodeIndex);
            if (step instanceof DrivingRouteLine.DrivingStep) {
                nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
                nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
            } else if (step instanceof WalkingRouteLine.WalkingStep) {
                nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
                nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
            } else if (step instanceof TransitRouteLine.TransitStep) {
                nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
                nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
            } else if (step instanceof BikingRouteLine.BikingStep) {
                nodeLocation = ((BikingRouteLine.BikingStep) step).getEntrance().getLocation();
                nodeTitle = ((BikingRouteLine.BikingStep) step).getInstructions();
            }
        } else if (nowSearchType == 0) {
            // 跨城综合交通  综合跨城公交的结果判断方式不一样


            if (massroute == null || massroute.getNewSteps() == null) {
                return;
            }
            if (nodeIndex == -1 && v.getId() == R.id.pre) {
                return;
            }
            boolean isSamecity = nowResultmass.getOrigin().getCityId() == nowResultmass.getDestination().getCityId();
            int size = 0;
            if (isSamecity) {
                size = massroute.getNewSteps().size();
            } else {
                for (int i = 0; i < massroute.getNewSteps().size(); i++) {
                    size += massroute.getNewSteps().get(i).size();
                }
            }

            // 设置节点索引
            if (v.getId() == R.id.next) {
                if (nodeIndex < size - 1) {
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
            if (isSamecity) {
                // 同城
                step = massroute.getNewSteps().get(nodeIndex).get(0);
            } else {
                // 跨城
                int num = 0;
                for (int j = 0; j < massroute.getNewSteps().size(); j++) {
                    num += massroute.getNewSteps().get(j).size();
                    if (nodeIndex - num < 0) {
                        int k = massroute.getNewSteps().get(j).size() + nodeIndex - num;
                        step = massroute.getNewSteps().get(j).get(k);
                        break;
                    }
                }
            }

            nodeLocation = ((MassTransitRouteLine.TransitStep) step).getStartLocation();
            nodeTitle = ((MassTransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }

        // 移动节点至中心
        mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(NavigationActivity.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
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
                mBaidumap.clear();
                // 地图标注物信息设置
                if (mRlPoiMessage.getVisibility() == View.VISIBLE) {
                    mRlPoiMessage.setVisibility(View.GONE);
                }
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
                if (mRlPoiMessage.getVisibility() == View.GONE) {
                    mRlPoiMessage.setVisibility(View.VISIBLE);
                }

                mTvPoiName.setText(poi.getName());
                endNodeStr = poi.getName();
                // TODO: 2018/4/4 0004 点击地图标识物显示其信息。

                return true;
            }
        });


        // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
// result.getSuggestAddrInfo()
// 直接显示
// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
// result.getSuggestAddrInfo()
// 直接显示
// 起终点模糊，获取建议列表
// 列表选择
// 同城
// 跨城
// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
// result.getSuggestAddrInfo()
// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
// result.getSuggestAddrInfo()
        onGetRoutePlanResultListener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(NavigationActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    // result.getSuggestAddrInfo()
                    AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nodeIndex = -1;
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);

                    if (result.getRouteLines().size() > 1) {
                        nowResultwalk = result;
                        if (!hasShownDialogue) {
                            MyTransitDlg myTransitDlg = new MyTransitDlg(NavigationActivity.this,
                                    result.getRouteLines(),
                                    Type.WALKING_ROUTE);
                            myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    hasShownDialogue = false;
                                }
                            });
                            myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                                public void onItemClick(int position) {
                                    route = nowResultwalk.getRouteLines().get(position);
                                    WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
                                    mBaidumap.setOnMarkerClickListener(overlay);
                                    routeOverlay = overlay;
                                    overlay.setData(nowResultwalk.getRouteLines().get(position));
                                    overlay.addToMap();
                                    overlay.zoomToSpan();
                                }

                            });
                            myTransitDlg.show();
                            hasShownDialogue = true;
                        }
                    } else if (result.getRouteLines().size() == 1) {
                        // 直接显示
                        route = result.getRouteLines().get(0);
                        WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
                        mBaidumap.setOnMarkerClickListener(overlay);
                        routeOverlay = overlay;
                        overlay.setData(result.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();

                    } else {
                        Log.d("route result", "结果数<0");
                        return;
                    }

                }

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult result) {

                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(NavigationActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    // result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nodeIndex = -1;
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);


                    if (result.getRouteLines().size() > 1) {
                        nowResultransit = result;
                        if (!hasShownDialogue) {
                            MyTransitDlg myTransitDlg = new MyTransitDlg(NavigationActivity.this,
                                    result.getRouteLines(),
                                    Type.TRANSIT_ROUTE);
                            myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    hasShownDialogue = false;
                                }
                            });
                            myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                                public void onItemClick(int position) {

                                    route = nowResultransit.getRouteLines().get(position);
                                    TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
                                    mBaidumap.setOnMarkerClickListener(overlay);
                                    routeOverlay = overlay;
                                    overlay.setData(nowResultransit.getRouteLines().get(position));
                                    overlay.addToMap();
                                    overlay.zoomToSpan();
                                }

                            });
                            myTransitDlg.show();
                            hasShownDialogue = true;
                        }
                    } else if (result.getRouteLines().size() == 1) {
                        // 直接显示
                        route = result.getRouteLines().get(0);
                        TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
                        mBaidumap.setOnMarkerClickListener(overlay);
                        routeOverlay = overlay;
                        overlay.setData(result.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();

                    } else {
                        Log.d("route result", "结果数<0");
                        return;
                    }


                }
            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(NavigationActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点模糊，获取建议列表
                    result.getSuggestAddrInfo();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nowResultmass = result;

                    nodeIndex = -1;
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);

                    if (!hasShownDialogue) {
                        // 列表选择
                        MyTransitDlg myTransitDlg = new MyTransitDlg(NavigationActivity.this,
                                result.getRouteLines(),
                                Type.MASS_TRANSIT_ROUTE);
                        nowResultmass = result;
                        myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                hasShownDialogue = false;
                            }
                        });
                        myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                            public void onItemClick(int position) {

                                MyMassTransitRouteOverlay overlay = new MyMassTransitRouteOverlay(mBaidumap);
                                mBaidumap.setOnMarkerClickListener(overlay);
                                routeOverlay = overlay;
                                massroute = nowResultmass.getRouteLines().get(position);
                                overlay.setData(nowResultmass.getRouteLines().get(position));

                                MassTransitRouteLine line = nowResultmass.getRouteLines().get(position);
                                overlay.setData(line);
                                if (nowResultmass.getOrigin().getCityId() == nowResultmass.getDestination().getCityId()) {
                                    // 同城
                                    overlay.setSameCity(true);
                                } else {
                                    // 跨城
                                    overlay.setSameCity(false);

                                }
                                mBaidumap.clear();
                                overlay.addToMap();
                                overlay.zoomToSpan();
                            }

                        });
                        myTransitDlg.show();
                        hasShownDialogue = true;
                    }
                }
            }


            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(NavigationActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    // result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nodeIndex = -1;


                    if (result.getRouteLines().size() > 1) {
                        nowResultdrive = result;
                        if (!hasShownDialogue) {
                            MyTransitDlg myTransitDlg = new MyTransitDlg(NavigationActivity.this,
                                    result.getRouteLines(),
                                    Type.DRIVING_ROUTE);
                            myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    hasShownDialogue = false;
                                }
                            });
                            myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                                public void onItemClick(int position) {
                                    route = nowResultdrive.getRouteLines().get(position);
                                    DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
                                    mBaidumap.setOnMarkerClickListener(overlay);
                                    routeOverlay = overlay;
                                    overlay.setData(nowResultdrive.getRouteLines().get(position));
                                    overlay.addToMap();
                                    overlay.zoomToSpan();
                                }

                            });
                            myTransitDlg.show();
                            hasShownDialogue = true;
                        }
                    } else if (result.getRouteLines().size() == 1) {
                        route = result.getRouteLines().get(0);
                        DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
                        routeOverlay = overlay;
                        mBaidumap.setOnMarkerClickListener(overlay);
                        overlay.setData(result.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();
                        mBtnPre.setVisibility(View.VISIBLE);
                        mBtnNext.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("route result", "结果数<0");
                        return;
                    }

                }
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(NavigationActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    // result.getSuggestAddrInfo()
                    AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nodeIndex = -1;
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);

                    if (result.getRouteLines().size() > 1) {
                        nowResultbike = result;
                        if (!hasShownDialogue) {
                            MyTransitDlg myTransitDlg = new MyTransitDlg(NavigationActivity.this,
                                    result.getRouteLines(),
                                    Type.DRIVING_ROUTE);
                            myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    hasShownDialogue = false;
                                }
                            });
                            myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                                public void onItemClick(int position) {
                                    route = nowResultbike.getRouteLines().get(position);
                                    BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
                                    mBaidumap.setOnMarkerClickListener(overlay);
                                    routeOverlay = overlay;
                                    overlay.setData(nowResultbike.getRouteLines().get(position));
                                    overlay.addToMap();
                                    overlay.zoomToSpan();
                                }

                            });
                            myTransitDlg.show();
                            hasShownDialogue = true;
                        }
                    } else if (result.getRouteLines().size() == 1) {
                        route = result.getRouteLines().get(0);
                        BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
                        routeOverlay = overlay;
                        mBaidumap.setOnMarkerClickListener(overlay);
                        overlay.setData(result.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();
                        mBtnPre.setVisibility(View.VISIBLE);
                        mBtnNext.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("route result", "结果数<0");
                        return;
                    }

                }
            }
        };

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
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);

        mTvPoiGoToHere.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2018/4/4 0004 点击进行路径设置
                // 重置浏览节点的路线数据
                route = null;
                mBtnPre.setVisibility(View.INVISIBLE);
                mBtnNext.setVisibility(View.INVISIBLE);
                mBaidumap.clear();
                // 处理搜索按钮响应
                // 设置起终点信息，对于tranist search 来说，城市名无意义
                PlanNode stNode = PlanNode.withCityNameAndPlaceName("济宁", startNodeStr);
                PlanNode enNode = PlanNode.withCityNameAndPlaceName("济宁", endNodeStr);

                // 实际使用中请对起点终点城市进行正确的设定

                mSearch = RoutePlanSearch.newInstance();
                mSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);

//                if (v.getId() == R.id.mass) {
//                    PlanNode stMassNode = PlanNode.withCityNameAndPlaceName("北京", "天安门");
//                    PlanNode enMassNode = PlanNode.withCityNameAndPlaceName("上海", "东方明珠");
//
//                    mSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stMassNode).to(enMassNode));
//                    nowSearchType = 0;
//                } else if (v.getId() == R.id.drive) {
//                    mSearch.drivingSearch((new DrivingRoutePlanOption())
//                            .from(stNode).to(enNode));
//                    nowSearchType = 1;
//                } else if (v.getId() == R.id.transit) {
//                    mSearch.transitSearch((new TransitRoutePlanOption())
//                            .from(stNode).city("北京").to(enNode));
//                    nowSearchType = 2;
//                } else if (v.getId() == R.id.walk) {
                mSearch.walkingSearch((new WalkingRoutePlanOption())
                        .from(stNode).to(enNode));
                nowSearchType = 3;
//                } else if (v.getId() == R.id.bike) {
//                    mSearch.bikingSearch((new BikingRoutePlanOption())
//                            .from(stNode).to(enNode));
//                    nowSearchType = 4;
//                }
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

    @OnClick({R.id.iv_swap_calls, R.id.tv_search, R.id.iv_car, R.id.iv_bus,
            R.id.iv_walk, R.id.iv_bike})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_swap_calls:
                break;
            case R.id.tv_search:
                break;
            case R.id.iv_car:
                break;
            case R.id.iv_bus:
                break;
            case R.id.iv_walk:
                break;
            case R.id.iv_bike:
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
            mBaidumap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaidumap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                Toast.makeText(NavigationActivity.this, "当前所在位置：" + location.getAddrStr(), Toast.LENGTH_LONG).show();
                driver_city.setText(location.getCity());
                loaclcity = location.getCity();
            }
        }


    }


    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    // 供路线选择的Dialog
    class MyTransitDlg extends Dialog {

        private List<? extends RouteLine> mtransitRouteLines;
        private ListView transitRouteList;
        private RouteLineAdapter mTransitAdapter;

        OnItemInDlgClickListener onItemInDlgClickListener;

        public MyTransitDlg(Context context, int theme) {
            super(context, theme);
        }

        public MyTransitDlg(Context context, List<? extends RouteLine> transitRouteLines, Type
                type) {
            this(context, 0);
            mtransitRouteLines = transitRouteLines;
            mTransitAdapter = new RouteLineAdapter(context, mtransitRouteLines, type);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        public void setOnDismissListener(OnDismissListener listener) {
            super.setOnDismissListener(listener);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transit_dialog);

            transitRouteList = (ListView) findViewById(R.id.transitList);
            transitRouteList.setAdapter(mTransitAdapter);

            transitRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onItemInDlgClickListener.onItemClick(position);
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);
                    dismiss();
                    hasShownDialogue = false;
                }
            });
        }

        public void setOnItemInDlgClickLinster(OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }

    }

    class RouteLineAdapter extends BaseAdapter {

        private List<? extends RouteLine> routeLines;
        private LayoutInflater layoutInflater;
        private Type mtype;

        public RouteLineAdapter(Context context, List<? extends RouteLine> routeLines, Type type) {
            this.routeLines = routeLines;
            layoutInflater = LayoutInflater.from(context);
            mtype = type;
        }

        @Override
        public int getCount() {
            return routeLines.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NodeViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.activity_transit_item, null);
                holder = new NodeViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.transitName);
                holder.lightNum = (TextView) convertView.findViewById(R.id.lightNum);
                holder.dis = (TextView) convertView.findViewById(R.id.dis);
                convertView.setTag(holder);
            } else {
                holder = (NodeViewHolder) convertView.getTag();
            }

            switch (mtype) {
                case TRANSIT_ROUTE:
                case WALKING_ROUTE:
                case BIKING_ROUTE:
                    holder.name.setText("路线" + (position + 1));
                    int time = routeLines.get(position).getDuration();
                    if (time / 3600 == 0) {
                        holder.lightNum.setText("大约需要：" + time / 60 + "分钟");
                    } else {
                        holder.lightNum.setText("大约需要：" + time / 3600 + "小时" + (time % 3600) / 60 + "分钟");
                    }
                    holder.dis.setText("距离大约是：" + routeLines.get(position).getDistance() + "米");
                    break;

                case DRIVING_ROUTE:
                    DrivingRouteLine drivingRouteLine = (DrivingRouteLine) routeLines.get(position);
                    holder.name.setText("线路" + (position + 1));
                    holder.lightNum.setText("红绿灯数：" + drivingRouteLine.getLightNum());
                    holder.dis.setText("拥堵距离为：" + drivingRouteLine.getCongestionDistance() + "米");
                    break;
                case MASS_TRANSIT_ROUTE:
                    MassTransitRouteLine massTransitRouteLine = (MassTransitRouteLine) routeLines.get(position);
                    holder.name.setText("线路" + (position + 1));
                    holder.lightNum.setText("预计达到时间：" + massTransitRouteLine.getArriveTime());
                    holder.dis.setText("总票价：" + massTransitRouteLine.getPrice() + "元");
                    break;

                default:
                    break;

            }

            return convertView;
        }

        private class NodeViewHolder {

            private TextView name;
            private TextView lightNum;
            private TextView dis;
        }

    }

    public enum Type {
        MASS_TRANSIT_ROUTE, // 综合交通
        TRANSIT_ROUTE, // 公交
        DRIVING_ROUTE, // 驾车
        WALKING_ROUTE, // 步行
        BIKING_ROUTE // 骑行

    }

    // 定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
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
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
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
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        public MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }


    }

    private class MyMassTransitRouteOverlay extends MassTransitRouteOverlay {
        public MyMassTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }


    }

    class DrivingRouteOverlay extends OverlayManager {

        private DrivingRouteLine mRouteLine = null;
        boolean focus = false;

        /**
         * 构造函数
         *
         * @param baiduMap 该DrivingRouteOvelray引用的 BaiduMap
         */
        public DrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public final List<OverlayOptions> getOverlayOptions() {
            if (mRouteLine == null) {
                return null;
            }

            List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();
            // step node
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().size() > 0) {

                for (DrivingRouteLine.DrivingStep step : mRouteLine.getAllStep()) {
                    Bundle b = new Bundle();
                    b.putInt("index", mRouteLine.getAllStep().indexOf(step));
                    if (step.getEntrance() != null) {
                        overlayOptionses.add((new MarkerOptions())
                                .position(step.getEntrance().getLocation())
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .rotate((360 - step.getDirection()))
                                .extraInfo(b)
                                .icon(BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_line_node.png")));
                    }
                    // 最后路段绘制出口点
                    if (mRouteLine.getAllStep().indexOf(step) == (mRouteLine
                            .getAllStep().size() - 1) && step.getExit() != null) {
                        overlayOptionses.add((new MarkerOptions())
                                .position(step.getExit().getLocation())
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .icon(BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_line_node.png")));

                    }
                }
            }

            if (mRouteLine.getStarting() != null) {
                overlayOptionses.add((new MarkerOptions())
                        .position(mRouteLine.getStarting().getLocation())
                        .icon(getStartMarker() != null ? getStartMarker() :
                                BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_start.png")).zIndex(10));
            }
            if (mRouteLine.getTerminal() != null) {
                overlayOptionses
                        .add((new MarkerOptions())
                                .position(mRouteLine.getTerminal().getLocation())
                                .icon(getTerminalMarker() != null ? getTerminalMarker() :
                                        BitmapDescriptorFactory
                                                .fromAssetWithDpi("Icon_end.png"))
                                .zIndex(10));
            }
            // poly line
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().size() > 0) {

                List<DrivingRouteLine.DrivingStep> steps = mRouteLine.getAllStep();
                int stepNum = steps.size();


                List<LatLng> points = new ArrayList<LatLng>();
                ArrayList<Integer> traffics = new ArrayList<Integer>();
                int totalTraffic = 0;
                for (int i = 0; i < stepNum; i++) {
                    if (i == stepNum - 1) {
                        points.addAll(steps.get(i).getWayPoints());
                    } else {
                        points.addAll(steps.get(i).getWayPoints().subList(0, steps.get(i).getWayPoints().size() - 1));
                    }

                    totalTraffic += steps.get(i).getWayPoints().size() - 1;
                    if (steps.get(i).getTrafficList() != null && steps.get(i).getTrafficList().length > 0) {
                        for (int j = 0; j < steps.get(i).getTrafficList().length; j++) {
                            traffics.add(steps.get(i).getTrafficList()[j]);
                        }
                    }
                }

//            Bundle indexList = new Bundle();
//            if (traffics.size() > 0) {
//                int raffic[] = new int[traffics.size()];
//                int index = 0;
//                for (Integer tempTraff : traffics) {
//                    raffic[index] = tempTraff.intValue();
//                    index++;
//                }
//                indexList.putIntArray("indexs", raffic);
//            }
                boolean isDotLine = false;

                if (traffics != null && traffics.size() > 0) {
                    isDotLine = true;
                }
                PolylineOptions option = new PolylineOptions().points(points).textureIndex(traffics)
                        .width(7).dottedLine(isDotLine).focus(true)
                        .color(getLineColor() != 0 ? getLineColor() : Color.argb(178, 0, 78, 255)).zIndex(0);
                if (isDotLine) {
                    option.customTextureList(getCustomTextureList());
                }
                overlayOptionses.add(option);
            }
            return overlayOptionses;
        }

        /**
         * 设置路线数据
         *
         * @param routeLine 路线数据
         */
        public void setData(DrivingRouteLine routeLine) {
            this.mRouteLine = routeLine;
        }

        /**
         * 覆写此方法以改变默认起点图标
         *
         * @return 起点图标
         */
        public BitmapDescriptor getStartMarker() {
            return null;
        }

        /**
         * 覆写此方法以改变默认绘制颜色
         *
         * @return 线颜色
         */
        public int getLineColor() {
            return 0;
        }

        public List<BitmapDescriptor> getCustomTextureList() {
            ArrayList<BitmapDescriptor> list = new ArrayList<BitmapDescriptor>();
            list.add(BitmapDescriptorFactory.fromAsset("Icon_road_blue_arrow.png"));
            list.add(BitmapDescriptorFactory.fromAsset("Icon_road_green_arrow.png"));
            list.add(BitmapDescriptorFactory.fromAsset("Icon_road_yellow_arrow.png"));
            list.add(BitmapDescriptorFactory.fromAsset("Icon_road_red_arrow.png"));
            list.add(BitmapDescriptorFactory.fromAsset("Icon_road_nofocus.png"));
            return list;
        }

        /**
         * 覆写此方法以改变默认终点图标
         *
         * @return 终点图标
         */
        public BitmapDescriptor getTerminalMarker() {
            return null;
        }

        /**
         * 覆写此方法以改变默认点击处理
         *
         * @param i 线路节点的 index
         * @return 是否处理了该点击事件
         */
        public boolean onRouteNodeClick(int i) {
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().get(i) != null) {
                Log.i("baidumapsdk", "DrivingRouteOverlay onRouteNodeClick");
            }
            return false;
        }

        @Override
        public final boolean onMarkerClick(Marker marker) {
            for (Overlay mMarker : mOverlayList) {
                if (mMarker instanceof Marker && mMarker.equals(marker)) {
                    if (marker.getExtraInfo() != null) {
                        onRouteNodeClick(marker.getExtraInfo().getInt("index"));
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onPolylineClick(Polyline polyline) {
            boolean flag = false;
            for (Overlay mPolyline : mOverlayList) {
                if (mPolyline instanceof Polyline && mPolyline.equals(polyline)) {
                    // 选中
                    flag = true;
                    break;
                }
            }
            setFocus(flag);
            return true;
        }

        public void setFocus(boolean flag) {
            focus = flag;
            for (Overlay mPolyline : mOverlayList) {
                if (mPolyline instanceof Polyline) {
                    // 选中
                    ((Polyline) mPolyline).setFocus(flag);

                    break;
                }
            }

        }
    }

    class WalkingRouteOverlay extends OverlayManager {

        private WalkingRouteLine mRouteLine = null;

        public WalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        /**
         * 设置路线数据。
         *
         * @param line 路线数据
         */
        public void setData(WalkingRouteLine line) {
            mRouteLine = line;
        }

        @Override
        public final List<OverlayOptions> getOverlayOptions() {
            if (mRouteLine == null) {
                return null;
            }

            List<OverlayOptions> overlayList = new ArrayList<OverlayOptions>();
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().size() > 0) {
                for (WalkingRouteLine.WalkingStep step : mRouteLine.getAllStep()) {
                    Bundle b = new Bundle();
                    b.putInt("index", mRouteLine.getAllStep().indexOf(step));
                    if (step.getEntrance() != null) {
                        overlayList.add((new MarkerOptions())
                                .position(step.getEntrance().getLocation())
                                .rotate((360 - step.getDirection()))
                                .zIndex(10)
                                .anchor(0.5f, 0.5f)
                                .extraInfo(b)
                                .icon(BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_line_node.png")));
                    }

                    // 最后路段绘制出口点
                    if (mRouteLine.getAllStep().indexOf(step) == (mRouteLine
                            .getAllStep().size() - 1) && step.getExit() != null) {
                        overlayList.add((new MarkerOptions())
                                .position(step.getExit().getLocation())
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .icon(BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_line_node.png")));

                    }
                }
            }
            // starting
            if (mRouteLine.getStarting() != null) {
                overlayList.add((new MarkerOptions())
                        .position(mRouteLine.getStarting().getLocation())
                        .icon(getStartMarker() != null ? getStartMarker() :
                                BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_start.png")).zIndex(10));
            }
            // terminal
            if (mRouteLine.getTerminal() != null) {
                overlayList
                        .add((new MarkerOptions())
                                .position(mRouteLine.getTerminal().getLocation())
                                .icon(getTerminalMarker() != null ? getTerminalMarker() :
                                        BitmapDescriptorFactory
                                                .fromAssetWithDpi("Icon_end.png"))
                                .zIndex(10));
            }

            // poly line list
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().size() > 0) {
                LatLng lastStepLastPoint = null;
                for (WalkingRouteLine.WalkingStep step : mRouteLine.getAllStep()) {
                    List<LatLng> watPoints = step.getWayPoints();
                    if (watPoints != null) {
                        List<LatLng> points = new ArrayList<LatLng>();
                        if (lastStepLastPoint != null) {
                            points.add(lastStepLastPoint);
                        }
                        points.addAll(watPoints);
                        overlayList.add(new PolylineOptions().points(points).width(10)
                                .color(getLineColor() != 0 ? getLineColor() : Color.argb(178, 0, 78, 255)).zIndex(0));
                        lastStepLastPoint = watPoints.get(watPoints.size() - 1);
                    }
                }

            }

            return overlayList;
        }

        /**
         * 覆写此方法以改变默认起点图标
         *
         * @return 起点图标
         */
        public BitmapDescriptor getStartMarker() {
            return null;
        }

        public int getLineColor() {
            return 0;
        }

        /**
         * 覆写此方法以改变默认终点图标
         *
         * @return 终点图标
         */
        public BitmapDescriptor getTerminalMarker() {
            return null;
        }

        /**
         * 处理点击事件
         *
         * @param i 被点击的step在
         *          {@link WalkingRouteLine#getAllStep()}
         *          中的索引
         * @return 是否处理了该点击事件
         */
        public boolean onRouteNodeClick(int i) {
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().get(i) != null) {
                Log.i("baidumapsdk", "WalkingRouteOverlay onRouteNodeClick");
            }
            return false;
        }

        @Override
        public final boolean onMarkerClick(Marker marker) {
            for (Overlay mMarker : mOverlayList) {
                if (mMarker instanceof Marker && mMarker.equals(marker)) {
                    if (marker.getExtraInfo() != null) {
                        onRouteNodeClick(marker.getExtraInfo().getInt("index"));
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onPolylineClick(Polyline polyline) {
            // TODO Auto-generated method stub
            return false;
        }
    }

    class TransitRouteOverlay extends OverlayManager {

        private TransitRouteLine mRouteLine = null;

        /**
         * 构造函数
         *
         * @param baiduMap 该TransitRouteOverlay引用的 BaiduMap 对象
         */
        public TransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public final List<OverlayOptions> getOverlayOptions() {

            if (mRouteLine == null) {
                return null;
            }

            List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();
            // step node
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().size() > 0) {

                for (TransitRouteLine.TransitStep step : mRouteLine.getAllStep()) {
                    Bundle b = new Bundle();
                    b.putInt("index", mRouteLine.getAllStep().indexOf(step));
                    if (step.getEntrance() != null) {
                        overlayOptionses.add((new MarkerOptions())
                                .position(step.getEntrance().getLocation())
                                .anchor(0.5f, 0.5f).zIndex(10).extraInfo(b)
                                .icon(getIconForStep(step)));
                    }
                    // 最后路段绘制出口点
                    if (mRouteLine.getAllStep().indexOf(step) == (mRouteLine
                            .getAllStep().size() - 1) && step.getExit() != null) {
                        overlayOptionses.add((new MarkerOptions())
                                .position(step.getExit().getLocation())
                                .anchor(0.5f, 0.5f).zIndex(10)
                                .icon(getIconForStep(step)));
                    }
                }
            }

            if (mRouteLine.getStarting() != null) {
                overlayOptionses.add((new MarkerOptions())
                        .position(mRouteLine.getStarting().getLocation())
                        .icon(getStartMarker() != null ? getStartMarker() :
                                BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_start.png")).zIndex(10));
            }
            if (mRouteLine.getTerminal() != null) {
                overlayOptionses
                        .add((new MarkerOptions())
                                .position(mRouteLine.getTerminal().getLocation())
                                .icon(getTerminalMarker() != null ? getTerminalMarker() :
                                        BitmapDescriptorFactory
                                                .fromAssetWithDpi("Icon_end.png"))
                                .zIndex(10));
            }
            // polyline
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().size() > 0) {

                for (TransitRouteLine.TransitStep step : mRouteLine.getAllStep()) {
                    if (step.getWayPoints() == null) {
                        continue;
                    }
                    int color = 0;
                    if (step.getStepType() != TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING) {
//                    color = Color.argb(178, 0, 78, 255);
                        color = getLineColor() != 0 ? getLineColor() : Color.argb(178, 0, 78, 255);
                    } else {
//                    color = Color.argb(178, 88, 208, 0);
                        color = getLineColor() != 0 ? getLineColor() : Color.argb(178, 88, 208, 0);
                    }
                    overlayOptionses.add(new PolylineOptions()
                            .points(step.getWayPoints()).width(10).color(color)
                            .zIndex(0));
                }
            }
            return overlayOptionses;
        }

        private BitmapDescriptor getIconForStep(TransitRouteLine.TransitStep step) {
            switch (step.getStepType()) {
                case BUSLINE:
                    return BitmapDescriptorFactory.fromAssetWithDpi("Icon_bus_station.png");
                case SUBWAY:
                    return BitmapDescriptorFactory.fromAssetWithDpi("Icon_subway_station.png");
                case WAKLING:
                    return BitmapDescriptorFactory.fromAssetWithDpi("Icon_walk_route.png");
                default:
                    return null;
            }
        }

        /**
         * 设置路线数据
         *
         * @param routeOverlay 路线数据
         */
        public void setData(TransitRouteLine routeOverlay) {
            this.mRouteLine = routeOverlay;
        }

        /**
         * 覆写此方法以改变默认起点图标
         *
         * @return 起点图标
         */
        public BitmapDescriptor getStartMarker() {
            return null;
        }

        /**
         * 覆写此方法以改变默认终点图标
         *
         * @return 终点图标
         */
        public BitmapDescriptor getTerminalMarker() {
            return null;
        }

        public int getLineColor() {
            return 0;
        }

        /**
         * 覆写此方法以改变起默认点击行为
         *
         * @param i 被点击的step在
         *          {@link TransitRouteLine#getAllStep()}
         *          中的索引
         * @return 是否处理了该点击事件
         */
        public boolean onRouteNodeClick(int i) {
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().get(i) != null) {
                Log.i("baidumapsdk", "TransitRouteOverlay onRouteNodeClick");
            }
            return false;
        }

        @Override
        public final boolean onMarkerClick(Marker marker) {
            for (Overlay mMarker : mOverlayList) {
                if (mMarker instanceof Marker && mMarker.equals(marker)) {
                    if (marker.getExtraInfo() != null) {
                        onRouteNodeClick(marker.getExtraInfo().getInt("index"));
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onPolylineClick(Polyline polyline) {
            // TODO Auto-generated method stub
            return false;
        }

    }

    class BikingRouteOverlay extends OverlayManager {

        private BikingRouteLine mRouteLine = null;

        public BikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        /**
         * 设置路线数据。
         *
         * @param line 路线数据
         */
        public void setData(BikingRouteLine line) {
            mRouteLine = line;
        }

        @Override
        public final List<OverlayOptions> getOverlayOptions() {
            if (mRouteLine == null) {
                return null;
            }

            List<OverlayOptions> overlayList = new ArrayList<OverlayOptions>();
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().size() > 0) {
                for (BikingRouteLine.BikingStep step : mRouteLine.getAllStep()) {
                    Bundle b = new Bundle();
                    b.putInt("index", mRouteLine.getAllStep().indexOf(step));
                    if (step.getEntrance() != null) {
                        overlayList.add((new MarkerOptions())
                                .position(step.getEntrance().getLocation())
                                .rotate((360 - step.getDirection()))
                                .zIndex(10)
                                .anchor(0.5f, 0.5f)
                                .extraInfo(b)
                                .icon(BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_line_node.png")));
                    }

                    // 最后路段绘制出口点
                    if (mRouteLine.getAllStep().indexOf(step) == (mRouteLine
                            .getAllStep().size() - 1) && step.getExit() != null) {
                        overlayList.add((new MarkerOptions())
                                .position(step.getExit().getLocation())
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .icon(BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_line_node.png")));

                    }
                }
            }
            // starting
            if (mRouteLine.getStarting() != null) {
                overlayList.add((new MarkerOptions())
                        .position(mRouteLine.getStarting().getLocation())
                        .icon(getStartMarker() != null ? getStartMarker() :
                                BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_start.png")).zIndex(10));
            }
            // terminal
            if (mRouteLine.getTerminal() != null) {
                overlayList
                        .add((new MarkerOptions())
                                .position(mRouteLine.getTerminal().getLocation())
                                .icon(getTerminalMarker() != null ? getTerminalMarker() :
                                        BitmapDescriptorFactory
                                                .fromAssetWithDpi("Icon_end.png"))
                                .zIndex(10));
            }

            // poly line list
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().size() > 0) {
                LatLng lastStepLastPoint = null;
                for (BikingRouteLine.BikingStep step : mRouteLine.getAllStep()) {
                    List<LatLng> watPoints = step.getWayPoints();
                    if (watPoints != null) {
                        List<LatLng> points = new ArrayList<LatLng>();
                        if (lastStepLastPoint != null) {
                            points.add(lastStepLastPoint);
                        }
                        points.addAll(watPoints);
                        overlayList.add(new PolylineOptions().points(points).width(10)
                                .color(getLineColor() != 0 ? getLineColor() : Color.argb(178, 0, 78, 255)).zIndex(0));
                        lastStepLastPoint = watPoints.get(watPoints.size() - 1);
                    }
                }

            }

            return overlayList;
        }

        /**
         * 覆写此方法以改变默认起点图标
         *
         * @return 起点图标
         */
        public BitmapDescriptor getStartMarker() {
            return null;
        }

        public int getLineColor() {
            return 0;
        }

        /**
         * 覆写此方法以改变默认终点图标
         *
         * @return 终点图标
         */
        public BitmapDescriptor getTerminalMarker() {
            return null;
        }

        /**
         * 处理点击事件
         *
         * @param i 被点击的step在
         *          {@link BikingRouteLine#getAllStep()}
         *          中的索引
         * @return 是否处理了该点击事件
         */
        public boolean onRouteNodeClick(int i) {
            if (mRouteLine.getAllStep() != null
                    && mRouteLine.getAllStep().get(i) != null) {
                Log.i("baidumapsdk", "BikingRouteOverlay onRouteNodeClick");
            }
            return false;
        }

        @Override
        public final boolean onMarkerClick(Marker marker) {
            for (Overlay mMarker : mOverlayList) {
                if (mMarker instanceof Marker && mMarker.equals(marker)) {
                    if (marker.getExtraInfo() != null) {
                        onRouteNodeClick(marker.getExtraInfo().getInt("index"));
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onPolylineClick(Polyline polyline) {
            // TODO Auto-generated method stub
            return false;
        }
    }

    class MassTransitRouteOverlay extends OverlayManager {

        private MassTransitRouteLine mRouteLine;
        private boolean isSameCity;

        /**
         * 构造函数
         *
         * @param baiduMap 该TransitRouteOverlay引用的 BaiduMap 对象
         */
        public MassTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }


        /**
         * 设置路线数据
         *
         * @param routeOverlay 路线数据
         */
        public void setData(MassTransitRouteLine routeOverlay) {
            this.mRouteLine = routeOverlay;
        }

        public void setSameCity(boolean sameCity) {
            isSameCity = sameCity;
        }

        /**
         * 覆写此方法以改变默认起点图标
         *
         * @return 起点图标
         */
        public BitmapDescriptor getStartMarker() {
            return null;
        }

        /**
         * 覆写此方法以改变默认终点图标
         *
         * @return 终点图标
         */
        public BitmapDescriptor getTerminalMarker() {
            return null;
        }

        public int getLineColor() {
            return 0;
        }

        @Override
        public List<OverlayOptions> getOverlayOptions() {
            if (mRouteLine == null) {
                return null;
            }

            List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();
            List<List<MassTransitRouteLine.TransitStep>> steps = mRouteLine.getNewSteps();
            if (isSameCity) {
                // 同城 (同城时，每个steps的get(i)对应的List是一条step的不同方案，此处都选第一条进行绘制，即get（0））

                // step node
                for (int i = 0; i < steps.size(); i++) {

                    MassTransitRouteLine.TransitStep step = steps.get(i).get(0);
                    Bundle b = new Bundle();
                    b.putInt("index", i + 1);

                    if (step.getStartLocation() != null) {
                        overlayOptionses.add((new MarkerOptions()).position(step.getStartLocation())
                                .anchor(0.5f, 0.5f).zIndex(10).extraInfo(b).icon(getIconForStep(step)));
                    }

                    // 最后一个终点
                    if ((i == steps.size() - 1) && (step.getEndLocation() != null)) {
                        overlayOptionses.add((new MarkerOptions()).position(step.getEndLocation())
                                .anchor(0.5f, 0.5f).zIndex(10)
                                .icon(getIconForStep(step))
                        );
                    }

                }

                // polyline
                for (int i = 0; i < steps.size(); i++) {
                    MassTransitRouteLine.TransitStep step = steps.get(i).get(0);
                    int color = 0;
                    if (step.getVehileType() != MassTransitRouteLine.TransitStep
                            .StepVehicleInfoType.ESTEP_WALK) {
                        // color = Color.argb(178, 0, 78, 255);
                        color = getLineColor() != 0 ? getLineColor() : Color.argb(178, 0, 78, 255);
                    } else {
                        // color = Color.argb(178, 88, 208, 0);
                        color = getLineColor() != 0 ? getLineColor() : Color.argb(178, 88, 208, 0);
                    }
                    overlayOptionses.add(new PolylineOptions()
                            .points(step.getWayPoints()).width(10).color(color)
                            .zIndex(0));
                }

            } else {
                // 跨城 （跨城时，每个steps的get(i)对应的List是一条step的子路线sub_step，需要将它们全部拼接才是一条完整路线）
                int stepSum = 0;
                for (int i = 0; i < steps.size(); i++) {
                    stepSum += steps.get(i).size();
                }

                // step node
                int k = 1;
                for (int i = 0; i < steps.size(); i++) {

                    for (int j = 0; j < steps.get(i).size(); j++) {
                        MassTransitRouteLine.TransitStep step = steps.get(i).get(j);
                        Bundle b = new Bundle();
                        b.putInt("index", k);

                        if (step.getStartLocation() != null) {
                            overlayOptionses.add((new MarkerOptions()).position(step.getStartLocation())
                                    .anchor(0.5f, 0.5f).zIndex(10).extraInfo(b).icon(getIconForStep(step)));
                        }

                        // 最后一个终点
                        if ((k == stepSum) && (step.getEndLocation() != null)) {
                            overlayOptionses.add((new MarkerOptions()).position(step.getEndLocation())
                                    .anchor(0.5f, 0.5f).zIndex(10).icon(getIconForStep(step)));
                        }

                        k++;
                    }
                }


                // polyline
                for (int i = 0; i < steps.size(); i++) {

                    for (int j = 0; j < steps.get(i).size(); j++) {
                        MassTransitRouteLine.TransitStep step = steps.get(i).get(j);
                        int color = 0;
                        if (step.getVehileType() != MassTransitRouteLine.TransitStep
                                .StepVehicleInfoType.ESTEP_WALK) {
                            // color = Color.argb(178, 0, 78, 255);
                            color = getLineColor() != 0 ? getLineColor() : Color.argb(178, 0, 78, 255);
                        } else {
                            // color = Color.argb(178, 88, 208, 0);
                            color = getLineColor() != 0 ? getLineColor() : Color.argb(178, 88, 208, 0);
                        }
                        if (step.getWayPoints() != null) {
                            overlayOptionses.add(new PolylineOptions()
                                    .points(step.getWayPoints()).width(10).color(color)
                                    .zIndex(0));
                        }
                    }
                }

            }

            // 起点
            if (mRouteLine.getStarting() != null && mRouteLine.getStarting().getLocation() != null) {
                overlayOptionses.add((new MarkerOptions()).position(mRouteLine.getStarting().getLocation())
                        .icon(getStartMarker() != null
                                ? getStartMarker() : BitmapDescriptorFactory.fromAssetWithDpi("Icon_start.png"))
                        .zIndex(10));
            }
            // 终点
            if (mRouteLine.getTerminal() != null && mRouteLine.getTerminal().getLocation() != null) {
                overlayOptionses
                        .add((new MarkerOptions())
                                .position(mRouteLine.getTerminal().getLocation())
                                .icon(getTerminalMarker() != null ? getTerminalMarker() :
                                        BitmapDescriptorFactory
                                                .fromAssetWithDpi("Icon_end.png"))
                                .zIndex(10));
            }

            return overlayOptionses;

        }

        private BitmapDescriptor getIconForStep(MassTransitRouteLine.TransitStep step) {
            switch (step.getVehileType()) {
                case ESTEP_WALK:
                    return BitmapDescriptorFactory.fromAssetWithDpi("Icon_walk_route.png");
                case ESTEP_TRAIN:
                    return BitmapDescriptorFactory.fromAssetWithDpi("Icon_subway_station.png");
                case ESTEP_DRIVING:
                case ESTEP_COACH:
                case ESTEP_PLANE:
                case ESTEP_BUS:
                    return BitmapDescriptorFactory.fromAssetWithDpi("Icon_bus_station.png");
                default:
                    return null;
            }
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            return false;
        }

        @Override
        public boolean onPolylineClick(Polyline polyline) {
            return false;
        }
    }


    abstract class OverlayManager implements BaiduMap.OnMarkerClickListener, BaiduMap.OnPolylineClickListener {

        BaiduMap mBaiduMap = null;
        private List<OverlayOptions> mOverlayOptionList = null;

        List<Overlay> mOverlayList = null;

        /**
         * 通过一个BaiduMap 对象构造
         *
         * @param baiduMap
         */
        public OverlayManager(BaiduMap baiduMap) {
            mBaiduMap = baiduMap;
            // mBaiduMap.setOnMarkerClickListener(this);
            if (mOverlayOptionList == null) {
                mOverlayOptionList = new ArrayList<OverlayOptions>();
            }
            if (mOverlayList == null) {
                mOverlayList = new ArrayList<Overlay>();
            }
        }

        /**
         * 覆写此方法设置要管理的Overlay列表
         *
         * @return 管理的Overlay列表
         */
        public abstract List<OverlayOptions> getOverlayOptions();

        /**
         * 将所有Overlay 添加到地图上
         */
        public final void addToMap() {
            if (mBaiduMap == null) {
                return;
            }

            removeFromMap();
            List<OverlayOptions> overlayOptions = getOverlayOptions();
            if (overlayOptions != null) {
                mOverlayOptionList.addAll(getOverlayOptions());
            }

            for (OverlayOptions option : mOverlayOptionList) {
                mOverlayList.add(mBaiduMap.addOverlay(option));
            }
        }

        /**
         * 将所有Overlay 从 地图上消除
         */
        public final void removeFromMap() {
            if (mBaiduMap == null) {
                return;
            }
            for (Overlay marker : mOverlayList) {
                marker.remove();
            }
            mOverlayOptionList.clear();
            mOverlayList.clear();

        }

        /**
         * 缩放地图，使所有Overlay都在合适的视野内
         * <p>
         * 注： 该方法只对Marker类型的overlay有效
         * </p>
         */
        public void zoomToSpan() {
            if (mBaiduMap == null) {
                return;
            }
            if (mOverlayList.size() > 0) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Overlay overlay : mOverlayList) {
                    // polyline 中的点可能太多，只按marker 缩放
                    if (overlay instanceof Marker) {
                        builder.include(((Marker) overlay).getPosition());
                    }
                }
                mBaiduMap.setMapStatus(MapStatusUpdateFactory
                        .newLatLngBounds(builder.build()));
            }
        }

    }


}

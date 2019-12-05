package com.example.nnugd;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.UiSettings;
import com.example.nnugd.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements OnClickListener, AMap.OnMyLocationChangeListener, RadioGroup.OnCheckedChangeListener {

    //定义地科院经纬度坐标
    private LatLng centerDKYPoint = new LatLng(32.114766, 118.916223);

    //是否需要检测后台定位权限，设置为true时，如果用户没有给予后台定位权限会弹窗提示
    private boolean needCheckBackLocation = false;
    //如果设置了target > 28，需要增加这个权限，否则不会弹出"始终允许"这个选择框
    private static String BACK_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION";

    private List<Marker> markerlst;

    private TextView input_edittext;

    //private List<Beacon> beacons099=new ArrayList<Beacon>();
    private List<Beacon> beacons100=new ArrayList<Beacon>();
    private List<Beacon> beacons103=new ArrayList<Beacon>();
    private List<Beacon> beacons104=new ArrayList<Beacon>();

    private MapView mapView;
    private AMap aMap;
    private Button basicmap;
    private Button rsmap;
    private Button nightmap;

    private List<POI> poilevel099 = new ArrayList<POI>();
    private List<POI> poilevel100 = new ArrayList<POI>();
    private List<POI> poilevel103 = new ArrayList<POI>();
    private List<POI> poilevel104 = new ArrayList<POI>();

    MyLocationStyle myLocationStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalysisPoiJson();
        AnalysisBeaconJson();
        if (Build.VERSION.SDK_INT > 28
                && getApplicationContext().getApplicationInfo().targetSdkVersion > 28) {
            needPermissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    BACK_LOCATION_PERMISSION
            };
            needCheckBackLocation = true;
        }
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(centerDKYPoint,
                    18, 0, 0)));

            aMap.getUiSettings().setMyLocationButtonEnabled(true);
//            aMap.getUiSettings().setCompassEnabled(true);
            setUpMap();
            RadioGroup level_choose = findViewById(R.id.level_choose);
            level_choose.setOnCheckedChangeListener(this);

            findViewById(R.id.btn_search).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    input_edittext=findViewById(R.id.input_edittext);
                    Serach(input_edittext.getText().toString());
                }
            });


        }

        //设置SDK 自带定位消息监听
        aMap.setOnMyLocationChangeListener(this);
        //设置定位模式
        aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER));
        //地图类型按钮监听
        basicmap = (Button) findViewById(R.id.basicmap);
        basicmap.setOnClickListener(this);
        rsmap = (Button) findViewById(R.id.rsmap);
        rsmap.setOnClickListener(this);
        nightmap = (Button) findViewById(R.id.nightmap);
        nightmap.setOnClickListener(this);


    }

    /**
     * 往地图上添加一个groundoverlay覆盖物
     */
    private void addOverlayToMap(int id) {
        //清除
        aMap.clear();
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(32.114452, 118.915633))
                .include(new LatLng(32.115038, 118.916669)).build();

        aMap.addGroundOverlay(new GroundOverlayOptions()
                .anchor(0.5f, 0.5f)
                .transparency(0.7f)
//				.zIndex(GlobalConstants.ZindexLine - 1)
                .image(BitmapDescriptorFactory
                        .fromResource(id))
                .positionFromBounds(bounds));
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        try {
            super.onResume();
            mapView.onResume();
            if (Build.VERSION.SDK_INT >= 23) {
                if (isNeedCheck) {
                    checkPermissions(needPermissions);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 设置地图样式
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.basicmap:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
                break;
            case R.id.rsmap:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
                break;
            case R.id.nightmap:
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图模式
                break;
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {

        // 如果要设置定位的默认状态，可以在此处进行设置
        myLocationStyle = new MyLocationStyle();
        aMap.setMyLocationStyle(myLocationStyle);

        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

    }

    @Override
    public void onMyLocationChange(Location location) {
        // 定位回调监听
        if (location != null) {
            Log.e("amap", "onMyLocationChange 定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                String errorInfo = bundle.getString(MyLocationStyle.ERROR_INFO);
                // 定位类型，可能为GPS WIFI等，具体可以参考官网的定位SDK介绍
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);
                /*
                errorCode
                errorInfo
                locationType
                */
                Log.e("amap", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType);
            } else {
                Log.e("amap", "定位信息， bundle is null ");

            }

        } else {
            Log.e("amap", "定位失败");
        }
    }

    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            BACK_LOCATION_PERMISSION
    };
    private static final int PERMISSON_REQUESTCODE = 0;

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;


    /**
     * @param
     * @since 2.5.0
     */
    @TargetApi(23)
    private void checkPermissions(String... permissions) {
        try {
            if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (null != needRequestPermissonList
                        && needRequestPermissonList.size() > 0) {
                    try {
                        String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                        Method method = getClass().getMethod("requestPermissions", new Class[]{String[].class, int.class});
                        method.invoke(this, array, 0);
                    } catch (Throwable e) {

                    }
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    @TargetApi(23)
    private List<String> findDeniedPermissions(String[] permissions) {
        try {
            List<String> needRequestPermissonList = new ArrayList<String>();
            if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
                for (String perm : permissions) {
                    if (checkMySelfPermission(perm) != PackageManager.PERMISSION_GRANTED
                            || shouldShowMyRequestPermissionRationale(perm)) {
                        if (!needCheckBackLocation
                                && BACK_LOCATION_PERMISSION.equals(perm)) {
                            continue;
                        }
                        needRequestPermissonList.add(perm);
                    }
                }
            }
            return needRequestPermissonList;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private int checkMySelfPermission(String perm) {
        try {
            Method method = getClass().getMethod("checkSelfPermission", new Class[]{String.class});
            Integer permissionInt = (Integer) method.invoke(this, perm);
            return permissionInt;
        } catch (Throwable e) {
        }
        return -1;
    }

    private boolean shouldShowMyRequestPermissionRationale(String perm) {
        try {
            Method method = getClass().getMethod("shouldShowRequestPermissionRationale", new Class[]{String.class});
            Boolean permissionInt = (Boolean) method.invoke(this, perm);
            return permissionInt;
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private boolean verifyPermissions(int[] grantResults) {
        try {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (requestCode == PERMISSON_REQUESTCODE) {
                    if (!verifyPermissions(paramArrayOfInt)) {
                        showMissingPermissionDialog();
                        isNeedCheck = false;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private void showMissingPermissionDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("当前应用缺少必要权限。\\n\\n请点击\\\"设置\\\"-\\\"权限\\\"-打开所需权限");

            // 拒绝, 退出应用
            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                finish();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });

            builder.setPositiveButton("设置",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startAppSettings();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });

            builder.setCancelable(false);

            builder.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private void startAppSettings() {
        try {
            Intent intent = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据各楼层的pois添加对应的marker
     *
     * @param pois
     */
    public void AddMarkWithpois(List<POI> pois) {
        for (int i = 0; i < pois.toArray().length; i++) {
            POI poi = pois.get(i);
            LatLng latlng = new LatLng(poi.latitude, poi.longitude);
            LatLng latlng1=convertCoordinate(latlng);
            Marker marker = aMap.addMarker(new MarkerOptions().title(poi.building_name).snippet(poi.name)
                    .icon(BitmapDescriptorFactory.fromResource(GetPOIDrawableId(poi.poi_type)))
                    .position(latlng1));
        }

    }

    /**
     * 按楼层分离pois
     *
     * @param pois
     */
    public void SeparatePois(List<POI> pois) {
        for (int i = 0; i < pois.toArray().length; i++) {
            POI poi = pois.get(i);
            switch (poi.level_code) {
                case "099":
                    poilevel099.add(poi);
                    break;
                case "100":
                    poilevel100.add(poi);
                    break;
                case "103":
                    poilevel103.add(poi);
                    break;
                case "104":
                    poilevel104.add(poi);
                    break;
            }
        }
    }

    /**
     * 解析poi json文件
     */

    public void AnalysisPoiJson() {
        String json = getJson(this, "poi.json");
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray tiles = jsonObject.getJSONArray("tiles");
            for (int j = 0; j < tiles.length(); j++) {
                JSONObject tile = (JSONObject) tiles.get(j);
                List<POI> pois = new ArrayList<POI>();
                JSONArray poiArray = tile.getJSONArray("pois");
                for (int k = 0; k < poiArray.length(); k++) {
                    JSONObject poiObject = (JSONObject) poiArray.get(k);
                    POI poi = new POI();
                    poi.title = poiObject.getString("title");
                    //楼层
                    poi.level_code = poiObject.getString("level_code");
                    //房间名
                    poi.name = poiObject.getString("name");
                    poi.map_scale = poiObject.getInt("map_scale");
                    poi.staff = poiObject.getString("staff");
                    //poi.visitor = poiObject.getBoolean("visitor");
                    poi.tile_code = poiObject.getString("tile_code");
                    //poi.interest_type = poiObject.getInt("interest_type");
                    //poi.parking_field = poiObject.getBoolean("parking_field");
                    JSONObject introduction = poiObject.getJSONObject("introduction");
                    //地科院
                    poi.building_name = poiObject.getString("building_name");
                    poi.descriptions_title = introduction.getString("title");
                    poi.descriptions_text = introduction.getString("text");
                    poi.website = poiObject.getString("website");
                    poi.tel = poiObject.getString("tel");
                    poi.address = poiObject.getString("address");
                    //经纬度
                    poi.latitude = poiObject.getDouble("latitude");
                    poi.longitude = poiObject.getDouble("longitude");
                    //类型
                    poi.poi_type = poiObject.getString("poi_type");
                    JSONArray imageArray = poiObject.getJSONArray("images");
                    pois.add(poi);
                }
                SeparatePois(pois);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 楼层切换监听
     *
     * @param radioGroup
     * @param i
     */
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.FloorP:
                addOverlayToMap(R.drawable.level_099);
                AddMarkWithpois(poilevel099);
                break;
            case R.id.Floor1:
                addOverlayToMap(R.drawable.level_100);
                AddMarkWithpois(poilevel100);
                break;
            case R.id.Floor4:
                addOverlayToMap(R.drawable.level_103);
                AddMarkWithpois(poilevel103);
                break;
            case R.id.Floor5:
                addOverlayToMap(R.drawable.level_104);
                AddMarkWithpois(poilevel104);
                break;
        }
    }

    /**
     * 获取poi的资源图片
     *
     * @param poi_type
     * @return
     */
    public int GetPOIDrawableId(String poi_type) {
        switch (poi_type) {
            case "entrance":
                return R.drawable.entrance;
            case "lift":
                return R.drawable.lift;
            case "room":
                return R.drawable.room;
            case "stairs":
                return R.drawable.stairs;
            case "washroom":
                return R.drawable.washroom;
            case "parking":
                return R.drawable.parking;
            default:
                return 0;
        }
    }

    /**
     * 获取楼层的资源图片
     *
     * @param level_code
     * @return
     */
    public int GetFloorPlanId(String level_code) {
        switch (level_code) {
            case "099":
                return R.drawable.level_099;
            case "100":
                return R.drawable.level_100;
            case "103":
                return R.drawable.level_103;
            case "104":
                return R.drawable.level_104;
            default:
                return 0;
        }

    }

    public static String getJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 坐标转换
     *
     * @param coordinate
     * @return
     */
    public static LatLng convertCoordinate(LatLng coordinate) {
        LocationConverter.LatLng latLng = new LocationConverter.LatLng(coordinate.latitude, coordinate.longitude);
        LocationConverter.LatLng convertedLatLng = LocationConverter.wgs84ToGcj02(latLng);
        LatLng newCoordinate = new LatLng(convertedLatLng.latitude, convertedLatLng.longitude);
        return newCoordinate;
    }

    /**
     * 解析beacon.json
     */
    public void AnalysisBeaconJson(){
        String json = getJson(this, "beacon.json");
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray tiles = jsonObject.getJSONArray("tiles");
            for (int i = 0; i < tiles.length(); i++) {
                JSONObject tile = (JSONObject) tiles.get(i);
                JSONArray beaconArray = tile.getJSONArray("beacons");
                List<Beacon> beacons = new ArrayList<Beacon>();
                for (int j = 0; j < beaconArray.length(); j++) {
                    JSONObject beaconObject = (JSONObject)beaconArray.get(j);
                    Beacon beacon = new Beacon();
                    beacon.uuid = Beacon.formatUUID(beaconObject.getString("uuid"));
                    beacon.plate_id = beaconObject.getString("plate_code");
                    beacon.tile_code = beaconObject.getString("tile_code");
                    beacon.level_code = beaconObject.getString("level_code");
                    beacon.name = beaconObject.getString("name");
                    beacon.searchable = (beaconObject.getLong("searchable") == 1);
                    beacon.speakout = beaconObject.getString("speakout");
                    beacon.room_id = beaconObject.getString("room_code");
                    beacon.room_name = beaconObject.getString("room_name");
                    beacon.building_id = beaconObject.getString("building_id");
                    beacon.building_name = beaconObject.getString("building_name");
                    beacon.postal_code = beaconObject.getString("postal_code");
                    beacon.type = beaconObject.getLong("beacon_type");
                    beacon.m_power = beaconObject.getLong("m_power");
                    beacon.latitude = beaconObject.getDouble("latitude");
                    beacon.longitude = beaconObject.getDouble("longitude");
                    beacon.indoor = (beaconObject.getLong("indoor") == 1);
                    beacons.add(beacon);
                }
                SeparateBeacons(beacons);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 按楼层分离beacons
     * @param Beacons
     */
    public void SeparateBeacons(List<Beacon> Beacons){
        for(Beacon beacon :Beacons){
            switch (beacon.level_code){
                case "100":
                    if(beacon.searchable){
                        beacons100.add(beacon);
                        break;
                    }
                    break;
                case "103":
                    if(beacon.searchable){
                        beacons103.add(beacon);
                        break;
                    }
                    break;
                case "104":
                    if(beacon.searchable){
                        beacons104.add(beacon);
                        break;
                    }
                    break;
                default:
                    break;
            }
        }

    }

    public void Serach(String name){
        int room=Integer.parseInt(name);
        if(room>500&room<600){
//            findViewById(R.id.Floor5).setSelected(true);
            addOverlayToMap(R.drawable.level_104);
            AddMarkWithpois(poilevel104);
            for(int i=0;i<beacons104.toArray().length;i++)
            {
                if(name.equals(beacons104.get(i).name)){
                    AddMarkerWithSearch(beacons104.get(i));
                    break;
                }
                if(i==beacons104.toArray().length-1){
                    Toast toast=Toast.makeText(this,"请重新输入合法的房间名!",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        }
        else if(room<500&room>400){
//            findViewById(R.id.Floor4).setSelected(true);
            addOverlayToMap(R.drawable.level_103);
            AddMarkWithpois(poilevel103);
            for(int i=0;i<beacons103.toArray().length;i++){
                if(name.equals(beacons103.get(i).name)){
                    AddMarkerWithSearch(beacons103.get(i));
                    break;
                }
                if(i==beacons103.toArray().length-1){
                    Toast toast=Toast.makeText(this,"请重新输入合法的房间名!",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        }
        else if(room<200&room>100){
//            findViewById(R.id.Floor1).setSelected(true);
            addOverlayToMap(R.drawable.level_100);
            AddMarkWithpois(poilevel100);
            for(int i=0;i<beacons100.toArray().length;i++){
                if(name.equals(beacons100.get(i).name)){
                    AddMarkerWithSearch(beacons100.get(i));
                    break;
                }
                if(i==beacons100.toArray().length-1){
                    Toast toast=Toast.makeText(this,"请重新输入合法的房间名!",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
        else {
            Toast toast=Toast.makeText(this,"请重新输入合法的房间名!",Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    public void AddMarkerWithSearch(Beacon beacon){
        LatLng latlng = new LatLng(beacon.latitude, beacon.longitude);
        LatLng latlng1=convertCoordinate(latlng);
        Marker marker = aMap.addMarker(new MarkerOptions().title("地科院").snippet(beacon.name)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_to))
                .position(latlng1));
    }
//    /**
//     * 向指定的URL发送POST方法的请求
//     * @param url    发送请求的URL
//     * @return       远程资源的响应结果
//     */
//    public static String sendPost(String url) {
//        StringBuilder json = new StringBuilder();
//        try {
//            URL urlObject = new URL(url);
//            URLConnection uc = urlObject.openConnection();
//            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(),"UTF-8"));
//            String inputLine = null;
//            while ( (inputLine = in.readLine()) != null) {
//                json.append(inputLine);
//            }
//            in.close();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return json.toString();
//    }
}

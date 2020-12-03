package tw.tcnr18.m1901;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class M1901 extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener {
    /**********************************************
     Layout
     **********************************************/
    private TextView txtOutput;
    private TextView tmsg;
    private Menu menu;
    private MenuItem b_item3, b_item4;
    private Spinner mSpnLocation, mSpnMapType;
    private ScrollView controlScroll;
    private CheckBox checkBox;

    private String TAG = "tcnr18=>";
    /**********************************************
     MAP
     **********************************************/
    private GoogleMap map;
    static LatLng VGPS = new LatLng(24.172127, 120.610313);
    /*    ...................................................................
        .  設定開啟地圖時,顯示倍數
        ..................................................................*/
    private float mapzoom = 16;
    private static String[] mapType = {"街道圖", "衛星圖", "地形圖", "混合圖", "開啟路況", "關閉路況"};
    private static BitmapDescriptor image_des;//圖標顯示
    private int icosel; //圖示旗標
    private String Selname;
    private String Seladdress;

    private double dLat, dLon;
    private LocationManager locationManager;
    private Location currentLocation;
    private Marker markerMe;
    private String provider; // 提供資料
    /*    ...................................................................
    .  設定地圖位置改變時間
    ..................................................................*/
    long minTime = 5000;// ms
    float minDist = 50.0f;// meter

    private ArrayList<LatLng> mytrace;//追蹤我的位置
    private int routeon = 0;
    private int routeon_type=1;
    private Polyline mPolyline;
    private String key = "key=" + "javascriptmap key";  //需使用 javascriptmap key;
    private int iSelect = 0;
    private String TGPS;
    //===============
    private int resID = 0;
    private int resID1 = 0;

    float Anchor_x = 0.5f;
    float Anchor_y = 0.9f;

    float infoAnchor_x = 0.5f;//水滴水平錨點
    float infoAnchor_y = 1.0f;
    //-------------------------------------------------
    private double ch_lat = 0;
    private double ch_lng = 0;
    private String ch_Myname = "";
    private String ch_id = "0";
    private int use_Tong = 1;  //更新資料庫音效  1=>開啟  0=>關閉
    /**********************************************
     SQLite
     **********************************************/
    int DBConnectorError = 0;
    private FriendDbHelper dbHper;
    private static final String DB_FILE = "friends.db";
    private static final String DB_TABLE = "member";
    private static final int DBversion = 1;

    private ArrayList<String> recSet;
    private String selectMYSQL = "";
    private String result = "";
    /**********************************************
     MySQL
     **********************************************/
    private String Myid = "0";
    private String Myname = "88號簡老爹";
    private String Myaddress = "24.172127,120.610313";
    private String Mygroup = "第零組"; //群組
    private long update_min;
    private int user_select;
    private String sqlctl;
    /**********************************************
     Thread Hander
     **********************************************/
//    private Handler mHandler = new Handler();
//    private long timer = 20; // thread每幾秒run 多久更新一次資料
//    private long timerang = 20; // 設定幾秒刷新Mysql
//    private Long startTime = System.currentTimeMillis(); // 上回執行thread time
//    private Long spentTime;

    /**********************************************
     permission
     **********************************************/
    //所需要申請的權限數組
    private static final String[][] permissionsArray = new String[][]{
            {Manifest.permission.ACCESS_FINE_LOCATION, String.valueOf(R.string.dialog_msg1)},
            {Manifest.permission.WRITE_EXTERNAL_STORAGE, String.valueOf(R.string.dialog_msg2)}
    };
    private List<String> permissionsList = new ArrayList<String>();
    //申請權限後的返回碼
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    //    ========================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableStrictMode(this);
        checkRequiredPermission(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1901);
        //--------設定MapFragment-------------------------------------------------------------------------------
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //-------------------------------------------------------------------------------------------------------------
//        checkRequiredPermission(this);
        initLocationProvider(); //核示定位
        // 設定class標題=======================
        Intent intent01 = this.getIntent();
        Myname = intent01.getStringExtra("googlename");
//        Mygroup = intent01.getStringExtra("googleEmail");
//=================================
        setupViewComponent();
    }

    private void setupViewComponent() {
        mSpnLocation = (Spinner) this.findViewById(R.id.spnLocation);
        mSpnMapType = (Spinner) this.findViewById(R.id.spnMapType);
        txtOutput = (TextView) findViewById(R.id.txtOutput);
        tmsg = (TextView) findViewById(R.id.msg);
        //---設定control控制鈕----------
        checkBox = (CheckBox) this.findViewById(R.id.checkcontrol);
        controlScroll = (ScrollView) this.findViewById(R.id.Scroll01);
        checkBox.setOnCheckedChangeListener(chklistener);
        controlScroll.setVisibility(View.INVISIBLE);
        //Parameters:對應的三個常量值: VISIBLE=0 INVISIBLE=4 GONE=8
        Show_MapType(); //設定地圖顯示圖層
        icosel = 0; //設定圖示初始值
        initDB();
        //-------檢查使用者是否存在--------------
        MySQL_Select(Myname);
/*****************************************************/
        /*        執行自動匯入                                                             */
/*****************************************************/
        // 設定Delay的時間
        // mHandler.postDelayed(updateTimer, timer * 1000);
//        MySQL_SQLite(); // 匯入database  //使用執行自動更新MySQL,開啟此行
        recSet = dbHper.getRecSet(); //重新載入SQLite  //
/******************************************************/
        Show_Spinner(); // 刷新spinner  //執行自動更新MySQL,關閉此行
        //===============檢查是否初次安裝================
        init_app();
        //==========================================
    }

    //=======================================
    private void init_app() {    // 檢查定位是否成功
        try {
            if (initLocationProvider()) {
                nowaddress();
            }
        } catch (Exception e) {
            //對話方塊啟用GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS未開啟")
                    .setMessage("GPS目前狀態是尚未啟用.\n" + "請先開啟定位!,再次執行APP!")
                    .setPositiveButton("離開再次執行", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //使用Intent物件啟動設定程式來更改GPS設定
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                            finish();
                        }
                    }).setNegativeButton("不啟用", null).create().show();
            return;
        }
    }

    //=========================================
    private void Show_MapType() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for (int i = 0; i < mapType.length; i++)
            adapter.add(mapType[i]);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnMapType.setAdapter(adapter);
        //-----------設定ARGB透明度----
        mSpnMapType.setPopupBackgroundDrawable(new ColorDrawable(0xF2FFFFFF));
        mSpnMapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    switch (position) {
                        case 0:
                            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);//道路地圖。
                            break;
                        case 1:
                            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);//衛星空照圖
                            break;
                        case 2:
                            map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);//地形圖
                            break;
                        case 3:
                            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);//道路地圖混合空照圖
                            break;
                        case 4://開啟路況
                            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);//道路地圖。
                            map.setTrafficEnabled(true);//交通路況圖
                            break;
                        case 5://關閉路況
                            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);//道路地圖。
                            map.setTrafficEnabled(false);//交通路況圖
                            break;
                    }
                } catch (Exception e) {
                    Log.d("Error=>", e.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void Show_Spinner() {
        /***************************************
         * 讀取SQLite => Spinner
         *****************************************/
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        for (int i = 0; i < recSet.size(); i++) {
            String[] fld = recSet.get(i).split("#");
//            adapter_s.add(fld[0] + " " + fld[1] + " " + fld[2] + " " + fld[3]);
            adapter.add(fld[1]);
        }
//--------------------------------------------------------
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnLocation.setAdapter(adapter);
        //==========================================
        if (iSelect == 0) {
            mSpnLocation.setSelection(user_select, true); //spinner 小窗跳到所選
        } else {
            mSpnLocation.setSelection(iSelect, true); //spinner 小窗跳到所選
        }
//        mSpnLocation.setSelection(iSelect, true); //spinner 小窗跳到所選
        //==========================================
        //指定事件處理物件
        mSpnLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                iSelect = mSpnLocation.getSelectedItemPosition(); // 找到按何項
                map.clear();
                mytrace = null;//清除軌跡圖
//                showloc();
                setMapLocation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //-------------監聽改變控制鈕------------
    private CheckBox.OnCheckedChangeListener chklistener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (checkBox.isChecked()) {
                controlScroll.setVisibility(View.VISIBLE);
                // Parameters: 對應的三個常量值：VISIBLE=0 INVISIBLE=4 GONE=8
            } else {
                controlScroll.setVisibility(View.INVISIBLE);
            }
        }
    };

    //-----Control控制項設定---------------
    private boolean isChecked(int id) {
        return ((CheckBox) findViewById(id)).isChecked();
    }

    //----檢查GoogleMap是否正確開啟---------
    private boolean checkReady() {
        if (map == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //-----地圖縮放----------------------
    public void setZoomButtonsEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        //Enables/disables zoom controls (+/-buttons in the bottom right of the map).
        map.getUiSettings().setZoomControlsEnabled(((CheckBox) v).isChecked());
    }

    //-----設定指北針-----------------------
    public void setCompassEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        //Enables/disables the compass (icon in the top left that indicates the orientation of the map).
        map.getUiSettings().setCompassEnabled(((CheckBox) v).isChecked());
    }

    //---顯示我的位置座標圖示
    public void setMyLocationLayerEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        //----------取得定位許可-----------------------
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
            map.setMyLocationEnabled(((CheckBox) v).isChecked());
        } else {
            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        }
    }

    // ----可用手勢操控
    public void setScrollGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables scroll gestures (i.e. panning the map).
        map.getUiSettings().setScrollGesturesEnabled(((CheckBox) v).isChecked());
    }

    // ----按兩下按一下或兩指拉大拉小----
    public void setZoomGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables zoom gestures (i.e., double tap, pinch & stretch).
        map.getUiSettings().setZoomGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setTiltGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables tilt gestures.
        map.getUiSettings().setTiltGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setRotateGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables rotate gestures.
        map.getUiSettings().setRotateGesturesEnabled(((CheckBox) v).isChecked());
    }

    // -----------------------------------------------------
    private void setMapLocation() {
        showloc(); //刷新所有景點
        try {
            //----------------------------------------------------------------
            recSet = dbHper.getRecSet();  //重新載入SQLite
            String[] fld = recSet.get(iSelect).split("#");
            Selname = fld[1];// 地名
            Seladdress = fld[3];// 緯經
            String[] sLocation = Seladdress.split(",");

            double dLat = Double.parseDouble(sLocation[0]); // 南北緯
            double dLon = Double.parseDouble(sLocation[1]); // 東西經
            String vtitle = Selname;//選擇的名字
            //--- 設定所選位置之當地圖示---//
            image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE); //使用系統水滴
            //水滴可選擇樣式顏色
            VGPS = new LatLng(dLat, dLon);
            //----設定自訂義infowindow----//

            /************************************
             * 此處要特別注意
             **************************************/
            for (int i = 0; i < recSet.size(); i++) {
                if (iSelect >= 0 && iSelect < 7) {
                    String imgName = "q" + String.format("%02d", iSelect);
                    resID1 = getResources().getIdentifier(imgName, "drawable",
                            getPackageName());
                } else {
                    resID1 = getResources().getIdentifier("q99", "drawable",
                            getPackageName());//超出範圍 用q99.png
                }
            }

            if (Selname.equals(Myname)) {
                String imgName = "z00";
                resID1 = getResources().getIdentifier(imgName, "drawable", getPackageName());
            }

            vtitle = vtitle + "#" + resID1;//存放圖片號碼
//**************************************
            map.setInfoWindowAdapter(new CustomInfoWindowAdapter());//產生一個副calss在本class裡面
            map.setOnMarkerClickListener(this);
            //map.setOnInfoWindowClickListener(this);
            //map.setOnMarkerDragListener(this);
            map.addMarker(new MarkerOptions()
                    .position(VGPS)
                    .title(vtitle)
                    .snippet("座標:" + dLat + "," + dLon)
                    .infoWindowAnchor(Anchor_x, Anchor_y)
                    .icon(image_des));//顯示圖標文字
            //================================
            if (routeon == 1) {
                String[] sLocationb = Myaddress.split(",");
                double dLat1 = Double.parseDouble(sLocationb[0]);    // 南北緯
                double dLon1 = Double.parseDouble(sLocationb[1]);    // 東西經
                VGPS = new LatLng(dLat1, dLon1);
            }
            //===============================
            mapzoom = map.getCameraPosition().zoom;  //取得目前zoom大小

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, mapzoom));
            onCameraChange(map.getCameraPosition());//始終把圖放在中間
            //----------------------------------------------------------------------------------
        } catch (Exception e) {
            Log.d("Error=>", e.toString());
        }

    }

//---------------------------------------------------------------------------------

    /*** onCameraChange */
    private void onCameraChange(CameraPosition cameraPosition) {
    }

    private void showloc() {
        for (int i = 0; i < recSet.size(); i++) {
            String[] fld = recSet.get(i).split("#");
//            adapter.add(fld[0] + " " + fld[1] + " " + fld[2] + " " + fld[3]);
            String[] sLocation = fld[3].split(",");
            double dLat_showloc = Double.parseDouble(sLocation[0]); // 南北緯
            double dLon_showloc = Double.parseDouble(sLocation[1]); // 東西經
            String vtitle = fld[1];
            resID = 0;//從R裡面抓出來的機碼(配置碼)
            resID1 = 0;
            // ---設定所選位置之當地圖片---//
            //drawable目錄下存放q01.png ~ q06.png t01.png ~t07.png 超出範圍用t99.png & q99.png
            if (i >= 0 && i < 7) {
                String idName = "t" + String.format("%02d", i);
                String imgName = "q" + String.format("%02d", i);
                resID = getResources().getIdentifier(idName, "drawable",
                        getPackageName());
                resID1 = getResources().getIdentifier(imgName, "drawable",
                        getPackageName());
                image_des = BitmapDescriptorFactory.fromResource(resID);// 使用照片
            } else {
                resID = getResources().getIdentifier("t99", "drawable",
                        getPackageName());//超出範圍 用t99.png
                resID1 = getResources().getIdentifier("q99", "drawable",
                        getPackageName());//超出範圍 用q99.png
            }
            // ---設定所選位置之當地圖片---//
            switch (icosel) {
                case 0:
                    image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET); // 使用橘色系統水滴
//                    Anchor_x = -0.5f;
//                    Anchor_y = 1.0f;
                    break;
                case 1:
                    // 運用巨集
                    image_des = BitmapDescriptorFactory.fromResource(resID);// 使用照片
                    break;
            }
            vtitle = vtitle + "#" + resID1;//存放圖片號碼
            VGPS = new LatLng(dLat_showloc, dLon_showloc);// 更新成欲顯示的地圖座標
//            ---根據所選位置項目顯示地圖 / 標示文字與圖片-- -//
            if (i != iSelect) {
                markerMe = map.addMarker(new MarkerOptions()
                        .position(VGPS)
                        .alpha(0.9f)
                        .title(i + "." + vtitle)
                        .snippet("緯度:" + String.valueOf(dLat_showloc) + "\n經度:" + String.valueOf(dLon_showloc))
                        .infoWindowAnchor(Anchor_x, Anchor_y)//設定圖標的基點位置
                        .icon(image_des)// 顯示圖標文字
                        .draggable(false)//設定marker可移動
                        .visible(true)
                );
            } else {
                if (markerMe != null) {
                    markerMe.remove();
                    markerMe = null;
                }
                markerMe = map.addMarker(new MarkerOptions()
                        .position(VGPS)
                        .alpha(0.9f)
                        .title(i + "." + vtitle)
                        .snippet("緯度:" + String.valueOf(dLat_showloc) + "\n經度:" + String.valueOf(dLon_showloc))
                        .infoWindowAnchor(Anchor_x, Anchor_y)//設定圖標的基點位置
                        .icon(image_des)// 顯示圖標文字
                        .draggable(false)//設定marker可移動
                        .visible(false));
            }


            //--------使用自定義視窗-------------------------
            map.setInfoWindowAdapter(new CustomInfoWindowAdapter());//外圓內方
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
//        mUiSettings = map.getUiSettings();//
        map.getUiSettings().setScrollGesturesEnabled(true);   //        開啟 Google Map 拖曳功能
        map.getUiSettings().setMapToolbarEnabled(true);       //        右下角的導覽及開啟 Google Map功能
        map.getUiSettings().setCompassEnabled(true);            //        左上角顯示指北針，要兩指旋轉才會出現
        map.getUiSettings().setZoomControlsEnabled(true);  //        右下角顯示縮放按鈕的放大縮小功能
        // --------------------------------
        map.addMarker(new MarkerOptions().position(VGPS).title("中區職訓"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, mapzoom));
        //------------------取得許可-----------------------------------------
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            return;
        } else {
            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        }
    }
/* *******************************************
             GPS位置座標
********************************************/
// ============ GPS =================

    /*** onMyLocationButtonClick */
    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(getApplicationContext(), "返回GPS目前位置", Toast.LENGTH_SHORT).show();
        return false;//使用false則用內定返回現在位置
    }

    private boolean initLocationProvider() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            return true;
        } else {
            return false;
        }
    }

    /*** 位置變更狀態監視*/
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
            tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    tmsg.setText("Out of Service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    tmsg.setText("Temporarily Unavailable");
                    break;
                case LocationProvider.AVAILABLE:
                    tmsg.setText("Available");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }
    };

    private void nowaddress() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(provider);
            updateWithNewLocation(location); //*****開啟GPS定位
            return;
        }

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            tmsg.setText("GPS 未開啟");
        else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                tmsg.setText("使用網路GPS");
            }

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                tmsg.setText("使用精確GPS");
            }
        }
    }

    private void updateWithNewLocation(Location location) {
        //----------紀錄更新時間及zoom大小--------
        float zoom = map.getCameraPosition().zoom;
        update_min = System.currentTimeMillis();
        //-----------------------------------------------
        String where = "";
        if (location != null) {
            double now_lat = location.getLatitude();//緯度
            double now_lng = location.getLongitude();//經度
            float speed = location.getSpeed();//速度
            long time = location.getTime();//時間
            String timeString = getTimeString(time);
            where = "經度:" + now_lng + "\n緯度:" + now_lat + "\n速度:" + speed + "\n時間:" + timeString + "\nProvider:" + provider;
            Myaddress = now_lat + "," + now_lng;
/* *******************************************
             變更sqlite我的位置座標
********************************************/
////-------------尋找使用者資料-----------
            ArrayList<String> recSet_use = dbHper.query_user(Myname);
            String[] fld = recSet_use.get(0).split("#");
            Myid = fld[0];
            Myname = fld[1];
            Mygroup = fld[2];
////            Myaddress = fld[3];
////---------------------------------------------------------------------
            ch_id = Myid;
            ch_Myname = Myname;
            ch_lat = now_lat;
            ch_lng = now_lng;
            Myaddress = ch_lat + "," + ch_lng;
/********************************************
 位置更新需更改資料庫
 ********************************************/
            MySQL_Select(Myname);     //位置移動是否更新資料庫  重覆執行多次
//                MySQL_SQLite(); // 匯入database 已經寫在 MySQL_Select
////////////////////////////////////////
            int rowsAffected = dbHper.updateRec(Myid, Myname, Mygroup, Myaddress); //修改SQLite目前位置座標
            Show_Spinner(); // 刷新spinner
//--------------------------------------
            //標記"我的位置"
//            showMarkerMe(now_lat, now_lng);
//            trackMe(lat, lng);//軌跡圖
            //--是否使用導航=====================
            if (routeon == 1) {
                cameraFocusOnMe(now_lat, now_lng);
                u_routeuse(now_lat, now_lng);   //未設定使用者更新座標=>不可使用
            }
            //========軌跡圖==============
            //                trackMe(lat, lng);//軌跡圖
            //==========================
        } else {
            where = "*位置訊號消失*";
        }
        //位置改變顯示
        txtOutput.setText(where);
    }

    private void u_routeuse(double lat, double lng) {
        // Already two locations
//==========================
        String toWhere = mSpnLocation.getSelectedItem().toString().trim();
        recSet = dbHper.query(toWhere);
        String[] fld = recSet.get(0).split("#");
        String vtitle1 = fld[1];
        TGPS = fld[3];
        String[] sLocationb = TGPS.split(",");
        double dLat1 = Double.parseDouble(sLocationb[0]);    // 南北緯
        double dLon1 = Double.parseDouble(sLocationb[1]);    // 東西經
//==========================
//        /** 起始及終點位置符號顏色      */
//                    Log.d(TAG, "test:"+"(" + lat + "," + lng+")   (" + dLat1 + "," + dLon1+")");
        LatLng origin = new LatLng(lat, lng);         //使用者
        LatLng dest = new LatLng(dLat1, dLon1); //目標
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions
        // API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;      // Origin of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;         // Destination of route
//Key
//uilding the parameters to the web service
//Travelling Mode-------------------------
//mode = "mode=driving";
//mode = "mode=bicycling";
// mode = "mode=walking";
// mode = "mode=transit";
//        DRIVING ( 開車 )、BICYCLING ( 腳踏車 )、TRANSIT ( 大眾運輸 ) 和 WALKING ( 走路 ) 四種模式
//------------------------
        String mode = "mode=driving";
        switch (routeon_type){
            case 1:
                mode = "mode=driving";
                break;
            case 2:
                mode = "mode=bicycling";
                break;
            case 3:
                mode = "mode=walking";
                break;
            case 4:
                mode = "mode=transit";
                break;
        }
// Building the parameters to the web service
        key = "key=" + getString(R.string.javascriptmap_key);  //需使用 javascriptmap key;
        String parameters = str_origin + "&" + str_dest + "&" + key + "&" + mode;
        // Output format
        String output = "json";
//--------
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    /*追蹤目前我的位置畫軌跡圖*/
    private void trackMe(double lat, double lng) {
        if (mytrace == null) {
            mytrace = new ArrayList<LatLng>();
        }
        mytrace.add(new LatLng(lat, lng));

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : mytrace) {
            polylineOpt.add(latlng);
        }

        polylineOpt.color(Color.BLUE);//軌跡顏色

        Polyline line = map.addPolyline(polylineOpt);//畫線
        line.setWidth(10);//軌跡寬度
        line.setPoints(mytrace);
    }

    /*** cameraFocusOnMe */
    private void cameraFocusOnMe(double lat, double lng) {
        CameraPosition camPosition = new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(map.getCameraPosition().zoom).build();
        /*移動地圖鏡頭*/
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
        tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
    }

    /**************************************
     *             顯示目前位置
     **************************************/
    private void showMarkerMe(double lat, double lng) {
//        if (markerMe != null) {
//            markerMe.remove();
//            markerMe=null;
//        }
        int resID = getResources().getIdentifier("q00", "drawable", getPackageName());
//------------------
        if (icosel != 0) {
            image_des = BitmapDescriptorFactory.fromResource(resID);//使用照片
        } else {
//            image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);//使用系統水滴
            image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);//使用系統水滴
        }
//-------------------------
        dLat = lat; // 南北緯
        dLon = lng; // 東西經
        String vtitle = "GPS位置:" + "#" + resID;
        String vsnippet = "座標:" + String.valueOf(dLat) + "," + String.valueOf(dLon);
        VGPS = new LatLng(lat, lng);// 更新成欲顯示的地圖座標
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng));
        markerOpt.title(vtitle);
        markerOpt.snippet(Myname + "在此\n" + "緯度:" + String.valueOf(lat) + "\n經度:" + String.valueOf(lng));
        markerOpt.infoWindowAnchor(Anchor_x, Anchor_y);//設定圖標的基點位置
        markerOpt.draggable(false);
        markerOpt.icon(image_des);
        markerMe = map.addMarker(markerOpt);
    }

    //**增加Marker監聽 使用Animation動畫**/
    @Override
    public boolean onMarkerClick(final Marker marker_Animation) {
//        if (!marker_Animation.getTitle().substring(0, 4).equals("Move")) {
        //非GPS移動位置;
        //設定動畫
        Log.d(TAG, "onMarkerClick");
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;//連續時間
        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker_Animation.setAnchor(infoAnchor_x, infoAnchor_y + 2 * t);//設定標的位置

                if (t > 0.0) {
                    //Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
//        } else {//GPS移動不做動畫
//            this.markerMe.hideInfoWindow();//不秀InfoWindow
//        }
        return false;
    }

//    /************************************************
//     * Thread Hander 固定要執行的方法
//     ***********************************************/
//    private final Runnable updateTimer = new Runnable() {
//        @Override
//        public void run() {
//            spentTime = System.currentTimeMillis() - startTime;
//            Long second = (spentTime / 1000);// 將運行時間後，轉換成秒數
//            if (second >= timerang) {
//                startTime = System.currentTimeMillis();
////                MySQL_SQLite(); // 匯入database
////                Show_Spinner(); // 刷新spinner
//            }
//            mHandler.postDelayed(this, timer * 1000);// time轉換成毫秒 updateTime
//        }
//    };

    /***********************************************
     * timeInMilliseconds
     ***********************************************/
    private String getTimeString(long timeInMilliseconds) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeInMilliseconds);
    }
    //=============================================

    /************************************************
     * SQLite Database
     ***********************************************/
    private void initDB() {
        if (dbHper == null) {
            dbHper = new FriendDbHelper(this, DB_FILE, null, DBversion);
            MySQL_SQLite();
        }
        recSet = dbHper.getRecSet(); //重新載入SQLite
    }
    //=============================================

    /************************************************
     * SQL Database
     ***********************************************/
    private void MySQL_Select(String myname) {
        selectMYSQL = "";
        result = "";
        try {
            sqlctl = "SELECT name FROM member WHERE name = '" + myname + "' ORDER BY id";
            ArrayList<String> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(sqlctl);
            result = DBConnector.executeQuery(nameValuePairs);
            Log.d(TAG, "result=" + result + "result.length()=" + result.length());
            if (result.length() <= 16) {
                /*******************************
                 * 執行MySQL_Insert()新增個人資料 可以直接呼叫DBConnector.executeInsert(a,b,c);
                 *******************************/
                Log.d(TAG, "1");
                MySQL_Insert(myname, Mygroup, Myaddress);
            } else {
                /**********************************
                 *變更mysql會員的座標
                 **********************************/
//                //-------------------------------------------
//                Log.d(TAG, "2");
//                                    Log.d(TAG, "t2:(" +Myid + ")/("
//                                            +Myid+")"
//                                            + Myname + "/"
//                                            + Myaddress + "user_select="
//                                            + user_select + "iselect="
//                                            + iSelect + "  (" + ch_lat + "," + ch_lng + ")" );
//                //-------------------------------------------
                MySQL_Update(Myid, Myname, Mygroup, Myaddress);//Myid是MySQL的id不是SQLite的

            }
            /**********************************
             *變更mysql會員的座標
             **********************************/
            MySQL_SQLite();
        } catch (Exception e) {
            Log.d("Error=>", e.toString());
        }
    }

    private void MySQL_SQLite() {
        // -----------------------匯入前 先刪除 SQLite 資料------------
        try {
            sqlctl = "SELECT * FROM member ORDER BY id ASC";
            ArrayList<String> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(sqlctl);

            result = DBConnector.executeQuery(nameValuePairs);

//            String result = DBConnector.executeQuery("SELECT * FROM member");

            if (result.length() <= 16) {//php找不到資料會回傳7,所以以7來判斷有沒有找到資料
                DBConnectorError++;//連線失敗次數
                if (DBConnectorError > 3)//連線失敗大於3次
                    Toast.makeText(getApplicationContext(), "伺服器狀態異常,請檢查您的網路狀態!", Toast.LENGTH_LONG).show();
                else//連線失敗小於等於3次
                    Toast.makeText(getApplicationContext(), "伺服器嘗試連線中,請稍候!", Toast.LENGTH_LONG).show();
            } else {//php找到資料,刪除SQLite資料
                DBConnectorError = 0;
//--------------------------------------------------------
                int rowsAffected = dbHper.clearRec();                 // 匯入前,刪除所有SQLite資料
//--------------------------------------------------------
            }
            /** SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
             * jsonData = new JSONObject(result);  */
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                ContentValues newRow = new ContentValues();
                newRow.put("id", jsonData.getString("id").toString());
                newRow.put("name", jsonData.getString("name").toString());
                newRow.put("grp", jsonData.getString("grp").toString());
                newRow.put("address", jsonData.getString("address").toString());
                // -------------------加入SQLite---------------------------------------
                if (jsonData.getString("name").toString().equals(ch_Myname)) {
                    user_select = i; // 儲存會員在spinner 的位置
                    //-------------------------------------------
                    //                    Log.d(TAG, "t2:" + jsonData.getString("name") + "/"
                    //                            + Myname + "/"
                    //                            + ch_Myname + "user_select="
                    //                            + user_select + "iselect="
                    //                            + iSelect + "  (" + ch_lat + "," + ch_lng + ")   (" + jsonData.getString("address").toString() + ")");
                    //-------------------------------------------
                    newRow.put("address", ch_lat + "," + ch_lng);  //匯入時,使用者座標用新座標(無進行MySQL_Update時執行)
                }
                long rowID = dbHper.insertRec_m(newRow);
            }
        } catch (Exception e) {
            Log.d("Error=>", e.toString());
        }
    }


    private void MySQL_Insert(String insmyname, String insmygroup, String insmyaddress) {
        /************使用 DBConnector的新增函數******************************/
//        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//        nameValuePairs.add(new BasicNameValuePair("name", insmyname));
//        nameValuePairs.add(new BasicNameValuePair("grp", insmygroup));
//        nameValuePairs.add(new BasicNameValuePair("address", insmyaddress));
//        String result = DBConnector.executeInsert("SELECT * FROM member", nameValuePairs);

        ArrayList<String> nameValuePairs = new ArrayList<>();
//        nameValuePairs.add(sqlctl);
        nameValuePairs.add(insmyname);
        nameValuePairs.add(insmygroup);
        nameValuePairs.add(insmyaddress);
        try {
            Thread.sleep(100); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = DBConnector.executeInsert(nameValuePairs);
//-----------------------------------------------
    }

    private void MySQL_Update(String upmyid, String upmyname, String upmygroup, String upmyaddress) {
        /*********************************
         * 使用 DBConnector的mysql_update函數
         /*********************************
         * 使用 DBConnector的mysql_update函數
         *********************************/

        ArrayList<String> nameValuePairs = new ArrayList<>();
//        nameValuePairs.add(sqlctl);
        nameValuePairs.add(upmyid);
        nameValuePairs.add(upmyname);
        nameValuePairs.add(upmygroup);
        nameValuePairs.add(upmyaddress);
        try {
            Thread.sleep(50); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = DBConnector.executeUpdate(nameValuePairs);
        Log.d(TAG, "Updateresult:" + result);
//-----------------------------------------------

        //------ 宣告鈴聲 ---------------------------
        if (use_Tong == 1) {
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 80); // 100=max
            //           toneG.startTone(ToneGenerator.TONE_SUP_INTERCEPT, 500);  //
            //            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 50); //50毫秒,聲音持續時間
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); //50毫秒,聲音持續時間
            toneG.release();
        }
////--------------------------------------------
    }

    /*** ***********************************
     *  生命週期
     * ************************************/
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mHandler.removeCallbacks(updateTimer);
        locationManager.removeUpdates(locationListener);
    }

    /*** ***********************************
     *  權限管理
     * ************************************/
    public static void enableStrictMode(Context context) {
        StrictMode.setThreadPolicy(
//                -------------抓取遠端資料庫設定執行續------------------------------
                new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog()
                        .build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .penaltyLog()
                        .build());
    }

    private void checkRequiredPermission(final Activity activity) { //
//        String permission_check= String[i][0] permission;
        for (int i = 0; i < permissionsArray.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, permissionsArray[i][0]) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permissionsArray[i][0]);
            }
        }
        if (permissionsList.size() != 0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new
                    String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    /*** request需要的權限*/
    private void requestNeededPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
    }

    /*** ***********************************
     *  所需要申請的權限數組
     * ************************************/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), permissionsArray[i][1] + "權限申請成功!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "權限被拒絕： " + permissionsArray[i][1], Toast.LENGTH_LONG).show();
                        //------------------
                        // 這邊是照官網說法，在確認沒有權限的時候，確認是否需要說明原因
                        // 需要的話就先顯示原因，在使用者看過原因後，再request權限
                        //-------------------
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            Util.showDialog(this, R.string.dialog_msg1, android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestNeededPermission();
                                }
                            });
                        } else {
                            // 否則就直接request
                            requestNeededPermission();
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //=====================================================

    /*** ***********************************
     *  menu 選單
     * ************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m1901, menu);
        this.menu = menu;
        b_item3 = menu.findItem(R.id.item3);
        b_item4 = menu.findItem(R.id.item4);
        b_item3.setVisible(true);
        b_item4.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.item1:
                map.clear(); //歸零
                if (icosel < 1) {
                    icosel = 1; //用照片顯示
                    showloc();
                } else
                    icosel = 0; //用水滴顯示
                showloc();
                break;
            case R.id.item3:
                routeon = 1;
                b_item3.setVisible(false);
                b_item4.setVisible(true);
                break;
            case R.id.item4:
                routeon = 0;
                iSelect = 0;
                map.clear();
                Show_Spinner();
                showloc();
                b_item3.setVisible(true);
                b_item4.setVisible(false);
                break;
            case R.id.action_settings:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*** ***********************************
     *  infowindows 副程式
     * ************************************/
    //====================================================================================
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            //依指定layout檔，建立地標訊息視窗View物件
            //-----------------------------------
            //單一框
//            View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_window,null);
            //有指示的外框
            View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_content, null);
            infoWindow.setAlpha(0.5f);
            //------------------------------------
            //顯示地標title
            TextView title = ((TextView) infoWindow.findViewById(R.id.title));
            String[] ss = marker.getTitle().split("#");
            title.setText(ss[0]);
            //顯示地標snippet
            TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
            snippet.setText(marker.getSnippet());
            //顯示圖片
            ImageView imageview = ((ImageView) infoWindow.findViewById(R.id.content_ico));
            imageview.setImageResource(Integer.parseInt(ss[1]));
            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Toast.makeText(getApplicationContext(), "getInfoContents", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    //===========================================================

    /*** ***********************************
     * Fetches data from url passed
     * ************************************/
    private class DownloadTask extends AsyncTask<String, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Error=>", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /*** ***********************************
     * 從URL下載JSON資料的方法   *
     * ************************************/
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("downloadUrl", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /*********************************************
     * 解析JSON格式
     **********************************************/
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                Log.d("Error=>", e.toString());
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(20); //導航路徑寬度
                lineOptions.color(Color.BLUE); //導航路徑顏色
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = map.addPolyline(lineOptions);

            } else
                Toast.makeText(getApplicationContext(), "找不到路徑", Toast.LENGTH_LONG).show();
        }

    }

/***********************************************
 subclass end
 **********************************************    */
    /***********************************************
     禁用返回鍵
     **********************************************    */
    public boolean onKeyDown(int keyCode, KeyEvent event) { //禁用返回鍵
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }
//-------------------------------------------------------------------
}

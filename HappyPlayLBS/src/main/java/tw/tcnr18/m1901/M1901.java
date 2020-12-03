package tw.tcnr18.m1901;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

import tw.tcnr18.m1901.providers.FriendsContentProvider;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class M1901 extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener {  // Manifest 要在+4個                          LocationListener 改 GoogleMap.OnCameraMoveListener   // 多LocationListener
    //==============================================================
    //所需要申請的權限數組
    private static final String[] permissionsArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private List<String> permissionsList = new ArrayList<String>();

    //申請權限後的返回碼
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    //==============================================================
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private LocationManager manager;
    private GoogleMap map;
    static LatLng VGPS = new LatLng(24.172127, 120.610313);
    float mapzoom = 16;  // 0-20

    private static String[] mapType = { //塗層
            "街道圖",
            "衛星圖",
            "地形圖",
            "混合圖",
            "開啟路況",
            "關閉路況"};
    private LocationManager locationManager;
    private Spinner mSpnLocation, mSpnMapType;
    private int icosel = 1; //圖示旗標
    private ArrayAdapter<String> adapter;
    private double dLat, dLon;
    private BitmapDescriptor image_des;
    private TextView txtOutput, tmsg;
    private String provider;
    private long minTime = 5000;
    private float minDist = 5.0f;
    private Marker markerMe;
    private String TAG = "tcnr18=>";
    private int resID = 0;
    private ArrayList<LatLng> mytrace;
    private ScrollView controlScroll;
    private CheckBox checkBox;
    private int resID1 = 0;
    private float Anchor_x = 0.5f;
    private float Anchor_y = 0.9f;

    ArrayList<LatLng> markerPoints;
    private Polyline mPolyline;
    private String key = "key=" + "AIzaSyCumZSn64Yd_HAD30UAZoKB8rt9KfjHisg";  //需使用 javascriptmap key;  我的
    private int routeon = 0;
    private String[] MYCOLUMN = new String[]{"id", "name", "grp", "address"};

    //=====================================================================================
    // ========= Thread Hander ============= 老師的
    private Handler mHandler = new Handler();
    private long timer = 10; // thread每幾秒run 多久更新一次資料
    private long timerang = 10; // 設定幾秒刷新Mysql
    private Long startTime = System.currentTimeMillis(); // 上回執行thread time
    private Long spentTime;
    //=============SQL Database================================
    private static ContentResolver mContRes;
    //----------------------------
    int DBConnectorError = 0;
    int MyspinnerNo = 0;
    int Spinnersel = 0;

    //    private String Myname = "26號顏瑋霆";
    private String Myname = "18號張介又";
    private String Myid = "0";
    private String Mygroup = "第三組";
    private String Myaddress = "24.172127,120.610313";
    /***********************************************/
//    private String Selname = "我的位置";
//    private String Seladdress = "24.172127,120.610313";
    private String Selname;
    private String Seladdress;
    private int old_index;
    private int index = 0;
    private Menu menu;
    private MenuItem b_item3;
    private MenuItem b_item4;
    private int iSelect = 0;
    private int My_select = 0;  //使用者姓名在第幾筆
    private String TGPS = "0.0,0.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1901);
        //        checkRequiredPermission(this);
        //-------------抓取遠端資料庫設定執行續------------------------------
        StrictMode.setThreadPolicy(new
                StrictMode.
                        ThreadPolicy.Builder().
                detectDiskReads().
                detectDiskWrites().
                detectNetwork().
                penaltyLog().
                build());
        StrictMode.setVmPolicy(
                new
                        StrictMode.
                                VmPolicy.
                                Builder().
                        detectLeakedSqlLiteObjects().
                        penaltyLog().
                        penaltyDeath().
                        build());
        mContRes = getContentResolver();
        //---------------------------------------------------------------------
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //------------設定MapFragment-----------------------------------
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //-------------------------------------------------------
        u_checkgps();  //檢查GPS是否開啟
        // ============================
        Intent intent01 = this.getIntent();
        Myname = intent01.getStringExtra("googlename");
        //-------------------------------------------------------------------------------
        setupViewComponent();
    }

    private void checkRequiredPermission(final Activity activity) {
        for (String permission : permissionsArray) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }
        if (permissionsList.size() != 0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new
                    String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    private void setupViewComponent() {
        mSpnLocation = (Spinner) this.findViewById(R.id.spnLocation);  //抓Spinner 要寫this這個
        mSpnMapType = (Spinner) this.findViewById(R.id.spnMapType);
        txtOutput = (TextView) findViewById(R.id.txtOutput);
        tmsg = (TextView) findViewById(R.id.msg);
        // ----設定control 控制鈕----------
        checkBox = (CheckBox) this.findViewById(R.id.checkcontrol);
        controlScroll = (ScrollView) this.findViewById(R.id.Scroll01);
        checkBox.setOnCheckedChangeListener(chklistener);
        controlScroll.setVisibility(View.INVISIBLE);
        // Parameters: 對應的三個常量值：VISIBLE=0 INVISIBLE=4 GONE=8
        icosel = 0;  //設定圖示初始值  用圖示的
        //-------檢查使用者是否存在--------------
        SelectMysql(Myname);
        //-------------------------------------
        // 設定Delay的時間
        mHandler.postDelayed(updateTimer, timer * 1000);
        // -------------------------
        Showspinner(); // 刷新spinner  走兩遍
    }

    private void Showspinner() {
        /***************************************
         * 讀取SQLite => Spinner
         *****************************************/
        mContRes = getContentResolver();
        Cursor cur_Spinner = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        cur_Spinner.moveToFirst();//一定要寫，不然會出錯

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        My_select = 0; //使用者姓名在第幾筆
        for (int i = 0; i < cur_Spinner.getCount(); i++) // 上面註解的寫來這裡
        {
            cur_Spinner.moveToPosition(i);
            // adapter.add("id:"+c.getString(0) + " 名:" + c.getString(1)+" 組"+
            // c.getString(2) + " 位:" + c.getString(3));

            if (cur_Spinner.getString(1).trim().equals(Myname)) {
                My_select = i;
            }

            adapter.add(cur_Spinner.getString(1));
        }
        cur_Spinner.close();

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnLocation.setAdapter(adapter);
        //==========================================
        if (iSelect > 0) {  //手動點Spinner的位置
            mSpnLocation.setSelection(iSelect, true); //spinner 小窗跳到所選
        } else {
            mSpnLocation.setSelection(My_select, true); //spinner 小窗跳到我的位置
        }
        //==========================================

        //指定事件處理物件
        mSpnLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                iSelect = mSpnLocation.getSelectedItemPosition();
                map.clear();
                mytrace = null;//清除軌跡圖
                showloc();
                setMapLocation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //---------------
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for (int i = 0; i < mapType.length; i++)
            adapter.add(mapType[i]);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnMapType.setAdapter(adapter);
        //-----------設定ARGB透明度----
        mSpnMapType.setPopupBackgroundDrawable(new ColorDrawable(0xF2FFFFFF));
        mSpnMapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            old_index = mSpnLocation.getSelectedItemPosition(); // 找到按何項

            spentTime = System.currentTimeMillis() - startTime;
            Long second = (spentTime / 1000);// 將運行時間後，轉換成秒數
            if (second >= timerang) {
                startTime = System.currentTimeMillis();
                dbmysql(); // 匯入database
                Showspinner(); // 刷新spinner
                mSpnLocation.setSelection(old_index, true); //spinner 小窗跳到第幾筆
            }
            mHandler.postDelayed(this, timer * 1000);// time轉換成毫秒 updateTime
        }
    };

    private String getTimeString(long timeInMilliseconds) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeInMilliseconds);
    }

    private void dbmysql() {
        mContRes = getContentResolver();
        // --------------------------- 先刪除 SQLite 資料------------
        Cursor cur_dbmysql = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        cur_dbmysql.moveToFirst(); // 一定要寫，不然會出錯
        // ------
        try {
            String result = DBConnector.executeQuery("SELECT * FROM member"); // 測試錯誤 SQLite應該要有資料
            // result.length() <= 7 這是 php那邊 case query 裡的 else echo"0 results" 裡面共9字
            if (result.length() <= 9) // 有沒有斷線 資料都不會刪掉 主要在這裡
            {//php找不到資料會回傳7,所以以7來判斷有沒有找到資料
                DBConnectorError++;//連線失敗次數
                if (DBConnectorError > 3)//連線失敗大於3次
                    Toast.makeText(M1901.this, "伺服器狀態異常,請檢查您的網路狀態!", Toast.LENGTH_LONG).show();
                else//連線失敗小於等於3次
                    Toast.makeText(M1901.this, "伺服器嘗試連線中,請稍候!", Toast.LENGTH_LONG).show();
            } else {//php找到資料,刪除SQLite資料
                DBConnectorError = 0;
                Uri uri = FriendsContentProvider.CONTENT_URI;
                mContRes.delete(uri, null, null); // mysql 已經抓到則刪除所有資料
            }   // where是null 全殺
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
                // ---------
//                    MyspinnerNo = i; // 儲存會員在spinner 的位置
                mContRes.insert(FriendsContentProvider.CONTENT_URI, newRow);
            }
        } catch (Exception e) {
        }
        cur_dbmysql.close();
    }

    private void SelectMysql(String myname) {
        String selectMYSQL = "";
        String result = "";
        try {
            selectMYSQL = "SELECT * FROM member WHERE name = '" + myname + "' ORDER BY id";
            result = DBConnector.executeQuery(selectMYSQL);
            int cc = result.length();

            if (result.length() <= 12) {
                InsertMysql(myname, Mygroup, Myaddress);

                selectMYSQL = "SELECT * FROM member WHERE name = '" + myname + "' ORDER BY id";
                result = DBConnector.executeQuery(selectMYSQL);
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonData = jsonArray.getJSONObject(0);
                Myid = jsonData.getString("id").toString();
                Myname = jsonData.getString("name").toString();
                Mygroup = jsonData.getString("grp").toString();
                Myaddress = jsonData.getString("address").toString();
            }
            UpdateMysql(Myid, Myname, Mygroup, Myaddress);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void InsertMysql(String insmyname, String insmygroup, String insmyaddress) {
        String result = DBConnector.executeInsert("", insmyname, insmygroup, insmyaddress);
    }

    private void UpdateMysql(String upmyid, String upmyname, String upmygroup, String upmyaddress) {
        String result = DBConnector.executeUpdate("", upmyid, upmyname, upmygroup, upmyaddress);

        //------ 宣告鈴聲 ---------------------------
//        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100); // 100=max
//        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 500);
//        toneG.release();
        //--------------------------------------------
    }

    //-------------監聽改變控制鈕 ------------
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

    private void setMapLocation() {  //景點要秀出來 刷新所有景點
        showloc(); //刷新所有景點
        int iSelect = mSpnLocation.getSelectedItemPosition();

        mContRes = getContentResolver();
        Cursor cur_setmap = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null,
                null);

        cur_setmap.moveToPosition(iSelect);
/**************************************
 * id: cur_setmap.getString(0) name: cur_setmap.getString(1) grp:
 * cur_setmap.getString(2) address:cur_setmap.getString(3)
 **************************************/
        Selname = cur_setmap.getString(1);// 地名
        Seladdress = cur_setmap.getString(3);// 緯經
        cur_setmap.close();
        String[] sLocation = Seladdress.split(",");

        double dLat = Double.parseDouble(sLocation[0]); // 南北緯
        double dLon = Double.parseDouble(sLocation[1]); // 東西經
        String vtitle = Selname;//選擇的名字
        //--- 設定所選位置之當地圖示---//
        image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE); //使用系統水滴
        //水滴可選擇樣式顏色
        VGPS = new LatLng(dLat, dLon);
        //----設定自訂義infowindow----//
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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, mapzoom));
        onCameraChange(map.getCameraPosition());//始終把圖放在中間
    }

    private void onCameraChange(CameraPosition cameraPosition) { //要計算出兩點的距離 要用多少倍的zoom 這邊要寫好
        tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom); // 自動換算ZOOM 原本寫死的 12 14
    }

    private void showloc() {
        Cursor cursholoc = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        int bb = cursholoc.getCount();
        int aa = 0;
        // 將所有景點位置顯示
        for (int i = 0; i < cursholoc.getCount(); i++) {
            cursholoc.moveToPosition(i);
            String[] sLocation = cursholoc.getString(3).split(",");
            dLat = Double.parseDouble(sLocation[0]); // 南北緯
            dLon = Double.parseDouble(sLocation[1]); // 東西經
            String vtitle = cursholoc.getString(1);
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
                    image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE); // 使用橘色系統水滴
//                    Anchor_x = -0.5f;
//                    Anchor_y = 1.0f;
                    break;
                case 1:
                    // 運用巨集
                    image_des = BitmapDescriptorFactory.fromResource(resID);// 使用照片
//                    Anchor_x =-0.5f;
//                    Anchor_y = 1.0f;
                    break;
            }
            vtitle = vtitle + "#" + resID1;//存放圖片號碼
            VGPS = new LatLng(dLat, dLon);// 更新成欲顯示的地圖座標
            //---根據所選位置項目顯示地圖/標示文字與圖片---//
            map.addMarker(new MarkerOptions()
                            .position(VGPS)
                            .alpha(0.9f)
                            .title(i + "." + vtitle)
                            .snippet("緯度:" + String.valueOf(dLat) + "\n經度:" + String.valueOf(dLon))
                            .infoWindowAnchor(Anchor_x, Anchor_y)//設定圖標的基點位置
                            .icon(image_des)// 顯示圖標文字
//                    .draggable(true)//設定marker可移動
            );
            //--------使用自定義視窗-------------------------
            map.setInfoWindowAdapter(new CustomInfoWindowAdapter());//外圓內方
        }
        cursholoc.close();
    }

    private void u_checkgps() {
        // 取得系統服務的LocationManager物件
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 檢查是否有啟用GPS
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 顯示對話方塊啟用GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("定位管理")
                    .setMessage("GPS目前狀態是尚未啟用.\n"
                            + "請問你是否現在就設定啟用GPS?")
                    .setPositiveButton("啟用", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 使用Intent物件啟動設定程式來更改GPS設定 沒有開啟GPS 會跳到開啟GPS的那個畫面
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("不啟用", null).create().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(getApplicationContext(), "返回GPS目前位置", Toast.LENGTH_LONG).show();
        return true;
    }

    //***********************************************/
    /* 檢查GPS 是否開啟 */
    private boolean initLocationProvider() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER; // provider 允許 出現用GPS 或 WIFI定位
            return true;
        }
        return false;
    }
//------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) { // 水滴的特性
        map = googleMap; //可選擇其他的

        map.getUiSettings().setScrollGesturesEnabled(true); // 拖曳功能 水滴可以移 換位置
        map.getUiSettings().setMapToolbarEnabled(true); // 導覽跟功能 要先有水滴 再點水滴 才會出現
        map.getUiSettings().setCompassEnabled(true); // 指北針
        map.getUiSettings().setZoomControlsEnabled(true); // 放大縮小

//        // Add a marker in Sydney and move the camera
//        map.addMarker(new MarkerOptions().position(VGPS).title("中區職訓局"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, mapzoom));// mapzoom 放大14倍

        //----------取得定位許可----------------------- 要先有定位 才會有右上角的我的位置
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
//            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        } else {
            //----顯示我的位置ICO-------
            map.setMyLocationEnabled(true);
            return;
        }
    }

    private void nowaddress() {
        // 取得上次已知的位置   使用者是否有按允許
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(provider);
            updateWithNewLocation(location);
            return;
        }
        // 監聽 GPS Listener----------------------------------
        // long minTime = 5000;// ms
        // float minDist = 5.0f;// meter
        //---網路和GPS來取得定位，因為GPS精準度比網路來的更好，所以先使用網路定位、
        // 後續再用GPS定位，如果兩者皆無開啟，則跳無法定位的錯誤訊息
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); // 抓到人造衛星定位
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); // WIFI 或 基地台定位
        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled)) // 2個都沒有
            tmsg.setText("GPS 未開啟");
        else {
            if (isNetworkEnabled) {   // minTime 5000 5秒   minDist 5公尺
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                tmsg.setText("使用網路GPS");
            }
//------------------------
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                tmsg.setText("使用精確GPS");
            }
        }
    }

    private void updateWithNewLocation(Location location) {
        String where = "";
        if (location != null) {
            double lng = location.getLongitude();// 經度
            double lat = location.getLatitude();// 緯度
            float speed = location.getSpeed();// 速度
            long time = location.getTime();// 時間
            String timeString = getTimeString(time);
            where = "經度: " + lng + "\n緯度: " + lat + "\n速度: " + speed + "\n時間: " + timeString + "\nProvider: " + provider;
//            Myid = "1";
            Myaddress = lat + "," + lng;
            /***********************************變更mysql會員的座標 *********************************/
            SelectMysql(Myname);
            // 標記"我的位置"
            showMarkerMe(lat, lng);
//            cameraFocusOnMe(lat, lng);
            // 畫軌跡圖 重要!!
//            trackMe(lat, lng);  // 目前所在位置 小藍點
            //--是否使用導航=====================
            if (routeon == 1) {
                cameraFocusOnMe(lat, lng);
                u_routeuse(lat, lng);
            }
            //            trackMe(lat, lng);  //軌跡圖
            //==========================
        } else {
            where = "*位置訊號消失*";
        }
        // 位置改變顯示
        txtOutput.setText(where);
    }

    private void u_routeuse(double lat, double lng) {  // 導航先關掉
        // Already two locations
        //==========================
        String toWhere = mSpnLocation.getSelectedItem().toString().trim();
        Log.d(TAG, toWhere);
        mContRes = getContentResolver();
        Cursor cur_routeuse = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN,
                "name =?", new String[]{toWhere}, null);
//        Cursor cur_routeuse = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN,
//                "name = ?  and  grp = ?", new String[]{toWhere, "第一組"}, null);
        cur_routeuse.moveToFirst();

        String vtitle1 = cur_routeuse.getString(1);
        TGPS = cur_routeuse.getString(3);
        cur_routeuse.close();
        String[] sLocationb = TGPS.split(",");
        double dLat1 = Double.parseDouble(sLocationb[0]);    // 南北緯
        double dLon1 = Double.parseDouble(sLocationb[1]);    // 東西經
        int wii = 0;//breakpoint
//==========================
//        /** 起始及終點位置符號顏色      */
        LatLng origin = new LatLng(lat, lng);
        LatLng dest = new LatLng(dLat1, dLon1);
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);

        M1901.DownloadTask downloadTask = new M1901.DownloadTask();

        // Start downloading json data from Google Directions
        // API
        downloadTask.execute(url);


//        int iii = mSpnLocation.getSelectedItemPosition();
//        mContRes = getContentResolver();
//        Cursor cur_sos = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
//        --------------------------------------------------------------------------------------------------------------------------
//        String toWhere = mSpnLocation.getSelectedItem().toString().trim();
//        Cursor cur_routeuse = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, " name = '"+toWhere+"'" , null, null);
//        Cursor cur_routeuse = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, " name = ?" , new String[] {toWhere}, null);
//        cur_routeuse.moveToFirst();
//        String vtitle1 =cur_routeuse.getString(1);
//        String TGPS = cur_routeuse.getString(3);
//        cur_routeuse.close();
//        --------------------------------------------------------------------------------------------------------------------------
//        cur_sos.moveToPosition(iii);
//        String vtitle1 = cur_sos.getString(1);
//        String TGPS = cur_sos.getString(3);
//        cur_sos.close();
//
//        String[] sLocationb = TGPS.split(",");
//        double dLat1 = Double.parseDouble(sLocationb[0]);    // 南北緯
//        double dLon1 = Double.parseDouble(sLocationb[1]);    // 東西經
//
//        /** 起始及終點位置符號顏色      */
//        LatLng origin = new LatLng(lat, lng);
//        LatLng dest = new LatLng(dLat1, dLon1);
////        // Getting URL to the Google Directions API
//        String url = getDirectionsUrl(origin, dest); // 起始點跟目的地
//
//        M1901.DownloadTask downloadTask = new M1901.DownloadTask();
////        // Start downloading json data from Google Directions
////        // API
//        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Key
        // Building the parameters to the web service
        // Travelling Mode-------------------------
//        String mode = "mode=driving";  // 開車
//           String mode = "mode=bicycling";  // 腳踏車
        String mode = "mode=walking";  // 走路
        //------------------------
//        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + key + "&" + mode;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private void trackMe(double lat, double lng) {
        if (mytrace == null) {
            mytrace = new ArrayList<LatLng>();
        }
        mytrace.add(new LatLng(lat, lng));
        PolylineOptions polylineOpt = new PolylineOptions(); //PolylineOptions 畫線段
        for (LatLng latlng : mytrace)  // 每次一改變就增加一點 陣列的for迴圈
        {
            polylineOpt.add(latlng);
        }
        polylineOpt.color(Color.BLUE); // 軌跡顏色
        Polyline line = map.addPolyline(polylineOpt);
        line.setWidth(15); // 軌跡寬度
//---
        line.setPoints(mytrace);
        //    ----虛線-----  這邊可以自己改
//    private void trackMe(double lat, double lng) {
//        if (mytrace == null) {
//            mytrace = new ArrayList<LatLng>();
//        }
//        mytrace.add(new LatLng(lat, lng));
//        PolylineOptions polylineOpt = new PolylineOptions()
//                .geodesic(true)
//                .color(Color.CYAN)
//                .width(10)
//                .pattern(PATTERN_POLYGON_ALPHA);
//
////        polylineOpt.addAll(Polyline.getPoints(mytrace));
////        polylinePaths.add(mGoogleMap.addPolyline(polylineOpt));
//
////        for (LatLng latlng : mytrace) {
////            polylineOpt.add(latlng);
////        }
//        // -----***軌跡顏色***-----
//        polylineOpt.color(Color.rgb(188 ,143,143));
//        Polyline line = map.addPolyline(polylineOpt);
//        line.setWidth(10); // 軌跡寬度
//        line.equals(10);
//        line.setPoints(mytrace);
//    }
    }

    private void showMarkerMe(double lat, double lng) { // 這邊顯示水滴的內容
        if (markerMe != null) {
            markerMe.remove();
        }
        int resID = getResources().getIdentifier("z00", "drawable", getPackageName());
//------------------
        if (icosel != 0) {
            image_des = BitmapDescriptorFactory.fromResource(resID);//使用照片
        } else {
            image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);//使用系統水滴
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
        markerOpt.snippet(vsnippet);
        markerOpt.infoWindowAnchor(Anchor_x, Anchor_y);
        markerOpt.draggable(true);
        markerOpt.icon(image_des);
        markerMe = map.addMarker(markerOpt);
    }

    private void cameraFocusOnMe(double lat, double lng) { // 顯示目前放大多少 Zoom的值
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(map.getCameraPosition().zoom)
                .build();
        /* 移動地圖鏡頭 */
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
        tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
    }

    //      位置變更狀態監視
    LocationListener locationListener = new LocationListener() {  //主要記錄軌跡圖

        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);  //主要記錄軌跡圖
            Log.d(TAG, "locationListener->onLocationChanged:" + map.getCameraPosition().zoom + " currentZoom:"
                    + mapzoom);
            tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
            Log.d(TAG, "onProviderDisabled");
        }

        @Override
        public void onProviderEnabled(String provider) {
            tmsg.setText("onProviderEnabled");
            Log.d(TAG, "onProviderEnabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.v(TAG, "Status Changed: Out of Service");
                    tmsg.setText("Out of Service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.v(TAG, "Status Changed: Temporarily Unavailable");
                    tmsg.setText("Temporarily Unavailable");
                    break;
                case LocationProvider.AVAILABLE:
                    Log.v(TAG, "Status Changed: Available");
                    tmsg.setText("Available");
                    break;
            }
        }
    };

    //===============================================================================

    // ---Control 控制項設定--------------------------------
    private boolean isChecked(int id) {
        return ((CheckBox) findViewById(id)).isChecked();
    }

    // -------- 地圖縮放 -------------------------------------------
    public void setZoomButtonsEnabled(View v) {
        if (!checkReady()) return;
        map.getUiSettings().setZoomControlsEnabled(((CheckBox) v).isChecked());
    }

    private boolean checkReady() {
        if (map == null) {
//            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ---------------設定指北針----------------------------------------------
    public void setCompassEnabled(View v) {
        if (!checkReady()) return;
        map.getUiSettings().setCompassEnabled(((CheckBox) v).isChecked());
    }

    // -----顯示 我的位置座標圖示
    public void setMyLocationLayerEnabled(View v) {
        if (!checkReady()) return;

        //----------取得定位許可-----------------------
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
            map.setMyLocationEnabled(((CheckBox) v).isChecked());
        } else {
//            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        }
    }

    // ---- 可用捲動手勢操控,用手指平移或捲動來拖曳地圖
    public void setScrollGesturesEnabled(View v) {
        if (!checkReady()) return;
        map.getUiSettings().setScrollGesturesEnabled(((CheckBox) v).isChecked());
    }

    // ---- 縮放手勢 按兩下 按一下 或兩指拉大拉小----
    public void setZoomGesturesEnabled(View v) {
        if (!checkReady()) return;
        map.getUiSettings().setZoomGesturesEnabled(((CheckBox) v).isChecked());
    }

    // ---- 傾斜手勢 改變地圖的傾斜角度 兩指上下拖曳來增加/減少傾斜角度----
    public void setTiltGesturesEnabled(View v) {
        if (!checkReady()) return;
        map.getUiSettings().setTiltGesturesEnabled(((CheckBox) v).isChecked());
    }

    // ---- 旋轉手勢 兩指旋轉地圖  ----
    public void setRotateGesturesEnabled(View v) {
        if (!checkReady()) return;
        map.getUiSettings().setRotateGesturesEnabled(((CheckBox) v).isChecked());
    }

    //================================ 下面是生命週期 ===========================================
//    1.當Activity準備要產生時，先呼叫onCreate方法。
//    2.Activity產生後（還未出現在手機螢幕上），呼叫onStart方法。
//    3.當Activity出現手機上後，呼叫onResume方法。
//    4.當使用者按下返回鍵結束Activity時， 先呼叫onPause方法。
//    5.當Activity從螢幕上消失時，呼叫onStop方法。
//    6.最後完全結束Activity之前，呼叫onDestroy方法。
    @Override
    protected void onStop() {
        super.onStop();
//        Toast.makeText(this, "onStop", Toast.LENGTH_LONG).show();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Toast.makeText(this, "onDestroy", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Toast.makeText(this, "onPause", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkRequiredPermission(this);
        try {
            if (initLocationProvider()) {
                nowaddress();
            } else {
                txtOutput.setText("GPS未開啟，請先開啟定位!");
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Toast.makeText(this, "onRestart", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
//        stop.performClick();// performClick 整個關掉
//        super.onBackPressed();
    }

    //================================ 下面是MENU ==========================================
    //  Menu下面這兩個最基本的要記起來 onCreateOptionsMenu & onOptionsItemSelected
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.m1901, menu);  //選layout ,menu
        this.menu = menu;
        b_item3 = menu.findItem(R.id.item3);
        b_item4 = menu.findItem(R.id.item4);
        b_item3.setVisible(true);
        b_item4.setVisible(false);
        return true;
    }

    //====================================================================================
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:  // 切換圖示
                map.clear(); // 歸零
                if (icosel < 1) {
                    icosel = 1; // 用照片顯示
                } else {
                    icosel = 0; // 用水滴顯示
                }
                showloc();
                break;
            case R.id.item2:
                map.clear(); //把點清乾淨
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
                Showspinner();
                showloc();
                b_item3.setVisible(true);
                b_item4.setVisible(false);
                break;
            case R.id.action_settings: //menu那邊+一個 結束 string也要+
                this.finish();
                onBackPressed();  // 關掉
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override  // 水滴 showloc showMakerMe 跟 onMarkerClick
//    public boolean onMarkerClick(Marker marker)  // 上面 implements 多 GoogleMap.OnMarkerClickListener
//    { // 按水滴會產生動畫
//        return false;
//    }

    //*** 增加 Marker 監聽 使用Animation動畫*/
    @Override
    public boolean onMarkerClick(final Marker marker_Animation) {
        if (!marker_Animation.getTitle().substring(0, 4).equals("Move")) {
            //非GPS移動位置;設定動畫
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500; //連續時間
            final Interpolator interpolator = new BounceInterpolator();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker_Animation.setAnchor(Anchor_x, Anchor_y + 2 * t); //設定標的位置
                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });
        } else {//GPS移動位置,不使用動畫
            M1901.this.markerMe.hideInfoWindow();
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //====================================================


    //================================ 下面是次類別之類的東西 ==============================================

    //==============================產生在內部的副程式=================================================================
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            // 依指定layout檔，建立地標訊息視窗View物件
            // --------------------------------------------------------------------------------------
            // 單一框
            // View infoWindow=
            // getLayoutInflater().inflate(R.layout.custom_info_window,
            // null);
            // 有指示的外框
            View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_content, null); // 改變圖框是這個
            infoWindow.setAlpha(0.8f);
            // ----------------------------------------------
            // 顯示地標title
            TextView title = ((TextView) infoWindow.findViewById(R.id.title));
            String[] ss = marker.getTitle().split("#");
            title.setText(ss[0]);  //custom_info_content.xml裡的title
            // 顯示地標snippet
            TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
            snippet.setText(marker.getSnippet());  //custom_info_content.xml裡的snippet
            // 測試 // custom_info_content.xml 加了兩個欄位 showloc要加 這邊也要加
//            TextView tel = ((TextView) infoWindow.findViewById(R.id.tel));
//            tel.setText(ss[2]);
//            TextView addr = ((TextView) infoWindow.findViewById(R.id.addr));
//            addr.setText(ss[3]);
            // 顯示圖片
            ImageView imageview = ((ImageView) infoWindow.findViewById(R.id.content_ico));
            imageview.setImageResource(Integer.parseInt(ss[1]));  //custom_info_content.xml裡的ico

            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Toast.makeText(getApplicationContext(), "getInfoContents", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> { //只要有用到導航 這幾個生在內部跟外部的一定要用 3內部class 1外部class 2key
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            M1901.ParserTask parserTask = new M1901.ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /*** 從URL下載JSON資料的方法   **/
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
            Log.d(TAG, e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * 解析JSON格式
     **/
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
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
                e.printStackTrace();
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
                lineOptions.width(8); //導航路徑寬度
                lineOptions.color(Color.RED); //導航路徑顏色
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = map.addPolyline(lineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "找不到路徑", Toast.LENGTH_LONG).show();
            }
        }
    }
}

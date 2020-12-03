package tw.tcnr18.m1901;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class M1421 extends AppCompatActivity implements View.OnClickListener {
    String TAG = "tcnr18=>";
    private TextView count_t;
    private EditText b_id, b_name, b_grp, b_address;

    private TextView tvTitle;
    private Button btNext, btPrev, btTop, btEnd;
    private ArrayList<String> recSet;
    private int index = 0;
    String msg = null;
    //--------------------------
    private float x1; // 觸控的 X 軸位置
    private float y1; // 觸控的 Y 軸位置
    private float x2;
    private float y2;
    int range = 50; // 兩點距離
    int ran = 60; // 兩點角度

    private Button btEdit, btDel;
    String tname, tgrp, taddr;

    private Spinner mSpnName;
    int up_item = 0;
    //------------------------------
    protected static final int BUTTON_POSITIVE = -1;
    protected static final int BUTTON_NEGATIVE = -2;
    private Button btAdd, btAbandon, btquery, btcancel, btreport;

    private String[] MYCOLUMN = new String[]{"id", "name", "grp", "address"};
    int tcount;

    // ------------------
    private RelativeLayout b_Relbutton;
    private RelativeLayout brelative01;
    private LinearLayout blinear02;
    private ListView listView;
    private TextView bsubTitle;
    //===============
    private FriendDbHelper dbHper;
    private static final String DB_FILE = "friends.db";
    private static final String DB_TABLE = "member";
    private static final int DBversion = 1;
    //-----------------
    private String sqlctl;
    private String tid;
    private String s_id;
    private String taddress;
    private int old_index = 0;
    // ----------定時更新--------------------------------
    private Long startTime;
    private Handler handler = new Handler();

    int autotime = 10;// 要幾秒的時間 更新匯入MySQL資料
    //------------------------------
    private MenuItem b_m_add, b_m_query, b_m_batch, b_m_list, b_m_mysql, b_m_edit_start, b_m_edit_stop, b_m_return;
    private boolean edittype = false; //控制編輯狀況
    private TextView b_editon;  //顯示是否開啟編輯狀態
    private Menu menu;
    private boolean touch_flag;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private TextView nowtime;  //顯示更新時間及次數
    int update_time = 0;
    private String str;
    private TextView b_servermsg;
    private String ser_msg;
    private int servermsgcolor;
    String showip;   //顯示手機ip
    private String stHead;
    // --------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableStrictMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1421);
        //--------取得目前時間
        startTime = System.currentTimeMillis();
//        ------------------
        setupViewComponent();
    }

    private void setupViewComponent() {
        tvTitle = (TextView) findViewById(R.id.tvIdTitle);
        b_id = (EditText) findViewById(R.id.edid);
        b_name = (EditText) findViewById(R.id.edtName);
        b_grp = (EditText) findViewById(R.id.edtGrp);
        b_address = (EditText) findViewById(R.id.edtAddr);
        count_t = (TextView) findViewById(R.id.count_t);

        btNext = (Button) findViewById(R.id.btIdNext);
        btPrev = (Button) findViewById(R.id.btIdPrev);
        btTop = (Button) findViewById(R.id.btIdtop);
        btEnd = (Button) findViewById(R.id.btIdend);

        btEdit = (Button) findViewById(R.id.btnupdate);
        btDel = (Button) findViewById(R.id.btIdDel);

        //-----------------------
        btAdd = (Button) findViewById(R.id.btnAdd);
        btAbandon = (Button) findViewById(R.id.btnabandon);
        btquery = (Button) findViewById(R.id.btnquery);
        btcancel = (Button) findViewById(R.id.btidcancel);
        btreport = (Button) findViewById(R.id.btnlist);

        brelative01 = (RelativeLayout) findViewById(R.id.relative01);
        blinear02 = (LinearLayout) findViewById(R.id.linear02);

        listView = (ListView) findViewById(R.id.listView);

        b_Relbutton = (RelativeLayout) findViewById(R.id.Relbutton);
        listView = (ListView) findViewById(R.id.listView);
        bsubTitle = (TextView) findViewById(R.id.subTitle);
        b_editon = (TextView) findViewById(R.id.edit_on);

        b_servermsg=(TextView)findViewById(R.id.servermsg);
        tvTitle.setBackgroundColor(ContextCompat.getColor(this, R.color.Teal));
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.Yellow));
        //-----------------
        mSpnName = (Spinner) this.findViewById(R.id.spnName);
        nowtime = (TextView) findViewById(R.id.now_time);
        //---------設定layout 顯示---------------
        u_layout_def();
        //-----------------------
        btNext.setOnClickListener(this);
        btPrev.setOnClickListener(this);
        btTop.setOnClickListener(this);
        btEnd.setOnClickListener(this);

        btEdit.setOnClickListener(this);
        btDel.setOnClickListener(this);

        btAdd.setOnClickListener(this);
        btAbandon.setOnClickListener(this);
        btquery.setOnClickListener(this);
        btcancel.setOnClickListener(this);
        btreport.setOnClickListener(this);

        //-----------------
        mSpnName = (Spinner) this.findViewById(R.id.spnName);
        startTime = System.currentTimeMillis();        // 取得目前時間
        //===================================
        handler.postDelayed(updateTimer, 1000);  // 設定Delay的時間
        //===================================
        java.sql.Date curDate = new java.sql.Date(System.currentTimeMillis()); //  獲取當前時間
        str = formatter.format(curDate);
        nowtime.setText(getString(R.string.now_time) + str);
        // ----------------------------------------
        initDB();
        showRec(index);
        u_setspinner();
        stHead = "顯示資料：第" + (index + 1) + " / " +tcount + " 筆";
        tvTitle.setText(stHead);
        b_id.setTextColor(ContextCompat.getColor(this, R.color.Red));
        // -------------------------
        mSpnName.setOnItemSelectedListener(mSpnNameOnItemSelLis);
    }

    // 固定要執行的方法
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            old_index = mSpnName.getSelectedItemPosition();
            Long spentTime = System.currentTimeMillis() - startTime;
            String hours = String.format("%02d", (spentTime / 1000) / 60 / 60);  // 計算目前已過分鐘數
            String minius = String.format("%02d", ((spentTime / 1000) / 60) % 60);  // 計算目前已過分鐘數
            String seconds = String.format("%02d", (spentTime / 1000) % 60);          // 計算目前已過秒數
            handler.postDelayed(this, autotime * 1000); // 真正延遲的時間
            // -------執行匯入MySQL
            dbmysql();
            recSet = dbHper.getRecSet();  //重新載入SQLite
            u_setspinner();  //重新設定spinner內容
            index = old_index;
            showRec(index); //重設spainner 小窗顯示及細目內容
            //-------------------------------------------------------------------------------
            ++update_time;
            nowtime.setText(getString(R.string.now_time) + "(每" + autotime + "秒)" + str + "->"
                    + hours + ":" + minius + ":" + seconds
                    + " (" + (update_time) + "次)");
            //------ 宣告鈴聲 ---------------------------
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 80); // 100=max
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 100);
            toneG.release();
            // --------------------------------------------------------
        }
    };

    //------------------------------------------------
    private void initDB() {
        if (dbHper == null)
            dbHper = new FriendDbHelper(this, DB_FILE, null, DBversion);
        recSet = dbHper.getRecSet(); //重新載入SQLite
    }
    //------------------------------------------------

    private void u_layout_def() {
        btAdd.setVisibility(View.INVISIBLE);
        btAbandon.setVisibility(View.INVISIBLE);
        btquery.setVisibility(View.INVISIBLE);
        btEdit.setVisibility(View.VISIBLE);
        btDel.setVisibility(View.VISIBLE);

        brelative01.setVisibility(View.VISIBLE);
        blinear02.setVisibility(View.INVISIBLE);
        btreport.setVisibility(View.INVISIBLE);

        b_id.setEnabled(false);
        //-----------------------
        b_Relbutton.setVisibility(View.INVISIBLE);
        b_editon.setVisibility(View.INVISIBLE);
        //-----------------------
    }

    private Spinner.OnItemSelectedListener mSpnNameOnItemSelLis = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView parent, View view, int position,
                                   long id) {
            int iSelect = mSpnName.getSelectedItemPosition(); //找到按何項
            String[] fld = recSet.get(iSelect).split("#");
            stHead = "顯示資料：第" + String.valueOf(iSelect + 1) + " / " + recSet.size() + " 筆";
            tvTitle.setText(stHead);
            b_id.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.Red));
            b_id.setText(fld[0]);
            b_name.setText(fld[1]);
            b_grp.setText(fld[2]);
            b_address.setText(fld[3]);
            //-------目前所選的item---
            up_item = iSelect;
            //-------------------------------
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

            b_id.setText("");
            b_name.setText("");
            b_grp.setText("");
            b_address.setText("");
        }
    };

    private ListView.OnItemClickListener listviewOnItemClkLis = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String s = "你按下第 " + Integer.toString(position + 1) + "筆"
                    + ((TextView) view.findViewById(R.id.txtView))
                    .getText()
                    .toString();
            bsubTitle.setText(s);
        }
    };

    @Override
    public void onClick(View v) {
        //---------------------------
        switch (v.getId()) {
            case R.id.btIdNext:
                ctlNext();
                break;
            case R.id.btIdPrev:
                ctlPrev();
                break;
            case R.id.btIdtop:
                ctlFirst();
                break;
            case R.id.btIdend:
                ctlLast();
                break;
            //------------------------------------
            case R.id.btnupdate:
                // 資料更新
                tid = b_id.getText().toString().trim();
                tname = b_name.getText().toString().trim();
                tgrp = b_grp.getText().toString().trim();
                taddr = b_address.getText().toString().trim();
                old_index = index;
                mysql_update(); // 執行MySQL更新
                // -------執行匯入MySQL
                dbmysql();
                recSet = dbHper.getRecSet();  //重新載入SQLite
                u_setspinner();  //重新設定spinner內容
                index = old_index;
                showRec(index); //重設spainner 小窗顯示及細目內容
                msg = "第 " + (index + 1) + " 筆記錄  已修改 ! ";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                break;
            //------------------------------------
            case R.id.btIdDel:
                // 刪除資料 --使用對話盒
                MyAlertDialog myAltDlg = new MyAlertDialog(this);
                myAltDlg.getWindow().setBackgroundDrawableResource(R.color.Yellow);
                myAltDlg.setTitle("清空所有資料");
                myAltDlg.setMessage("資料刪除無法復原\n確定將所有資料刪除嗎?");
                myAltDlg.setCancelable(false);
                myAltDlg.setIcon(android.R.drawable.ic_delete);
                myAltDlg.setButton(DialogInterface.BUTTON_POSITIVE, "確定刪除", aldBtListener);
                myAltDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "取消刪除", aldBtListener);
                myAltDlg.show();
                break;
            //-----------------------
            case R.id.btnAdd: //按下新增鈕
                // 查詢name是否有有此筆資料
                btAdd.setVisibility(View.VISIBLE);
                btAbandon.setVisibility(View.VISIBLE);
                tname = b_name.getText().toString().trim();
                tgrp = b_grp.getText().toString().trim();
                taddr = b_address.getText().toString().trim();
                if (tname.equals("") || tgrp.equals("")) {
                    Toast.makeText(getApplicationContext(), "資料空白無法新增 !", Toast.LENGTH_SHORT).show();
                    return;
                }
                //-------直接增加到MySQL-------------------------------
                mysql_insert();
                // -------執行匯入MySQL
                dbmysql();
                recSet = dbHper.getRecSet();  //重新載入SQLite
                u_setspinner();  //重新設定spinner內容
                index = recSet.size() - 1;
                showRec(index); //重設spainner 小窗顯示及細目內容
                //----------------------------------------
                msg = null;
// -------------------------
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                count_t.setText("共計:" + Integer.toString(dbHper.RecCount()) + "筆");
                break;
            //------------------------------------
            case R.id.btnabandon: //按下放棄鈕
                stop_edit();
                break;
            //------------------------------------
            case R.id.btnquery: //按下查詢鈕
                tname = b_name.getText().toString().trim();
                tgrp = b_grp.getText().toString().trim();
                taddr = b_address.getText().toString().trim();
                msg = null;
                recSet = dbHper.getRecSet_query(tname, tgrp, taddr);
                Toast.makeText(getApplicationContext(), "顯示資料： 共 " + recSet.size() + " 筆", Toast.LENGTH_SHORT).show();
                u_setspinner(); //重新設定spinner內容
                break;
            //------------------------------------
            case R.id.btnlist: //按下列表鈕
                tname = b_name.getText().toString().trim();
                tgrp = b_grp.getText().toString().trim();
                taddr = b_address.getText().toString().trim();
                msg = null;
                recSet = dbHper.getRecSet_query(tname, tgrp, taddr);
                Toast.makeText(getApplicationContext(), "顯示資料： 共 " + recSet.size() + " 筆", Toast.LENGTH_SHORT).show();
//                bsubTitle.setText("顯示資料： 共 " + recSet.size() + " 筆");
                //===========取SQLite 資料=============
                List<Map<String, Object>> mList;
                mList = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < recSet.size(); i++) {
                    Map<String, Object> item = new HashMap<String, Object>();
                    String[] fld = recSet.get(i).split("#");
                    item.put("imgView", R.drawable.userconfig);
                    item.put("txtView", "id:" + fld[0] + "\nname:" + fld[1] + "\ngroup:" + fld[2] + "\naddr:" + fld[3]);
                    mList.add(item);
                }
//                //=========設定listview========
                brelative01.setVisibility(View.INVISIBLE);
                blinear02.setVisibility(View.VISIBLE);
//
                SimpleAdapter adapter = new SimpleAdapter(
                        this,
                        mList,
                        R.layout.list_item,
                        new String[]{"imgView", "txtView"},
                        new int[]{R.id.imgView, R.id.txtView}
                );
                listView.setAdapter(adapter);
                listView.setTextFilterEnabled(true);
                listView.setOnItemClickListener(listviewOnItemClkLis);
                break;
            //------------------------------------
            case R.id.btidcancel:
                brelative01.setVisibility(View.VISIBLE);
                blinear02.setVisibility(View.INVISIBLE);
                b_Relbutton.setVisibility(View.VISIBLE);
                old_index = mSpnName.getSelectedItemPosition();
                u_menu_main();
                edittype = false;
                dbmysql();
                recSet = dbHper.getRecSet();  //重新載入SQLite
                u_setspinner();  //重新設定spinner內容
                index = old_index;
                showRec(index); //重設spainner 小窗顯示及細目內容
                //----------------------------------
                break;
        }
    }

    private void mysql_update() {
        s_id = b_id.getText().toString().trim();
        tname = b_name.getText().toString().trim();
        tgrp = b_grp.getText().toString().trim();
        taddress = b_address.getText().toString().trim();

        ArrayList<String> nameValuePairs = new ArrayList<>();
//        nameValuePairs.add(sqlctl);
        nameValuePairs.add(s_id);
        nameValuePairs.add(tname);
        nameValuePairs.add(tgrp);
        nameValuePairs.add(taddr);
        try {
            Thread.sleep(100); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = DBConnector.executeUpdate(nameValuePairs);
        Log.d(TAG, "Updateresult:" + result);
//-----------------------------------------------

    }

    private void mysql_insert() {
        //        sqlctl = "SELECT * FROM member ORDER BY id ASC";
        ArrayList<String> nameValuePairs = new ArrayList<>();
//        nameValuePairs.add(sqlctl);
        nameValuePairs.add(tname);
        nameValuePairs.add(tgrp);
        nameValuePairs.add(taddr);
        try {
            Thread.sleep(100); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = DBConnector.executeInsert(nameValuePairs);
//-----------------------------------------------
    }

    // ---------------------------------------------
    private DialogInterface.OnClickListener aldBtListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case BUTTON_POSITIVE:
                    tid = b_id.getText().toString().trim();
                    old_index = index;
                    // ---------------------------
                    mysql_del();// 執行MySQL刪除
                    dbmysql(); //執行匯入MySQL
                    recSet = dbHper.getRecSet();  //重新載入SQLite
                    u_setspinner();  //重新設定spinner內容
                    if (index == dbHper.RecCount()) {
                        index--;
                    }
                    showRec(index); //重設spainner 小窗顯示及細目內容
                    // ---------------------------
                    //                    mSpnName.setSelection(index, true); //spinner 小窗跳到第幾筆
                    msg = "資料已刪除";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
                case BUTTON_NEGATIVE:
                    msg = "放棄刪除所有資料 !";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void mysql_del() {
        s_id = b_id.getText().toString().trim();
        ArrayList<String> nameValuePairs = new ArrayList<>();
//        nameValuePairs.add(sqlctl);
        nameValuePairs.add(s_id);
        try {
            Thread.sleep(100); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = DBConnector.executeDelet(nameValuePairs);

        Log.d(TAG, "Delete result:" + result);
    }

    private void u_setspinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        for (int i = 0; i < recSet.size(); i++) {
            String[] fld = recSet.get(i).split("#");
//            adapter.add(fld[0] + " " + fld[1] + " " + fld[2] + " " + fld[3]);
            adapter.add(fld[0] + " " + fld[1] + " " + fld[2] + " " + fld[3]);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnName.setAdapter(adapter);

        mSpnName.setOnItemSelectedListener(mSpnNameOnItemSelLis);
        //        mSpnName.setSelection(index, true); //spinner 小窗跳到第幾筆
    }

    //========================================================
    private void showRec(int s_index) {
        msg = "";
        if (recSet.size() != 0) {
            stHead = "顯示資料：第" + (index + 1) + " / " + recSet.size() + " 筆";
            msg = getString(R.string.count_t) + recSet.size() + "筆";
            tvTitle.setText(stHead);

            String[] fld = recSet.get(index).split("#");
            b_id.setTextColor(ContextCompat.getColor(this, R.color.Red));
            b_id.setBackgroundColor(ContextCompat.getColor(this, R.color.Yellow));
            b_id.setText(fld[0]);
            b_name.setText(fld[1]);
            b_grp.setText(fld[2]);
            b_address.setText(fld[3]);
            mSpnName.setSelection(s_index, true); //spinner 小窗跳到第幾筆
        } else {

            String stHead = "顯示資料：第" + "0" + " / " + recSet.size() + " 筆";
            msg = getString(R.string.count_t) + "0筆";
            tvTitle.setText(stHead);
            b_id.setText("");
            b_name.setText("");
            b_grp.setText("");
            b_address.setText("");
        }

        count_t.setText(msg);
    }

    //------------------------------------------------
    private void ctlFirst() {
        // 第一筆
        index = 0;
        showRec(index); //重設spainner 小窗顯示及細目內容
    }

    private void ctlPrev() {
        // 上一筆
        index--;
        if (index < 0)
            index = recSet.size() - 1;
        showRec(index); //重設spainner 小窗顯示及細目內容
    }

    private void ctlNext() {
        // 下一筆
        index++;
        if (index >= recSet.size())
            index = 0;
        showRec(index); //重設spainner 小窗顯示及細目內容
    }


    private void ctlLast() {
        // 最後一筆
        index = recSet.size() - 1;
        showRec(index); //重設spainner 小窗顯示及細目內容
    }

    //---------------------------------------------------
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // ----------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimer);
    }
    //---------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 按下
                x1 = event.getX(); // 觸控按下的 X 軸位置
                y1 = event.getY(); // 觸控按下的 Y 軸位置

                break;
            case MotionEvent.ACTION_MOVE: // 拖曳

                break;
            case MotionEvent.ACTION_UP: // 放開
                x2 = event.getX(); // 觸控放開的 X 軸位置
                y2 = event.getY(); // 觸控放開的 Y 軸位置
                // 判斷左右的方法，因為屏幕的左上角是：0，0 點右下角是max,max
                // 並且移動距離需大於 > range
                float xbar = Math.abs(x2 - x1);
                float ybar = Math.abs(y2 - y1);
                double z = Math.sqrt(xbar * xbar + ybar * ybar);
                int angle = Math.round((float) (Math.asin(ybar / z) / Math.PI * 180));// 角度
                if (x1 != 0 && y1 != 0) {
                    if (x1 - x2 > range) { // 向左滑動
                        ctlPrev();
                    }
                    if (x2 - x1 > range) { // 向右滑動
                        ctlNext();
                        // t001.setText("向右滑動\n" + "滑動參值x1=" + x1 + " x2=" + x2 + "
                        // r=" + (x2 - x1)+"\n"+"ang="+angle);
                    }
                    if (y2 - y1 > range && angle > ran) { // 向下滑動
                        // 往下角度需大於50
                        // 最後一筆
                        ctlLast();
                    }
                    if (y1 - y2 > range && angle > ran) { // 向上滑動
                        // 往上角度需大於50
                        ctlFirst();// 第一筆
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void u_insert() {
        mSpnName.setEnabled(false);
        btAdd.setVisibility(View.VISIBLE);
        btAbandon.setVisibility(View.VISIBLE);
        btEdit.setVisibility(View.INVISIBLE);
        btDel.setVisibility(View.INVISIBLE);
        btquery.setVisibility(View.INVISIBLE);
//-----------------------------
        b_id.setEnabled(false);
        b_id.setText("");
        b_name.setText("");
        b_grp.setText("");
        b_address.setText("");
        b_id.setHint("請輸入");
        //-----住址放使用者IP---------
        showip = NetwordDetect();
        b_address.setText(showip);
//-----------------------------

    }

    private ContentValues FillRec() { //
        ContentValues contVal = new ContentValues();
        contVal.put("id", b_id.getText().toString());
        contVal.put("name", b_name.getText().toString());
        contVal.put("grp", b_grp.getText().toString());
        contVal.put("address", b_address.getText().toString());
        return contVal;
    }

    // 讀取MySQL 資料
    private void dbmysql() {
        sqlctl = "SELECT * FROM member ORDER BY id ASC";
        ArrayList<String> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(sqlctl);
        try {
            String result = DBConnector.executeQuery(nameValuePairs);
//=========================================
            chk_httpstate();
            //=========================================
            JSONArray jsonArray = new JSONArray(result);
            // -------------------------------------------------------
            if (jsonArray.length() > 0) { // MySQL 連結成功有資料
                //--------------------------------------------------------
                int rowsAffected = dbHper.clearRec();                 // 匯入前,刪除所有SQLite資料
                //--------------------------------------------------------
                // 處理JASON 傳回來的每筆資料
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    ContentValues newRow = new ContentValues();
                    // --(1) 自動取的欄位 --取出 jsonObject 每個欄位("key","value")-----------------------
                    Iterator itt = jsonData.keys();
                    while (itt.hasNext()) {
                        String key = itt.next().toString();
                        String value = jsonData.getString(key); // 取出欄位的值
                        if (value == null) {
                            continue;
                        } else if ("".equals(value.trim())) {
                            continue;
                        } else {
                            jsonData.put(key, value.trim());
                        }
                        // ------------------------------------------------------------------
                        newRow.put(key, value.toString()); // 動態找出有幾個欄位
                        // -------------------------------------------------------------------
                    }
                    // -------------------加入SQLite---------------------------------------
                    long rowID = dbHper.insertRec_m(newRow);
                }
                // ---------------------------
            } else {
                ser_msg = "主機資料庫無資料(code:" + DBConnector.httpstate + ") ";
                servermsgcolor=ContextCompat.getColor(this, R.color.Red);
                b_servermsg.setTextColor(servermsgcolor);
//                Toast.makeText(getApplicationContext(), "主機資料庫無資料", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    //**************************************************
//*       檢查連線狀況
//**************************************************
    private void chk_httpstate() {
        ////-------------------------------
        //存取類別成員 DBConnector01.httpstate 判定是否回應 200(連線要求成功)
        if (DBConnector.httpstate == 200) {
            ser_msg = "伺服器匯入資料(code:" + DBConnector.httpstate + ") ";
            servermsgcolor = ContextCompat.getColor(this, R.color.Navy);
//                Toast.makeText(getBaseContext(), "由伺服器匯入資料 ",
//                        Toast.LENGTH_SHORT).show();
        } else {
            int checkcode = DBConnector.httpstate / 100;
            switch (checkcode) {
                case 1:
                    ser_msg = "資訊回應(code:" + DBConnector.httpstate + ") ";
                    break;
                case 2:
                    ser_msg = "已經完成由伺服器會入資料(code:" + DBConnector.httpstate + ") ";
                    break;
                case 3:
                    ser_msg = "伺服器重定向訊息，請稍後在試(code:" + DBConnector.httpstate + ") ";
                    servermsgcolor = ContextCompat.getColor(this, R.color.Red);
                    break;
                case 4:
                    ser_msg = "用戶端錯誤回應，請稍後在試(code:" + DBConnector.httpstate + ") ";
                    servermsgcolor = ContextCompat.getColor(this, R.color.Red);
                    break;
                case 5:
                    ser_msg = "伺服器error responses，請稍後在試(code:" + DBConnector.httpstate + ") ";
                    servermsgcolor = ContextCompat.getColor(this, R.color.Red);
                    break;
            }
//                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
        }
        if (DBConnector.httpstate == 0) {
            ser_msg = "遠端資料庫異常(code:" + DBConnector.httpstate + ") ";
        }
        b_servermsg.setText(ser_msg);
        b_servermsg.setTextColor(servermsgcolor);
        //-------------------------------------------------------------------
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m1421, menu);
        this.menu = menu;
        u_menu_main();
//
        b_m_add = menu.findItem(R.id.m_add);
        b_m_query = menu.findItem(R.id.m_query);
        b_m_list = menu.findItem(R.id.m_list);
        b_m_edit_start = menu.findItem(R.id.m_edit_start);
        b_m_edit_stop = menu.findItem(R.id.m_edit_stop);
        b_m_return = menu.findItem(R.id.m_return);
        return true;
    }

    private void u_menu_main() {
        menu.setGroupVisible(R.id.m_group1, true);
        menu.setGroupVisible(R.id.m_group2, false);
        menu.setGroupVisible(R.id.m_group3, false);
        b_Relbutton.setVisibility(View.INVISIBLE);
        mSpnName.setVisibility(View.VISIBLE);
        b_editon.setVisibility(View.INVISIBLE);
        btEdit.setVisibility(View.INVISIBLE);
        btDel.setVisibility(View.INVISIBLE);
        u_button_ontouch();

    }

    private void u_menu_edit_main() {
        handler.removeCallbacks(updateTimer); //關閉自動匯入

        menu.setGroupVisible(R.id.m_group1, false);
        menu.setGroupVisible(R.id.m_group2, true);
        menu.setGroupVisible(R.id.m_group3, false);
        b_Relbutton.setVisibility(View.VISIBLE);
        b_editon.setVisibility(View.VISIBLE);
        btAdd.setVisibility(View.INVISIBLE);
        btAbandon.setVisibility(View.INVISIBLE);
        btquery.setVisibility(View.INVISIBLE);
        btreport.setVisibility(View.INVISIBLE);
        btEdit.setVisibility(View.VISIBLE);
        btDel.setVisibility(View.VISIBLE);
        mSpnName.setVisibility(View.VISIBLE);
        u_button_ontouch();
        touch_flag = true;  //開啟ontuchevent
        index = mSpnName.getSelectedItemPosition(); // 找到按何項
        //        mSpnName.setEnabled(false);
        showRec(index); //重設spainner 小窗顯示及細目內容
    }

    private void u_button_ontouch() {
        btTop.setVisibility(View.VISIBLE);
        btNext.setVisibility(View.VISIBLE);
        btPrev.setVisibility(View.VISIBLE);
        btEnd.setVisibility(View.VISIBLE);
    }

    private void u_menu_return() {
        menu.setGroupVisible(R.id.m_group1, false);
        menu.setGroupVisible(R.id.m_group2, false);
        menu.setGroupVisible(R.id.m_group3, true);
    }

    private void stop_edit() {
        mSpnName.setEnabled(true);
        old_index = mSpnName.getSelectedItemPosition();
        u_menu_main();
        edittype = false;
        // -------執行匯入MySQL
        dbmysql();
        recSet = dbHper.getRecSet();  //重新載入SQLite
        u_setspinner();  //重新設定spinner內容
        index = old_index;
        showRec(index); //重設spainner 小窗顯示及細目內容
        handler.post(updateTimer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent it = new Intent();
        switch (item.getItemId()) {
            case R.id.m_add://新增
                btTop.setVisibility(View.INVISIBLE);
                btNext.setVisibility(View.INVISIBLE);
                btPrev.setVisibility(View.INVISIBLE);
                btEnd.setVisibility(View.INVISIBLE);
                mSpnName.setVisibility(View.VISIBLE);
                touch_flag = false;  //關閉ontuchevent
                u_menu_return();
                u_insert();
                break;
            case R.id.m_query://查詢
                btAdd.setVisibility(View.INVISIBLE);
                btAbandon.setVisibility(View.VISIBLE);
                btEdit.setVisibility(View.INVISIBLE);
                btDel.setVisibility(View.INVISIBLE);
                btquery.setVisibility(View.VISIBLE);
                b_id.setEnabled(false);
                b_id.setText("");
                b_name.setText("");
                b_grp.setText(" ");
                b_address.setText(" ");
                b_id.setHint("(欄位未輸入時,表示皆可)");
                break;
            case R.id.m_list://列表
                btAdd.setVisibility(View.INVISIBLE);
                btAbandon.setVisibility(View.VISIBLE);
                btEdit.setVisibility(View.INVISIBLE);
                btDel.setVisibility(View.INVISIBLE);
                btquery.setVisibility(View.INVISIBLE);
                btreport.setVisibility(View.VISIBLE);

                brelative01.setVisibility(View.VISIBLE);
                blinear02.setVisibility(View.INVISIBLE);
                b_id.setEnabled(false);
                b_id.setText("");
                b_name.setText("");
                b_grp.setText("");
                b_address.setText("");
                b_id.setHint("請輸入");
                break;
            case R.id.m_return:
                btAbandon.performClick(); //觸發放棄按鈕
                break;
            case R.id.action_settings:
                this.finish();
                // finish()：結束當前 Activity，不會立即釋放內存。遵循 android 內存管理機制。
                // exit()：結束當前組件如 Activity，並立即釋放當前 Activity 所占資源。
                // killProcess()：結束當前組件如 Activity，並立即釋放當前Activity  所占資源。
                // restartPackage()：結束整個 App，包括 service 等其它 Activity 組件。
//                btAbandon.performClick();
                break;
            case R.id.m_edit_start:  //啟用編輯
                u_menu_edit_main();
                edittype = true;
                break;
            case R.id.m_edit_stop: //關閉編輯
                stop_edit(); //返回起始狀態
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // ---------------------------------------------
    private String NetwordDetect() { //取得手機IP
        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wm = (WifiManager) getApplicationContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        String IPaddress = Finduserip.NetwordDetect(CM, wm);
        return IPaddress;
    }
    // ---------------------------------------------
    public boolean onKeyDown(int keyCode, KeyEvent event) { //進用返回鍵
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }
}

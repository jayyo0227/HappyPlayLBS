package tw.tcnr18.m1901;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DBConnector {
    public static int httpstate = 0;
    //--------------------------------------------------------
    private static String postUrl;
    private static String myResponse;
    static String result = null;
    private static String TAG = "tcnr18=>";
    private static OkHttpClient client = new OkHttpClient();
    //---------------------------------------------------------
    static String connect_ip = " https://medicalcarehelper.com/tcnr18/android_mysql_connect/android_connect_db_all_member.php";
//    static String connect_ip = " https://109atcnr18.000webhostapp.com/android_mysql_connect/android_connect_db_all.php";
// -------HOSTING-------
//    static String connect_ip = "https://www.oldpa88.com/android_mysql_connect/android_connect_db_all.php";
//-------000webhost oldpa-------
//    static String connect_ip = "https://oldpa88.000webhostapp.com/android_mysql_connect/android_connect_db_all.php";
    //-----------班長--------
//static String connect_ip = "https://109atcnr01.000webhostapp.com/android_mysql_connect/android_connect_db_all.php";
//-----------01--------
//static String connect_ip = "https://109atcnr02.000webhostapp.com/android_mysql_connect/android_connect_db_all.php";
//-----------02--------
//static String connect_ip = "https://109atcnr05.000webhostapp.com/android_mysql_connect/android_connect_db_all.php";
//-----------03--------
//static String connect_ip = "https://109atcnr26.000webhostapp.com/android_connect_db_all.php";
//-----------04--------
//static String connect_ip = "https://109atcnr28.000webhostapp.com/android_mysql_connect/android_connect_db_all.php";
    //----------------------------------------------------------------------------------------
    /* *************************************************
     *************---MySQL查詢資料----------------
     *************************************************   */
    public static String executeQuery(ArrayList<String> query_string) {
//         client = new OkHttpClient();
        postUrl = connect_ip;
        //--------------
        String query_0 = query_string.get(0);
        FormBody body = new FormBody.Builder()
                .add("selefunc_string", "query")
                .add("query_string", query_0)
                .build();
        Log.d(TAG, "Query=" + query_0);
//--------------
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        httpstate = 0;   //設 httpcode初始直
        try (Response response = client.newCall(request).execute()) {
            // ===========================================
            // 使用httpResponse的方法取得http 狀態碼設定給httpstate變數
            httpstate = response.code();
//            Log.d(TAG, "executeQuery=" + response);
            // ===========================================
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    /*  *************************************************
     *************---MySQL新增資料----------------
     *************************************************   */
    public static String executeInsert(ArrayList<String> query_string) {
//        OkHttpClient client = new OkHttpClient();
        postUrl = connect_ip;
        //--------------
        String query_0 = query_string.get(0);
        String query_1 = query_string.get(1);
        String query_2 = query_string.get(2);

        FormBody body = new FormBody.Builder()
                .add("selefunc_string", "insert")
                .add("name", query_0)
                .add("grp", query_1)
                .add("address", query_2)
                .build();
//--------------
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    /* *************************************************
     *************---MySQL更新資料----------------
     *************************************************   */
    public static String executeUpdate(ArrayList<String> query_string) {
//        OkHttpClient client = new OkHttpClient();
        postUrl = connect_ip;
        //--------------
        String query_0 = query_string.get(0);
        String query_1 = query_string.get(1);
        String query_2 = query_string.get(2);
        String query_3 = query_string.get(3);
        FormBody body = new FormBody.Builder()
                .add("selefunc_string", "update")
                .add("id", query_0)
                .add("name", query_1)
                .add("grp", query_2)
                .add("address", query_3)
                .build();
//--------------
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* *************************************************
     *************---MySQL刪除資料----------------
     *************************************************   */
    public static String executeDelet(ArrayList<String> query_string) {
//        OkHttpClient client = new OkHttpClient();
        postUrl = connect_ip;
        //--------------
        String query_0 = query_string.get(0);

        FormBody body = new FormBody.Builder()
                .add("selefunc_string", "delete")
                .add("id", query_0)
                .build();
//--------------
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
//==========================
}




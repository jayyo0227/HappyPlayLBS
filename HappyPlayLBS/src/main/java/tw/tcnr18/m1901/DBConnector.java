package tw.tcnr18.m1901;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DBConnector {
    public static int httpstate = 0;
    //宣告類別變數以方便存取，並判斷是否連線成功
//    public static int httpstate = 0;
    static String result = null;
    static String TAG = "tcnr18 =>";
    //---------------------------
    static InputStream is = null;
    static String line = null;
    static int code;
    static String mysql_code = null;

    //----定義一個字符串，值為服務器地址，由於這裡是post方式進行請求數據------------
//------- localhost-------
// static String connect_ip = "http://192.168.0.88/android_mysql_connect/";
// -----------我的--------
    static String connect_ip = " https://109atcnr18.000webhostapp.com/android_mysql_connect/";
    //-------000webhost-------
//    static String connect_ip = "http://oldpa88.000webhostapp.com/android_mysql_connect/";
//-----------班長--------
//            static String connect_ip = "https://109atcnr01.000webhostapp.com/android_mysql_connect/";
//-----------01--------
//            static String connect_ip =" https://109atcnr02.000webhostapp.com/android_mysql_connect/";
//-----------02--------
//            static String connect_ip =" https://109atcnr05.000webhostapp.com/android_mysql_connect/";
//-----------03--------
//    static String connect_ip = " https://109atcnr26.000webhostapp.com/android_mysql_connect/";
//-----------04--------
//            static String connect_ip = " https://109atcnr28.000webhostapp.com/android_mysql_connect/";

    public static String executeQuery(String query_string) {
        String result = "";
        //--創建URL對象，並將上述服務器地址字符串轉化成URL地址
        HttpURLConnection urlConnection = null;
        InputStream is = null;
        try {
            URL url = new URL(connect_ip + "android_connect_db_all.php");//php的位置
            //--創建HttpURLConnection對象，通過URL對象的openConnection打開網址，注意此時只是打開網址
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");// 设置请求方法为post

            urlConnection.setReadTimeout(5000);// 设置读取超时为5秒
            urlConnection.setConnectTimeout(10000);// 设置连接网络超时为10秒
            urlConnection.setDoOutput(true);// 设置此方法,允许向服务器输出内容
            urlConnection.connect();//接通資料庫
            // selefunc_string -> 給php 使用的參數query:選擇insert:新增update:更新delete:刪除
            // post请求的参数
            String data = "selefunc_string=query&query_string=" + query_string;
            // 获得一个输出流,向服务器写数据,默认情况下,系统不允许向服务器输出内容
            //==============執行輸出===========================================
            OutputStream out = urlConnection.getOutputStream();

            out.write(data.getBytes());
            out.flush();
            out.close();
            //==============取得回傳值=========================================
            is = urlConnection.getInputStream();//從database開啟stream
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            StringBuilder builder = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            is.close();
            result = builder.toString();
            //---------------------------------------------------
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        Log.d(TAG, "DB_RESULT:" + result);
        return result;
    }

    public static String executeInsert(String s, String insmyname, String insmygroup, String insmyaddress) {
        Log.d(TAG, "executeInsert()");
        is = null;
        result = null;
        line = null;
        HttpURLConnection urlConnection = null;
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //---- 連結MySQL-------------------
        try {
            URL url = new URL(connect_ip + "android_connect_db_all.php");//php的位置
            //--創建HttpURLConnection對象，通過URL對象的openConnection打開網址，注意此時只是打開網址
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");// 设置请求方法为post

            urlConnection.setReadTimeout(5000);// 设置读取超时为5秒
            urlConnection.setConnectTimeout(10000);// 设置连接网络超时为10秒
            urlConnection.setDoOutput(true);// 设置此方法,允许向服务器输出内容
            urlConnection.connect();//接通資料庫
            // selefunc_string -> 給php 使用的參數query:選擇insert:新增update:更新delete:刪除
            // post请求的参数
            String data = "selefunc_string=insert&name=" + insmyname + "&grp=" + insmygroup + "&address=" + insmyaddress;
            // 获得一个输出流,向服务器写数据,默认情况下,系统不允许向服务器输出内容
            //==============執行輸出===========================================
            OutputStream out = urlConnection.getOutputStream();

            out.write(data.getBytes());
            out.flush();
            out.close();
            //==============取得回傳值=========================================
            is = urlConnection.getInputStream();//從database開啟stream
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            StringBuilder builder = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            is.close();
            result = builder.toString();
            //---------------------------------------------------
        } catch (Exception e) {
            Log.d(TAG, "Fail 1" + e.toString());
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.d(TAG, "pass 2:" + "connection success ");
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
        try {
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));

            if (code == 1) {
                Log.d(TAG, "pass 3:" + "Inserted Successfully");
            } else {
                Log.d(TAG, "pass 3:" + "Sorry, Try Again");
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return result;
    }

    public static String executeUpdate(String s, String upmyid, String upmyname, String upmygroup, String upmyaddress) {
        Log.d(TAG, "executeUpdate()");
        is = null;
        result = null;
        line = null;
        HttpURLConnection urlConnection = null;
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //---- 連結MySQL-------------------
        try {
            URL url = new URL(connect_ip + "android_connect_db_all.php");//php的位置
            //--創建HttpURLConnection對象，通過URL對象的openConnection打開網址，注意此時只是打開網址
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");// 设置请求方法为post

            urlConnection.setReadTimeout(5000);// 设置读取超时为5秒
            urlConnection.setConnectTimeout(10000);// 设置连接网络超时为10秒
            urlConnection.setDoOutput(true);// 设置此方法,允许向服务器输出内容
            urlConnection.connect();//接通資料庫
            // selefunc_string -> 給php 使用的參數query:選擇insert:新增update:更新delete:刪除
            // post请求的参数
            String data = "selefunc_string=update&id=" + upmyid + "&name=" + upmyname + "&grp=" + upmygroup + "&address=" + upmyaddress;
            // 获得一个输出流,向服务器写数据,默认情况下,系统不允许向服务器输出内容
            //==============執行輸出===========================================
            OutputStream out = urlConnection.getOutputStream();

            out.write(data.getBytes());
            out.flush();
            out.close();
            //==============取得回傳值=========================================
            is = urlConnection.getInputStream();//從database開啟stream
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            StringBuilder builder = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            is.close();
            result = builder.toString();
            //---------------------------------------------------
        } catch (Exception e) {
            Log.d(TAG, "Fail 1" + e.toString());
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.d(TAG, "pass 2:" + "connection success ");
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
        try {
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));

            if (code == 1) {
                Log.d(TAG, "pass 3:" + "Inserted Successfully");
            } else {
                Log.d(TAG, "pass 3:" + "Sorry, Try Again");
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return result;
    }

    //--------------------------------------
    public static String executeQuery_1(String query_string) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(connect_ip + "android_connect_db_all.php");
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
            params.add(new BasicNameValuePair("selefunc_string", "query"));

            // query_string -> 給php 使用的參數
            params.add(new BasicNameValuePair("query_string", query_string));
            Log.d(TAG, "Value=" + params.toString());

            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            // ===========================================
            // 使用httpResponse的方法取得http 狀態碼設定給httpstate變數
            httpstate = httpResponse.getStatusLine().getStatusCode();
            // ===========================================
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();

            Log.d(TAG, "inputStream=" + inputStream.toString());

            BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            inputStream.close();
            result = builder.toString();
            Log.d(TAG, "result" + result);
        } catch (Exception e) {
            Log.d("TAG", "Exception e" + e.toString());
        }

        return result;
    }

    public static String executeInsert(String s, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //---- 連結MySQL-------------------
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(connect_ip + "android_connect_db_all.php");
            // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
            nameValuePairs.add(new BasicNameValuePair("selefunc_string", "insert"));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httppost);  //執行
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.d(TAG, "pass 1:" + "connection success ");
        } catch (Exception e) {
            Log.d(TAG, "Fail 1" + e.toString());
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.d(TAG, "pass 2:" + "connection success ");
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
        //--------真正新增資料-----
        try {
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));

            if (code == 1) {
                Log.d(TAG, "pass 3:" + "Inserted Successfully");
            } else {
                Log.d(TAG, "pass 3:" + "Sorry, Try Again");
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return result;
    }

    public static String executeUpdate(String s, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        String update_code = null;
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
//
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(connect_ip + "android_connect_db_all.php"); //
//--------------------------
        // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
        nameValuePairs.add(new BasicNameValuePair("selefunc_string", "update"));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            HttpResponse response;
            response = httpClient.execute(httpPost); //
            HttpEntity entity = response.getEntity();
            try {
                is = entity.getContent(); // InputStream is = null;

            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "pass 1:" + "connection success");
//------------------------------------------------------------------------------
        try {
            BufferedReader reader = new BufferedReader( new InputStreamReader(is, "utf8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
//----------------------------------------
        try {
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));
            if (code == 1) {
                update_code = "updata Successfully";
            } else {
                update_code = "Sorry,Try Again";
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return update_code;
    }

    public static String executeDelet(String delete_from_member_, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        mysql_code = null;
        Log.d(TAG, "value=" + nameValuePairs);
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
//--------------------------------------------------------------------------------------
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(connect_ip + "android_connect_db_all.php");
        // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
        nameValuePairs.add(new BasicNameValuePair("selefunc_string", "delete"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            HttpResponse response;
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            try {
                is = entity.getContent();
            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "pass 1:" + "connection success");
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "utf8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.d(TAG, "pass 2:" + "connection success");
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
        try {
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));
            if (code == 1) {
                mysql_code = "delete Successfully";
            } else {
                mysql_code = "Sorry,Try Again";
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return mysql_code;
    }
}
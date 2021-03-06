package com.example.jsrgjhl.hlapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jsrgjhl.hlapp.Adapter.Record;
import com.example.jsrgjhl.hlapp.Adapter.RecordsAdapter;
import com.example.jsrgjhl.hlapp.PersonalSetting.OperateRecord;
import com.example.jsrgjhl.hlapp.R;
import com.example.jsrgjhl.hlapp.Utils.jsonstr2map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecordActivity extends AppCompatActivity implements Serializable{

    private Handler mHandler;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isPause = false;
    private boolean isStop = true;
    private static int delay = 2; //1s
    private static int period = 2;  //1s
    private static int count = 0;
    private final int UPDATE_LIST=0;

    private TextView mstatictv;
    private List<Record> mrecordsList=new ArrayList<>();
    private List<Record> mnowrecordsList=new ArrayList<>();
    private String getrecordpath="http://47.100.107.158:8080/api/record/getrecordlist";
    private int flag;
    private RecordsAdapter adapter;
    private final static String Tag= OperateRecord.class.getSimpleName();
    private Date recordtime;
    private Date deltime;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        //toolbar部分
        Toolbar toolbar = (Toolbar) findViewById(R.id.recordtoolbar);
        mstatictv=(TextView)findViewById(R.id.static_tv);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        }
        //统计按钮事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mstatictv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RecordActivity.this,"这是要跳转到统计页面后面在做", Toast.LENGTH_SHORT).show();
            }
        });

        sp=getSharedPreferences("userinfo", MODE_PRIVATE);
        editor=sp.edit();
        userName = sp.getString("username","");
        //初始化Records列表
        initRecords();
        mnowrecordsList.clear();
        mnowrecordsList.addAll(mrecordsList);
        adapter=new RecordsAdapter(RecordActivity.this, R.layout.record_list,mnowrecordsList);
        ListView recordlistView=(ListView)findViewById(R.id.record_listview);
        recordlistView.setAdapter(adapter);

        //item点击事件
        recordlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(RecordActivity.this, SolveActivity.class);
                Record record0=mrecordsList.get(i);
                intent.putExtra("record",(Serializable)record0);
                startActivity(intent);
            }
        });
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_LIST:
                        mnowrecordsList.clear();
                        mnowrecordsList.addAll(mrecordsList);
                        adapter.notifyDataSetChanged();
                    default:
                        break;
                }
            }
        };
        startTimer();

    }

    /*
     * 向服务器请求记录列表
     */
    private void initRecords() {
        flag=0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(getrecordpath).build();
                try {
                    Response response = client.newCall(request).execute();//发送请求
                    String result = response.body().string();
                    Map<String, Object> map= jsonstr2map.jsonstr2map(result);
                    /**
                     * 将 string 转为json格式
                     */
                    String temp = map.get("data").toString();
                    if(temp.equals("null")){
                        flag=1;
                        return;
                    }
                    temp = temp.substring(1, temp.length() - 1).replace(" ", "").replace("{", "").replace("}", "").replace("\"","").replace("\"","");
                    Log.i(Tag,temp);
                    String[] strs = temp.split(",");
                    Map<String, String> map2 = new HashMap<String, String>();
                    for (String s : strs) {
                        String sss=s.replace(" ","");
                        String[] ms = sss.split("=");

                        if(ms.length==2){
                            if (ms[1].equals("null")) {
                                ms[1] = "";
                            }
                        }
                        if (map2.containsKey(ms[0])&&ms.length==2) {
                            Record record1 = new Record(map2.get("recordID"),(String) map2.get("recordnum"),map2.get("recordtime"), map2.get("recordstatus"), map2.get("solutionID"),map2.get("userID"), (String) map2.get("title"), (String) map2.get("context"), map2.get("deviceID"),(String)map2.get("devicenum"),(String)map2.get("deviceaddress"),map2.get("regionID"),map2.get("defposID"),map2.get("devicelat"),map2.get("devicelng"),map2.get("devicetype"),map2.get("devicestatus"),map2.get("deltime"));
                            mrecordsList.add(record1);
                            map2.clear();
                            map2.put(ms[0],ms[1]);
                        }
                        else{
                            if(ms.length==2)
                            map2.put(ms[0], ms[1]);
                        }
                    }
                    Record record1 = new Record(map2.get("recordID"),(String) map2.get("recordnum"),map2.get("recordtime"), map2.get("recordstatus"), map2.get("solutionID"),map2.get("userID"), (String) map2.get("title"), (String) map2.get("context"), map2.get("deviceID"),(String)map2.get("devicenum"),(String)map2.get("deviceaddress"),map2.get("regionID"),map2.get("defposID"),map2.get("devicelat"),map2.get("devicelng"),map2.get("devicetype"),map2.get("devicestatus"),map2.get("deltime"));
                    mrecordsList.add(record1);
                    if(mrecordsList.size()!=0){
                        deletzero();
                        flag=1;
                    }else flag=2;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        while (flag==0){
            try{
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        if(flag==1){
            Log.i(Tag,"设备记录请求成功");
        }

    }

    /**
     * 删掉列表中的0
     */
    public void deletzero(){
        for (int i=0;i<mrecordsList.size();i++){
            if(mrecordsList.get(i).getDevicenum().equals("0")){
                mrecordsList.remove(i);
                i--;
            }
        }
    }

    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run(){
                    flag=0;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mrecordsList.clear();
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder().url(getrecordpath).build();
                            try {
                                Response response = client.newCall(request).execute();//发送请求
                                String result = response.body().string();
                                Map<String, Object> map= jsonstr2map.jsonstr2map(result);
                                /**
                                 * 将 string 转为json格式
                                 */
                                String temp = map.get("data").toString();
                                if(temp.equals("null")){
                                    flag=1;
                                    return;
                                }
                                temp = temp.substring(1, temp.length() - 1).replace(" ", "").replace("{", "").replace("}", "").replace("\"","").replace("\"","");
                                Log.i(Tag,temp);
                                String[] strs = temp.split(",");
                                Map<String, String> map2 = new HashMap<String, String>();
                                for (String s : strs) {
                                    String sss=s.replace(" ","");
                                    String[] ms = sss.split("=");

                                    if (ms.length==2&&ms[1].equals("null")) {
                                        ms[1] = "";
                                    }
                                    if (map2.containsKey(ms[0])) {
                                        Record record1 = new Record(map2.get("recordID"),(String) map2.get("recordnum"),map2.get("recordtime"), map2.get("recordstatus"), map2.get("solutionID"),map2.get("userID"), (String) map2.get("title"), (String) map2.get("context"), map2.get("deviceID"),(String)map2.get("devicenum"),(String)map2.get("deviceaddress"),map2.get("regionID"),map2.get("defposID"),map2.get("devicelat"),map2.get("devicelng"),map2.get("devicetype"),map2.get("devicestatus"),map2.get("deltime"));
                                        mrecordsList.add(record1);
                                        map2.clear();
                                        if(ms.length==2)
                                        map2.put(ms[0],ms[1]);
                                    }
                                    else{
                                        if(ms.length==2)
                                        map2.put(ms[0], ms[1]);
                                    }
                                }
                                Record record1 = new Record(map2.get("recordID"),(String) map2.get("recordnum"),map2.get("recordtime"), map2.get("recordstatus"), map2.get("solutionID"),map2.get("userID"), (String) map2.get("title"), (String) map2.get("context"), map2.get("deviceID"),(String)map2.get("devicenum"),(String)map2.get("deviceaddress"),map2.get("regionID"),map2.get("defposID"),map2.get("devicelat"),map2.get("devicelng"),map2.get("devicetype"),map2.get("devicestatus"),map2.get("deltime"));
                                mrecordsList.add(record1);
                                if(mrecordsList.size()!=0){
                                    deletzero();
                                    flag=1;
                                }else flag=2;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    while (flag==0){
                        try{
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    };
                    sendMessage(UPDATE_LIST);
                    do {
                        try {
                            Log.i(Tag, "sleep(4000)...");
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                        }
                    } while (isPause);
                }
            };
        }
        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask,delay, period);
    }

    private void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        count = 0;
    }

    public void sendMessage(int id){
        if (mHandler != null) {
            Message message = Message.obtain(mHandler, id);
            mHandler.sendMessage(message);
        }
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
    protected void onDestroy(){
        super.onDestroy();
        stopTimer();
    }

}

package com.xinyu.dronecontroller;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnClickListener{
    private static final int ACTION_SEARCH = 1;
    private static final int ACTION_CONNECT  = 2;

    private Button btnRefresh;
    private ListView listDrone;
    private ProgressBar progressBar;
    private Toast toast;

    private SimpleAdapter adapter;  // listview 适配器
    private List<Map<String, String>> dronelist = new ArrayList<Map<String, String>>(); // 设备列表

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what) {
                case ACTION_SEARCH:
                    dronelist.clear();
                    dronelist.addAll((List<Map<String, String>>) msg.obj);
                    adapter.notifyDataSetChanged();
                    hideProgressBar();
                    break;
                case ACTION_CONNECT:
                    // 连接成功
                    if(((int) msg.obj) == 0){
                        // 跳转到控制器
                        gotoController();
                    }else{
                        // Toast
                    }
                    break;
            }
        }
    };

    /**
     * 跳转到ControllerActivity
     */
    private void gotoController(){
        Intent controllerIntent = new Intent(this, ControllerActivity.class);
        startActivity(controllerIntent);
    }

    /**
     * 显示加载动画
     */
    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏加载动画
     */
    private void hideProgressBar(){
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * 将Int类型的IP地址转换成String
     * @param ip 32位ip地址
     * @return  String类型的IP地址
     */
    private String IntToIP(int ip){
        return (ip & 0xFF ) + "." +
                ((ip >> 8 ) & 0xFF) + "." +
                ((ip >> 16 ) & 0xFF) + "." +
                ( ip >> 24 & 0xFF);
    }

    /**
     * 获取本机IP(WIFI)
     * @return
     */
    private String getHostIP() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip;
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        ip = IntToIP(ipAddress);

        return ip;
    }

    /**
     * 搜索设备
     * @return 设备列表
     */
    private List<Map<String, String>> getData(){
        return dronelist;
    }

    /**
     * 刷新设备列表
     */
    private void refresh(){
        showProgressBar();
        listDrone.removeViews(0, listDrone.getCount());

        // 开新线程执行
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = getHostIP();
                String segment = ip.substring(0, ip.lastIndexOf('.') + 1);

                Message msg = Message.obtain();
                msg.what = ACTION_SEARCH;
                msg.obj = SocketService.search(segment);
                handler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 链接设备
     * @param ip 设备IP地址
     */
    private void connect(String name, final String ip){
        // 显示toast
        Toast toast = Toast.makeText(this, "连接到" + name + "...", Toast.LENGTH_SHORT);
        toast.show();

        // 开新线程连接设备
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = ACTION_CONNECT;
                msg.obj = SocketService.connect(ip);
                handler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 刷新按钮
        btnRefresh = (Button) findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressbar_search);

        // 设备列表
        listDrone = (ListView) findViewById(R.id.list_drone);

        // 设置ListAdapter
        adapter = new SimpleAdapter(this, getData(), R.layout.dronelist,
                new String[]{"name", "ip"},
                new int[]{R.id.dronelist_item_name, R.id.dronelist_item_ip});
        listDrone.setAdapter(adapter);
        listDrone.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connect(dronelist.get(position).get("name"), dronelist.get(position).get("ip"));
            }
        });

        refresh();
    }

    @Override
    public void onClick(View view) {
//        Intent intent = new Intent(this, ControllerActivity.class);
//        startActivity(intent);
        switch (view.getId()) {
            case R.id.btn_refresh:
                showProgressBar();
                refresh();
                break;
        }
    }
}

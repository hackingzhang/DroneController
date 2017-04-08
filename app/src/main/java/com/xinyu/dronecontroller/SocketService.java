package com.xinyu.dronecontroller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SocketService extends Service {
    private static final int PORT_CONTROL = 60000;
    private static final int PORT_DATA = 61000;

    private static Socket socketControl; // 控制信号
    private static Socket socketData;    // 回传信号

    private static BufferedReader in_control;
    private static PrintWriter out_control;
    private static BufferedReader in_data;
    private static PrintWriter out_data;

    public SocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    /**
     * 检测IP和端口是否可连接
     * @param ip IP地址
     * @param port 端口
     * @return true 可连接， false 不可连接
     */
    public static boolean isHostReachable(String ip, int port){
        Socket tmpSocket = new Socket();
        try {
            tmpSocket.connect(new InetSocketAddress(ip, port), 200);
        }
        catch(IOException e){
            return false;
        }
        finally {
            try {
                tmpSocket.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * 搜索设备
     * @param segment
     * @return
     */
    public static List<Map<String, String>> search(String segment){
        List<Map<String, String>> deviceList = new ArrayList<Map<String, String>>();

        for(int i = 1; i < 10; i++){
            String ip = segment + String.valueOf(i);

            if(isHostReachable(ip, PORT_CONTROL)){
                Map<String, String> device = new HashMap<String, String>();
                device.put("name", ip);
                device.put("ip", ip);
                deviceList.add(device);
            }
        }

        return deviceList;
    }

    /**
     * 连接到设备
     * @param ip 设备IP地址
     * @return 0 连接成功 -1 连接失败
     */
    public static int connect(String ip){
        try{
            socketControl = new Socket(ip, PORT_CONTROL);
//            socketData = new Socket(ip, PORT_DATA);

            in_control = new BufferedReader(new InputStreamReader(socketControl.getInputStream()));
            out_control = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketControl.getOutputStream())));

//            in_data = new BufferedReader(new InputStreamReader(socketData.getInputStream()));
//            out_data = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketData.getOutputStream())));

            return 0;
        }
        catch(IOException e){
            Log.i("socket", e.getMessage());
            close();
            return -1;
        }
    }

    /**
     * 发送数据到服务器
     * @param data 要发送的数据
     */
    public static void sendControl(String data){
        out_control.print(data);
    }

    /**
     * 关闭socket连接
     * @return
     */
    public static int close(){
        try{
            socketControl.close();
            socketData.close();

            return 0;
        }
        catch(IOException e){
            Log.i("socket", e.getMessage());
            return -1;
        }
    }
}
package com.xinyu.dronecontroller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

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
    private static final String ECHO_KEY = "XINYU_DRONE";
    private static final String CONNECT_REFUSE = "REFUSE";

    private static final int PORT_ECHO = 60001;
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
        BufferedReader reader;
        Socket tmpSocket = new Socket();
        try {
            tmpSocket.connect(new InetSocketAddress(ip, port), 200);
            reader = new BufferedReader(new InputStreamReader(tmpSocket.getInputStream()));
            String echo = reader.readLine();
            if(echo.equals(ECHO_KEY)){
                return true;
            }else{
                return false;
            }
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

            if(isHostReachable(ip, PORT_ECHO)){
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

            // 连接验证
            String token = in_control.readLine();   // 1. 获取token
            out_control.println(token);             // 2. 将token发回
            out_control.flush();
            String ack = in_control.readLine();     // 3. 获取应答信息
            if(ack == CONNECT_REFUSE && ack == null){           // 4. 如果应答信息是REFUSE，连接失败
                return -1;
            }

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
     * 发送控制信号到服务器
     * @param data 要发送的数据
     */
    public static int sendControl(String data){
        if(socketControl.isConnected() && !socketControl.isClosed()) {
            Log.i("socket", data);
            out_control.println(data);
            out_control.flush();
            return 0;
        }else{
            return -1;
        }
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

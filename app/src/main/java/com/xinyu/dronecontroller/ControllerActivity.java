package com.xinyu.dronecontroller;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.xinyu.joystick.Joystick;

import java.net.Socket;

public class ControllerActivity extends AppCompatActivity {
    private static final int ACTION_CONTROL_SEND = 3;
    private static final int ACTION_CONTROL_ACK = 4;
    private static final int ACTION_DATA_REQUEST = 5;
    private static final int ACTION_DATA_ACK = 6;

    private boolean unlocked = false;   // 解锁标志
    private boolean started = false;

    private Joystick ctlThrottleAndYaw;
    private Joystick ctlPitchAndRoll;
    private Switch swtUnlock;
    private TextView txtLeftAngle;
    private TextView txtRightAngle;
    private TextView txtThrottle;
    private TextView txtYaw;
    private TextView txtPitch;
    private TextView txtRoll;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case ACTION_CONTROL_SEND:
                    break;
                case ACTION_CONTROL_ACK:
                    break;
                case ACTION_DATA_REQUEST:
                    break;
                case ACTION_DATA_ACK:
                    break;
            }
        }
    };

    private void send(final String data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketService.sendControl(data);
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_controller);

        // 油门/偏航摇杆 和 俯仰/横滚摇杆
        ctlThrottleAndYaw = (Joystick) findViewById(R.id.throttle_and_yaw);
        ctlPitchAndRoll = (Joystick) findViewById(R.id.pitch_and_roll);
        ctlThrottleAndYaw.setOnNavAndSpeedListener(new ThrottleAndYawListener());
        ctlPitchAndRoll.setOnNavAndSpeedListener(new PitchAndRollListener());

        // 解锁/锁定开关
        swtUnlock = (Switch) findViewById(R.id.swtUnlock);
        swtUnlock.setOnCheckedChangeListener(new UnlockChangeListener());

        // 舵量信息
        txtLeftAngle = (TextView) findViewById(R.id.txtLeftAngle);
        txtRightAngle = (TextView) findViewById(R.id.txtRightAngle);
        txtThrottle = (TextView) findViewById(R.id.txtThrottle);
        txtYaw = (TextView) findViewById(R.id.txtYaw);
        txtPitch = (TextView) findViewById(R.id.txtPitch);
        txtRoll = (TextView) findViewById(R.id.txtRoll);
    }

    /**
     * 油门和偏航监听器
     */
    class ThrottleAndYawListener implements Joystick.onAngleAndStrengthListenner{
        public void onAngleAndStrength(double angle, double strengthX, double strengthY){
            txtLeftAngle.setText(String.valueOf(angle));
            txtThrottle.setText(String.valueOf(strengthY));
            txtYaw.setText(String.valueOf(strengthX));
            send("{\"angle\": " + angle + ", \"strengthX\":" + strengthX + ", \"strengthX\":" + strengthX + "}");
        }
    }

    class PitchAndRollListener implements Joystick.onAngleAndStrengthListenner{
        public void onAngleAndStrength(double angle, double strengthX, double strengthY) {
            txtRightAngle.setText(String.valueOf(angle));
            txtPitch.setText(String.valueOf(strengthY));
            txtRoll.setText(String.valueOf(strengthX));
            send("{\"angle\": " + angle + ", \"strengthX\":" + strengthX + ", \"strengthX\":" + strengthX + "}");
        }
    }

    class UnlockChangeListener implements Switch.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton btn, boolean checked){
            if(checked)
                unlocked = true;
            else
                unlocked = false;
        }
    }
}
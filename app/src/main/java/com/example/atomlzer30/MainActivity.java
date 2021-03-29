package com.example.atomlzer30;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.liys.view.LineProView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public  SerialPortThread serialPortThread;
    private long countdown1=0,countdown2=0,countdown3=0,countdown4=0;//设备倒计时时长
    private int taskTime1=0,taskTime2=0,taskTime3=0,taskTime4=0;//设备定时时长
    private Timer timerTask;//计时器
    private boolean state1=false,state2=false,state3=false,state4=false;//设备状态
    private byte sendData=0x20;
    /***********控件初始化*************/
    protected DashboardView tempDashboardView,humDashboardView,levelDashboard;
    protected Button device1Button,device2Button,device3Button,device4Button;
    protected TextView lastTime1,lastTime2,lastTime3,lastTime4;
    protected CircleProgress mCpLoading;
    protected MyNumberPicker np1,np2,np3,np4;
    protected LineProView lineProView1,lineProView2,lineProView3,lineProView4;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        hideBottomUIMenu();

        device1Button=findViewById(R.id.device1Button);
        device2Button=findViewById(R.id.device2Button);
        device3Button=findViewById(R.id.device3Button);
        device4Button=findViewById(R.id.device4Button);

        lastTime1=findViewById(R.id.lastTime1);
        lastTime2=findViewById(R.id.lastTime2);
        lastTime3=findViewById(R.id.lastTime3);
        lastTime4=findViewById(R.id.lastTime4);

        np1 = findViewById(R.id.np1);
        np2 = findViewById(R.id.np2);
        np3 = findViewById(R.id.np3);
        np4 = findViewById(R.id.np4);

        lineProView1=findViewById(R.id.lineProView1);
        lineProView2=findViewById(R.id.lineProView2);
        lineProView3=findViewById(R.id.lineProView3);
        lineProView4=findViewById(R.id.lineProView4);

        device1Button.setOnClickListener(this);
        device2Button.setOnClickListener(this);
        device3Button.setOnClickListener(this);
        device4Button.setOnClickListener(this);

        np1.setMinValue(0);
        np1.setMaxValue(100);
        np1.setValue(50);

        taskTime1=np1.getValue();
        Log.d("TAG","taskTime1：" + taskTime1);
        np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                taskTime1=newVal;
                //Log.d("TAG","oldVal：" + oldVal + "   newVal：" + newVal);
            }
        });

        np2.setMinValue(0);
        np2.setMaxValue(100);
        np2.setValue(50);
        np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        taskTime2=np2.getValue();
        Log.d("TAG","taskTime2：" + taskTime2);
        np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                taskTime2=newVal;
                //Log.d("TAG","oldVal：" + oldVal + "   newVal：" + newVal);
            }
        });

        np3.setMinValue(0);
        np3.setMaxValue(100);
        np3.setValue(50);
        np3.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        taskTime3=np3.getValue();
        Log.d("TAG","taskTime3：" + taskTime3);
        np3.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np3.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                taskTime3=newVal;
                //Log.d("TAG","oldVal：" + oldVal + "   newVal：" + newVal);
            }
        });

        np4.setMinValue(0);
        np4.setMaxValue(100);
        np4.setValue(50);
        np4.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        taskTime4=np4.getValue();
        Log.d("TAG","taskTime4：" + taskTime4);
        np4.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np4.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                taskTime4=newVal;
                //Log.d("TAG","oldVal：" + oldVal + "   newVal：" + newVal);
            }
        });

        mCpLoading = findViewById(R.id.cp_loading);
        //mCpLoading.setProgress(100,5000);
        mCpLoading.setProgress(90);
        mCpLoading.setOnCircleProgressListener(new CircleProgress.OnCircleProgressListener() {
            @Override
            public boolean OnCircleProgress(int progress) {
                return false;
            }
        });

        if (timerTask==null){
            timerTask = new Timer(true);
            timerTask.schedule(countTask, 500, 1000);
        }
    }
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {

            Window _window = getWindow();
            WindowManager.LayoutParams params = _window.getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
            _window.setAttributes(params);
        }
    }
    public TimerTask countTask = new TimerTask() {
        public void run() {
            long currentTime = System.currentTimeMillis();
            TaskData taskData1=new TaskData();
            if (currentTime>=countdown1){//结束
                sendData=(byte)(sendData&(~0x01));
                state1=false;
                sendHandler(1,taskData1);
            }else{//进行中
                sendData=(byte)(sendData|0x01);
                double progess=((double) (countdown1-currentTime))/(double)(taskTime1*60*1000);
                //Log.v("tag","progess:"+progess+"  "+(countdown1-currentTime)+"  "+(taskTime1*60*1000));
                taskData1.setProgess(progess);
                int time= (int) ((countdown1-currentTime)/1000);
                taskData1.setLastTime(time);
                sendHandler(1,taskData1);

            }
            TaskData taskData2=new TaskData();
            if (currentTime>=countdown2){//结束
                sendData=(byte)(sendData&(~0x02));
                state2=false;
                sendHandler(2,taskData2);
            }else{//进行中
                sendData=(byte)(sendData|0x02);
                double progess=((double) (countdown2-currentTime))/(double)(taskTime2*60*1000);
                //Log.v("tag","progess:"+progess+"  "+(countdown2-currentTime)+"  "+(taskTime2*60*1000));
                taskData2.setProgess(progess);
                int time= (int) ((countdown2-currentTime)/1000);
                taskData2.setLastTime(time);
                sendHandler(2,taskData2);
                byte[] sendBuf={0x25};
                //serialPortThread.sendSerialPort(sendBuf);
            }
            TaskData taskData3=new TaskData();
            if (currentTime>=countdown3){//结束
                sendData=(byte)(sendData&(~0x04));
                state3=false;
                sendHandler(3,taskData3);
            }else{//进行中
                sendData=(byte)(sendData|0x04);
                double progess=((double) (countdown3-currentTime))/(double)(taskTime3*60*1000);
                //Log.v("tag","progess:"+progess+"  "+(countdown1-currentTime)+"  "+(taskTime1*60*1000));
                taskData3.setProgess(progess);
                int time= (int) ((countdown3-currentTime)/1000);
                taskData3.setLastTime(time);
                sendHandler(3,taskData3);
                byte[] sendBuf={0x25};
                //serialPortThread.sendSerialPort(sendBuf);
            }
            TaskData taskData4=new TaskData();
            if (currentTime>=countdown4){//结束
                sendData=(byte)(sendData&(~0x08));
                state4=false;
                sendHandler(4,taskData4);
            }else{//进行中
                sendData=(byte)(sendData|0x08);
                double progess=((double) (countdown4-currentTime))/(double)(taskTime4*60*1000);
                //Log.v("tag","progess:"+progess+"  "+(countdown1-currentTime)+"  "+(taskTime1*60*1000));
                taskData4.setProgess(progess);
                int time= (int) ((countdown4-currentTime)/1000);
                taskData4.setLastTime(time);
                sendHandler(4,taskData4);
                byte[] sendBuf={0x25};
                //serialPortThread.sendSerialPort(sendBuf);
            }

            byte[] sendBuf={0};
            sendBuf[0]=sendData;
            //Log.d("TAG","sendData:"+sendData);
            //serialPortThread.sendSerialPort(sendBuf);

        }
    };

    private Handler mHandler  = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if (state1){
                        TaskData taskData=new TaskData();
                        taskData=(TaskData)msg.obj;
                        lineProView1.setProgress(100-taskData.getProgess()*100);
                        String minutes=String.format("%0" + 2 + "d", taskData.getLastTime()/60);
                        String second=String.format("%0" + 2 + "d", ((int)taskData.getLastTime()%60));
                        lastTime1.setText(minutes+":"+second);
                    }else {
                        lineProView1.setProgress(0);
                        lastTime1.setText("00:00");
                        device1Button.setText("开始");
                    }
                    break;
                case 2:
                    if (state2){
                        TaskData taskData=new TaskData();
                        taskData=(TaskData)msg.obj;
                        lineProView2.setProgress(100-taskData.getProgess()*100);
                        String minutes=String.format("%0" + 2 + "d", taskData.getLastTime()/60);
                        String second=String.format("%0" + 2 + "d", ((int)taskData.getLastTime()%60));
                        lastTime2.setText(minutes+":"+second);
                    }else {
                        lineProView2.setProgress(0);
                        lastTime2.setText("00:00");
                        device2Button.setText("开始");
                    }
                    break;
                case 3:
                    if (state3){
                        TaskData taskData=new TaskData();
                        taskData=(TaskData)msg.obj;
                        lineProView3.setProgress(100-taskData.getProgess()*100);
                        String minutes=String.format("%0" + 2 + "d", taskData.getLastTime()/60);
                        String second=String.format("%0" + 2 + "d", ((int)taskData.getLastTime()%60));
                        lastTime3.setText(minutes+":"+second);
                    }else {
                        lineProView3.setProgress(0);
                        lastTime3.setText("00:00");
                        device3Button.setText("开始");
                    }
                    break;
                case 4:
                    if (state4){
                        TaskData taskData=new TaskData();
                        taskData=(TaskData)msg.obj;
                        lineProView4.setProgress(100-taskData.getProgess()*100);
                        String minutes=String.format("%0" + 2 + "d", taskData.getLastTime()/60);
                        String second=String.format("%0" + 2 + "d", ((int)taskData.getLastTime()%60));
                        lastTime4.setText(minutes+":"+second);
                    }else {
                        lineProView4.setProgress(0);
                        lastTime4.setText("00:00");
                        device4Button.setText("开始");
                    }
                    break;
            }
        }
    };

    private void sendHandler(int what,Object obj){
        Message msg = new Message();
        msg.what=what;
        msg.obj=obj;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.device1Button:
                if (!state1){
                    device1Button.setText("停止");
                    long currentTime = System.currentTimeMillis();
                    countdown1=currentTime+taskTime1*60*1000;
                    state1=true;
                }else {
                    device1Button.setText("开始");
                    long currentTime = System.currentTimeMillis();
                    countdown1=System.currentTimeMillis();
                    state1=false;
                }
                break;
            case R.id.device2Button:
                if (!state2){
                    device2Button.setText("停止");
                    long currentTime = System.currentTimeMillis();
                    countdown2=currentTime+taskTime2*60*1000;
                    state2=true;
                }else {
                    device2Button.setText("开始");
                    long currentTime = System.currentTimeMillis();
                    countdown2=System.currentTimeMillis();
                    state2=false;
                }
                break;
            case R.id.device3Button:
                if (!state3){
                    device3Button.setText("停止");
                    long currentTime = System.currentTimeMillis();
                    countdown3=currentTime+taskTime3*60*1000;
                    state3=true;
                }else {
                    device3Button.setText("开始");
                    long currentTime = System.currentTimeMillis();
                    countdown3=System.currentTimeMillis();
                    state3=false;
                }
                break;
            case R.id.device4Button:
                if (!state4){
                    device4Button.setText("停止");
                    long currentTime = System.currentTimeMillis();
                    countdown4=currentTime+taskTime4*60*1000;
                    state4=true;
                }else {
                    device4Button.setText("开始");
                    long currentTime = System.currentTimeMillis();
                    countdown4=System.currentTimeMillis();
                    state4=false;
                }
                break;
        }
    }
}

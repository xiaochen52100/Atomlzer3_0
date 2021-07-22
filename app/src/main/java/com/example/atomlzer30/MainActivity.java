package com.example.atomlzer30;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.liys.view.LineProView;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.atomlzer30.CloseBarUtil.showNavigation;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public  SerialPortThread serialPortThread;
    private boolean first=false;
    private long countdown1=0,countdown2=0;//设备倒计时时长
    private int taskTime1=0,taskTime2=0;//设备定时时长
    private Timer timerTask;//计时器
    private boolean state1=false,state2=false;//设备状态
    private byte sendData=0x20;
    private int temperature=25;
    private int humidity =60;
    private int level=0;
    private int levelCount=0;
    private int finshFlag=0;
    /***********控件初始化*************/
    protected DashboardView tempDashboardView,humDashboardView,levelDashboard;
    protected Button device1Button,device2Button;
    protected TextView lastTime1,lastTime2;
    protected CircleProgress mCpLoading;
    protected MyNumberPicker np1,np2;
    protected LineProView lineProView1,lineProView2;
    protected TextView temperatureTextView,humidityTextView;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        hideBottomUIMenu();
        CloseBarUtil.closeBar();
        serialPortThread=new SerialPortThread(mHandler);
        serialPortThread.openSerialPort();
        device1Button=findViewById(R.id.device1Button);
        device2Button=findViewById(R.id.device2Button);

        lastTime1=findViewById(R.id.lastTime1);
        lastTime2=findViewById(R.id.lastTime2);

        np1 = findViewById(R.id.np1);
        np2 = findViewById(R.id.np2);

        lineProView1=findViewById(R.id.lineProView1);
        lineProView2=findViewById(R.id.lineProView2);

        temperatureTextView=findViewById(R.id.temperature);
        humidityTextView=findViewById(R.id.humidityTextView);

        device1Button.setOnClickListener(this);
        device2Button.setOnClickListener(this);

        temperatureTextView.setOnClickListener(this);

        np1.setMinValue(0);
        np1.setMaxValue(15);
        np1.setValue(5);

        taskTime1=np1.getValue();
        //Log.d("TAG","taskTime1：" + taskTime1);
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
        np2.setMaxValue(15);
        np2.setValue(5);
        np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        taskTime2=np2.getValue();
        //Log.d("TAG","taskTime2：" + taskTime2);
        np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                taskTime2=newVal;
                //Log.d("TAG","oldVal：" + oldVal + "   newVal：" + newVal);
            }
        });



        mCpLoading = findViewById(R.id.cp_loading);
        //mCpLoading.setProgress(100,5000);
        mCpLoading.setProgress(0);
        mCpLoading.setOnCircleProgressListener(new CircleProgress.OnCircleProgressListener() {
            @Override
            public boolean OnCircleProgress(int progress) {
                return false;
            }
        });
        mCpLoading.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                //CloseBarUtil.showBar();
                finshFlag++;
                if (finshFlag==2){
                    showNavigation();
                    finish();
                }

            }
        });

        if (timerTask==null){
                timerTask = new Timer(true);
                timerTask.schedule(countTask, 500, 1000);
            }
            new Udp.udpReceiveBroadCast("232.11.12.13",6000,mHandler).start();


        //new Udp.udpReceiveBroadCast("232.11.12.13",7000,mHandler).start();
    }
    @Override protected void onDestroy() {
        timerTask.cancel();
        timerTask=null;
        super.onDestroy();
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
            Log.d("TAG","test");
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

            byte[] sendBuf={0};
            sendBuf[0]=sendData;
            Log.d("TAG","sendData:"+sendData);
            serialPortThread.sendSerialPort(sendBuf);
            //发送udp数据格式
            byte[] udpSendBuf=new byte[80];
            System.arraycopy(DateForm.intToBytesArray(temperature),0,udpSendBuf,0,4);
            System.arraycopy(DateForm.intToBytesArray(humidity),0,udpSendBuf,4,4);
            System.arraycopy(DateForm.intToBytesArray(level),0,udpSendBuf,8,4);
            System.arraycopy(DateForm.intToBytesArray((int)sendData),0,udpSendBuf,12,4);

            System.arraycopy(DateForm.intToBytesArray(taskTime1),0,udpSendBuf,16,4);
            System.arraycopy(DateForm.intToBytesArray(taskData1.getLastTime()),0,udpSendBuf,20,4);
            System.arraycopy(DateForm.doubleToByteArray(taskData1.getProgess()),0,udpSendBuf,24,8);
            //Log.d("TAG","getLastTime1:"+taskData1.getLastTime());

            System.arraycopy(DateForm.intToBytesArray(taskTime2),0,udpSendBuf,32,4);
            System.arraycopy(DateForm.intToBytesArray(taskData2.getLastTime()),0,udpSendBuf,36,4);
            System.arraycopy(DateForm.doubleToByteArray(taskData2.getProgess()),0,udpSendBuf,40,8);
            //Log.d("TAG","getLastTime2:"+taskData2.getLastTime());

            new Udp.udpSendBroadCast("232.11.12.13",6000,udpSendBuf).start();
            byte[] LastTime1Byte=new byte[4];
            System.arraycopy(udpSendBuf,20,LastTime1Byte,0,4);
            //Log.d("TAG",DateForm.byteArrayToInt(LastTime1Byte)+"");
            //Log.d("TAG", Arrays.toString(udpSendBuf));

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

                    break;
                case 4:

                    break;

                case 5://设备1 开始
                    device1Button.setText("停止");
                    taskTime1=(int)msg.obj;
                    np1.setValue(taskTime1);
                    countdown1=System.currentTimeMillis()+taskTime1*60*1000;
                    state1=true;
                    break;
                case 6://设备1 停止
                    device1Button.setText("开始");
                    countdown1=System.currentTimeMillis();
                    state1=false;
                    break;
                case 7://设备2 开始
                    device2Button.setText("停止");
                    taskTime2=(int)msg.obj;
                    np2.setValue(taskTime2);
                    countdown2=System.currentTimeMillis()+taskTime2*60*1000;
                    state2=true;
                    break;
                case 8://设备2 停止
                    device2Button.setText("开始");
                    countdown2=System.currentTimeMillis();
                    state2=false;
                    break;
                case 9://设备3 开始

                    break;
                case 10://设备3 停止

                    break;
                case 11://设备4 开始

                    break;
                case 12://设备4 停止

                    break;

                case 13:
                    byte[] rcvByte=(byte[])msg.obj;
                    if (rcvByte[0]==(byte)0xFE&&rcvByte.length>=11){
                        float tem1= (float) ((((rcvByte[3] << 8) | rcvByte[2] & 0xff))/10.0);
                        //Log.v("tag","tem1:"+tem1);
                        float hum1=(float) ((((rcvByte[7] << 8) | rcvByte[6] & 0xff)));
                        float levels=(float)rcvByte[10];
                        if (tem1>100) break;
                        if (tem1<0) break;
                        if (hum1>100) break;
                        if (hum1<0) break;
                        if (levels>100) break;
                        if (levels<0) break;
                        temperature=(int)tem1;
                        humidity=(int)hum1;
                        level=(int)((levels-17)/24.00*100);//用于液位校准 7-41
                        if (level<=0) level=0;
                        if (level>=100) level=100;
                        temperatureTextView.setText(temperature+"℃");
                        humidityTextView.setText(humidity+"％");
                        //mCpLoading.setProgress(level);
                        levelCount++;
                        if (levelCount==2){
                            mCpLoading.setProgress(level);
                            levelCount=0;
                        }
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
                //device1Button.setText("开始");
                long currentTime = System.currentTimeMillis();
                countdown1=System.currentTimeMillis();
                sendHandler(10,null);
            }
            CountDownTimer counttimer3 = new CountDownTimer(5000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //device1Button.setText(((millisUntilFinished-1) / 1000)+"秒后停止");
                    device1Button.setEnabled(false);
                }
                @Override
                public void onFinish() {
                    device1Button.setEnabled(true);
                }
            };
            counttimer3.start();
            break;
            case R.id.device2Button:
                if (!state2){
                    device2Button.setText("停止");
                    long currentTime = System.currentTimeMillis();
                    countdown2=currentTime+taskTime2*60*1000;
                    state2=true;
                }else {
                    //device2Button.setText("开始");
                    long currentTime = System.currentTimeMillis();
                    countdown2=System.currentTimeMillis();
                    sendHandler(11,null);

                }
                CountDownTimer counttimer4 = new CountDownTimer(5000, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //device1Button.setText(((millisUntilFinished-1) / 1000)+"秒后停止");
                        device2Button.setEnabled(false);
                    }
                    @Override
                    public void onFinish() {
                        device2Button.setEnabled(true);
                    }
                };
                counttimer4.start();
                break;
            case R.id.temperature:
                //new Udp.udpReceiveBroadCast().start();
                break;
        }
    }


}

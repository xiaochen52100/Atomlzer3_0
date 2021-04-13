package com.example.atomlzer30;

import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import android.os.Handler;

public class Udp {
    /* 发送udp多播 */
    public static class udpSendBroadCast extends Thread {
        MulticastSocket sender = null;
        DatagramPacket dj = null;
        InetAddress group = null;
        private int PORT=6000;

        byte[] data = new byte[1024];

        public udpSendBroadCast(String dataString) {
            data = dataString.getBytes();
        }
        public udpSendBroadCast(byte[] dataByte) {
            data = dataByte;
        }

        @Override
        public void run() {
            try {
                sender = new MulticastSocket();
                group = InetAddress.getByName("232.11.12.13");
                dj = new DatagramPacket(data,data.length,group,6000);
                sender.send(dj);
                //Log.d("TAG","send udp:"+dj);

            } catch(IOException e) {
                sender.close();
                e.printStackTrace();
            }
        }
    }


    /*接收udp多播*/
    public static class udpReceiveBroadCast extends  Thread {
        private MulticastSocket ms = null;
        private DatagramPacket dp;
        private Handler mhandler;
        public udpReceiveBroadCast(Handler handler){
            mhandler=handler;
        }
        private void sendHandler(int what,Object obj){
            Message msg = new Message();
            msg.what=what;
            msg.obj=obj;
            mhandler.sendMessage(msg);
        }
       private int bytesToInt(byte[] b, int offset) {
           int res = 0;
           for(int i=offset;i<offset+4;i++){
               res += (b[i] & 0xff) << ((3-i)*8);
           }
           return res;
       }
        @Override
        public void run() {

            try {
                InetAddress groupAddress = InetAddress.getByName("232.11.12.13");
                ms = new MulticastSocket(6000);
                ms.joinGroup(groupAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (true) {
                byte[] data = new byte[10];
                try {

                    dp = new DatagramPacket(data, data.length);
                    if (ms != null)
                        ms.receive(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (dp.getAddress() != null) {
                    final String quest_ip = dp.getAddress().toString();

                    /* 若udp包的ip地址 是 本机的ip地址的话，丢掉这个包(不处理)*/

                    //String host_ip = getLocalIPAddress();

                    String host_ip = getLocalHostIp();

                    //Log.d("TAG","host_ip:  --------------------  " + host_ip);
                    //Log.d("TAG","quest_ip: --------------------  " + quest_ip.substring(1));

                    if( (!host_ip.equals(""))  && host_ip.equals(quest_ip.substring(1)) ) {
                        continue;
                    }
                    Log.d("TAG","rcv: " + Arrays.toString(data) + "\n");
                    int time=bytesToInt(data,1);
                    if (time<0&&time>100)   continue;
                    switch ((int)data[0]){
                        case 1:
                            if (time==0)
                                sendHandler(6,0);
                            else{
                                byte[] intbuf=new byte[4];
                                System.arraycopy(data,1,intbuf,0,4);
                                Log.d("TAG","time:"+DateForm.byteArrayToInt(intbuf));
                                sendHandler(5,DateForm.byteArrayToInt(intbuf));
                            }

                            break;
                        case 2:
                            if (time==0)
                                sendHandler(8,0);
                            else{
                                byte[] intbuf=new byte[4];
                                System.arraycopy(data,1,intbuf,0,4);
                                //Log.d("TAG","time:"+DateForm.byteArrayToInt(intbuf));
                                sendHandler(7,DateForm.byteArrayToInt(intbuf));
                            }

                            break;
                        case 3:
                            if (time==0)
                                sendHandler(10,0);
                            else{
                                byte[] intbuf=new byte[4];
                                System.arraycopy(data,1,intbuf,0,4);
                                //Log.d("TAG","time:"+DateForm.byteArrayToInt(intbuf));
                                sendHandler(9,DateForm.byteArrayToInt(intbuf));
                            }
                            break;
                        case 4:
                            if (time==0)
                                sendHandler(12,0);
                            else{
                                byte[] intbuf=new byte[4];
                                System.arraycopy(data,1,intbuf,0,4);
                                //Log.d("TAG","time:"+DateForm.byteArrayToInt(intbuf));
                                sendHandler(11,DateForm.byteArrayToInt(intbuf));
                            };
                            break;
                    }

                }
            }
        }
    }

    public static String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()&&IpVersionCheckUtil.checkIPVersion(ip.getHostAddress())==1) {
                        return ip.getHostAddress();
                    }
                }
            }
        }
        catch(SocketException e)
        {
            Log.e("feige", "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;
    }

    private static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("TAG", ex.toString());
        }
        return null;
    }
}

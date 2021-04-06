package com.example.atomlzer30;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class Udp {
    /* 用于 udpReceiveAndTcpSend 的3个变量 */
    MulticastSocket ms = null;
    DatagramPacket dp;
    /* 发送udp多播 */
    public static class udpSendBroadCast extends Thread {
        MulticastSocket sender = null;
        DatagramPacket dj = null;
        InetAddress group = null;

        byte[] data = new byte[1024];

        public udpSendBroadCast(String dataString) {
            data = dataString.getBytes();
        }

        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(5887);
                InetAddress serverAddress=InetAddress.getByName("192.168.1.255");
//                sender = new MulticastSocket();
//                group = InetAddress.getByName("224.0.0.1");
//                dj = new DatagramPacket(data,data.length,group,6789);
                DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 6789);
                socket.send(packet);
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static class udpReceiveBroadCast extends  Thread {
        @Override
        public void run() {
            byte[] data = new byte[1024];
            try {
                DatagramSocket socket = new DatagramSocket(7412);
                DatagramPacket packet = new DatagramPacket(data, data.length);
                while (true){
                    socket.receive(packet);
                    String receive = new String(packet.getData(), 0, packet.getLength(), "utf-8");
                    Log.e("TAG", "收到的内容为：" + receive);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
/*
    public String getLocalHostIp() {
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
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip
                            .getHostAddress())) {
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
    }*/

    private String getLocalIPAddress() {
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

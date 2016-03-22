package com.kipa.sockettest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

public class SocketThread implements Runnable{

	private static String TAG = "SocketThread";
	private SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	private Handler handler = null;
	private Handler newHandler = null;
	private Thread newThread = null;
	private String host = "";
	private int port = 0;
	private DatagramSocket ds = null;
	
	public SocketThread(Handler handler){
		this.handler = handler;
		//监听端口 
		try {
			ds = new DatagramSocket(getPort());
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		Looper.prepare();
		
		//创建此线程的Handler对象来管理消息队列,并使用 postDelayed 循环方法
		newHandler = new Handler();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				host = MyApplication.getApplication().getSocketServerIP();
				port = Integer.parseInt(MyApplication.getApplication().getSocketServerPort());
				
				System.out.println("当前线程 Name ： "+Thread.currentThread().getName());
				//表示消息队列5秒后会再次执行该线程
				newHandler.postDelayed(this, 5000);
				//将要发送的数据封装到数据包中 
				System.out.println("UDP Socket Test : "+getHost()+"---"+getPort());
				String str=sim.format(new Date()) + " : UDP Socket Test "+getHost()+"---"+getPort(); 
				//使用DatagramPacket将数据封装到该对象中 
				byte[] buf=str.getBytes(); 
				DatagramPacket dp;
				try {
					dp = new DatagramPacket(buf, buf.length,InetAddress.getByName(getHost()),getPort());
					//通过udp的socket服务将数据包发送出去，通过send方法 
					ds.send(dp);
					//反馈
					Message msg = new Message();
					msg.what = 1;
					handler.handleMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
					//关闭资源 
					ds.close();
					//关闭消息队列中的该线程
					newHandler.removeCallbacks(this);
					//反馈
					Message msg = new Message();
					msg.what = 0;
					handler.handleMessage(msg);
				}
			}
		};
		runnable.run();
		Looper.loop();
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}

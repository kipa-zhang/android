package com.kipa.sockettest;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MyApplication extends Application {

	private static MyApplication application = null;
	
	private SharedPreferences sharedPreferences = null;
	
	private String SocketServerIP = "";
	private String SocketServerPort = "";
	
	@Override
	public void onCreate(){
		super.onCreate();
		application = this;
		initSocket();
		
	}
	
	@Override
	public void onTerminate(){
		super.onTerminate();
	}
	
	private void initSocket(){
		sharedPreferences = getSharedPreferences("SOCKET_Test", MODE_PRIVATE);
		if(sharedPreferences.getString(SocketInfo.SocketServer_IP, "0.0.0.0").equals("0.0.0.0")
				||sharedPreferences.getString(SocketInfo.SocketServer_PORT, "0").equals("0")){
			//第一次安装
			setSocketServerIP(SocketInfo.SocketServerIP);
			setSocketServerPort(SocketInfo.port);
			Editor editor = sharedPreferences.edit();
			editor.putString(SocketInfo.SocketServer_PORT, getSocketServerPort());
			editor.putString(SocketInfo.SocketServer_IP, getSocketServerIP());
			editor.commit();
		}else{
			//非第一次安装
			setSocketServerIP(sharedPreferences.getString(SocketInfo.SocketServer_IP, "0.0.0.0"));
			setSocketServerPort(sharedPreferences.getString(SocketInfo.SocketServer_PORT, "0.0.0.0"));
		}
		
	}
	
	public static MyApplication getApplication(){
		return application;
	}
	

	public String getSocketServerIP() {
		return SocketServerIP;
	}

	public void setSocketServerIP(String socketServerIP) {
		SocketServerIP = socketServerIP;
	}

	public String getSocketServerPort() {
		return SocketServerPort;
	}

	public void setSocketServerPort(String socketServerPort) {
		SocketServerPort = socketServerPort;
	}
}

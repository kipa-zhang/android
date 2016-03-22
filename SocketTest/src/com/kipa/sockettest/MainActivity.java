package com.kipa.sockettest;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

	private static String TAG = "MainActivity";
	
	private MyApplication application = null;
	private EditText ip_config;
	private EditText port_config;
	private Button btn_save;
	
	SocketThread socketThread = null;
	Thread newThread = null;
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			if(msg.what == 1){
				Log.i(TAG, "Socket thread is sending");
				Toast.makeText(getApplication(), "Socket Info is sending!", Toast.LENGTH_SHORT).show();
			}else if(msg.what == 0){
				Log.i(TAG, "Socket thread has something wrong");
				Toast.makeText(getApplication(), "Socket thread has something wrong!", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Toast.makeText(this, "Create", Toast.LENGTH_SHORT).show();
		setContentView(R.layout.activity_main);
		
		application = MyApplication.getApplication();
		
		ip_config = (EditText)findViewById(R.id.ip_config);
		port_config = (EditText)findViewById(R.id.port_config);
		ip_config.setText(application.getSocketServerIP());
		port_config.setText(application.getSocketServerPort());
		
		btn_save = (Button)findViewById(R.id.save);
		btn_save.setOnClickListener(this);
		
		socketThread = new SocketThread(handler);
		newThread = new Thread(socketThread);
		newThread.start();
	}
	
	@Override
	protected void onDestroy(){
		Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show();
		try {
			newThread.interrupt();
			newThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "Old thread is blocked!");
		super.onDestroy();
		Log.i(TAG, "Socket thread is closed!");
		Log.i(TAG, "MainActivity is closed!");
	}
	
	@Override
	protected void onPause() {
		Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();
		super.onPause();
	}

	@Override
	protected void onResume() {
		Toast.makeText(this, "Resume", Toast.LENGTH_SHORT).show();
		super.onResume();
	}

	public EditText getIp_config() {
		return ip_config;
	}

	public void setIp_config(EditText ip_config) {
		this.ip_config = ip_config;
	}

	public EditText getPort_config() {
		return port_config;
	}

	public void setPort_config(EditText port_config) {
		this.port_config = port_config;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.save){
			application.setSocketServerIP(ip_config.getText().toString());
			application.setSocketServerPort(port_config.getText().toString());
			
			Editor editor = getSharedPreferences("SOCKET_Test", MODE_PRIVATE).edit();
			editor.putString(SocketInfo.SocketServer_IP, application.getSocketServerIP());
			editor.putString(SocketInfo.SocketServer_PORT, application.getSocketServerPort());
			editor.commit();
			Toast.makeText(this, "Save Success!", Toast.LENGTH_SHORT).show();
		}
	}

}

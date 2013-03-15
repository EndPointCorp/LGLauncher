package com.endpoint.lglauncher;

import java.net.InetAddress;

import android.app.Activity;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;

public class MainActivity extends Activity {
	private NumberPicker offsetPicker;
	private EditText editTextBroadcastIp;
	private boolean useCustomBroadcastIp = false;
	private String[] nums;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		offsetPicker = (NumberPicker) findViewById(R.id.np_slave_offset);
	    nums = new String[]{"2", "1", "-1", "-2"};
	    offsetPicker.setMinValue(0);
	    offsetPicker.setMaxValue(3);
	    offsetPicker.setValue(1);
	    offsetPicker.setDisplayedValues(nums);
	    
	    editTextBroadcastIp = (EditText) findViewById(R.id.et_broadcast_ip);
	    editTextBroadcastIp.setVisibility(View.GONE); // Hide by default
	    
	    onIpPrefsChanged(findViewById(R.id.cb_use_custom_broadcast_ip));
	}

	public void onIpPrefsChanged(View v) {
		useCustomBroadcastIp = ((CheckBox) v).isChecked();
		
		if (useCustomBroadcastIp) {
			editTextBroadcastIp.setVisibility(View.VISIBLE);
		} else {
			editTextBroadcastIp.setVisibility(View.GONE);
		}
	}

	public String getOffset() {
		int rawValue = offsetPicker.getValue();
		if (LGLauncher.DEBUG) Log.d(LGLauncher.APP_NAME, "returning value: " + Integer.parseInt(nums[rawValue]));
		return nums[rawValue];
	}

	public void startMaster(View v) {
		if (LGLauncher.DEBUG) Log.d(LGLauncher.APP_NAME, "in startMaster");
		Intent intent = new Intent();
		intent.setAction("com.google.earth.VIEWSYNC");
		intent.putExtra("master", true);
		intent.putExtra("protocol", "udp");
		intent.putExtra("address", getBroadcastAddress());		
		try {
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			if (LGLauncher.DEBUG) Log.d(LGLauncher.APP_NAME, "Error starting Google Earth.");
		}
	}
	
	public void startSlave(View v) {
		if (LGLauncher.DEBUG) Log.d("LGLauncher", "in startSlave");
		Intent intent = new Intent();
		intent.setAction("com.google.earth.VIEWSYNC");
		intent.putExtra("protocol", "udp");
		intent.putExtra("x", getOffset());
		startActivity(intent);
	}
	
	private String getBroadcastAddress() {
		if (useCustomBroadcastIp) {
			String ip = editTextBroadcastIp.getText().toString();
			if (LGLauncher.DEBUG) Log.d(LGLauncher.APP_NAME, "using custom broadcast IP: " + ip);
			return ip;
		}
		
		WifiManager myWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		DhcpInfo myDhcpInfo = myWifiManager.getDhcpInfo();
		if (myDhcpInfo == null) {
			Log.e(LGLauncher.APP_NAME, "Could not get broadcast address");
			return null;
		}
		
		int broadcast = (myDhcpInfo.ipAddress & myDhcpInfo.netmask) | ~myDhcpInfo.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++) {
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		}
		
		String address = null;		
		try {
			address = InetAddress.getByAddress(quads).toString().replace("/", "");
		} catch (Exception e) {
			LGLauncher.reportError(e);
			e.printStackTrace();
		}
		
		if (LGLauncher.DEBUG) Log.d(LGLauncher.APP_NAME, "Returning broadcast address: " + address);
		return address;
	}
}

package com.NewApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

import android.R.*;
import android.app.Activity;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.*;
import zephyr.android.HxMBT.*;

public class MainActivity extends Activity {
	private ObjectInputStream ois; // 입력
	private ObjectOutputStream oos; // 출력
	/** Called when the activity is first created. */
	BluetoothAdapter adapter = null;
	BTClient _bt;
	ZephyrProtocol _protocol;
	NewConnectedListener _NConnListener;
	private final int HEART_RATE = 0x100;
	private final int INSTANT_SPEED = 0x101;
	String BhMacID;
	String DeviceName;
	private LinearLayout m_dialog;
	private EditText et_dialoginputip;
	private String ip_addr;
	private Socket socket;
	private int port;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		/*
		 * Sending a message to android that we are going to initiate a pairing
		 * request
		 */
		IntentFilter filter = new IntentFilter(
				"android.bluetooth.device.action.PAIRING_REQUEST");
		/*
		 * Registering a new BTBroadcast receiver from the Main Activity context
		 * with pairing request event
		 */
		this.getApplicationContext().registerReceiver(
				new BTBroadcastReceiver(), filter);
		// Registering the BTBondReceiver in the application that the status of
		// the receiver has changed to Paired
		IntentFilter filter2 = new IntentFilter(
				"android.bluetooth.device.action.BOND_STATE_CHANGED");
		this.getApplicationContext().registerReceiver(new BTBondReceiver(),
				filter2);

		// Obtaining the handle to act on the CONNECT button
		TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
		String ErrorText = "Not Connected to HxM ! !";
		tv.setText(ErrorText);
		port = 5000;
		socket = null;
		Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
		if (btnConnect != null) {
			btnConnect.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					m_dialog = (LinearLayout) View.inflate(MainActivity.this,
							R.layout.dialog_ip, null);

					new AlertDialog.Builder(MainActivity.this)
							.setTitle("서버 연결 및 제피르 연결")
							.setView(m_dialog)
							.setPositiveButton("취소",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											Log.i("speech.path", "cancel");
										}
									})
							.setNegativeButton("연결",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											et_dialoginputip = (EditText) m_dialog
													.findViewById(R.id.dialoginputip);
											Log.i("speech.path",
													et_dialoginputip.getText()
															.toString());
											ip_addr = et_dialoginputip
													.getText().toString();

											if (socket == null) {
												if (!ip_addr.equals(""))
													socketconnect.start();
												else {
													Toast t = Toast
															.makeText(
																	getApplicationContext(),
																	"서버 IP 입력하세요",
																	Toast.LENGTH_SHORT);
													t.show();
												}
											} else {
												Toast t = Toast
														.makeText(
																getApplicationContext(),
																"서버는 이미 연결상태입니다. 제피르와 연결합니다.",
																Toast.LENGTH_SHORT);
												t.show();
											}
											connectToHxM();
										}
									}).show();
				}
			});
		}
		/* Obtaining the handle to act on the DISCONNECT button */
		Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
		if (btnDisconnect != null) {
			btnDisconnect.setOnClickListener(new OnClickListener() {
				@Override
				/* Functionality to act if the button DISCONNECT is touched */
				public void onClick(View v) {
					// TODO Auto-generated method stub
					disConnectToHxM();
				}
			});
		}

	}

	private void disConnectToHxM() {

		// 서버 로그 끝 및 제피르 해제
		if (socket != null) {
			try {
				oos.writeObject("com#end");
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/* Reset the global variables */
		TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
		String ErrorText = "Disconnected from HxM!";
		tv.setText(ErrorText);

		/*
		 * This disconnects listener from acting on received messages
		 */
		_bt.removeConnectedEventListener(_NConnListener);
		/*
		 * Close the communication with the device & throw an exception if
		 * failure
		 */
		_bt.Close();
	}

	private void connectToHxM() {
		BhMacID = "00:07:80:9D:8A:E8";
		// String BhMacID = "00:07:80:88:F6:BF";
		adapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName().startsWith("HXM")) {
					BluetoothDevice btDevice = device;
					BhMacID = btDevice.getAddress();
					break;
				}
			}
		}
		// BhMacID = btDevice.getAddress();
		BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);

		DeviceName = Device.getName();

		_bt = new BTClient(adapter, BhMacID);
		_NConnListener = new NewConnectedListener(Newhandler, Newhandler);
		_bt.addConnectedEventListener(_NConnListener);

		TextView tv1 = (EditText) findViewById(R.id.labelHeartRate);
		tv1.setText("000");

		tv1 = (EditText) findViewById(R.id.labelInstantSpeed);
		tv1.setText("0.0");

		if (_bt.IsConnected()) {
			_bt.start();
			TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
			String ErrorText = "Connected to HxM " + DeviceName;
			tv.setText(ErrorText);

			// Reset all the values to 0s

		} else {
			TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
			String ErrorText = "Unable to Connect !";
			tv.setText(ErrorText);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		try {
			if (socket != null) {
				oos.writeObject("com#stop");
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class BTBondReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			BluetoothDevice device = adapter.getRemoteDevice(b.get(
					"android.bluetooth.device.extra.DEVICE").toString());
			Log.d("Bond state", "BOND_STATED = " + device.getBondState());
		}
	}

	private class BTBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BTIntent", intent.getAction());
			Bundle b = intent.getExtras();
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE")
					.toString());
			Log.d("BTIntent",
					b.get("android.bluetooth.device.extra.PAIRING_VARIANT")
							.toString());
			try {
				BluetoothDevice device = adapter.getRemoteDevice(b.get(
						"android.bluetooth.device.extra.DEVICE").toString());
				Method m = BluetoothDevice.class.getMethod("convertPinToBytes",
						new Class[] { String.class });
				byte[] pin = (byte[]) m.invoke(device, "1234");
				m = device.getClass().getMethod("setPin",
						new Class[] { pin.getClass() });
				Object result = m.invoke(device, pin);
				Log.d("BTTest", result.toString());
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	final Handler Newhandler = new Handler() {
		public void handleMessage(Message msg) {
			TextView tv;
			switch (msg.what) {
			case HEART_RATE:
				String HeartRatetext = msg.getData().getString("HeartRate");
				tv = (EditText) findViewById(R.id.labelHeartRate);
				System.out.println("Heart Rate Info is " + HeartRatetext);
					
				if (tv != null) {
					tv.setText(HeartRatetext);
					if(socket != null){
						try {
							oos.writeObject("msg#" + HeartRatetext);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				break;

			case INSTANT_SPEED:
				String InstantSpeedtext = msg.getData().getString(
						"InstantSpeed");
				tv = (EditText) findViewById(R.id.labelInstantSpeed);
				if (tv != null)
					tv.setText(InstantSpeedtext);

				break;

			}
		}

	};

	private Thread socketconnect = new Thread() {

		public void run() {

			try {
				socket = new Socket(ip_addr, port);
				try {
					oos = new ObjectOutputStream(socket.getOutputStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
				ois = new ObjectInputStream(socket.getInputStream());
				
				if (socket != null) {
					oos.writeObject("com#start");
				}
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

}

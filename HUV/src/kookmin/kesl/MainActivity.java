package kookmin.kesl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;
import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {

	protected static final int RESULT_SPEECH = 1;
	private ObjectOutputStream oos; // ���

	// ���Ǹ�
	BluetoothAdapter adapter = null;
	BTClient _bt;
	ZephyrProtocol _protocol;
	NewConnectedListener _NConnListener;
	private final int HEART_RATE = 0x100;
	String BhMacID;
	String DeviceName;

	// ���Ǹ�

	private ImageButton btnSpeak;
	private ImageButton btnheart;
	private ImageButton btnkinect;
	private Button btn_ip;
	private Button btn_log_start;
	private Button btn_log_end;
	private TextView txtText;

	private EditText et_dialoginputip;

	private LinearLayout m_dialog;
	private String ip_addr;
	private Socket socket;
	private int port;

	boolean isHeart;
	boolean isKinect;
	boolean isLogging;

	@Override
	protected void onStart() {
		super.onStart();
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		ip_addr = null;
		port = 5000;
		txtText = (TextView) findViewById(R.id.txtText);
		btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
		btnheart = (ImageButton) findViewById(R.id.btnheart);
		btnkinect = (ImageButton) findViewById(R.id.btnkinect);
		btn_ip = (Button) findViewById(R.id.btnip);
		btn_log_start = (Button) findViewById(R.id.btnlogstart);
		btn_log_end = (Button) findViewById(R.id.btnlogend);
		socket = null;
		isHeart = false;
		isKinect = false;
		isLogging = false;
		
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

		/* �����ν� ��ư ������ �� */
		btnSpeak.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("speech.path", "clickon");
				if (socket != null) {
					Intent intent = new Intent(
							RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							"en-US");

					try {
						startActivityForResult(intent, RESULT_SPEECH);
						txtText.setText("");
					} catch (ActivityNotFoundException a) {
						Toast t = Toast
								.makeText(
										getApplicationContext(),
										"Ops! Your device doesn't support Speech to Text",
										Toast.LENGTH_SHORT);
						t.show();
					}
				} else {
					Toast t = Toast.makeText(getApplicationContext(),
							"���� ���� ��ư�� ���� ��������", Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});
		/* �������� ��ư ������ �� */
		btn_ip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (socket == null) {
					Log.i("speech.path", "log ip");
					m_dialog = (LinearLayout) View.inflate(MainActivity.this,
							R.layout.dialog_ip, null);
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("���� ����")
							.setView(m_dialog)
							.setPositiveButton("���",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											Log.i("speech.path", "cancel");
										}
									})
							.setNegativeButton("����",
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
											socketconnect.start();
										}
									}).show();
				} else {
					Toast t = Toast.makeText(getApplicationContext(),
							"�̹� ������ �����Դϴ�.", Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});
		btn_log_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (socket != null) {
					Log.i("speech.path", "log start");
					try {
						oos.writeObject("com#start");
						isLogging = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Toast t = Toast.makeText(getApplicationContext(),
							"���� ���� ��ư�� ���� ��������", Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});
		btn_log_end.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (socket != null) {
					Log.i("speech.path", "log end");
					try {
						oos.writeObject("com#end");
						isLogging = false;
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Toast t = Toast.makeText(getApplicationContext(),
							"���� ���� ��ư�� ���� ��������", Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});
		/* When heartrate button pushed */
		btnheart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isHeart == false) {
					isHeart = true;
					// heart ����
					connectToHxM();
					btnheart.setBackgroundResource(R.drawable.heart);
				} else {
					isHeart = false;
					// heart ���� ���
					btnheart.setBackgroundResource(R.drawable.heart_gray);
					disConnectToHxM();
				}

			}
		});
		/* When kinect button pushed */
		btnkinect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isKinect == false) {
					isKinect = true;
					btnkinect.setBackgroundResource(R.drawable.kinect);
					// Ű��Ʈ ���
				} else {
					isKinect = false;
					btnkinect.setBackgroundResource(R.drawable.kinect_gray);
				}
			}
		});
	}

	// onCreate

	/* ���� �ν��� ���� �� */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SPEECH: {
			if (resultCode == RESULT_OK && null != data) {
				ArrayList<String> text = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				txtText.setText(text.get(0));
				String str = text.get(0).toString();
				String[] strarry = str.split(" ");
				Log.i("speech.path", str);
				try {
					/* str : �����νĵ� ���� */
					if(isLogging == true)
						oos.writeObject("msg_voice#" + strarry[0]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
				Log.i("speech.path", "speech success message is " + str);
			}
		}
		}
	}

	private Thread socketconnect = new Thread() {
		public void run() {
			try {
				socket = new Socket(ip_addr, port);
				try {
					oos = new ObjectOutputStream(socket.getOutputStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
				// ����
				socketConnectedhandler.sendEmptyMessage(0);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

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

	private void disConnectToHxM() {
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
		if (_bt.IsConnected()) {
			_bt.start();
			String ErrorText = "Connected to HxM " + DeviceName;
			System.out.println(ErrorText);
			// Reset all the values to 0s

		} else {
			String ErrorText = "Unable to Connect !";
			System.out.println(ErrorText);
		}
	}

	final Handler Newhandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HEART_RATE:
				String HeartRatetext = msg.getData().getString("HeartRate");
				System.out.println("Heart Rate Info is " + HeartRatetext);
				if (socket != null) {
					try {
						if(isLogging == true)
							oos.writeObject("msg_heart#" + HeartRatetext);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			}
		}

	};

	private Handler socketConnectedhandler = new Handler() {
		public void handleMessage(Message msg) {
			Toast.makeText(getApplicationContext(), "��������ƽ��ϴ�.", 0).show();
			super.handleMessage(msg);
		}
	};
}

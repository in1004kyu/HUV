package net.viralpatel.android.speechtotextdemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected static final int RESULT_SPEECH = 1;
	private ObjectInputStream ois; // 입력
	private ObjectOutputStream oos; // 출력
	private ImageButton btnSpeak;
	private Button btn_ip;
	private Button btn_log_start;
	private Button btn_log_end;
	private TextView txtText;

	private EditText et_dialoginputip;

	private LinearLayout m_dialog;
	private String ip_addr;
	private Socket socket;
	private int port;

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
		btn_ip = (Button) findViewById(R.id.btnip);
		btn_log_start = (Button) findViewById(R.id.btnlogstart);
		btn_log_end = (Button) findViewById(R.id.btnlogend);
		socket = null;

		/* 음석인식 버튼 눌렀을 때 */
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
							"서버 연결 버튼을 먼저 누르세요", Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});
		/* 서버연결 버튼 눌렀을 때 */
		btn_ip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (socket == null) {
					Log.i("speech.path", "log ip");
					m_dialog = (LinearLayout) View.inflate(MainActivity.this,
							R.layout.dialog_ip, null);
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("서버 연결")
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
											socketconnect.start();
										}
									}).show();
				} else {
					Toast t = Toast.makeText(getApplicationContext(),
							"이미 연결이 상태입니다.", Toast.LENGTH_SHORT);
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
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Toast t = Toast.makeText(getApplicationContext(),
							"서버 연결 버튼을 먼저 누르세요", Toast.LENGTH_SHORT);
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
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Toast t = Toast.makeText(getApplicationContext(),
							"서버 연결 버튼을 먼저 누르세요", Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/* 음석 인식이 됐을 떄 */
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
					/* str : 음석인식된 문장 */
					oos.writeObject("msg#" + strarry[0]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (str == null)
					Log.i("speech.path", "no string");
				else
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
				ois = new ObjectInputStream(socket.getInputStream());

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

package com.RC_Tiger_1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ActivityArrows extends Activity {

	private cBluetooth bl = null;

	private String address;
	private boolean show_Debug;
	private int xRperc;
	private int pwmMax;
	private String commandLeft = "L";
	private String commandRight = "R";
	private String commandTower = "T";
	private String commandGun = "G";
	private String commandTowerDefaultMod = "C";
	private String commandGoForward = "F";
	private String commandTurnToSide = "S";
	//private String arrowPressed;
	//private String btCommand;
	private RelativeLayout mRelativeLayout_arrow;
	private int aliveMessageTime = 1000;


	Handler handler = new Handler();
	Handler moveHandler = new Handler();

	Runnable runnable = new Runnable() {
		public void run() {
			bl.sendData(String.valueOf("A\n"));
			handler.postDelayed(this, aliveMessageTime);
		}
	};

	/*Runnable moveRunnable = new Runnable() {
		public void run() {
			if (arrowPressed == "Accelerate"){
				btCommand = String.valueOf(commandLeft+"+"+255+"\r"+commandRight+"+"+255+"\r");
			}else if (arrowPressed == "Reverse"){
				btCommand = String.valueOf(commandLeft+"-"+255+"\r"+commandRight+"-"+255+"\r");
			}else if (arrowPressed == "TurnRight"){
				btCommand = String.valueOf(commandLeft+"+"+255+"\r"+commandRight+"-"+255+"\r");
			}else if (arrowPressed == "TurnLeft"){
				btCommand = String.valueOf(commandLeft+"-"+255+"\r"+commandRight+"+"+255+"\r");
			}else{ // if (arrowPressed == "Nothing") or no command yet
				btCommand = String.valueOf(commandLeft+"+"+0+"\r"+commandRight+"+"+0+"\r");
			}

			bl.sendData(btCommand);
			moveHandler.postDelayed(this, 10);
		}
	};*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.touch_arrows);
		mRelativeLayout_arrow = (RelativeLayout) findViewById(R.id.mRelativeLayout_arrow);
		//RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1000, 800);
		//params.leftMargin = 0;
		//params.topMargin = 0;

		address = (String) getResources().getText(R.string.default_MAC);
		xRperc = Integer.parseInt((String) getResources().getText(R.string.default_xRperc));
		pwmMax = Integer.parseInt((String) getResources().getText(R.string.default_pwmMax));

		loadPref();

		bl = new cBluetooth(this, mHandler);
		bl.checkBTState();
		handler.postDelayed(runnable, 2000);
		//moveHandler.postDelayed(moveRunnable,1000);

		final Button AT_towerRight =  (Button) mRelativeLayout_arrow.findViewById(R.id.TA_towerRight);
		AT_towerRight.setText("Jobbra");
		AT_towerRight.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					sendToBT(String.valueOf(commandTower +"1\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					sendToBT(String.valueOf(commandTower +"0\r"));
				}
				return true;
			}
		});

		final Button towerLeft = (Button) mRelativeLayout_arrow.findViewById(R.id.TA_towerLeft);
		towerLeft.setText("Balra");
		towerLeft.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					sendToBT(String.valueOf(commandTower +"-1\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					sendToBT(String.valueOf(commandTower +"0\r"));
				}
				return true;
			}
		});

		final Button gunUp = (Button) mRelativeLayout_arrow.findViewById(R.id.TA_gunUp);
		gunUp.setText("Fel");
		gunUp.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				sendToBT(String.valueOf(commandGun+"1\r"));
			}
		});

		final Button gunDown = (Button) mRelativeLayout_arrow.findViewById(R.id.TA_gunDown);
		gunDown.setText("Le");
		gunDown.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				sendToBT(String.valueOf(commandGun+"-1\r"));
			}
		});

		final Button towerDefaultPlus = (Button) mRelativeLayout_arrow.findViewById(R.id.TA_towerDefaultPlus);

		towerDefaultPlus.setText("+");
		towerDefaultPlus.setHeight(20);
		towerDefaultPlus.setWidth(30);
		towerDefaultPlus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				bl.sendData(String.valueOf(commandTowerDefaultMod+"1\r"));
			}
		});

		final Button towerDefaultMinus = (Button) mRelativeLayout_arrow.findViewById(R.id.TA_towerDefaultMinus);

		towerDefaultMinus.setText("-");
		towerDefaultMinus.setHeight(20);
		towerDefaultMinus.setWidth(30);
		towerDefaultMinus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				bl.sendData(String.valueOf(commandTowerDefaultMod+"-1\r"));
			}
		});

		final Button buttonAccelerate =  (Button) mRelativeLayout_arrow.findViewById(R.id.TA_accelerate);
		buttonAccelerate.setText("Előre");
		buttonAccelerate.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					bl.sendData(String.valueOf(commandGoForward+"+1\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					bl.sendData(String.valueOf(commandGoForward+"+0\r"));
				}
				return true;
			}
		});

		final Button buttonReverse =  (Button) mRelativeLayout_arrow.findViewById(R.id.TA_reverse);
		buttonReverse.setText("Hátra");
		buttonReverse.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					bl.sendData(String.valueOf(commandGoForward+"-1\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					bl.sendData(String.valueOf(commandGoForward+"+0\r"));
				}
				return true;
			}
		});

		final Button buttonTurnRight =  (Button) mRelativeLayout_arrow.findViewById(R.id.TA_turnRight);
		buttonTurnRight.setText("Jobbra");
		buttonTurnRight.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					bl.sendData(String.valueOf(commandTurnToSide+"+1\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					bl.sendData(String.valueOf(commandTurnToSide+"+0\r"));
				}
				return true;
			}
		});

		final Button buttonTurnLeft =  (Button) mRelativeLayout_arrow.findViewById(R.id.TA_turnLeft);
		buttonTurnLeft.setText("Balra");
		buttonTurnLeft.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					bl.sendData(String.valueOf(commandTurnToSide+"-1\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					bl.sendData(String.valueOf(commandTurnToSide+"+0\r"));
				}
				return true;
			}
		});

	}
	@Override

	public void onDestroy(){
		super.onDestroy();
		handler.removeCallbacks(runnable);
	}

	private final Handler mHandler =  new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case cBluetooth.BL_NOT_AVAILABLE:
					Log.d(cBluetooth.TAG, "Bluetooth is not available. Exit");
					Toast.makeText(getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
					finish();
					break;
				case cBluetooth.BL_INCORRECT_ADDRESS:
					Log.d(cBluetooth.TAG, "Incorrect MAC address");
					Toast.makeText(getBaseContext(), "Incorrect Bluetooth address", Toast.LENGTH_SHORT).show();
					break;
				case cBluetooth.BL_REQUEST_ENABLE:
					Log.d(cBluetooth.TAG, "Request Bluetooth Enable");
					BluetoothAdapter.getDefaultAdapter();
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, 1);
					break;
				case cBluetooth.BL_SOCKET_FAILED:
					Toast.makeText(getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
					finish();
					break;
			}
		};
	};


	public void sendToBT(String data){
		bl.sendData(data);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadPref();
	}

	@Override
	protected void onResume() {
		super.onResume();
		bl.BT_Connect(address);
	}

	@Override
	protected void onPause() {
		super.onPause();
		bl.BT_onPause();
	}

	private void loadPref(){
		SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		address = mySharedPreferences.getString("pref_MAC_address", address);			// ������ ��� ��������� ��������� ��������
		xRperc = Integer.parseInt(mySharedPreferences.getString("pref_xRperc", String.valueOf(xRperc)));
		pwmMax = Integer.parseInt(mySharedPreferences.getString("pref_pwmMax", String.valueOf(pwmMax)));
		show_Debug = mySharedPreferences.getBoolean("pref_Debug", false);
		commandLeft = mySharedPreferences.getString("pref_commandLeft", commandLeft);
		commandRight = mySharedPreferences.getString("pref_commandRight", commandRight);
		commandTower = mySharedPreferences.getString("pref_commandTower", commandTower);
	}
}

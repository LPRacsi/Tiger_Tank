package com.RC_Tiger_1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
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

public class ActivityTouch extends Activity {
	
    private cBluetooth bl = null;
	
	private final static int BIG_CIRCLE_SIZE = 320;
	private final static int FINGER_CIRCLE_SIZE = 25;
	
    private int motorLeft = 0;
    private int motorRight = 0;

    private String address;			// MAC-����� ����������
    private boolean show_Debug;		// ����������� ���������� ����������
    private int xRperc;				// ����� ���������
    private int pwmMax;	   			// ������������ �������� ���
    private String commandLeft = "L";		// ������ ������� ������ ���������
    private String commandRight = "R";	// ������ ������� ������� ���������
    private String commandTower = "T";		// ������ ������� ��� ���. ������ (�������� ������)
	private String commandToTower = "";
	private String commandGun = "G";
	private String commandTowerDefaultMod = "C";
	private RelativeLayout mRelativeLayout;
	private int aliveMessageTime = 1000;


	Handler handler = new Handler();

	Runnable runnable = new Runnable() {
		public void run() {
			bl.sendData(String.valueOf("A\n"));
			handler.postDelayed(this, aliveMessageTime);
		}
	};


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		MyView custom = new MyView(this);
		setContentView(R.layout.touch_control);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.mRelativeLayout);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1000, 800);
		params.leftMargin = 0;
		params.topMargin = 0;
		mRelativeLayout.addView(custom,params);

		handler.postDelayed(runnable, 2000);

        address = (String) getResources().getText(R.string.default_MAC);
        xRperc = Integer.parseInt((String) getResources().getText(R.string.default_xRperc));
        pwmMax = Integer.parseInt((String) getResources().getText(R.string.default_pwmMax));

        loadPref();
        
        bl = new cBluetooth(this, mHandler);
        bl.checkBTState();

		final Button towerRight =  (Button) mRelativeLayout.findViewById(R.id.towerRight);
		towerRight.setText("Jobbra");
		towerRight.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					sendToBT(String.valueOf(commandTower +"1\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					sendToBT(String.valueOf(commandTower +"0\r"));
				}
				return true;
			}
		});

		final Button towerLeft = (Button) mRelativeLayout.findViewById(R.id.towerLeft);
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

		final Button gunUp = (Button) mRelativeLayout.findViewById(R.id.gunUp);
		gunUp.setText("Fel");
		gunUp.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				sendToBT(String.valueOf(commandGun+"1\r"));
			}
		});

		final Button gunDown = (Button) mRelativeLayout.findViewById(R.id.gunDown);
		gunDown.setText("Le");
		gunDown.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				sendToBT(String.valueOf(commandGun+"-1\r"));
			}
		});

		final Button towerDefaultPlus = (Button) mRelativeLayout.findViewById(R.id.towerDefaultPlus);

		towerDefaultPlus.setText("+");
		towerDefaultPlus.setHeight(20);
		towerDefaultPlus.setWidth(30);
		towerDefaultPlus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				bl.sendData(String.valueOf(commandTowerDefaultMod+"1\r"));
			}
		});

		final Button towerDefaultMinus = (Button) mRelativeLayout.findViewById(R.id.towerDefaultMinus);

		towerDefaultMinus.setText("-");
		towerDefaultMinus.setHeight(20);
		towerDefaultMinus.setWidth(30);
		towerDefaultMinus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				bl.sendData(String.valueOf(commandTowerDefaultMod+"-1\r"));
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

	class MyView extends View {

		Paint fingerPaint, borderPaint, textPaint;

        int dispWidth;
        int dispHeight;

        float x;
        float y;

        float xcirc;
        float ycirc;

    	String temptxtMotor;

        boolean drag = false;



        public MyView(Context context) {
        	super(context);
        	fingerPaint = new Paint();
        	fingerPaint.setAntiAlias(true);
        	fingerPaint.setColor(Color.RED);

        	borderPaint = new Paint();
        	borderPaint.setColor(Color.BLUE);
        	borderPaint.setAntiAlias(true);
        	borderPaint.setStyle(Style.STROKE);
        	borderPaint.setStrokeWidth(3);

	        textPaint = new Paint();
	        textPaint.setColor(Color.WHITE);
	        textPaint.setStyle(Style.FILL);
	        textPaint.setColor(Color.BLACK);
	        textPaint.setTextSize(14);
        }


        protected void onDraw(Canvas canvas) {
        	dispWidth = Math.round(this.getWidth()/2);
        	dispHeight = Math.round(this.getHeight()/2);
        	if(!drag){
        		x = dispWidth;
        		y = dispHeight;
        		fingerPaint.setColor(Color.RED);
        	}

            canvas.drawCircle(x, y, FINGER_CIRCLE_SIZE, fingerPaint);              
            canvas.drawCircle(dispWidth, dispHeight, BIG_CIRCLE_SIZE, borderPaint);
            
            if(show_Debug){
	            canvas.drawText(String.valueOf("X:"+xcirc), 10, 75, textPaint);
	            canvas.drawText(String.valueOf("Y:"+(-ycirc)), 10, 95, textPaint);
	            canvas.drawText(String.valueOf("Motor:"+temptxtMotor), 10, 115, textPaint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
        	
        	// ���������� Touch-�������
        	float evX = event.getX();
        	float evY = event.getY();
                          
        	xcirc = event.getX() - dispWidth;
        	ycirc = event.getY() - dispHeight;
        	//Log.d("4WD", String.valueOf("X:"+this.getRight()+" Y:"+dispHeight));
            	   
        	float radius = (float) Math.sqrt(Math.pow(Math.abs(xcirc),2)+Math.pow(Math.abs(ycirc),2));

        	switch (event.getAction()) {

        	case MotionEvent.ACTION_DOWN:        
        		if(radius >= 0 && radius <= BIG_CIRCLE_SIZE){
        			x = evX;
        			y = evY;
        			fingerPaint.setColor(Color.GREEN);
        			temptxtMotor = CalcMotor(xcirc,ycirc);
        			invalidate();
        			drag = true;
        		}
        		break;

        	case MotionEvent.ACTION_MOVE:
        		// ���� ����� �������������� �������
        		if (drag && radius >= 0 && radius <= BIG_CIRCLE_SIZE) {
        			x = evX;
        			y = evY;
        			fingerPaint.setColor(Color.GREEN);
        			temptxtMotor = CalcMotor(xcirc,ycirc);
        			invalidate();
        		}
        		break;

        	// ������� ���������
        	case MotionEvent.ACTION_UP:
        		// ��������� ����� ��������������
        		xcirc = 0;
        		ycirc = 0; 
        		drag = false;
        		temptxtMotor = CalcMotor(xcirc,ycirc);
        		invalidate();
        		break;
        	}
        	return true;
        }
	}
	
	private String CalcMotor(float calc_x, float calc_y){
    	String directionL = "";
    	String directionR = "";
    	String cmdSend;
    	
    	calc_x = -calc_x;
		
		int xAxis = Math.round(calc_x*pwmMax/BIG_CIRCLE_SIZE);
		int yAxis = Math.round(calc_y*pwmMax/BIG_CIRCLE_SIZE);
		//Log.d("4WD", String.valueOf("xAxis:"+xAxis+"  yAxis"+yAxis));
		
		int xR = Math.round(BIG_CIRCLE_SIZE*xRperc/100);		// ��������� �������� ����� ���������
       
        if(xAxis > 0) {		// ���� �����, �� �������� ����� �����
        	motorRight = yAxis;
        	if(Math.abs(Math.round(calc_x)) > xR){
        		motorLeft = Math.round((calc_x-xR)*pwmMax/(BIG_CIRCLE_SIZE-xR));
        		motorLeft = Math.round(-motorLeft * yAxis/pwmMax);
        	}
        	else motorLeft = yAxis - yAxis*xAxis/pwmMax;
        }
        else if(xAxis < 0) {		// ������ ������
        	motorLeft = yAxis;
        	if(Math.abs(Math.round(calc_x)) > xR){
        		motorRight = Math.round((Math.abs(calc_x)-xR)*pwmMax/(BIG_CIRCLE_SIZE-xR));
        		motorRight = Math.round(-motorRight * yAxis/pwmMax);
        	}
        	else motorRight = yAxis - yAxis*Math.abs(xAxis)/pwmMax;
        }
        else if(xAxis == 0) {
        	motorLeft = yAxis;
        	motorRight = yAxis;
        }
        
        if(motorLeft > 0) {			// ���� ������ ������
        	directionL = "-";
        }      
        if(motorRight > 0) {		// ���� ������ ������
        	directionR = "-";
        }
        motorLeft = Math.abs(motorLeft);
        motorRight = Math.abs(motorRight);
               
        if(motorLeft > pwmMax) motorLeft = pwmMax;
        if(motorRight > pwmMax) motorRight = pwmMax;
        
        cmdSend = String.valueOf(commandLeft+directionL+motorLeft+"\r"+commandRight+directionR+motorRight+"\r");
        bl.sendData(cmdSend);
        
		return cmdSend;
	}
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

package com.RC_Tiger_1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

	Button btnActTouch;
	Button btnActArrow;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);

		btnActArrow = (Button) findViewById(R.id.button_arrows);
		btnActArrow.setOnClickListener(this);

	    btnActTouch = (Button) findViewById(R.id.button_touch);
	    btnActTouch.setOnClickListener(this);

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_arrows:
			Intent intent_arrows = new Intent(this, ActivityArrows.class);
			startActivity(intent_arrows);
			break;

	    case R.id.button_touch:
	    	Intent intent_touch = new Intent(this, ActivityTouch.class);
	    	startActivity(intent_touch);
	    	break;

	    default:
	    	break;
	    }
	}
  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	  
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SetPreferenceActivity.class);
		startActivityForResult(intent, 0); 
	  
		return true;
	}
}

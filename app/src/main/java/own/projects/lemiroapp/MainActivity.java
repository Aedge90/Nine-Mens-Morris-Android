package own.projects.lemiroapp;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import own.projects.lemiroapp.Options.MillMode;

import android.R.layout;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final static int REQUEST_ENABLE_BT = 1;
	
	private final static int RUN_GAME = 67;
	private final static int SET_OPTIONS = 100;
	
	private int screenWidth;
	private final MainActivity THIS = this;
	
	private Options options;

	private LayoutInflater vi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		options = new Options();

		vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;

		setContentView(R.layout.activity_main);
		
		Intent setOptionsIntent = new Intent(this, OptionsActivity.class);
		startActivityForResult(setOptionsIntent, SET_OPTIONS);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	// receives notification when bluetooth was inactive
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, R.string.bluetooth_enabled,
						Toast.LENGTH_SHORT);
				Intent setOptionsIntent = new Intent(this, OptionsActivity.class);
				startActivityForResult(setOptionsIntent, SET_OPTIONS);
			} else {
				new AlertDialog.Builder(this)
						.setCancelable(false)
						.setTitle(R.string.bt_couldnt_be_activated)
						.setMessage(R.string.app_unusable)
						.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
								}
						}).show();
			}
		}else if(requestCode == SET_OPTIONS) {
			if (resultCode == RESULT_OK) {
				
				options = data.getParcelableExtra("own.projects.lemiroapp.Options");
				
				Log.i("MainActivity", "Starting new Game with Options:\n" + options);

				if(options.gameMode.equals(Options.GameMode.HUMANBOT)){
					Intent appvsbotIntent = new Intent(THIS, HumanVsBot.class);
					appvsbotIntent.setExtrasClassLoader(Options.class.getClassLoader());
					appvsbotIntent.putExtra("own.projects.lemiroapp.Options", options);
					startActivityForResult(appvsbotIntent, RUN_GAME);
				}else if(options.gameMode.equals(Options.GameMode.BOTBOT)){
					Intent botvsbotIntent = new Intent(THIS, BotVsBot.class);
					botvsbotIntent.setExtrasClassLoader(Options.class.getClassLoader());
					botvsbotIntent.putExtra("own.projects.lemiroapp.Options", options);
					startActivityForResult(botvsbotIntent, RUN_GAME);
				}else{
					Intent humanvshumanIntent = new Intent(THIS, HumanVsHuman.class);
					humanvshumanIntent.setExtrasClassLoader(Options.class.getClassLoader());
					humanvshumanIntent.putExtra("own.projects.lemiroapp.Options", options);
					startActivityForResult(humanvshumanIntent, RUN_GAME);
				}
				
			}else {
				finish();
			}
		}else if(requestCode == RUN_GAME) {
			if (resultCode == RESULT_OK) {
				new AlertDialog.Builder(this)
				.setCancelable(false)
				.setTitle("new Game?")
				.setMessage("start new Game?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						Intent setOptionsIntent = new Intent(THIS, OptionsActivity.class);
						startActivityForResult(setOptionsIntent, SET_OPTIONS);
					}})
				.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							finish();
						}
				}).show();
			}else if(resultCode == RESULT_CANCELED){
				finish();
			}else{
				Intent setOptionsIntent = new Intent(THIS, OptionsActivity.class);
				startActivityForResult(setOptionsIntent, SET_OPTIONS);
			}
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void quit() {
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			new AlertDialog.Builder(THIS)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("Quit")
					.setMessage("Do you really want to quit?")
					.setPositiveButton("YES",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									quit();
								}
							}).setNegativeButton("NO", null).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
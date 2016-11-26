/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package own.projects.lemiroapp;

import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class GameModeActivity extends android.support.v4.app.FragmentActivity{

	protected final static int COMPUTE_DONE = 20;
	protected final static int READ_READY_DONE_HUMAN_SET = 74;
	protected final static int READ_READY_DONE_HUMAN_MOVE = 75;
	protected final static int RESULT_RESTART = Activity.RESULT_FIRST_USER + 1;
	
	protected final int LENGTH = 7;

	volatile Move currMove;
	volatile int remiCount;
	Thread gameThread;
	Options options;
	GridLayout fieldLayout;
	GameBoardView fieldView;
	TextView progressText;
	ProgressBar progressBar;
	ProgressUpdater progressUpdater;
	GameBoard field;
	Handler handler;
	int screenWidth;
	ImageView redSector;
	ImageView[] millSectors;
	final GameModeActivity THIS = this;

	private void setDefaultUncaughtExceptionHandler() {
	    try {
	        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

	            @Override
	            public void uncaughtException(Thread t, Throwable e) {
	            	StackTraceElement[] trace = e.getStackTrace();
	            	String tracem = "";
	            	for(int i=0; i<trace.length; i++){
	            		tracem += trace[i] + "\n";
	            	}
	                Log.e("GameModeActivity", "Uncaught Exception detected in thread {}" + t + e + "\n" + tracem);

                    final String message = e.getMessage();

                    //display message so that the user sees that something went wrong
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run(){
                            new AlertDialog.Builder(THIS)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("Error")
                            .setMessage(message)
                            .setCancelable(false)
                            .setNeutralButton("Quit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            })
                            .show();
                        }
                    });

	            }
	        });
	    } catch (SecurityException e) {
	        Log.e("GameModeActivity", "Could not set the Default Uncaught Exception Handler" + e);
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setDefaultUncaughtExceptionHandler();

        options = getIntent().getParcelableExtra("own.projects.lemiroapp.Options");

        millSectors = new ImageView[3];

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;

		setContentView(R.layout.activity_main);
		fieldLayout = (GridLayout) findViewById(R.id.field);
		progressText = (TextView) findViewById(R.id.progressText);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		((LinearLayout) fieldLayout.getParent()).updateViewLayout(fieldLayout,
				new LinearLayout.LayoutParams(screenWidth, screenWidth));

		progressUpdater = new ProgressUpdater(progressBar, this);
		
		currMove = null;
		remiCount = 20;
		fieldView = new GameBoardView(THIS, fieldLayout);
		
		init();

		gameThread.start();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		//TODO dont cover the gameboard after the game is lost with the game over message
		//TODO as the player probably wants to see why he lost

		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			new AlertDialog.Builder(THIS)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle("Options")
			.setMessage("What do you want to do?")
			.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int id) {
					new AlertDialog.Builder(THIS)
					.setCancelable(false)
					.setTitle("Quit?")
					.setMessage("Do you really want to Quit?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							setResult(RESULT_CANCELED);
							gameThread.interrupt();
							finish();
						}})
					.setNegativeButton("No", null)
					.show();
				}
			})
			.setNegativeButton("Cancel", null)
			.setNeutralButton("Restart", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int id) {
					new AlertDialog.Builder(THIS)
					.setTitle("Restart?")
					.setMessage("Do you want to restart?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							setResult(RESULT_RESTART);
							gameThread.interrupt();
							finish();
						}})
					.setNegativeButton("No", null)
					.show();
				}
			})
			.show();
			return true;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}
	
	protected void init(){};
	
	protected void showToast(String text){
		Toast toast = Toast.makeText(this,text ,Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM,0,0);
		toast.show();
	}
	
	void setTextinUIThread(final TextView view, final String text){
		runOnUiThread(new Runnable() {
			public void run() {
				view.setText(text);
			}
		});
	}
	
	void setTextinUIThread(final TextView view, final int stringID){
		runOnUiThread(new Runnable() {
			public void run() {
				view.setText(getString(stringID));
			}
		});
	}
	
	 protected void showGameOverMsg(final String title, final String message){
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			runOnUiThread(new Runnable(){

				@Override
				public void run() {

					new AlertDialog.Builder(THIS)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(title)
					.setMessage(message)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
							setResult(RESULT_OK);
							finish();
						}
					})
					.show();
				}
			});
	 }

}

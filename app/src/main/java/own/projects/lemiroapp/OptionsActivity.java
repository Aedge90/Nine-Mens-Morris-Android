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

import android.support.v4.content.ContextCompat;
import java.util.ArrayList;
import android.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class OptionsActivity extends android.support.v4.app.FragmentActivity{

	private Options options;
	private Spinner gameModeSpinner;
	private Spinner millModeSpinner;
	private Spinner difficulty1Spinner;
	private Spinner difficulty2Spinner;
	private Spinner whoStartsSpinner;
	private Spinner colorSpinner;
	private Button buttonOK;
	
	private final OptionsActivity THIS = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.options_layout);
        this.options = new Options();
        
        setGameMode();
        setMillMode();
        setDifficulty1();
        setDifficulty2();
        setColor();
        setWhoStarts();
                
        buttonOK = (Button) findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				Intent i = new Intent();
				setResult(RESULT_OK, i.putExtra("own.projects.lemiroapp.Options", options));
				finish();
				
			}
        	
        });
        
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			new AlertDialog.Builder(THIS)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle("Quit?")
			.setMessage("Do you want to quit?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int id) {
					setResult(RESULT_CANCELED);
					finish();
				}
			})
			.setNegativeButton("No", null)			
			.show();
		}
		return super.onKeyDown(keyCode, event);
	}
    
    private void setGameMode() {
    	
    	gameModeSpinner = (Spinner) findViewById(R.id.gameModeSpinner);
    	
    	final ArrayAdapter<String> items = new ArrayAdapter<String>(
				this, layout.simple_list_item_1);
		items.add("Human vs. Bot");
		items.add("Bot vs. Bot");
		items.add("Human vs. Human");
    	
    	gameModeSpinner.setAdapter(items);
    	gameModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

    		@Override
    		public void onItemSelected(AdapterView<?> parent, View view, 
    				int pos, long id) {
    			if(pos == 0){
    				setDifficulty1();
    				options.gameMode = Options.GameMode.HUMANBOT;
    				disableDifficulty2();
    			}else if(pos == 1){
    				setDifficulty1();
    				setDifficulty2();
    				options.gameMode = Options.GameMode.BOTBOT;
    			}else if(pos == 2){
    				options.gameMode = Options.GameMode.HUMANHUMAN;
    				disableDifficulty1();
    				disableDifficulty2();
    			}else{
    				Log.e("Options", "setGameMode Failed");
    				setResult(Activity.RESULT_CANCELED);
    				finish();
    			}
    		}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
    		
		});
		
	}

	private void setMillMode() {
		
		final ItemsAdapter items = new ItemsAdapter(this,
				layout.simple_list_item_1);
		items.add("Nine Mens Morris", R.drawable.brett9);
		items.add("Seven Mens Morris", R.drawable.brett7);
		items.add("Five Mens Morris", R.drawable.brett5);
		Drawable img0 = ContextCompat.getDrawable(this, R.drawable.brett9);
		Drawable img1 = ContextCompat.getDrawable(this, R.drawable.brett7);
		Drawable img2 = ContextCompat.getDrawable(this, R.drawable.brett5);
		img0.setBounds(0, 0, 60, 60);
		img1.setBounds(0, 0, 60, 60);
		img2.setBounds(0, 0, 60, 60);
		
		millModeSpinner = (Spinner) findViewById(R.id.millModeSpinner);
    	
    	millModeSpinner.setAdapter(items);
    	millModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
				if(pos == 0){
					options.millMode = Options.MillMode.MILL9;
				}else if(pos == 1){
					options.millMode = Options.MillMode.MILL7;
				}else if(pos == 2){
					options.millMode = Options.MillMode.MILL5;
				}else{
					Log.e("Options", "setMillMode Failed");
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
				//enableOKButtonIfReady(1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
    		
		});

	}

	private void setDifficulty1() {
		
		final ArrayAdapter<String> items = new ArrayAdapter<String>(
				this, layout.simple_list_item_1);
		items.add("Easy");
		items.add("Normal");
		items.add("Hard");
		items.add("Harder");
		items.add("Hardest");
		
		difficulty1Spinner = (Spinner) findViewById(R.id.difficulty1Spinner);
    	
		difficulty1Spinner.setAdapter(items);
		difficulty1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
				if(pos == 0){
					options.difficulty1 = Options.Difficulties.EASY;
				}else if(pos == 1){
					options.difficulty1  = Options.Difficulties.NORMAL;
				}else if(pos == 2){
					options.difficulty1  = Options.Difficulties.HARD;
				}else if(pos == 3){
					options.difficulty1  = Options.Difficulties.HARDER;
				}else if(pos == 4){
					options.difficulty1 = Options.Difficulties.HARDEST;
				}else{
					Log.e("Options", "setDifficulty1 Failed");
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
    		
		});
		
		difficulty1Spinner.setClickable(true);
		difficulty1Spinner.setEnabled(true);
		
	}
	
	private void disableDifficulty1() {
		
		final ArrayAdapter<String> items = new ArrayAdapter<String>(
				this, layout.simple_list_item_1);
		items.add("No Bots in this Mode");
		
		difficulty1Spinner = (Spinner) findViewById(R.id.difficulty1Spinner);
    	
		difficulty1Spinner.setAdapter(items);

		difficulty1Spinner.setClickable(false);
		difficulty1Spinner.setEnabled(false);
		
	}
	
	private void disableDifficulty2() {
		
		final ArrayAdapter<String> items = new ArrayAdapter<String>(
				this, layout.simple_list_item_1);
		if(options.gameMode.equals(Options.GameMode.HUMANHUMAN)){
			items.add("No Bots in this Mode");
		}else{ 	//HUMANVSBOT
			items.add("Only one Bot in this Mode");
		}
		
		difficulty2Spinner = (Spinner) findViewById(R.id.difficulty2Spinner);
    	
		difficulty2Spinner.setAdapter(items);

		difficulty2Spinner.setClickable(false);
		difficulty2Spinner.setEnabled(false);
		
	}

	private void setDifficulty2() {
		
		final ArrayAdapter<String> items = new ArrayAdapter<String>(
				this, layout.simple_list_item_1);
		items.add("Easy");
		items.add("Normal");
		items.add("Hard");
		items.add("Harder");
		items.add("Hardest");
		
		difficulty2Spinner = (Spinner) findViewById(R.id.difficulty2Spinner);
    	
		difficulty2Spinner.setAdapter(items);
		difficulty2Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
				if(pos == 0){
					options.difficulty2 = Options.Difficulties.EASY;
				}else if(pos == 1){
					options.difficulty2  = Options.Difficulties.NORMAL;
				}else if(pos == 2){
					options.difficulty2  = Options.Difficulties.HARD;
				}else if(pos == 3){
					options.difficulty2  = Options.Difficulties.HARDER;
				}else if(pos == 4){
					options.difficulty2 = Options.Difficulties.HARDEST;
				}else{
					Log.e("Options", "setDifficulty2 Failed");
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
				//enableOKButtonIfReady(3);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
    		
		});

		difficulty2Spinner.setClickable(true);
		difficulty2Spinner.setEnabled(true);
		
	}

	private void setColor() {
		
		final ArrayAdapter<String> items = new ArrayAdapter<String>(
				this, layout.simple_list_item_1);
		items.add("White");
		items.add("Black");
		
		colorSpinner = (Spinner) findViewById(R.id.colorSpinner);
    	
		colorSpinner.setAdapter(items);
		colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
				if(pos == 0){
					options.colorPlayer1 = Options.Color.WHITE;
					options.colorPlayer2 = Options.Color.BLACK;
				}else if(pos == 1){
					options.colorPlayer1 = Options.Color.BLACK;
					options.colorPlayer2 = Options.Color.WHITE;
				}else{
					Log.e("Options", "setColor Failed");
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
				//enableOKButtonIfReady(4);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
    		
		});

	}

	private void setWhoStarts() {
		
		final ArrayAdapter<String> items = new ArrayAdapter<String>(
				this, layout.simple_list_item_1);
		items.add("White");
		items.add("Black");
		
		whoStartsSpinner = (Spinner) findViewById(R.id.whoStartsSpinner);
    	
		whoStartsSpinner.setAdapter(items);
		whoStartsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
				if(pos == 0){
					options.whoStarts = Options.Color.WHITE;
				}else if(pos == 1){
					options.whoStarts = Options.Color.BLACK;
				}else{
					Log.e("Options", "setWhoStarts Failed");
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
				//enableOKButtonIfReady(5);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
    		
		});

	}
	
	private class ItemsAdapter extends ArrayAdapter<String> {

		private ArrayList<String> str;
		private Context context;
		private ArrayList<Integer> imageIds;
		private int screenWidth;

		public ItemsAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			this.context = context;
			this.str = new ArrayList<String>();
			this.imageIds = new ArrayList<Integer>();
		}

		public void add(String object, int id) {
			super.add(object);
			str.add(object);
			imageIds.add(id);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			String item = str.get(position);

			LinearLayout layout = new LinearLayout(context);
			layout.setGravity(Gravity.CENTER_VERTICAL);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.setPadding(10, 10, 10, 10);

			LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			margin.setMargins(0, 0, 0, 0);
			
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			screenWidth = size.x;

			ImageView image = new ImageView(context);
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					imageIds.get(position));
			bmp = Bitmap.createScaledBitmap(bmp, screenWidth / 6,
					screenWidth / 6, false);
			image.setImageBitmap(bmp);
			layout.addView(image, margin);

			TextView mTitle = new TextView(context);
			mTitle.setText(item);
			layout.addView(mTitle, margin);

			return layout;
		}
	}

}

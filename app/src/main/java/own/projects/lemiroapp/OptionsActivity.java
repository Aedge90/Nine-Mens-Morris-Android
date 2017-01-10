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
    private Spinner millModeSpinner;
    private Spinner playerWhiteSpinner;
    private Spinner playerBlackSpinner;
    private Spinner whoStartsSpinner;
    private Button buttonOK;

    
    private final OptionsActivity THIS = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.options_layout);
        this.options = new Options();

        setMillVariant();
        setPlayerDifficultyFor(options.playerWhite);
        setPlayerDifficultyFor(options.playerBlack);
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

    private void setMillVariant() {
        
        final ItemsAdapter items = new ItemsAdapter(this,
                layout.simple_list_item_1);
        items.add("Nine Mens Morris", R.drawable.brett9);
        items.add("Seven Mens Morris", R.drawable.brett7);
        items.add("Five Mens Morris", R.drawable.brett5);
        
        millModeSpinner = (Spinner) findViewById(R.id.millModeSpinner);
        
        millModeSpinner.setAdapter(items);
        millModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, 
                    int pos, long id) {
                if(pos == 0){
                    options.millVariant = Options.MillVariant.MILL9;
                }else if(pos == 1){
                    options.millVariant = Options.MillVariant.MILL7;
                }else if(pos == 2){
                    options.millVariant = Options.MillVariant.MILL5;
                }else{
                    Log.e("Options", "setMillVariant Failed");
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

    private void setPlayerDifficultyFor(final Player player) {
        
        final ArrayAdapter<String> items = new ArrayAdapter<String>(
                this, layout.simple_list_item_1);
        items.add("Human Player");

        for(Options.Difficulties diff : Options.Difficulties.values()){
            items.add(diff + " Bot");
        }

        Spinner spinner = null;
        if(player.getColor().equals(Options.Color.WHITE)) {
            playerWhiteSpinner = (Spinner) findViewById(R.id.playerWhiteSpinner);
            spinner = playerWhiteSpinner;
        }else {
            playerBlackSpinner = (Spinner) findViewById(R.id.playerBlackSpinner);
            spinner = playerBlackSpinner;
        }

        spinner.setAdapter(items);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                if(pos == 0){
                    // no difficulty for human player
                    return;
                }else {
                    // -1 offset because first entry is the human player
                    player.setDifficulty(Options.Difficulties.values()[pos - 1]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                
            }
            
        });

        spinner.setClickable(true);
        spinner.setEnabled(true);
        
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
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), imageIds.get(position));
            bmp = Bitmap.createScaledBitmap(bmp, screenWidth / 6, screenWidth / 6, false);
            image.setImageBitmap(bmp);
            layout.addView(image, margin);

            TextView mTitle = new TextView(context);
            mTitle.setText(item);
            layout.addView(mTitle, margin);

            return layout;
        }
    }

}

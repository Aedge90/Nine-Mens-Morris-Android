package com.github.aedge90.nmm;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.view.Display;
import android.view.KeyEvent;

public class MainActivity extends Activity {

    private final static int RUN_GAME = 67;
    private final static int SET_OPTIONS = 100;
    
    private final MainActivity THIS = this;
    
    private Options options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new Options();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        startSetOptionsIntent();

    }

    private void startSetOptionsIntent () {
        Intent setOptionsIntent = new Intent(this, OptionsActivity.class);
        setOptionsIntent.putExtra("own.projects.lemiroapp.Options", options);
        startActivityForResult(setOptionsIntent, SET_OPTIONS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SET_OPTIONS) {
            if (resultCode == RESULT_OK) {
                
                options = data.getParcelableExtra("own.projects.lemiroapp.Options");

                Intent gameModeIntent = new Intent(THIS, GameModeActivity.class);
                gameModeIntent.setExtrasClassLoader(Options.class.getClassLoader());
                gameModeIntent.putExtra("own.projects.lemiroapp.Options", options);
                startActivityForResult(gameModeIntent, RUN_GAME);
                
            }else {
                finish();
            }
        }else if(requestCode == RUN_GAME) {
            if(resultCode == RESULT_CANCELED){
                finish();
            }else{
                startSetOptionsIntent();
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
                    .setTitle(getResources().getString(R.string.quit_game))
                    .setMessage(getResources().getString(R.string.want_to_quit))
                    .setPositiveButton(getResources().getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    quit();
                                }
                            }).setNegativeButton(getResources().getString(R.string.no), null).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
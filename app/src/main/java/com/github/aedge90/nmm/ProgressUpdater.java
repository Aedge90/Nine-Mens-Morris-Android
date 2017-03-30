package com.github.aedge90.nmm;

import android.widget.ProgressBar;

public class ProgressUpdater {

    private boolean active;
    private ProgressBar progressBar;
    GameModeActivity c;
    private volatile int progress;
    
    public ProgressUpdater(ProgressBar progressBar, GameModeActivity c) {
        this.progressBar = progressBar;
        this.c = c;
        this.active = false;
        this.progress = 0;
    }

    public void setMax(final int max) {
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setMax(max);
            }
        });
    }
    
    public void increment() {
        progress++;
        if(active) {
            c.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(progress);
                }
            });
        }else{
            c.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(0);
                }
            });
        }
    }
    
    public void reset(){
        progress = 0;
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(0);
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}

package own.projects.lemiroapp;

import android.widget.ProgressBar;

public class ProgressUpdater {

	private ProgressBar progressBar;
	private int progress;
	GameModeActivity c;
	
	public ProgressUpdater(ProgressBar progressBar, GameModeActivity c) {
		this.progressBar = progressBar;
        this.progress = 0;
		this.c = c;
	}

    public void setProgress (int progress){
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public void setMax(final int max) {
		c.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressBar.setMax(max);
			}
		});
	}
	
	public void update() {
		c.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressBar.setProgress(progress);
			}
		});
	}
	
	public void reset(){
		c.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressBar.setProgress(0);
			}
		});
	}

}

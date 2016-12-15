package own.projects.lemiroapp;

import android.widget.ProgressBar;

public class ProgressUpdater {

	private ProgressBar progressBar;
	GameModeActivity c;
	
	public ProgressUpdater(ProgressBar progressBar, GameModeActivity c) {
		this.progressBar = progressBar;
		this.c = c;
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
		c.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressBar.incrementProgressBy(1);
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

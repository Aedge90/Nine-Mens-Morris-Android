package own.projects.lemiroapp;

import android.widget.ProgressBar;

public class ProgressUpdater {

	private ProgressBar progress;
	private int max;
	GameModeActivity c;
	
	public ProgressUpdater(ProgressBar progress, GameModeActivity c) {
		this.progress = progress;
		this.c = c;
	}

	public void initialize(final int max) {
		c.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progress.setMax(max);
				progress.setProgress(0);
			}
		});
	}
	
	public void update() {
		c.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progress.incrementProgressBy(1);
			}
		});
	}
	
	public void reset(){
		c.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progress.setProgress(0);
			}
		});
	}

}

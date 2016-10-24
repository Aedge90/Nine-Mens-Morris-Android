package own.projects.lemiroapp;

import android.widget.ProgressBar;

public class ProgressUpdater {

	private ProgressBar progress;
	private int max;
	GameModeActivity c;
	private static ProgressUpdater instance = null;

	/*
	private ProgressUpdater(int max, ProgressBar progress, GameModeActivity c) {
		this.progress = progress;
		this.progress.setMax(max);
		this.progress.setProgress(0);
		this.c = c;
		this.max = max;
	}
	*/
	
	public ProgressUpdater(ProgressBar progress, GameModeActivity c) {
		this.progress = progress;
		this.c = c;
	}
	
	/*
	private ProgressUpdater(ProgressBar progress, Mill9 field) {
		this.progress = progress;
		int  piecesOnField = field.getStoneCount();
		if(field.getMillVar() == Options.MillMode.MILL5){
			this.max = 16 - piecesOnField;
			this.progress.setMax(max);
			this.progress.setProgress(0);
		}else if(field.getMillVar() == Options.MillMode.MILL7){
			this.max = 17 - piecesOnField;
			this.progress.setMax(max);
			this.progress.setProgress(0);
		}else if(field.getMillVar() == Options.MillMode.MILL9){
			this.max = 24 - piecesOnField;
			this.progress.setMax(max);
			this.progress.setProgress(0);
		}
	}
	*/

	/*
	public static ProgressUpdater getInstance(int max, ProgressBar progress, GameModeActivity c) {
		if (instance == null) {
			instance = new ProgressUpdater(max, progress, c);
		}
		return instance;
	}
	*/
	
	/*
	public static void reset(){
		instance = null;
	}
	*/

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
	
	
	/*
	public void finished(){
		progress.setProgress(max);
	}
	 */
}

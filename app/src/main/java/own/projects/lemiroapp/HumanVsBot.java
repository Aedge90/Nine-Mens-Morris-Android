

package own.projects.lemiroapp;

import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;
import android.widget.ImageView;

public class HumanVsBot extends GameModeActivity{

	private Lock lock = new ReentrantLock();
    private Condition selection = lock.newCondition();
    private volatile boolean selected;
    
	private Strategie brain;
	private volatile int setCountHuman;
	private volatile int setCountBot;
	private volatile State state;
	private static enum State {
		SET, MOVEFROM, MOVETO, IGNORE, KILL, GAMEOVER
	};
    
    @Override
    protected void init(){

    	state = State.IGNORE;
    	
		progressBar.setMax(options.difficulty1.ordinal() + 2);
		// Mill Settings are Set
		if (options.millMode == Options.MillMode.MILL5) {
			setCountHuman = 5;
			setCountBot = 5;
			field = new Mill5(options.millMode);
			fieldLayout.setBackgroundResource(R.drawable.brett5);
		} else if (options.millMode == Options.MillMode.MILL7) {
			setCountHuman = 7;
			setCountBot = 7;
			field = new Mill7(options.millMode);
			fieldLayout.setBackgroundResource(R.drawable.brett7);
		} else if (options.millMode == Options.MillMode.MILL9) {
			setCountHuman = 9; 
			setCountBot = 9;
			field = new Mill9(options.millMode);
			fieldLayout.setBackgroundResource(R.drawable.brett9);
		}
		selected = false;
		brain = new Strategie(field, progressUpdater);
		
		setSectorListeners();
		
		game();
	}
    
    private void setSectorListeners() {

		for (int y = 0; y < LENGTH; y++) {
			for (int x = 0; x < LENGTH; x++) {
				if (!field.getPos(x, y).equals(Options.Color.INVALID)) {
					fieldView.getPos(new Position(x, y)).setOnClickListener(
							new  OnFieldClickListener(x,y));			
				}
			}
		}
	}

    public void game(){

    	Runnable game = new Runnable(){

    		@Override
    		public void run(){

    			try {
    				if(options.whoStarts.equals(Options.Color.WHITE) && options.colorPlayer1.equals(Options.Color.WHITE)
    						|| options.whoStarts.equals(Options.Color.BLACK) && options.colorPlayer1.equals(Options.Color.BLACK)){

    				}else{
    					state = State.IGNORE;
    					setTextinUIThread(progressText, "Bot is Computing!");
    					botTurn();
    					currMove = new Zug(null, null, null, null);
    				}
    				while(true){

    					setTextinUIThread(progressText, R.string.player_turn);

    					humanTurn();

    					if(whoWon()){
    						break;
    					}
    					currMove = new Zug(null, null, null, null);

    					setTextinUIThread(progressText, "Bot is Computing!");

    					botTurn();

    					if(whoWon()){
    						break;
    					}
    					currMove = new Zug(null, null, null, null);
    				}
    			} catch ( InterruptedException e ) {
    				Log.d("HumanVsBot", "Interrupted!");
    				e.printStackTrace();
    				gameThread.interrupt();
    			}
    		}

    	};

    	gameThread = new Thread(game);
    	gameThread.start();

    }
    
    private void waitforSelection() throws InterruptedException{
		lock.lock();
		try {
            while(!selected) { //necessary to avoid lost wakeup
               selection.await();
            }
        } finally { 
        	selected = false;
            lock.unlock();
        }
	}
	
	private void signalSelection(){
		lock.lock();
		selected = true;
		selection.signal();
		lock.unlock();
	}
    
    private void humanTurn() throws InterruptedException{
    	Position newPosition = null;
		if(setCountHuman <= 0){
			while(currMove.getDest() == null){
				state = State.MOVEFROM;
				//wait for Human to select source
				waitforSelection();
				state = State.MOVETO;
				//wait for Human to select destination
				waitforSelection();
			}
			field.makeMove(currMove.getSrc(), currMove.getDest(), options.colorPlayer1);
			fieldView.makeMove(currMove, options.colorPlayer1);
			fieldView.getPos(currMove.getSrc()).setOnClickListener(new OnFieldClickListener(currMove.getSrc()));
			fieldView.getPos(currMove.getDest()).setOnClickListener(new OnFieldClickListener(currMove.getDest()));
			newPosition = currMove.getDest();
		}else{
			state = State.SET;
			// wait for human to set
			waitforSelection();
			field.setPos(currMove.getSet(), options.colorPlayer1);
			fieldView.setPos(currMove.getSet(), options.colorPlayer1);
			fieldView.getPos(currMove.getSet()).setOnClickListener(new OnFieldClickListener(currMove.getSet()));
			setCountHuman--;
			newPosition = currMove.getSet();
		}
		if (field.inMill(newPosition, options.colorPlayer1)) {
			state = State.KILL;
			Position[] mill = field.getMill(newPosition, options.colorPlayer1);
			fieldView.paintMill(mill, millSectors);
			//wait until kill is chosen
			waitforSelection();
			field.setPos(currMove.getKill(), Options.Color.NOTHING);
			fieldView.setPos(currMove.getKill(), Options.Color.NOTHING);
			fieldView.getPos(currMove.getKill()).setOnClickListener(new OnFieldClickListener(currMove.getKill()));
			fieldView.unpaintMill(millSectors);
		}
		state = State.IGNORE;
    }

    
    private void botTurn() throws InterruptedException{
    	Position newPosition = null;
    	if(setCountBot <= 0){
			currMove = brain.computeMove(options.colorPlayer2, options.difficulty1, setCountBot, setCountHuman);

			setTextinUIThread(progressText, "Bot is moving!");
			
	    	field.makeMove(currMove.getSrc(), currMove.getDest() , options.colorPlayer2);

	    	fieldView.makeMove(currMove, options.colorPlayer2);
	    	fieldView.getPos(currMove.getSrc()).setOnClickListener(
	    			new OnFieldClickListener(currMove.getSrc()));
	    	fieldView.getPos(currMove.getDest()).setOnClickListener(
	    			new OnFieldClickListener(currMove.getDest()));
	    	newPosition = currMove.getDest();
		}else{
			long time = SystemClock.elapsedRealtime();
			
			currMove = brain.computeMove(options.colorPlayer2, options.difficulty1, setCountBot, setCountHuman);
	    	
			setTextinUIThread(progressText, "Bot is moving!");
			
			//wait a moment if computation was very fast else don´t wait
			long computationTime = time - SystemClock.elapsedRealtime();
			if(computationTime < 1000){
				Thread.sleep(1000 - computationTime);
			}
			
	    	field.setPos(currMove.getSet(), options.colorPlayer2);

	    	fieldView.setPos(currMove.getSet(),options.colorPlayer2);
	    	fieldView.getPos(currMove.getSet()).setOnClickListener(
	    			new OnFieldClickListener(currMove.getSet()));
	    	newPosition = currMove.getSet();
	    	setCountBot --;
		}
    	if (currMove.getKill() != null) {
    		Position[] mill = field.getMill(newPosition, options.colorPlayer2);
    		fieldView.paintMill(mill, millSectors);
    		field.setPos(currMove.getKill(), Options.Color.NOTHING);
    		fieldView.setPos(currMove.getKill(), Options.Color.NOTHING);
    		fieldView.getPos(currMove.getKill()).setOnClickListener(
    				new OnFieldClickListener(currMove.getKill()));
    		Thread.sleep(1500);
    		fieldView.unpaintMill(millSectors);
    	}
    }
    
	private boolean whoWon() {
		if(field.getPositions(options.colorPlayer1).size() == 3 && field.getPositions(options.colorPlayer2).size() == 3){
			remiCount --;
			if(remiCount == 0){
				showGameOverMsg("Draw!", "Nobody wins.");
				return true;
			}
		}
		
		if(!field.movesPossible(options.colorPlayer1, setCountHuman)){
			showGameOverMsg("You have lost!", "You could not make any further move.");
			return true;
		}else if ((field.getPositions(options.colorPlayer1).size() < 3 && setCountHuman <= 0)) {
			showGameOverMsg("You have lost!", "You lost all of your stones.");
			return true;
		}else if(!field.movesPossible(options.colorPlayer2, setCountBot)){
			showGameOverMsg("Bot has lost!", "He could not make any further move.");
			return true;
		}else if ((field.getPositions(options.colorPlayer2).size() < 3 && setCountBot <= 0)) {
			showGameOverMsg("Bot has lost!", "He has lost all of his stones.");
			return true;
		}
		return false;
	}

	private class OnFieldClickListener implements OnClickListener {

		final int x;
		final int y;

		OnFieldClickListener(int x ,int y){
			this.x = x;
			this.y = y;
		}

		OnFieldClickListener(Position pos){
			this.x = pos.getX();
			this.y = pos.getY();
		}

		@Override
		public void onClick(View arg0) {

			if (state == State.SET) {
				if(field.getPos(new Position(x,y)).equals(options.colorPlayer1)
						|| field.getPos(new Position(x,y)).equals((options.colorPlayer2))){
					showToast("You can not set to this Position!");
				}else{
					Position pos = new Position(x, y);
					currMove.setSet(pos);
					signalSelection();
				}
			} else if (state == State.MOVEFROM) {
				if(!(field.getPos(new Position(x,y)).equals(options.colorPlayer1))){
					showToast("Nothing to move here!");
				}else{
					redSector = fieldView.createSector(Options.Color.RED);
					redSector.setLayoutParams(new GridLayout.LayoutParams(
							GridLayout.spec(y, 1), GridLayout.spec(x, 1)));
					fieldLayout.addView(redSector);
					currMove.setSrc(new Position(x,y));
					signalSelection();
				}
			} else if (state == State.MOVETO) {
				if(!field.movePossible(currMove.getSrc(), new Position(x,y))){
					state = State.MOVEFROM;
					fieldLayout.removeView(redSector);
					showToast("You can not move to this Position!");
				}else{
					fieldLayout.removeView(redSector);
					currMove.setDest(new Position(x, y));
				}
				signalSelection();
			} else if (state == State.IGNORE) {
				showToast("It is not your turn!");
			}else if (state == State.KILL) { 
				if(!(field.getPos(new Position(x,y)) == options.colorPlayer2)){
					showToast("Nothing to kill here!");
				}else if(field.inMill(new Position(x,y), options.colorPlayer2)){
					//if every single stone of enemy is part of a mill we are allowed to kill
					LinkedList<Position> enemypos = field.getPositions(options.colorPlayer2);
					boolean allInMill = true;
					for(int i = 0; i<enemypos.size(); i++){
						if(!field.inMill(enemypos.get(i), options.colorPlayer2)){
							allInMill = false;
							break;
						}
					}
					if(allInMill){
						Position killPos = new Position(x, y);
						currMove.setKill(killPos);
						signalSelection();
					}else{
						showToast("You can not kill a mill! Choose another target!");
					}
				}else{
					Position killPos = new Position(x, y);
					currMove.setKill(killPos);
					signalSelection();
				}
			}
			
			//showToast("x = " + x + "  y = " + y);	 

		}
	}
}

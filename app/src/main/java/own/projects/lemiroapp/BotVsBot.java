
//TODO include this file again

/*

package own.projects.lemiroapp;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.SystemClock;
import android.util.Log;


public class BotVsBot extends GameModeActivity{
    
	private volatile Zug currMove;
	private Strategie brain;
	private volatile int remiCount;
	private volatile int setCountBot1;
	private volatile int setCountBot2;
    
    @Override
    protected void init(){
    	
		progressBar.setMax(options.difficulty1.ordinal() + 2);
		// Mill Settings are Set
		if (options.millMode == Options.MillMode.MILL5) {
			setCountBot1 = 5;
			setCountBot2 = 5;
			field = new Mill5();
			fieldLayout.setBackgroundResource(R.drawable.brett5);
		} else if (options.millMode == Options.MillMode.MILL7) {
			setCountBot1 = 7;
			setCountBot2 = 7;
			field = new Mill7();
			fieldLayout.setBackgroundResource(R.drawable.brett7);
		} else if (options.millMode == Options.MillMode.MILL9) {
			setCountBot1 = 9; 
			setCountBot2 = 9;
			field = new Mill9();
			fieldLayout.setBackgroundResource(R.drawable.brett9);
		}
		
		brain = new Strategie(field , progressUpdater);
		
		gameThread = createGameThread();

	}

    public Thread createGameThread(){

    	Runnable game = new Runnable(){

    		@Override
    		public void run(){

    			try {
    				if(options.whoStarts.equals(Options.Color.WHITE) && options.colorPlayer1.equals(Options.Color.WHITE)
    						|| options.whoStarts.equals(Options.Color.BLACK) && options.colorPlayer1.equals(Options.Color.BLACK)){
   
    				}else{
    					setTextinUIThread(progressText, "Bot 2 is Computing!");
    					botTurn(options.colorPlayer2,options.difficulty2, setCountBot2, setCountBot1, 2);
    					currMove = new Zug(null, null, null, null);
    				}
    				while(true){

    					setTextinUIThread(progressText, "Bot 1 is Computing!");
    					botTurn(options.colorPlayer1, options.difficulty1, setCountBot1, setCountBot2, 1);

    					if(whoWon()){
    						break;
    					}
    					currMove = new Zug(null, null, null, null);

    					setTextinUIThread(progressText, "Bot 2 is Computing!");
    					botTurn(options.colorPlayer2, options.difficulty2, setCountBot2, setCountBot1, 2);

    					if(whoWon()){
    						break;
    					}
    					currMove = new Zug(null, null, null, null);
    				}
    			} catch ( InterruptedException e ) {
    				Log.d("BotVsBot", "Interrupted!");
    				e.printStackTrace();
    				gameThread.interrupt();
    			}
    		}

    	};

    	return new Thread(game);

    }

    private void botTurn(Options.Color botColor, Options.Difficulties hisDifficulty, int hisSetCount, int otherSetCount, int botNR) throws InterruptedException{
    	Position newPosition = null;
    	if(hisSetCount <= 0){
			currMove = brain.computeMove(botColor, hisDifficulty, hisSetCount, otherSetCount);
			
			setTextinUIThread(progressText, "Bot " + botNR + " is moving!");

	    	field.makeMove(currMove.getSrc(), currMove.getDest() , botColor);

	    	fieldView.makeMove(currMove, botColor);
	    	
	    	newPosition = currMove.getDest();
		}else{
			
			long time = SystemClock.elapsedRealtime();
			
			currMove = brain.computeMove(botColor, hisDifficulty, hisSetCount, otherSetCount);
			
			setTextinUIThread(progressText, "Bot " + botNR + " is moving!");
			
			//wait a moment if computation was very fast else don´t wait
			long computationTime = time - SystemClock.elapsedRealtime();
			if(computationTime < 1000){
				Thread.sleep(1000 - computationTime);
			}
			
	    	field.setPos(currMove.getSet(), botColor);

	    	fieldView.setPos(currMove.getSet(),botColor);
	    	
	    	newPosition = currMove.getSet();
	    	if(botNR == 1){
	    		setCountBot1 --;
	    	}else{
	    		setCountBot2 --;
	    	}
		}
    	if (currMove.getKill() != null) {
    		Position[] mill = field.getMill(newPosition, botColor);
    		fieldView.paintMill(mill, millSectors);
    		field.setPos(currMove.getKill(), Options.Color.NOTHING);
    		fieldView.setPos(currMove.getKill(), Options.Color.NOTHING);
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
		
		if(!field.movesPossible(options.colorPlayer1, setCountBot1)){
			showGameOverMsg("Bot 1 has lost!", "He could not make any further move.");
			return true;
		}else if ((field.getPositions(options.colorPlayer1).size() < 3 && setCountBot1 <= 0)) {
			showGameOverMsg("Bot 1 has lost!", "He has lost all of his stones.");
			return true;
		}else if(!field.movesPossible(options.colorPlayer2, setCountBot2)){
			showGameOverMsg("Bot 2 has lost!", "He could not make any further move.");
			return true;
		}else if ((field.getPositions(options.colorPlayer2).size() < 3 && setCountBot2 <= 0)) {
			showGameOverMsg("Bot 2 has lost!", "He has lost all of his stones.");
			return true;
		}
		return false;
	}
}

*/
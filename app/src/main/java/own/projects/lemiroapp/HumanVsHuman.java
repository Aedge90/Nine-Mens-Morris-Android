/*

package own.projects.lemiroapp;

import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import own.projects.lemiroapp.Options.Color;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;
import android.widget.ImageView;


public class HumanVsHuman extends GameModeActivity{

	private Lock lock = new ReentrantLock();
    private Condition selection = lock.newCondition();
    private volatile boolean selected;

	private volatile int setCountHuman1;
	private volatile int setCountHuman2;
	private volatile State state;

	//his Opponent
	Options.Color opponentPlayer;
    
    @Override
    protected void init(){

    	state = State.IGNORE;
    	
		progressBar.setMax(options.difficulty1.ordinal() + 2);
		// Mill Settings are Set
		if (options.millMode == Options.MillMode.MILL5) {
			setCountHuman1 = 5;
			setCountHuman2 = 5;
			field = new Mill5();
			fieldLayout.setBackgroundResource(R.drawable.brett5);
		} else if (options.millMode == Options.MillMode.MILL7) {
			setCountHuman1 = 7;
			setCountHuman2 = 7;
			field = new Mill7();
			fieldLayout.setBackgroundResource(R.drawable.brett7);
		} else if (options.millMode == Options.MillMode.MILL9) {
			setCountHuman1 = 9; 
			setCountHuman2 = 9;
			field = new Mill9();
			fieldLayout.setBackgroundResource(R.drawable.brett9);
		}
		selected = false;
		
		setSectorListeners();

        gameThread = createGameThread();

	}
    
    private void setSectorListeners() {

		for (int y = 0; y < LENGTH; y++) {
			for (int x = 0; x < LENGTH; x++) {
				if (!field.getColorAt(x, y).equals(Options.Color.INVALID)) {
					fieldView.getPos(new Position(x, y)).setOnClickListener(
							new  OnFieldClickListener(x,y));			
				}
			}
		}
	}

    public Thread createGameThread(){

    	Runnable game = new Runnable(){

    		@Override
    		public void run(){

    			try {
    				if(options.whoStarts.equals(Options.Color.WHITE) && options.colorPlayer1.equals(Options.Color.WHITE)
    						|| options.whoStarts.equals(Options.Color.BLACK) && options.colorPlayer1.equals(Options.Color.BLACK)){

    				}else{
    					state = State.IGNORE;
    					setTextinUIThread(progressText, "Turn of Player 2");
    					changeCurrPlayerTo(options.colorPlayer2);
    					humanTurn(options.colorPlayer2, setCountHuman2 , 2);
    					currMove = new Move(null, null, null, null);
    				}
    				while(true){

    					setTextinUIThread(progressText, "Turn of Player 1");
    					changeCurrPlayerTo(options.colorPlayer1);
    					humanTurn(options.colorPlayer1, setCountHuman1 , 1);

    					if(whoWon()){
    						break;
    					}
    					currMove = new Move(null, null, null, null);

    					setTextinUIThread(progressText, "Turn of Player 2");
    					changeCurrPlayerTo(options.colorPlayer2);
    					humanTurn(options.colorPlayer2, setCountHuman2 , 2);

    					if(whoWon()){
    						break;
    					}
    					currMove = new Move(null, null, null, null);
    				}
    			} catch ( InterruptedException e ) {
    				Log.d("HumanVsBot", "Interrupted!");
    				e.printStackTrace();
    				gameThread.interrupt();
    			}
    		}

    	};

		return new Thread(game);

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

    private void humanTurn(Options.Color humanColor, int hisSetCount, int humanNR) throws InterruptedException{
    	Position newPosition = null;
		if(hisSetCount <= 0){
			while(currMove.getDest() == null){
				state = State.MOVEFROM;
				//wait for Human to select source
				waitforSelection();
				state = State.MOVETO;
				//wait for Human to select destination
				waitforSelection();
			}
			field.makeMove(currMove.getSrc(), currMove.getDest(), humanColor);
			fieldView.makeMove(currMove, humanColor);
			fieldView.getColorAt(currMove.getSrc()).setOnClickListener(new OnFieldClickListener(currMove.getSrc()));
			fieldView.getColorAt(currMove.getDest()).setOnClickListener(new OnFieldClickListener(currMove.getDest()));
			newPosition = currMove.getDest();
		}else{
			state = State.SET;
			// wait for human to set
			waitforSelection();
			field.setPos(currMove.getSet(), humanColor);
			fieldView.setPos(currMove.getSet(), humanColor);
			fieldView.getColorAt(currMove.getSet()).setOnClickListener(new OnFieldClickListener(currMove.getSet()));
			if(humanNR == 1){
	    		setCountHuman1 --;
	    	}else{
	    		setCountHuman2 --;
	    	}
			newPosition = currMove.getSet();
		}
		if (field.inMill(newPosition, humanColor)) {
			state = State.KILL;
			Position[] mill = field.getMill(newPosition, humanColor);
			fieldView.paintMill(mill, millSectors);
			//wait until kill is chosen
			waitforSelection();
			field.setPos(currMove.getKill(), Options.Color.NOTHING);
			fieldView.setPos(currMove.getKill(), Options.Color.NOTHING);
			fieldView.getColorAt(currMove.getKill()).setOnClickListener(new OnFieldClickListener(currMove.getKill()));
			fieldView.unpaintMill(millSectors);
		}
		state = State.IGNORE;
    }
    
	private boolean whoWon() {
		if(field.getPositions(options.colorPlayer1).size() == 3 && field.getPositions(options.colorPlayer2).size() == 3){
			remiCount --;
			if(remiCount == 0){
				showGameOverMsg("Draw!", "Nobody wins.");
				return true;
			}
		}
		
		if(!field.movesPossible(options.colorPlayer1, setCountHuman1)){
			showGameOverMsg("Player 1 has lost!", "He could not make any further move.");
			return true;
		}else if ((field.getPositions(options.colorPlayer1).size() < 3 && setCountHuman1 <= 0)) {
			showGameOverMsg("Player 1 has lost!", "He has lost all of his stones.");
			return true;
		}else if(!field.movesPossible(options.colorPlayer2, setCountHuman2)){
			showGameOverMsg("Player 2 has lost!", "He could not make any further move.");
			return true;
		}else if ((field.getPositions(options.colorPlayer2).size() < 3 && setCountHuman2 <= 0)) {
			showGameOverMsg("Player 2 has lost!", "He has lost all of his stones.");
			return true;
		}
		return false;
	}
	
	private void changeCurrPlayerTo(Options.Color player){
		currPlayer = player;
		if(player.equals(options.colorPlayer1)){
			opponentPlayer = options.colorPlayer2;
		}else{
			opponentPlayer = options.colorPlayer1;
		}
	}

}
*/
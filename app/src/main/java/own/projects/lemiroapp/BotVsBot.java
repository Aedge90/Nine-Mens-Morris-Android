
package own.projects.lemiroapp;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.SystemClock;
import android.util.Log;


public class BotVsBot extends GameModeActivity{

    Player bot1;
    Player bot2;

    // TODO use two brains. Important as prevMove in Strategy
    // will be overwritten by the other bot at the moment
    
    @Override
    protected void init(){

        bot1 = new Player(options.colorPlayer1);
        bot2 = new Player(options.colorPlayer2);
        bot1.setOtherPlayer(bot2);
        bot2.setOtherPlayer(bot1);
        bot1.setDifficulty(options.difficulty1);
        bot2.setDifficulty(options.difficulty2);

		// Mill Settings are Set
		if (options.millMode == Options.MillMode.MILL5) {
            bot1.setSetCount(5);
            bot2.setSetCount(5);
			field = new Mill5();
			fieldLayout.setBackgroundResource(R.drawable.brett5);
		} else if (options.millMode == Options.MillMode.MILL7) {
            bot1.setSetCount(7);
            bot2.setSetCount(7);
			field = new Mill7();
			fieldLayout.setBackgroundResource(R.drawable.brett7);
		} else if (options.millMode == Options.MillMode.MILL9) {
            bot1.setSetCount(9);
            bot2.setSetCount(9);
			field = new Mill9();
			fieldLayout.setBackgroundResource(R.drawable.brett9);
		}
		
		brain = new Strategy(field , progressUpdater);
		
		gameThread = createGameThread();

	}

    public Thread createGameThread(){

    	Runnable game = new Runnable(){

    		@Override
    		public void run(){

    			try {
                    if(options.whoStarts.equals(bot2.getColor())){
                        setTextinUIThread(progressText, "Bot " + bot2.getColor().toString().toLowerCase() + " is computing!");
                        currPlayer = bot2;
                        botTurn(bot2);
                    }
    				while(true){

                        setTextinUIThread(progressText, "Bot " + bot1.getColor().toString().toLowerCase() + " is computing!");

                        currPlayer = bot1;
                        botTurn(bot1);

    					if(whoWon()){
    						break;
    					}

                        setTextinUIThread(progressText, "Bot " + bot2.getColor().toString().toLowerCase() + " is computing!");

                        currPlayer = bot2;
                        botTurn(bot2);

    					if(whoWon()){
    						break;
    					}

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

}

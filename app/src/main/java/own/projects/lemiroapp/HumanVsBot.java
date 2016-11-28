

package own.projects.lemiroapp;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;

public class HumanVsBot extends GameModeActivity{

    @Override
    protected void init(){

    	state = State.IGNORE;

        human = new Player(options.colorPlayer1);
        bot = new Player(options.colorPlayer2);
        human.setOtherPlayer(bot);
        bot.setOtherPlayer(human);
        bot.setDifficulty(options.difficulty1);
    	
		progressBar.setMax(options.difficulty1.ordinal() + 2);
		// Mill Settings are Set
		if (options.millMode == Options.MillMode.MILL5) {
			human.setSetCount(5);
			bot.setSetCount(5);
			field = new Mill5();
			fieldLayout.setBackgroundResource(R.drawable.brett5);
		} else if (options.millMode == Options.MillMode.MILL7) {
            human.setSetCount(7);
            bot.setSetCount(7);
			field = new Mill7();
			fieldLayout.setBackgroundResource(R.drawable.brett7);
		} else if (options.millMode == Options.MillMode.MILL9) {
            human.setSetCount(9);
            bot.setSetCount(9);
			field = new Mill9();
			fieldLayout.setBackgroundResource(R.drawable.brett9);
		}
		selected = false;
		brain = new Strategy(field, progressUpdater);
		
		setSectorListeners();
		
		gameThread = createGameThread();

	}

    void setSectorListeners() {

        for (int y = 0; y < LENGTH; y++) {
            for (int x = 0; x < LENGTH; x++) {
                if (!field.getColorAt(x, y).equals(Options.Color.INVALID)) {
                    fieldView.getPos(new Position(x, y)).setOnClickListener(
                            new  OnFieldClickListener(x,y));
                }
            }
        }
    }

    Thread createGameThread(){

    	Runnable game = new Runnable(){

    		@Override
    		public void run(){

    			try {
    				if(options.whoStarts.equals(bot.getColor())){
    					state = State.IGNORE;
    					setTextinUIThread(progressText, "Bot is Computing!");
                        currPlayer = bot;
    					botTurn(bot);
    				}else{
                        currPlayer = human;
                    }
    				while(true){

    					setTextinUIThread(progressText, R.string.player_turn);

                        currPlayer = currPlayer.getOtherPlayer();
    					humanTurn(human);

    					if(whoWon()){
    						break;
    					}

    					setTextinUIThread(progressText, "Bot is Computing!");

                        currPlayer = currPlayer.getOtherPlayer();
    					botTurn(bot);

    					if(whoWon()){
    						break;
    					}

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

}

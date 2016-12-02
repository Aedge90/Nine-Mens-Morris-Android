

package own.projects.lemiroapp;

import android.util.Log;


public class HumanVsBot extends GameModeActivity{

	Player human;
	Player bot;
	Strategy botBrain;

    @Override
    protected void init(){

    	state = State.IGNORE;

        human = new Player(options.colorPlayer1);
        bot = new Player(options.colorPlayer2);
        human.setOtherPlayer(bot);
        bot.setOtherPlayer(human);
        bot.setDifficulty(options.difficulty1);

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
		botBrain = new Strategy(field, bot, progressUpdater);
		
		setSectorListeners();
		
		gameThread = createGameThread();

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
    					botTurn(bot, botBrain);
    				}
    				while(true){

    					setTextinUIThread(progressText, R.string.player_turn);

                        currPlayer = human;
    					humanTurn(human);

    					if(whoWon()){
    						break;
    					}

    					setTextinUIThread(progressText, "Bot is Computing!");

                        currPlayer = bot;
    					botTurn(bot, botBrain);

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

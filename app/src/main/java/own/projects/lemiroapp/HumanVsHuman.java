
package own.projects.lemiroapp;

import android.util.Log;


public class HumanVsHuman extends GameModeActivity{

    Player human1;
    Player human2;
    
    @Override
    protected void init(){

    	state = State.IGNORE;

        human1 = new Player(options.colorPlayer1);
        human2 = new Player(options.colorPlayer2);
        human1.setOtherPlayer(human2);
        human2.setOtherPlayer(human1);

		progressBar.setMax(options.difficulty1.ordinal() + 2);
		// Mill Settings are Set
		if (options.millMode == Options.MillMode.MILL5) {
            human1.setSetCount(5);
            human2.setSetCount(5);
			field = new Mill5();
			fieldLayout.setBackgroundResource(R.drawable.brett5);
		} else if (options.millMode == Options.MillMode.MILL7) {
            human1.setSetCount(7);
            human2.setSetCount(7);
			field = new Mill7();
			fieldLayout.setBackgroundResource(R.drawable.brett7);
		} else if (options.millMode == Options.MillMode.MILL9) {
            human1.setSetCount(9);
            human2.setSetCount(9);
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
                    if(options.whoStarts.equals(human2.getColor())){
                        currPlayer = human2;
                        humanTurn(human2);
                    }
    				while(true){

    					setTextinUIThread(progressText, "Turn of Player " + human1.getColor().toString().toLowerCase());

                        currPlayer = human1;
                        humanTurn(human1);

    					if(whoWon()){
    						break;
    					}

                        setTextinUIThread(progressText, "Turn of Player " + human2.getColor().toString().toLowerCase());

                        currPlayer = human2;
                        humanTurn(human2);

                        if(whoWon()){
    						break;
    					}

    				}
    			} catch ( InterruptedException e ) {
    				Log.d("HumanVsHuman", "Interrupted!");
    				e.printStackTrace();
    				gameThread.interrupt();
    			}
    		}

    	};

		return new Thread(game);

	}

}


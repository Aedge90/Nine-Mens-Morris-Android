package com.github.aedge90.nmm;


import android.test.mock.MockContext;
import android.widget.ProgressBar;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class StrategyMemoryTestNonParameterized {

    @Test
    public void computeMoveShouldHaveSameEvaluationForAnyNumberOfThreads_Run5Times() throws InterruptedException {
        for (int i =0; i<5; i++){
            computeMoveShouldHaveSameEvaluationForAnyNumberOfThreads();
        }
    }

    //test for same evaluation, as resulting move may be different for different nThreads
    public void computeMoveShouldHaveSameEvaluationForAnyNumberOfThreads () throws InterruptedException {

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());

        int setCount = 7;

        GameBoard gameBoardMemory = new Mill7();
        Player playerBlackMemory = new Player(Options.Color.BLACK);
        Player playerWhiteMemory = new Player(Options.Color.WHITE);
        playerBlackMemory.setDifficulty(Options.Difficulties.HARDER);
        playerWhiteMemory.setDifficulty(Options.Difficulties.NORMAL);
        playerBlackMemory.setOtherPlayer(playerWhiteMemory);
        playerWhiteMemory.setOtherPlayer(playerBlackMemory);
        playerBlackMemory.setSetCount(setCount);
        playerWhiteMemory.setSetCount(setCount);
        Strategy strategyMemory = new Strategy(gameBoardMemory, updater);

        GameBoard gameBoardNoMemory = new Mill7();
        Player playerBlackNoMemory = new Player(Options.Color.BLACK);
        Player playerWhiteNoMemory = new Player(Options.Color.WHITE);
        playerBlackNoMemory.setDifficulty(Options.Difficulties.HARDER);
        playerWhiteNoMemory.setDifficulty(Options.Difficulties.NORMAL);
        playerBlackNoMemory.setOtherPlayer(playerWhiteNoMemory);
        playerWhiteNoMemory.setOtherPlayer(playerBlackNoMemory);
        playerBlackNoMemory.setSetCount(setCount);
        playerWhiteNoMemory.setSetCount(setCount);
        Strategy strategyNoMemory = new Strategy(gameBoardNoMemory, updater);

        Player currPlayerMemory = playerBlackMemory;
        Player currPlayerNoMemory = playerBlackNoMemory;

        for(int i = 0; i<80; i++){

            System.out.println(gameBoardMemory);
            System.out.println(gameBoardNoMemory);

            Move resultMoveMemory = strategyMemory.computeMove(currPlayerMemory);
            Move resultMoveNoMemory = strategyNoMemory.computeMove(currPlayerNoMemory);
            double resultEvalMemory = strategyMemory.getResultEvaluation();
            double resultEvalNoMemory = strategyNoMemory.getResultEvaluation();

            assertEquals("n moves: " + i, resultEvalMemory, resultEvalNoMemory);

            Move result = resultMoveNoMemory;
            // execute the same result move on both gameboards
            // important as there are different moves with same evaluation and we want every copy of the gameboard to be the same
            gameBoardMemory.executeCompleteTurn(result, currPlayerMemory);
            gameBoardNoMemory.executeCompleteTurn(result, currPlayerNoMemory);
                //also the strategies that decided for another move must have the same previous move
            currPlayerMemory.setPrevMove(result);
            currPlayerNoMemory.setPrevMove(result);
            strategyMemory.replaceLastMove(result);
            strategyNoMemory.replaceLastMove(result);

            if(!gameBoardNoMemory.getState(currPlayerNoMemory).equals(GameBoard.GameState.RUNNING)){
                if(!gameBoardNoMemory.getState(currPlayerNoMemory).equals(GameBoard.GameState.REMIS)) {
                    break;
                }
            }

            currPlayerMemory = currPlayerMemory.getOtherPlayer();
            currPlayerNoMemory = currPlayerNoMemory.getOtherPlayer();
        }

    }


}

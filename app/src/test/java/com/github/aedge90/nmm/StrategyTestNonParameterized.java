package com.github.aedge90.nmm;


import android.content.Context;
import android.widget.ProgressBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class StrategyTestNonParameterized {

    @Mock
    private Context mockContext;


    @Test
    //Run this test more than once to be sure, as it may fail with very low probability
    public void computeMoveShouldHaveSameEvaluationForAnyNumberOfThreads_Run10Times() throws InterruptedException {
        for (int i =0; i<10; i++){
            computeMoveShouldHaveSameEvaluationForAnyNumberOfThreads();
        }
    }

    //test for same evaluation, as resulting move may be different for different nThreads
    public void computeMoveShouldHaveSameEvaluationForAnyNumberOfThreads () throws InterruptedException {

        int nThreads = 12;

        ProgressBar progBar = new ProgressBar(mockContext);
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());

        GameBoard[] gameBoards = new Mill9[nThreads];
        Player[] mPlayerBlacks = new Player[nThreads];
        Player[] mPlayerWhites = new Player[nThreads];
        Strategy[] strategiesBlack = new Strategy[nThreads];
        Strategy[] strategiesWhite = new Strategy[nThreads];

        for(int j = 0; j < nThreads; j++) {
            gameBoards[j] = new Mill9();
            mPlayerBlacks[j] = new Player(Options.Color.BLACK);
            mPlayerWhites[j] = new Player(Options.Color.WHITE);
            mPlayerBlacks[j].setDifficulty(Options.Difficulties.HARDER);
            mPlayerWhites[j].setDifficulty(Options.Difficulties.HARDER);
            mPlayerBlacks[j].setOtherPlayer(mPlayerWhites[j]);
            mPlayerWhites[j].setOtherPlayer(mPlayerBlacks[j]);
            mPlayerBlacks[j].setSetCount(9);
            mPlayerWhites[j].setSetCount(9);
            strategiesBlack[j] = new Strategy(gameBoards[j], mPlayerBlacks[j], updater, j+1);
            strategiesWhite[j] = new Strategy(gameBoards[j], mPlayerWhites[j], updater, j+1);
        }

        //make some rounds and check if the results on all possible thread counts are the same
        for(int i = 0; i<40; i++){
            computeMoveShouldHaveSameEvaluationForAnyNumberOfThreads_Turn(i, gameBoards, mPlayerBlacks, strategiesBlack, nThreads);
            if(!gameBoards[0].getState(mPlayerBlacks[0]).equals(GameBoard.GameState.RUNNING)){
                if(!gameBoards[0].getState(mPlayerBlacks[0]).equals(GameBoard.GameState.REMIS)) {
                    break;
                }
            }
            computeMoveShouldHaveSameEvaluationForAnyNumberOfThreads_Turn(i, gameBoards, mPlayerWhites, strategiesWhite, nThreads);
            if(!gameBoards[0].getState(mPlayerWhites[0]).equals(GameBoard.GameState.RUNNING)){
                if(!gameBoards[0].getState(mPlayerWhites[0]).equals(GameBoard.GameState.REMIS)) {
                    break;
                }
            }
        }

    }


    public void computeMoveShouldHaveSameEvaluationForAnyNumberOfThreads_Turn (int round, GameBoard[] gameBoards, Player[] players, Strategy[] strategies, int nThreads) throws InterruptedException{

        double prevResultEval = 0;
        double resultEval = 0;
        Move resultMove = null;

        for(int j = 0; j < nThreads; j++) {
            resultMove = strategies[j].computeMove();
            resultEval = strategies[j].getResultEvaluation();
            if(j > 0) {
                assertEquals("round " + round + " strategy with: " + (j+1) + " threads" +
                        "; resultEval was different from previous one\n previous result: " +
                        prevResultEval + "\n result: " + resultEval, prevResultEval, resultEval);
            }
            prevResultEval = resultEval;
        }

        // execute only the last result move on every gameboard
        // important as there are different moves with same evaluation and we want every copy of the gameboard to be the same
        for(int j = 0; j < nThreads; j++) {
            gameBoards[j].executeCompleteTurn(resultMove, players[j]);
            //also the strategies that decided for another move must have the same result move
            strategies[j].setPreviousMove(resultMove);
        }
    }

}

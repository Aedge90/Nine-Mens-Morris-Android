package com.github.aedge90.nmm;

import android.support.annotation.VisibleForTesting;

import java.util.Collections;
import java.util.LinkedList;

public class Strategy {

    private int nThreads;
    private Thread[] threads;
    private StrategyRunnable[] runnables;
    private final ProgressUpdater up;

    private final GameBoard gameBoard;
    private final Player maxPlayer;

    double maxWertKickoff;
    Move resultMove;
    double resultEvaluation;
    LinkedList<Move> possibleMovesKickoff;
    int nPossibleMovesKickoff;

    Strategy(final GameBoard field, final Player player, final ProgressUpdater up) {
        this(field, player, up, 7);
    }

    @VisibleForTesting
    Strategy(final GameBoard field, final Player player, final ProgressUpdater up, final int nThreads) {
        this.gameBoard = field;
        this.maxPlayer = player;
        this.nThreads = nThreads;
        this.threads = new Thread[nThreads];
        this.runnables = new StrategyRunnable[nThreads];
        this.up = up;
        for (int i = 0; i < nThreads; i++){
            runnables[i] = new StrategyRunnable(gameBoard, maxPlayer, up, this, i);
        }
    }

    public Move computeMove() throws InterruptedException {

        // shuffle list, so we dont end up with the same moves every game
        possibleMovesKickoff = shuffleListOfPossMoves();

        nPossibleMovesKickoff = possibleMovesKickoff.size();

        up.setMax(nPossibleMovesKickoff);

        //not Double.MIN_VALUE as thats the number with the smallest magnitude....
        maxWertKickoff = -Double.MAX_VALUE;
        resultMove = null;
        //not StrategyRunnable.MIN as StrategyRunnable.MIN might be multiplied in evaluation and thus is not the minimal possible number
        resultEvaluation = -Double.MAX_VALUE;
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }
        for (int i = 0; i < nThreads; i++){
            threads[i].join();
        }

        for (int i = 0; i < nThreads; i++) {
           //runnables need to know which move was chosen
           runnables[i].setPreviousMove(resultMove);
        }

        up.reset();

        return resultMove;
    }

    @VisibleForTesting
    public LinkedList<Move> shuffleListOfPossMoves(){

        LinkedList<Move> shuffle = gameBoard.possibleMoves(maxPlayer);

        Collections.shuffle(shuffle);
        LinkedList<Move> result = new LinkedList<Move>();
        // but make sure the kill moves are at the beginning again, to improve performance
        for(Move m : shuffle) {
            if (m.getKill() != null) {
                result.addFirst(m);
            } else {
                result.addLast(m);
            }
        }
        return result;
    }

    @VisibleForTesting
    public double getResultEvaluation() {
        return resultEvaluation;
    }

    @VisibleForTesting
    public void setPreviousMove(Move move) {
        for (int i = 0; i < nThreads; i++) {
            //runnables need to know which move was chosen
            runnables[i].setPreviousMove(move);
        }
    }
    
}

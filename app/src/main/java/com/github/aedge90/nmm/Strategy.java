package com.github.aedge90.nmm;

import android.support.annotation.VisibleForTesting;

import java.util.Collections;
import java.util.LinkedList;

public class Strategy {

    private int nThreads;
    private Thread[] threads;
    private StrategyRunnable[] runnables;
    final ProgressUpdater up;

    private final GameBoard gameBoard;

    double maxWertKickoff;
    Move resultMove;
    double resultEvaluation;
    LinkedList<Move> possibleMovesKickoff;

    Strategy(final GameBoard field, final ProgressUpdater up) {
        this(field, up, 8);
    }

    @VisibleForTesting
    Strategy(final GameBoard field, final ProgressUpdater up, final int nThreads) {
        this.gameBoard = field;
        this.nThreads = nThreads;
        this.threads = new Thread[nThreads];
        this.runnables = new StrategyRunnable[nThreads];
        this.up = up;
    }

    public Move computeMove(Player maxPlayer) throws InterruptedException {

        // shuffle list, so we dont end up with the same moves every game
        possibleMovesKickoff = shuffleListOfPossMoves(gameBoard.possibleMoves(maxPlayer));

        up.setMax(possibleMovesKickoff.size());

        maxWertKickoff = Integer.MIN_VALUE;
        resultMove = null;
        //not StrategyRunnable.MIN as StrategyRunnable.MIN might be multiplied in evaluation and thus is not the minimal possible number
        resultEvaluation = Integer.MIN_VALUE;

        for (int i = 0; i < nThreads; i++){
            runnables[i] = new StrategyRunnable(gameBoard, maxPlayer, up, this, i);
        }
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }
        for (int i = 0; i < nThreads; i++){
            threads[i].join();
        }

        maxPlayer.setPrevMove(resultMove);

        up.reset();

        return resultMove;
    }

    @VisibleForTesting
    public LinkedList<Move> shuffleListOfPossMoves(LinkedList<Move> shuffle){

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
    
}

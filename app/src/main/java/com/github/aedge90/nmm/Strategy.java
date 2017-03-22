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
    private final Player maxPlayer;

    Strategy(final GameBoard field, final Player player, final ProgressUpdater up) {
        this(field, player, up, 8);
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
            runnables[i] = new StrategyRunnable(gameBoard, maxPlayer, up, i);
        }
    }

    public Move computeMove() throws InterruptedException {

        // shuffle list, so we dont end up with the same moves every game
        StrategyRunnable.possibleMovesKickoff = shuffleListOfPossMoves();

        up.setMax(StrategyRunnable.possibleMovesKickoff.size());

        StrategyRunnable.maxWertKickoff = Integer.MIN_VALUE;
        StrategyRunnable.resultMove = null;
        //not StrategyRunnable.MIN as StrategyRunnable.MIN might be multiplied in evaluation and thus is not the minimal possible number
        StrategyRunnable.resultEvaluation = Integer.MIN_VALUE;
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }
        for (int i = 0; i < nThreads; i++){
            threads[i].join();
        }

        for (int i = 0; i < nThreads; i++) {
           //runnables need to know which move was chosen
           runnables[i].setPreviousMove(StrategyRunnable.resultMove);
        }

        up.reset();

        return StrategyRunnable.resultMove;
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
        return StrategyRunnable.resultEvaluation;
    }

    @VisibleForTesting
    public void setPreviousMove(Move move) {
        for (int i = 0; i < nThreads; i++) {
            //runnables need to know which move was chosen
            runnables[i].setPreviousMove(move);
        }
    }

}

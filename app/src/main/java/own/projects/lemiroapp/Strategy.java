package own.projects.lemiroapp;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.LinkedList;

public class Strategy {

    private int nThreads;
    private Thread[] threads;
    private StrategyRunnable[] runnables;

    private final GameBoard gameBoard;
    private final Player maxPlayer;

    Strategy(final GameBoard field, final Player player, final ProgressUpdater up) {
        this(field, player, up, 4); //TODO decide number
    }

    @VisibleForTesting
    Strategy(final GameBoard field, final Player player, final ProgressUpdater up, final int nThreads) {
        this.gameBoard = field;
        this.maxPlayer = player;
        this.nThreads = nThreads;
        this.threads = new Thread[nThreads];
        this.runnables = new StrategyRunnable[nThreads];
        for (int i = 0; i < nThreads; i++){
            runnables[i] = new StrategyRunnable(gameBoard, maxPlayer, up, i, nThreads);
        }
    }

    public Move computeMove() throws InterruptedException {
        // TODO shuffle this, but keep kill moves at the beginning of the list
        runnables[0].updateState();
        StrategyRunnable.possibleMovesKickoff = runnables[0].possibleMoves(maxPlayer);
        StrategyRunnable.maxWertKickoff = Integer.MIN_VALUE;
        StrategyRunnable.resultMove = null;
        //not StrategyRunnable.MIN as StrategyRunnable.MIN might be multiplied in evaluation and thus is not the minimal possible number
        StrategyRunnable.resultEvaluation = Integer.MIN_VALUE;
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new Thread(runnables[i]);
            Log.i("Strategy", "thread " + i + "started");
            threads[i].start();
        }
        for (int i = 0; i < nThreads; i++){
            threads[i].join();
        }

        for (int i = 0; i < nThreads; i++) {
           //runnables need to know which move was chosen
           runnables[i].setPreviousMove(StrategyRunnable.resultMove);
        }

        return StrategyRunnable.resultMove;
    }
}

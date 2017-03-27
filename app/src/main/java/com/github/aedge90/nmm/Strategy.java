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
    MoveNode lastMove;
    double resultEvaluation;
    LinkedList<Move> possibleMovesKickoff;

    StrategyMemory memory;
    protected int nTotalEval = 0;
    protected int nSkippedEval = 0;

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
        this.memory = new StrategyMemory();
    }

    public Move computeMove(Player maxPlayer) throws InterruptedException {

        memory.addPossibleMoveNodesToRoot(maxPlayer, gameBoard);

        // shuffle list, so we dont end up with the same moves every game
        possibleMovesKickoff = shuffleListOfPossMoves(gameBoard.possibleMoves(maxPlayer));

        up.setMax(possibleMovesKickoff.size());

        //not Double.MIN_VALUE as thats the number with the smallest magnitude....
        maxWertKickoff = -Double.MAX_VALUE;
        lastMove = null;
        //not StrategyRunnable.MIN as StrategyRunnable.MIN might be multiplied in evaluation and thus is not the minimal possible number
        resultEvaluation = -Double.MAX_VALUE;

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

        maxPlayer.setPrevMove(lastMove.getMove());

        // set one of the possibleMoveNodesKickoff as the new root
        memory.setRoot(lastMove);

        up.reset();

        return lastMove.getMove();
    }

    // registers a child of the rootmove as the new rootmove.
    // Can not be undone as the old root is gone when a new one is set
    // this is called AFTER the move was done on the gameboard
    public void registerLastMove(Move move, Player player){
        gameBoard.reverseCompleteTurn(move, player);    //reverse the turn as the move was already done
        memory.addPossibleMoveNodesToRoot(player, gameBoard);
        gameBoard.executeCompleteTurn(move, player);
        for(MoveNode n : memory.getPossibleMoveNodesKickoff()){
            if (n.getMove().equals(move)) {
                memory.setRoot(n);
                lastMove = n;
                break;
            }
        }
    }

    @VisibleForTesting
    public void replaceLastMove(Move move){
        for(MoveNode n : memory.getPossibleMoveNodesKickoff()){
            if(n.getMove().equals(move)){
                lastMove = n;
                break;
            }
        }
        memory.setRoot(lastMove);
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

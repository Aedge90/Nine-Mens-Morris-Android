package com.github.aedge90.nmm;

import java.util.LinkedList;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

public class StrategyRunnable implements Runnable{

    //runnable will only work with local copies of the global gameboard and player objects
    private final GameBoard globalGameBoard;
    private GameBoard localGameBoard;
    private final Player globalMaxPlayer;
    private Player localMaxPlayer;

    private final Strategy strategy;
    private final ProgressUpdater up;
    //not Int.Max as the evaluation function would create overflows
    static final double MAX = (int) Math.pow(2,25);
    static final double MIN = - (int) Math.pow(2,25);
    private LinkedList<Move> movesToEvaluate;
    private Move prevMove;

    private final int threadNr;
    private int startDepth;

    StrategyRunnable(final GameBoard gameBoard, final Player maxPlayer, final ProgressUpdater up, Strategy strategy, final int threadNr) {
        this.globalGameBoard = gameBoard;
        this.globalMaxPlayer = maxPlayer;
        this.strategy = strategy;
        this.up = up;
        this.movesToEvaluate = new LinkedList<Move>();
        this.threadNr = threadNr;
    }

    public void updateState(){
        //copy these so every thread has its own one to avoid concurrency problems
        this.localGameBoard = globalGameBoard.getCopy();
        //copy BOTH!!! the maxPlayer and the other player
        this.localMaxPlayer = new Player(globalMaxPlayer);
        Player other = new Player(globalMaxPlayer.getOtherPlayer());
        other.setOtherPlayer(localMaxPlayer);
        this.localMaxPlayer.setOtherPlayer(other);
    }

    @Override
    public void run() {
        try {
            updateState();
            computeMove();
        }catch ( InterruptedException e ) {
            Log.d("computeMove Thread " + threadNr, "Interrupted!");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    // maximizing player has got to return higher values for better situations
    // minimizing player has got to return lower values the better his situation
    @VisibleForTesting
    double evaluation(Player player, boolean isAnyMovePossible, int depth) {
        
        double ret = 0;

        if (!isAnyMovePossible) {
            if(movesToEvaluate.size() > 1) {
                localGameBoard.reverseCompleteTurn(movesToEvaluate.getLast(), player.getOtherPlayer());
                //check if the loosing player, prevented a mill in his last move (which is the size-2th move)
                if (localGameBoard.preventedMill(movesToEvaluate.get(movesToEvaluate.size() - 2).getDest(), player)) {
                    //evaluate this better, as it looks stupid if he does not try to prevent one mill even if the other player
                    //can close another mill despite that
                    ret = 1;
                }
                localGameBoard.executeCompleteTurn(movesToEvaluate.getLast(), player.getOtherPlayer());
            }
            //worst case: player can not make any moves --> game is lost
            //or player has less than 3 pieces and has no pieces left to set --> game is lost
            if (player.equals(localMaxPlayer)) {
                //multiply with an number (which is >= 1) depending on depth
                //necessary as the evaluation has to be higher if the player can win after fewer moves
                //you may think depth is always 0 here, but it can be higher
                ret = MIN * (depth + 1) + ret;
            }else{
                ret = MAX * (depth + 1) - ret;
            }
            return ret;
        }

        //evaluate undoing a move, as its probably of no use. If it is, the other evaluation should overwrite this
        //this should break endless undoing and redoing of moves if all have the same evaluation so far
        if(prevMove != null){
            // closing and opening a mill should not be downgraded. Ignore setting phase
            if (prevMove.getKill() != null || movesToEvaluate.get(0).getKill() != null || prevMove.getSrc() == null){
                //do nothing
            }else if (prevMove.getSrc().equals(movesToEvaluate.get(0).getDest())
                    && prevMove.getDest().equals(movesToEvaluate.get(0).getSrc())) {
                ret -= 1;
            }
        }

        int i = 0;
        int nKillsMax = 0;
        int nKillsMin = 0;
        for(Move move : movesToEvaluate){
            if(i % 2 == 0) {    //even numbers are moves of the maximizing player
                if(move.getKill() != null){
                    nKillsMax++;
                }
            }else{
                if(move.getKill() != null){
                    nKillsMin++;
                }
            }
            i++;
        }

        localGameBoard.calculateStatsFor(player);

        int EvalPlayer =
                + 18* nKillsMax
                + 26* localGameBoard.nMills
                + 1 * localGameBoard.nBlockedEnemyPieces
                + 6 * localGameBoard.nPieces
                + 12* localGameBoard.nSinglePotMills
                + 7 * localGameBoard.nMultiPotMills;

        localGameBoard.calculateStatsFor(player.getOtherPlayer());

        int EvalOtherPlayer =
                + 18* nKillsMin
                + 26* localGameBoard.nMills
                + 1 * localGameBoard.nBlockedEnemyPieces
                + 6 * localGameBoard.nPieces
                + 12* localGameBoard.nSinglePotMills
                + 7 * localGameBoard.nMultiPotMills;

        return EvalPlayer - EvalOtherPlayer;

    }

    private void evaluateMove(Move move, Player player) {


    }

    private double max(int depth, double alpha, double beta, Player player, int nPrevPossMoves) throws InterruptedException {
        if(Thread.interrupted()){
            throw new InterruptedException("Computation of Bot " + player + " was interrupted!");
        }
        if (depth <= 0){
            return evaluation(player, localGameBoard.movesPossible(player), depth);
        }
        LinkedList<Move> moves = localGameBoard.possibleMoves(player);
        if (moves.size() == 0){
            return evaluation(player, false, depth);
        }
        depth = lowerDepth (depth, nPrevPossMoves, moves.size());
        double maxWert = alpha;
        for (Move z : moves) {
            localGameBoard.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
            evaluateMove(z, player);
            double wert = min(depth-1, maxWert, beta, player.getOtherPlayer(), moves.size());
            movesToEvaluate.removeLast();
            localGameBoard.reverseCompleteTurn(z, player);
            if (wert > maxWert) {
                maxWert = wert;
                if (maxWert >= beta) {
                    break;
                }
            }
        }
        return maxWert;
    }

    //same as max, but slightly modified to distribute work among threads
    private void maxKickoff(int depth, Player player) throws InterruptedException{

        while(true) {

            Move z;
            synchronized (strategy) {
                if(strategy.possibleMovesKickoff.size() > 0) {
                    z = strategy.possibleMovesKickoff.removeFirst();
                }else{
                    break;
                }
            }

            localGameBoard.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
            evaluateMove(z, player);
            double wert = min(depth - 1, strategy.maxWertKickoff, Double.MAX_VALUE, player.getOtherPlayer(), strategy.nPossibleMovesKickoff);
            movesToEvaluate.removeLast();
            localGameBoard.reverseCompleteTurn(z, player);

            synchronized (strategy) {
                if (wert > strategy.maxWertKickoff) {
                    strategy.maxWertKickoff = wert;
                    strategy.resultMove = z;
                    strategy.resultEvaluation = wert;
                }
            }

            up.increment();
        }
    }

    private double min(int depth, double alpha, double beta, Player player, int nPrevPossMoves) throws InterruptedException {
        if(Thread.interrupted()){
            throw new InterruptedException("Computation of Bot " + player + " was interrupted!");
        }
        if (depth <= 0){
            return evaluation(player, localGameBoard.movesPossible(player), depth);
        }
        LinkedList<Move> moves = localGameBoard.possibleMoves(player);
        if (moves.size() == 0){
            return evaluation(player, false, depth);
        }
        depth = lowerDepth (depth, nPrevPossMoves, moves.size());
        double minWert = beta;
        for (Move z : moves) {
            localGameBoard.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
            evaluateMove(z, player);
            double wert = max(depth-1, alpha, minWert, player.getOtherPlayer(), moves.size());
            movesToEvaluate.removeLast();
            localGameBoard.reverseCompleteTurn(z, player);
            if (wert < minWert) {
                minWert = wert;
                if (minWert <= alpha){ 
                    break;
                }
            }
        }
        return minWert;

    }

    private void computeMove() throws InterruptedException {
                
        startDepth = localMaxPlayer.getDifficulty().ordinal() + 1;

        maxKickoff(startDepth, localMaxPlayer);
    }

    private int lowerDepth (int depth, int nPrevPossMoves, int nPossMoves) {
        if(depth <= (startDepth - 3)) {             //this ensures a minimum depth of 4 has been reached
            if (nPrevPossMoves * nPossMoves > 20*20) {
                return Math.min(depth, 1);
            }
            if (nPrevPossMoves * nPossMoves > 14*14) {
                return Math.min(depth, 2);
            }
            if (nPrevPossMoves * nPossMoves > 8*8) {
                return Math.min(depth, 3);
            }
        }
        return depth;
    }

    public void setPreviousMove(Move prevMove) {
       this.prevMove = prevMove;
    }

}

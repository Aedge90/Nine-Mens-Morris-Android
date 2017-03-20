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

    private final ProgressUpdater up;
    //not Int.Max as the evaluation function would create overflows
    static final int MAX = (int) Math.pow(2,25);
    static final int MIN = - (int) Math.pow(2,25);
    private LinkedList<Move> movesToEvaluate;
    private Move prevMove;

    private final int threadNr;
    static int maxWertKickoff;
    static Move resultMove;
    static int resultEvaluation;
    static LinkedList<Move> possibleMovesKickoff;

    StrategyRunnable(final GameBoard gameBoard, final Player maxPlayer, final ProgressUpdater up, final int threadNr) {
        this.globalGameBoard = gameBoard;
        this.globalMaxPlayer = maxPlayer;
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
    
    private void addnonJumpMoves(LinkedList<Move> moves, Player player){
        for (Position p : localGameBoard.getPositions(player.getColor())) {
            if (localGameBoard.moveUp(p) != null) {
                addpossibleKillstoMove(moves, localGameBoard.moveUp(p), player);
            }
            if (localGameBoard.moveDown(p) != null) {
                addpossibleKillstoMove(moves, localGameBoard.moveDown(p), player);
            }
            if (localGameBoard.moveRight(p) != null) {
                addpossibleKillstoMove(moves, localGameBoard.moveRight(p), player);
            }
            if (localGameBoard.moveLeft(p) != null) {
                addpossibleKillstoMove(moves, localGameBoard.moveLeft(p), player);
            }
        }
    }

    private void addJumpMoves(LinkedList<Move> moves, Player player){
        for (Position all : localGameBoard.getAllValidPositions()) {
            if (localGameBoard.getColorAt(all).equals(Options.Color.NOTHING)) {
                for (Position own: localGameBoard.getPositions(player.getColor())) {
                    addpossibleKillstoMove(moves, new Move(all, own, null), player);
                }
            }
        }
    }
    
    private void addSetMoves(LinkedList<Move> moves, Player player){
        for (Position all : localGameBoard.getAllValidPositions()) {
            if (localGameBoard.getColorAt(all).equals(Options.Color.NOTHING)) {
                addpossibleKillstoMove(moves, new Move(all, null, null), player);
            }
        }
    }

    @VisibleForTesting
    void addpossibleKillstoMove(LinkedList<Move> possibleMovessoFar, Move move, Player player){
            boolean inMill = false;
            //this is important so there are no mills wrongly detected when move contains a source and
            //destination that is inside the same mill
            localGameBoard.executeSetOrMovePhase(move, player);
            inMill = localGameBoard.isInMill(move.getDest(), player.getColor());
            localGameBoard.reverseCompleteTurn(move, player);
            //player has a mill after doing this move --> he can kill a piece of the opponent
            if(inMill){
                int added = 0;
                for (Position kill : localGameBoard.getPositions(player.getOtherPlayer().getColor())) {
                    if(!localGameBoard.isInMill(kill, player.getOtherPlayer().getColor())){
                        Move killMove = new Move(move.getDest(), move.getSrc(), kill);
                        // using add first is important, so the kill moves will be at the beginning of the list
                        // by that its more likely that the alpha beta algorithms does more cutoffs
                        possibleMovessoFar.addFirst(killMove);
                        added++;
                    }
                }
                //no pieces to kill because all are in a mill --> do it again but now add all pieces
                //as you are allowed to kill if all pieces are part of a mill
                if(added == 0){
                    for (Position kill2 : localGameBoard.getPositions(player.getOtherPlayer().getColor())) {
                        Move killMove = new Move(move.getDest(), move.getSrc(), kill2);
                        possibleMovessoFar.addFirst(killMove);
                    }
                }
            }else{
                possibleMovessoFar.add(move);
            }
    }

    //returns a list of moves that the player is able to do
    LinkedList<Move> possibleMoves(Player player) {
        LinkedList<Move> poss = new LinkedList<Move>();
        int nPositions = localGameBoard.getPositions(player.getColor()).size();
        //do not compute possible moves if the player has lost, otherwise it breaks the evaluation
        //as a state AFTER loosing would be evaluated instead of the final state after the final kill
        if(nPositions < 3 && player.getSetCount() <= 0){
            return poss;
        }
        if(player.getSetCount() > 0){
            addSetMoves(poss, player);
        }else{
            boolean jump = false;
            if (nPositions <= 3){
                jump = true;
            }
            if (!jump) {
                addnonJumpMoves(poss, player);
            } else {
                addJumpMoves(poss, player);
            }
        }
        return poss;
    }

    // maximizing player has got to return higher values for better situations
    // minimizing player has got to return lower values the better his situation
    @VisibleForTesting
    int evaluation(Player player, LinkedList<Move> moves, int depth) {

        int ret = 0;

        if (moves.size() == 0) {
            if(movesToEvaluate.size() > 1) {
                localGameBoard.reverseCompleteTurn(movesToEvaluate.getLast(), player.getOtherPlayer());
                //check if the loosing player, prevented a mill in his last move (which is the size-2th move)
                if (localGameBoard.isInMill(movesToEvaluate.get(movesToEvaluate.size() - 2).getDest(), player.getOtherPlayer().getColor())) {
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

        //evaluate how often the players can kill, and prefer kills that are in the near future
        int i = 0;
        for(Move move : movesToEvaluate){
            if(i % 2 == 0) {    //even numbers are moves of the maximizing player
                ret += move.getEvaluation();
            }else{
                ret -= move.getEvaluation();
            }
            i++;
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

        return ret;

    }

    private void evaluateMove(Move move, Player player) {
        int eval = 0;
        int killweight = (int) (Math.pow(2, 24) / Math.pow(2, movesToEvaluate.size()));
        int weight = (int) (Math.pow(2, 12) / Math.pow(2, movesToEvaluate.size()));
        if (move.getKill() != null) {
            // next weight will be half the weight
            // this has to be done so players wont do the same move over and over again
            // as they would not choose a path in which they kill but the other player kills in a
            // distant future (which is seen in higher difficulties, when he can make jump moves)
            // thus lowers the evaluation drastically and the game is stalled
            // also this prefers kills in the near future, so they are done now and not later
            // as could be the case if all were weighted equally
            eval += killweight;
        }else if(move.getSrc() != null && movesToEvaluate.size() == 1){
            // evaluate opening a mill in the first move better, so the bot will open mills.
            // There is no need to check if the mill can be opened safely (without the enemy blocking it in the next move)
            // as even depth 2 bots will already NOT open a mill as preventedMill will be true for the next move
            localGameBoard.reverseCompleteTurn(move, player);
            if(localGameBoard.isInMill(move.getSrc(), player.getColor())){
                eval += 1;
            }
            localGameBoard.executeCompleteTurn(move, player);
        }else if(localGameBoard.preventedMill(move.getDest(), player)){
            eval += weight;
        }else if(player.getOtherPlayer().getSetCount() >= 1 &&
                localGameBoard.isInNPotentialMills(move.getDest(), player.getOtherPlayer().getColor()) > 0){
            // check if a potential mill of the other player is prevented. This is necessary, as if the enemy
            // can form two potential mills in the next move, there will always be a negative evaluation
            // and any move can be chosen. This way a move will be chosen that prevents one of the two
            eval += weight;
        }else if(player.getSetCount() >= 1 &&   //only makes sense if the player can actually close the mill
                localGameBoard.isInNPotentialMills(move.getDest(), player.getColor()) > 0){
            // evaluate having a potential future mill better, as otherwise the bot will just randomly place pieces
            // this causes the bot to be weaker especially on bigger gameboards as he does not really try to build a mill.
            eval += weight;
        }
        move.setEvaluation(eval);
    }

    private int max(int depth, int alpha, int beta, Player player) throws InterruptedException {
        if(Thread.interrupted()){
            throw new InterruptedException("Computation of Bot " + player + " was interrupted!");
        }
        LinkedList<Move> moves = possibleMoves(player);
        //end reached or no more moves available, maybe because he is trapped or because he lost
        if (depth == 0 || moves.size() == 0){
            int bewertung = evaluation(player, moves, depth);
            return bewertung;
        }
        int maxWert = alpha;
        for (Move z : moves) {
            localGameBoard.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
            evaluateMove(z, player);
            int wert = min(depth-1, maxWert, beta, player.getOtherPlayer());
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
            synchronized (StrategyRunnable.class) {
                if(possibleMovesKickoff.size() > 0) {
                    z = possibleMovesKickoff.removeFirst();
                }else{
                    break;
                }
            }

            localGameBoard.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
            evaluateMove(z, player);
            int wert = min(depth - 1, maxWertKickoff, Integer.MAX_VALUE, player.getOtherPlayer());
            movesToEvaluate.removeLast();
            localGameBoard.reverseCompleteTurn(z, player);

            synchronized (StrategyRunnable.class) {
                if (wert > maxWertKickoff) {
                    maxWertKickoff = wert;
                    resultMove = z;
                    resultEvaluation = wert;
                }
            }

            up.increment();
        }
    }

    private int min(int depth, int alpha, int beta, Player player) throws InterruptedException {
        if(Thread.interrupted()){
            throw new InterruptedException("Computation of Bot " + player + " was interrupted!");
        }
        LinkedList<Move> moves = possibleMoves(player);
        if (depth == 0 || moves.size() == 0){
            int bewertung = evaluation(player, moves, depth);
            return bewertung;
        }
        int minWert = beta;
        for (Move z : moves) {
            localGameBoard.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
            evaluateMove(z, player);
            int wert = max(depth-1, alpha, minWert, player.getOtherPlayer());
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
                
        int startDepth = localMaxPlayer.getDifficulty().ordinal() + 2;

        startDepth = lowerDepthOnGameStart(startDepth);

        startDepth = lowerDepthOnGameEnd(startDepth);

        maxKickoff(startDepth, localMaxPlayer);
    }

    private int lowerDepthOnGameStart (int startDepth) {
        if(localMaxPlayer.getSetCount() == 0){
            return startDepth;
        }
        int nPositions = localGameBoard.getPositions(localMaxPlayer.getColor()).size();
        if(nPositions > 3){
            return startDepth;
        }else if(nPositions == 0){
            return Math.min(startDepth, 3);
        }else if (nPositions == 1){
            //4 is minimun, as otherwise high difficulty player will look dumb if they dont prevent mills
            return Math.min(startDepth, 4);
        }else if (nPositions == 2){
            return Math.min(startDepth, 5);
        }else if (nPositions == 3){
            return Math.min(startDepth, 6);
        }
        return startDepth;
    }

    private int lowerDepthOnGameEnd (int startDepth) {
        if(localMaxPlayer.getSetCount() > 0){
            return startDepth;
        }
        int nPlayerPos = localGameBoard.getPositions(localMaxPlayer.getColor()).size() +
                localGameBoard.getPositions(localMaxPlayer.getOtherPlayer().getColor()).size();
        if(nPlayerPos == 7 || nPlayerPos == 8){
            return Math.min(startDepth, 5);
        }
        if(nPlayerPos == 6){
            return Math.min(startDepth, 4);
        }
        return startDepth;
    }


    public void setPreviousMove(Move prevMove) {
       this.prevMove = prevMove;
    }

}

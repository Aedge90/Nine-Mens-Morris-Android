package com.github.aedge90.nmm;


import java.util.LinkedList;

public class StrategyMemory {

    MoveNode root;   //node of the last move
    MoveNode[] possibleMoveNodesKickoff;

    //just for debugging
    protected int nTotalEval = 0;
    protected int nSkippedEval = 0;

    public StrategyMemory() {
        this.root = new MoveNode(null);
    }

    public MoveNode[] getPossibleMoveNodesKickoff() {
        return possibleMoveNodesKickoff;
    }

    public MoveNode getRoot() {
        return root;
    }

    public void setRoot(MoveNode rootMoveNode) {
        root = rootMoveNode;
    }


    public void addPossibleMoveNodesToRoot(Player player, GameBoard gameBoard){
        possibleMoveNodesKickoff = addPossibleMovesTo(root, player, gameBoard);
    }

    public MoveNode[] addPossibleMovesTo(MoveNode parent, Player player, GameBoard gameBoard) {
        if (parent.getChildren() == null) {
            //first time we add children
            LinkedList<Move> moves = gameBoard.possibleMoves(player);
            parent.addChildren(moves);
        }
        //if getChildren() is not null there may still bei zero children in the array, which is ok if no moves are left
        return parent.getChildren();
    }

    public boolean hasEvaluation (Move move) {
        if(move.getEvaluation() != -Double.MAX_VALUE){
            //System.out.println("already evaluated: " + move + ": " + move.getEvaluation());
            logSkipped();
            return true;     //move was already evaluated
        }
        logTotal();
        return false;
    }

    public void logSkipped () {}

    public void logTotal () {}

}

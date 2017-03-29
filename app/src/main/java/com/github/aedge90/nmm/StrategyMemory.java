package com.github.aedge90.nmm;


import java.util.LinkedList;

public class StrategyMemory {

    MoveNode root;   //node of the last move
    MoveNode[] possibleMoveNodesKickoff;


    public StrategyMemory() {
        this.root = new MoveNode(null, null, null);
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
            addExecutedPossMoveCalculation();
            //first time we add children
            LinkedList<Move> moves = gameBoard.possibleMoves(player);
            parent.addChildren(moves);
        }else {
            addSkippedPossMoveCalculation();
        }
        //if getChildren() is not null there may still bei zero children in the array, which is ok if no moves are left
        return parent.getChildren();
    }

    public boolean hasEvaluation (Move move) {
        if(move.getEvaluation() != -Double.MAX_VALUE){
            //System.out.println("already evaluated: " + move + ": " + move.getEvaluation());
            addSkippedEvaluation();
            return true;     //move was already evaluated
        }else {
            addExecutedEvaluation();
            return false;
        }
    }

    public void addSkippedEvaluation() {}

    public void addExecutedEvaluation() {}

    public void addSkippedPossMoveCalculation() {}

    public void addExecutedPossMoveCalculation() {}
}

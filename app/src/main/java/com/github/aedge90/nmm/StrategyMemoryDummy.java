package com.github.aedge90.nmm;

import java.util.LinkedList;


// this class is solely used for testing in order to disable memory functionality

public class StrategyMemoryDummy extends StrategyMemory {

    @Override
    public MoveNode[] addPossibleMovesTo(MoveNode parent, Player player, GameBoard gameBoard) {
        addExecutedPossMoveCalculation();
        //first time we add children
        LinkedList<Move> moves = gameBoard.possibleMoves(player);
        parent.removeChildren();
        parent.addChildren(moves);
        return parent.getChildren();
    }

    @Override
    public boolean hasEvaluation (Move move) {
        return false;
    }

}

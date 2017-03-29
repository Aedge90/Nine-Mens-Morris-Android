package com.github.aedge90.nmm;

import org.junit.Test;

import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;

public class MoveNodeTest {

    @Test
    public void getDepthShouldBe4(){

        Player playerBlack = new Player(Options.Color.BLACK);
        Player playerWhite = new Player(Options.Color.WHITE);
        playerBlack.setOtherPlayer(playerWhite);
        playerWhite.setOtherPlayer(playerBlack);
        playerBlack.setSetCount(5);
        playerWhite.setSetCount(5);

        GameBoard gameBoard = new Mill5();

        MoveNode root = new MoveNode(null, null, null);
        LinkedList<Move> moves = gameBoard.possibleMoves(playerBlack);
        root.addChildren(moves);

        root.getChildren()[2].addChildren(moves);

        root.getChildren()[2].getChildren()[6].addChildren(moves);

        assertEquals(4, root.getDepth());

        LinkedList<Move> emptyList = new LinkedList<>();
        root.getChildren()[2].getChildren()[6].getChildren()[0].addChildren(emptyList);

        //shoud still be 4
        assertEquals(4, root.getDepth());
    }

}

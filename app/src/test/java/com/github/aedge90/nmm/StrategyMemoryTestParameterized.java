package com.github.aedge90.nmm;


import android.test.mock.MockContext;
import android.widget.ProgressBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class StrategyMemoryTestParameterized {

    private Player mPlayer1;
    private Player mPlayer2;

    // name attribute is optional, provide an unique name for test
    // multiple parameters, uses Collection<Object[]>
    @Parameterized.Parameters(name = "P1: {0}, P2: {1}")
    public static Collection<Object[] > data() {

        LinkedList<Object[]> player1andnThreadsList = new LinkedList<>();
        for (Options.Difficulties playerBlackDiff : Options.Difficulties.values()) {

            //test every possible configuration of players with different difficulties
            for (Options.Difficulties playerWhiteDiff : Options.Difficulties.values()) {

                Player playerBlack = new Player(Options.Color.BLACK);
                playerBlack.setDifficulty(playerBlackDiff);
                playerBlack.setSetCount(5);
                Player playerWhite = new Player(Options.Color.WHITE);
                playerWhite.setDifficulty(playerWhiteDiff);
                playerWhite.setSetCount(5);

                playerWhite.setOtherPlayer(playerBlack);
                playerBlack.setOtherPlayer(playerWhite);

                player1andnThreadsList.add(new Object[]{playerBlack, playerWhite});
                player1andnThreadsList.add(new Object[]{playerWhite, playerBlack});
            }

        }

        return player1andnThreadsList;
    }

    // Inject paremeters via constructor, constructor is called before each test
    public StrategyMemoryTestParameterized(Player player1, Player player2){

        mPlayer1 = player1;
        mPlayer2 = player2;

    }


    @Test
    public void stategyShouldHaveSameDepthAfterEachComputeMove() throws InterruptedException {

        GameBoard gameBoard = new Mill5();

        mPlayer1.setSetCount(5);
        mPlayer2.setSetCount(5);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, updater);

        for (int i = 0; i < 50; i++) {

            //System.out.println("round " + i + "\n\n" + gameBoard);

            Move result1 = strategy.computeMove(mPlayer1);
            gameBoard.executeCompleteTurn(result1, mPlayer1);
            if (!gameBoard.getState(mPlayer1).equals(GameBoard.GameState.RUNNING)) {
                if(!gameBoard.getState(mPlayer1).equals(GameBoard.GameState.REMIS)) {
                    break;
                }
            }
            // the height of the tree should be 1 (the root move = last move) + startDepth
            // or if the other player has a higher difficulty he explored the tree deeper already
            // so it should then be his startDepth (but not his root move as the root was set one deeper by the weaker player)
            int expectedDepth1 = Math.max(mPlayer1.getDifficulty().ordinal() + 2 + 1, mPlayer2.getDifficulty().ordinal() + 2);
            if(i > 1) {
                assertEquals("Round " + i, expectedDepth1, strategy.root.getDepth());
            }

            //System.out.println(gameBoard);

            Move result2 = strategy.computeMove(mPlayer2);
            gameBoard.executeCompleteTurn(result2, mPlayer2);
            if (!gameBoard.getState(mPlayer2).equals(GameBoard.GameState.RUNNING)) {
                if(!gameBoard.getState(mPlayer2).equals(GameBoard.GameState.REMIS)) {
                    break;
                }
            }
            int expectedDepth2 = Math.max(mPlayer2.getDifficulty().ordinal() + 2 + 1, mPlayer1.getDifficulty().ordinal() + 2);
            assertEquals("Round " + i, expectedDepth2, strategy.root.getDepth());

        }

    }


}

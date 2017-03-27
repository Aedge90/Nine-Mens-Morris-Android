package com.github.aedge90.nmm;


import android.test.mock.MockContext;
import android.widget.ProgressBar;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

@RunWith(value = Parameterized.class)
public class StrategyMemoryTestParameterized {

    private final Options.Color P1;
    private final Options.Color P2;
    private final Options.Color N = Options.Color.NOTHING;
    private final Options.Color I = Options.Color.INVALID;

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

        P1 = mPlayer1.getColor();
        P2 = mPlayer2.getColor();
    }


    @Test
    public void stategyShouldHaveSameDepthAfterEachComputeMove() throws InterruptedException {

        GameBoard gameBoard = new Mill9();

        mPlayer1.setSetCount(9);
        mPlayer2.setSetCount(9);

        int checkStart = 4;
        int checkEnd = 7;

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, updater);

        for (int i = 0; i < 50; i++) {

            Move result1 = strategy.computeMove(mPlayer1);
            gameBoard.executeCompleteTurn(result1, mPlayer1);
            if (!gameBoard.getState(mPlayer1).equals(GameBoard.GameState.RUNNING)) {
                if(!gameBoard.getState(mPlayer1).equals(GameBoard.GameState.REMIS)) {
                    break;
                }
            }

            //System.out.println("round: " + i + "\n" + gameBoard);

            // the height of the tree should be 1 (the root move = last move) + startDepth
            // or if the other player has a higher difficulty he explored the tree deeper already
            // so it should then be his startDepth (but not his root move as the root was set one deeper by the weaker player)
            int expectedDepth1 = Math.max(mPlayer1.getDifficulty().ordinal() + 2 + 1, mPlayer2.getDifficulty().ordinal() + 2);
            //do not check depth in the beginning as its lowered intentionally in StrategyRunnable
            if(i > checkStart) {
                if(i < checkEnd){
                    // workaround to at least check two times if root is same as expected
                    assertEquals("Round " + i, expectedDepth1, strategy.memory.getRoot().getDepth());
                }else{
                    // it could be that there is no move left and one player wins, so a lower depth would be ok
                    assertTrue("Round " + i, expectedDepth1 >= strategy.memory.getRoot().getDepth());
                }
            }

            Move result2 = strategy.computeMove(mPlayer2);
            gameBoard.executeCompleteTurn(result2, mPlayer2);
            if (!gameBoard.getState(mPlayer2).equals(GameBoard.GameState.RUNNING)) {
                if(!gameBoard.getState(mPlayer2).equals(GameBoard.GameState.REMIS)) {
                    break;
                }
            }

            //System.out.println("round: " + i + "\n" + gameBoard);

            int expectedDepth2 = Math.max(mPlayer2.getDifficulty().ordinal() + 2 + 1, mPlayer1.getDifficulty().ordinal() + 2);

            if(i > checkStart) {
                if(i < checkEnd){
                    assertEquals("Round " + i, expectedDepth2, strategy.memory.getRoot().getDepth());
                }else{
                    assertTrue("Round " + i, expectedDepth2 >= strategy.memory.getRoot().getDepth());
                }
            }

        }

    }

    @After
    public void tearDown()
    {
        this.mPlayer1 = null;
        this.mPlayer2 = null;
    }


    @Test
    public void strategyRootMoveShouldHave36Children() throws InterruptedException {

        Options.Color[][] mill9 =
            {{N , I , I , P2, I , I , P1},
            { I , N , I , N , I , N , I },
            { I , I , P2, P2, P1, I , I },
            { N , P2, P2, I , P2, N , N },
            { I , I , P2, P1, N , I , I },
            { I , N , I , P2, I , N , I },
            { N , I , I , P2, I , I , N }};

        GameBoard gameBoard = new Mill9(mill9);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, updater);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result2 = strategy.computeMove(mPlayer2);
        gameBoard.executeCompleteTurn(result2, mPlayer2);

        //player 2 should have initialized the tree and set his last move as root
        // so now the children should be the possible moves of P1! which should be 36 in total
        assertEquals(3*12, strategy.memory.getRoot().getChildren().length);
    }


    @Test
    public void strategyShouldSkipEvaluations() throws InterruptedException {

        Options.Color[][] mill9 =
                {{N , I , I , P2, I , I , P1},
                { I , P2, I , N , I , N , I },
                { I , I , P2, P2, N , I , I },
                { N , P2, N , I , N , P1, N },
                { I , I , P2, P2, N , I , I },
                { I , P2, I , N , I , N , I },
                { P1, I , I , P2, I , I , P1}};

        GameBoard gameBoard = new Mill9(mill9);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, updater, 8, new StrategyMemoryLogger());

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result2 = strategy.computeMove(mPlayer2);
        gameBoard.executeCompleteTurn(result2, mPlayer2);

        //System.out.println("chosen move: " + result2);

        //player 2 should have initialized the tree and set his last move as root

        // player 1 should compute his move and use values that were computed already
        Move result1 = strategy.computeMove(mPlayer1);
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        assertTrue(strategy.memory.nSkippedEval > 0);

        System.out.println("total evaluations: " + strategy.memory.nTotalEval);
        System.out.println("skipped evaluations: " + strategy.memory.nSkippedEval);
    }

}

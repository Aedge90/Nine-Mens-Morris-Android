package com.github.aedge90.nmm;


import android.test.mock.MockContext;
import android.widget.ProgressBar;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeTrue;


@RunWith(value = Parameterized.class)
public class StrategyTestParameterized {

    private final Options.Color P1;
    private final Options.Color P2;
    private final Options.Color N = Options.Color.NOTHING;
    private final Options.Color I = Options.Color.INVALID;

    private Player mPlayer1;
    private Player mPlayer2;
    private int nThreads;

    // name attribute is optional, provide an unique name for test
    // multiple parameters, uses Collection<Object[]>
    @Parameterized.Parameters(name = "P1 is {0}, nThreads: {1}")
    public static Collection<Object[] > data() {

        LinkedList<Object[]> player1andnThreadsList = new LinkedList<>();
        for (int i = 0; i < Options.Difficulties.values().length; i++) {

            Player playerBlack = new Player(Options.Color.BLACK);
            Player playerWhite = new Player(Options.Color.WHITE);
            playerBlack.setDifficulty(Options.Difficulties.values()[i]);
            playerWhite.setDifficulty(Options.Difficulties.values()[i]);
            playerBlack.setSetCount(5);
            playerWhite.setSetCount(5);

            int nrThreads = 1;
            player1andnThreadsList.add(new Object[]{playerBlack, nrThreads});
            player1andnThreadsList.add(new Object[]{playerWhite, nrThreads});
            nrThreads = 2;
            player1andnThreadsList.add(new Object[]{playerBlack, nrThreads});
            player1andnThreadsList.add(new Object[]{playerWhite, nrThreads});
            nrThreads = 3;
            player1andnThreadsList.add(new Object[]{playerBlack, nrThreads});
            player1andnThreadsList.add(new Object[]{playerWhite, nrThreads});
            nrThreads = 5;
            player1andnThreadsList.add(new Object[]{playerBlack, nrThreads});
            player1andnThreadsList.add(new Object[]{playerWhite, nrThreads});
            nrThreads = 8;
            player1andnThreadsList.add(new Object[]{playerBlack, nrThreads});
            player1andnThreadsList.add(new Object[]{playerWhite, nrThreads});

        }

        return player1andnThreadsList;
    }

    // Inject paremeters via constructor, constructor is called before each test
    public StrategyTestParameterized(Player player1, int nThreads){

        mPlayer1 = player1;
        if(mPlayer1.getColor().equals(Options.Color.BLACK)){
            mPlayer2 = new Player(Options.Color.WHITE);
        } else {
            mPlayer2 = new Player(Options.Color.BLACK);
        }
        mPlayer2.setDifficulty(mPlayer1.getDifficulty());
        mPlayer2.setSetCount(mPlayer1.getSetCount());
        mPlayer2.setOtherPlayer(mPlayer1);
        mPlayer1.setOtherPlayer(mPlayer2);

        P1 = mPlayer1.getColor();
        P2 = mPlayer2.getColor();

        this.nThreads = nThreads;
    }


    @Test
    public void computeMoveShouldNotCloseMillButFormTwoPotentialMills () throws InterruptedException {

        // bots starting with depth 5 should see that they will kill more if they form two potential mills first
        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.HARD.ordinal());

        Options.Color[][] mill9 =
                {{N , I , I , N , I , I , N },
                { I , N , I , P1, I , N , I },
                { I , I , N , P2, N , I , I },
                { P1, N , N , I , N , P1, N },
                { I , I , P2, N , N , I , I },
                { I , N , I , N , I , N , I },
                { P1, I , I , P2, I , I , P2}};

        GameBoard gameBoard = new Mill9(mill9);

        mPlayer1.setSetCount(5);
        mPlayer2.setSetCount(5);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());

        Strategy strategyP1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);
        Strategy strategyP2 = new Strategy(gameBoard, mPlayer2, updater, nThreads);

        Move result1 = strategyP1.computeMove();
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        assertEquals(new Position(5,1), result1.getDest());
    }


    @Test
    public void computeMoveShouldNotCloseMillAsHeWillLooseThen () throws InterruptedException {

        // bots starting with depth 4 should see that closing their mill will definitely result in loosing
        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.ADVANCED.ordinal());

        Options.Color[][] mill5 =
                {{N , I , I , N , I , I , N },
                { I , I , I , I , I , I , I },
                { I , I , N , P1, N , I , I },
                { N , I , P2, I , P1, I , P2},
                { I , I , N , N , P1, I , I },
                { I , I , I , I , I , I , I },
                { P2, I , I , P2, I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove();

        assertTrue(result.getKill() == null);

    }

    @Test
    public void computeMoveShouldFormTwoPotentialMillsInOneMove () throws InterruptedException {

        //check if the bot P1 forms both potential mills instead of only one, in which case P1 could definitely close a mill

        Options.Color[][] mill9 =
                {{N , I , I , N , I , I , N },
                { I , N , I , N , I , N , I },
                { I , I , P1, N , N , I , I },
                { N , N , P2, I , N , N , N },
                { I , I , P2, N , P1, I , I },
                { I , N , I , N , I , N , I },
                { N , I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill9(mill9);

        mPlayer1.setSetCount(7);
        mPlayer2.setSetCount(7);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());

        Strategy strategyP1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        Move result = strategyP1.computeMove();

        assertEquals(new Position(4,2), result.getDest());

    }


    @Test
    public void computeMoveShouldPreventUnPreventableMill() throws InterruptedException {

        //check if the bot prevents P2 from having an unpreventable mill when the sets to 4,2.

        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.EASY.ordinal());

        Options.Color[][] mill9 =
                {{N , I , I , N , I , I , N },
                { I , N , I , N , I , N , I },
                { I , I , P2, N , N , I , I },
                { N , N , P1, I , N , N , N },
                { I , I , N , N , P2, I , I },
                { I , N , I , N , I , N , I },
                { N , I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill9(mill9);

        mPlayer1.setSetCount(7);
        mPlayer2.setSetCount(8);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());

        Strategy strategyP1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        Move result = strategyP1.computeMove();

        assertThat(result.getDest(), anyOf(is(new Position(3,2)), is(new Position(4,2)), is(new Position(4,3))));

    }

    @Test
    public void computeMoveShouldPreventMillWhileSetting () throws InterruptedException {

        Options.Color[][] mill5 =
                {{N , I , I , P1, I , I , P2},
                { I , I , I , I , I , I , I },
                { I , I , P1, N , N , I , I },
                { N , I , N , I , N , I , N },
                { I , I , P2, P2, N , I , I },
                { I , I , I , I , I , I , I },
                { P1, I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(2);
        mPlayer2.setSetCount(2);

        Move result = strategy.computeMove();

        assertEquals(new Position(4, 4), result.getDest());
        assertEquals(null, result.getSrc());

    }

    @Test
    public void computeMoveShouldCloseMill() throws InterruptedException {

        Options.Color[][] mill5 =
                {{N , I , I , P1, I , I , P2},
                { I , I , I , I , I , I , I },
                { I , I , P1, N , P2, I , I },
                { P1, I , P1, I , N , I , N },
                { I , I , P2, P2, N , I , I },
                { I , I , I , I , I , I , I },
                { P1, I , I , P2, I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove();

        assertEquals(new Position(0, 0), result.getDest());
        assertEquals(new Position(3, 0), result.getSrc());

    }

    @Test
    public void computeMoveShouldUseDoubleMill() throws InterruptedException {

        Options.Color[][] mill5 =
                {{P1, I , I , N , I , I , P2},
                { I , I , I , I , I , I , I },
                { I , I , P1, N , P2, I , I },
                { P1, I , N , I , N , I , N },
                { I , I , P1, P2, N , I , I },
                { I , I , I , I , I , I , I },
                { P1, I , I , P2, I , I , P2}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result1 = strategy.computeMove();

        assertEquals(new Position(2,3), result1.getDest());
        assertEquals(new Position(0,3), result1.getSrc());

        gameBoard.executeCompleteTurn(result1, mPlayer1);

        //just let black do the next move again, white cant do anything
        Move result2 = strategy.computeMove();

        assertEquals(new Position(0,3), result2.getDest());
        assertEquals(new Position(2,3), result2.getSrc());

    }

    @Test
    public void computeMoveShouldCloseMillAndPreventOtherPlayersMill() throws InterruptedException {

        //its ok that the EASIER bot can not see that the other player can kill after his move
        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.EASY.ordinal());

        Options.Color[][] mill5 =
                {{N , I , I , P1, I , I , P2},
                { I , I , I , I , I , I , I },
                { I , I , P1, N , N , I , I },
                { P1, I , P1, I , P2, I , N },
                { I , I , P2, N , N , I , I },
                { I , I , I , I , I , I , I },
                { P1, I , I , P2, I , I , P2}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove();

        assertEquals(new Position(0, 0), result.getDest());
        assertEquals(new Position(3, 0), result.getSrc());

        assertNotEquals(new Position(2, 4), result.getKill());
        assertNotEquals(new Position(3, 6), result.getKill());

    }

    @Test
    // on difficulties, when the bot can see the field after 3 of his moves, he must see he has a closed mill then
    // the other player can do nothing about it in this constellation
    public void computeMoveShouldCloseMillOnHighDifficulties() throws InterruptedException {

        //workaround to skip easier difficulties
        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.HARD.ordinal());

        Options.Color[][] mill5 =
                {{N , I , I , N , I , I , P2 },
                { I , I , I , I , I , I , I },
                { I , I , P1, P2, N , I , I },
                { P1, I , N , I , N , I , P1},
                { I , I , P2, N , P2, I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , N , I , I , P1}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategyPlayer1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);
        Strategy strategyPlayer2 = new Strategy(gameBoard, mPlayer2, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result1 = strategyPlayer1.computeMove();
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        Move result2 = strategyPlayer2.computeMove();
        gameBoard.executeCompleteTurn(result2, mPlayer2);

        Move result3 = strategyPlayer1.computeMove();
        gameBoard.executeCompleteTurn(result3, mPlayer1);

        Move result4 = strategyPlayer2.computeMove();
        gameBoard.executeCompleteTurn(result4, mPlayer2);

        Move result5 = strategyPlayer1.computeMove();
        gameBoard.executeCompleteTurn(result5, mPlayer1);

        assertEquals(3, gameBoard.getPositions(mPlayer2.getColor()).size());

    }

    @Test
    // P1 should at least try to prevent a mill, although he cant prevent that the P2 still can close
    // his mill in another way. Is ok that P2 does not make the perfect move on EASIER
    public void computeMoveShouldTryToPreventLoosingEvenIfItsImpossibleWhileJumping() throws InterruptedException {

        Options.Color[][] mill5 =
                {{N , I , I , N , I , I , P1},
                { I , I , I , I , I , I , I },
                { I , I , N , P2, P2, I , I },
                { N , I , N , I , N , I , P1},
                { I , I , N , N , P2, I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , N , I , I , P1}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategyPlayer1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);
        Strategy strategyPlayer2 = new Strategy(gameBoard, mPlayer2, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result3 = strategyPlayer1.computeMove();
        gameBoard.executeCompleteTurn(result3, mPlayer1);

        assertThat(result3.getDest(), anyOf(is(new Position(2,2)), is(new Position(4,3))));
    }

    @Test
    // P1 should at least try to prevent a mill, an it has to be one that can actually be closed
    public void computeMoveShouldTryToPreventLoosingEvenIfItsImpossible() throws InterruptedException {

        Options.Color[][] mill9 =
                {{P2, I , I , N , I , I , N },
                { I , N , I , N , I , P1, I },
                { I , I , N , P2, P2, I , I },
                { N , P2, N , I , N , P1, N },
                { I , I , N , N , N , I , I },
                { I , N , I , P2, I , P1, I },
                { P2, I , I , N , I , I ,P2}};

        GameBoard gameBoard = new Mill9(mill9);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategyPlayer1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategyPlayer1.computeMove();
        gameBoard.executeCompleteTurn(result, mPlayer1);

        assertThat(result.getDest(), anyOf(is(new Position(0,3)), is(new Position(3,6))));
    }

    @Test
    public void computeMoveShouldTryToPreventLoosingByNotLeavingMill() throws InterruptedException {

        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.EASY.ordinal());

        Options.Color[][] mill9 =
                {{N , I , I , N , I , I , N },
                { I , P2, I , N , I , P2, I },
                { I , I , N , N , N , I , I },
                { P2, P1, P2, I , P2, P1, P2},
                { I , I , N , N , N , I , I },
                { I , N , I , N , I , N , I },
                { P1, I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill9(mill9);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategyPlayer1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategyPlayer1.computeMove();
        gameBoard.executeCompleteTurn(result, mPlayer1);

        assertEquals(result.getSrc(), new Position(0,6));
    }

    @Test
    public void computeMoveShouldCloseMillAndPreventOtherPlayersMillWhenJumping() throws InterruptedException {

        //its ok that the EASIER bot can not see that the other player can kill after his move
        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.EASY.ordinal());

        Options.Color[][] mill5 =
                {{P2, I , I , N , I , I , N },
                { I , I , I , I , I , I , I },
                { I , I , N , N , P1, I , I },
                { N , I , N , I , N , I , P1},
                { I , I , P2, N , P2, I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , P2, I , I , P1}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove();
        gameBoard.executeCompleteTurn(result, mPlayer1);

        assertEquals(new Position(6,0), result.getDest());
        assertThat(result.getKill(), anyOf(is(new Position(2,4)), is(new Position(4,4))));

    }

    @Test
    public void computeMoveShouldNotUndoHisMove() throws InterruptedException {

        Options.Color[][] mill5 =
                {{N , I , I , P1, I , I , N },
                { I , I , I , I , I , I , I },
                { I , I , P1, P2, P1, I , I },
                { P1, I , N , I , N , I , N },
                { I , I , P2, P2, N , I , I },
                { I , I , I , I , I , I , I },
                { P2, I , I , P1, I , I , P2}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategyPlayer1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);
        Strategy strategyPlayer2 = new Strategy(gameBoard, mPlayer2, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        LinkedList<Position> positionsP1Before = gameBoard.getPositions(mPlayer1.getColor());

        Move result1 = strategyPlayer1.computeMove();
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        strategyPlayer2.computeMove();
        //do not use this move, but compute it to check that it doesnt influence Player1s decision
        gameBoard.executeCompleteTurn(new Move(new Position(4,4), new Position(3,4), null), mPlayer2);

        Move result2 = strategyPlayer1.computeMove();
        gameBoard.executeCompleteTurn(result2, mPlayer1);

        LinkedList<Position> positionsP1After = gameBoard.getPositions(mPlayer1.getColor());

        assertNotEquals(positionsP1Before, positionsP1After);

    }

    @Test
    public void computeMoveShouldUndoHisMoveIfItClosesMill1() throws InterruptedException {

        //only bots on depth 3 can see that they can open and close the mill again
        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.NORMAL.ordinal());

        Options.Color[][] mill5 =
                {{N , I , I , P1, I , I , P1 },
                { I , I , I , I , I , I , I },
                { I , I , P1, P2, N , I , I },
                { P1, I , N , I , N , I , P2},
                { I , I , P2, P2, N , I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , P1, I , I , P2}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result1 = strategy.computeMove();
        assertEquals(new Position(0,3), result1.getSrc());
        assertEquals(new Position(0,0), result1.getDest());

        //change kill of the move, as it may be another equally evaluated kill, but we want to test with this one
        result1 = new Move(result1.getDest(), result1.getSrc(), new Position(6,6));
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        gameBoard.executeCompleteTurn(new Move(new Position(4,4), new Position(3,4), null), mPlayer2);

        Move result2 = strategy.computeMove();
        gameBoard.executeCompleteTurn(result2, mPlayer1);

        assertEquals(new Position(0,0), result2.getSrc());
        assertEquals(new Position(0,3), result2.getDest());

    }

    @Test
    public void computeMoveShouldUndoHisMoveIfItClosesMill2() throws InterruptedException {

        //only bots on depth 3 can see that they can open and close the mill again
        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.NORMAL.ordinal());

        Options.Color[][] mill5 =
                {{P1, I , I , P1, I , I , P1},
                { I , I , I , I , I , I , I },
                { I , I , P1, P2, N , I , I },
                { N , I , N , I , N , I , P2},
                { I , I , P2, P2, N , I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , P1, I , I , P2}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result1 = strategy.computeMove();
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        assertEquals(new Position(0,3), result1.getDest());

        gameBoard.executeCompleteTurn(new Move(new Position(4,4), new Position(3,4), null), mPlayer2);

        Move result2 = strategy.computeMove();
        gameBoard.executeCompleteTurn(result2, mPlayer1);

        assertEquals(new Position(0,0), result2.getDest());

    }

    @Test
    public void computeMoveShouldWinAsNoMovesLeft() throws InterruptedException {

        Options.Color[][] mill5 =
                {{P1, I , I , N , I , I , N },
                { I , I , I , I , I , I , I },
                { I , I , P2, P1, P2, I , I },
                { N , I , P1, I , N , I , P1},
                { I , I , P2, P1, P2, I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove();

        assertEquals(new Position(4, 3), result.getDest());
        assertEquals(new Position(6, 3), result.getSrc());

    }

    @Test
    public void computeMoveShouldOpenMill() throws InterruptedException {

        //only bots on depth 3 can see that they can open and close the mill again
        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.NORMAL.ordinal());

        Options.Color[][] mill9 =
                {{P1, I , I , N , I , I , N },
                { I , N , I , P2, I , P1, I },
                { I , I , N , N , P2, I , I },
                { N , N , N , I , N , P1, N },
                { I , I , N , N , N , I , I },
                { I , N , I , P2, I , P1, I },
                { P2, I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill9(mill9);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove();

        assertThat(result.getDest(), anyOf(is(new Position(6,3)), is(new Position(4,3))));

    }

    @Test
    public void computeMoveShouldNotOpenMill() throws InterruptedException {

        //Bots starting with depth 2 should notice that the enemy can prevent the mill
        assumeTrue(mPlayer1.getDifficulty().ordinal() >= Options.Difficulties.EASY.ordinal());

        Options.Color[][] mill9 =
                {{P1, I , I , N , I , I , N },
                { I , N , I , P2, I , P1, I },
                { I , I , N , N , N , I , I },
                { N , N , N , I , P2, P1, N },
                { I , I , N , N , N , I , I },
                { I , N , I , P2, I , P1, I },
                { P2, I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill9(mill9);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove();

        assertEquals(result.getSrc(), new Position(0,0));

    }

    @Test
    public void computeMoveDoesNotAlterPassedObjects() throws InterruptedException {

        GameBoard gameBoard = new Mill9();

        mPlayer1.setSetCount(9);
        mPlayer2.setSetCount(9);

        Player mPlayer1Before = new Player(mPlayer1);
        Player mPlayer2Before = new Player(mPlayer2);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategyP1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);
        Strategy strategyP2 = new Strategy(gameBoard, mPlayer2, updater, nThreads);

        for(int i = 0; i<30; i++){

            GameBoard gameBoardBefore1 = gameBoard.getCopy();

            //test if computeMove makes unallowed changed to gameBoard or players now
            Move result1 = strategyP1.computeMove();
            gameBoard.executeCompleteTurn(result1, mPlayer1);
            if(!gameBoard.getState(mPlayer1).equals(GameBoard.GameState.RUNNING)){
                if(!gameBoard.getState(mPlayer1).equals(GameBoard.GameState.REMIS)) {
                    break;
                }
            }

            //computeMove should not alter anything but the last move of course

            //do the same but this time manually without computeMove to check if its the same
            gameBoardBefore1.executeCompleteTurn(result1, mPlayer1Before);
            assertEquals("round " + i, gameBoardBefore1.toString(), gameBoard.toString());

            //computeMove should not have altered anything but the setCount of P1 (was done in executeCompleteTurn)
            assertEquals("round " + i, mPlayer1Before, mPlayer1);
            assertEquals("round " + i, mPlayer2Before, mPlayer2);

            GameBoard gameBoardBefore2 = gameBoard.getCopy();
            Move result2 = strategyP2.computeMove();
            gameBoard.executeCompleteTurn(result2, mPlayer2);
            if(!gameBoard.getState(mPlayer2).equals(GameBoard.GameState.RUNNING)){
                if(!gameBoard.getState(mPlayer2).equals(GameBoard.GameState.REMIS)) {
                    break;
                }
            }

            gameBoardBefore2.executeCompleteTurn(result2, mPlayer2Before);

            assertEquals("round " + i, gameBoardBefore2.toString(), gameBoard.toString());

            assertEquals("round " + i, mPlayer1Before, mPlayer1);
            assertEquals("round " + i, mPlayer2Before, mPlayer2);

        }

    }

    //if this test fails may (but probably not) be because of a tiny chance that not all possible moves were chosen
    @Test
    public void computeMoveShouldReturn8DifferentMovesOverTime () throws InterruptedException {

        int nPosExpected = 8;

        Options.Color[][] mill5 =
                {{N , I , I , N , I , I , N },
                { I , I , I , I , I , I , I },
                { I , I , N , N , N , I , I },
                { N , I , N , I , N , I , N },
                { I , I , N , N , N , I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);

        mPlayer1.setSetCount(5);
        mPlayer2.setSetCount(5);

        LinkedList<Move> list = new LinkedList<Move>();

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());

        for(int i = 0; i < 1000; i++) {
            Strategy strategyP1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);
            Move result = strategyP1.computeMove();
            if(!list.contains(result)) {
                list.add(result);
            }
            //check after 100 iterations if list contains enough (or too much) elements and break
            if(i % 100 == 0 && list.size() >= nPosExpected){
                break;
            }
        }

    }

    @Test
    public void computeMoveShouldReturn5DifferentKillMovesOverTime () throws InterruptedException {

        // test only for dumbest bot as others may have preferences what to kill
        assumeTrue(mPlayer1.getDifficulty().ordinal() == Options.Difficulties.EASIER.ordinal());

        Options.Color[][] mill9 =
                {{P2, I , I , N , I , I , N },
                { I , P1, I , N , I , P2, I },
                { I , I , N , N , N , I , I },
                { P1, N , N , I , N , N , P2},
                { I , I , P2, N , N , I , I },
                { I , P1, I , N , I , N , I },
                { P2, I , I , N , I , I , N }};

        GameBoard gameBoard = new Mill9(mill9);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        LinkedList<Move> list = new LinkedList<Move>();

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());

        for(int i = 0; i < 1000; i++) {
            Strategy strategyP1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);
            Move result = strategyP1.computeMove();
            if(!list.contains(result)) {
                list.add(result);
            }
            //check after 50 iterations if list contains enough (or too much) elements and break
            if(i % 50 == 0 && list.size() >= 5){
                break;
            }
        }

        for(Move m : list){
            assertTrue(m.getKill() != null);
        }

        assertEquals(5, list.size());
    }

    @Test
    public void shuffleListShouldHaveKillsAtBeginning(){

        Options.Color[][] mill9 =
                {{N , I , I , N , I , I , P2},
                { I , N , I , N , I , N , I },
                { I , I , N , P1, P1, I , I },
                { P2, N , P1, I , P2, N , N },
                { I , I , N , P1, N , I , I },
                { I , N , I , N , I , P2, I },
                { N , I , I , P2, I , I , N }};

        GameBoard gameBoard = new Mill9(mill9);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());

        Strategy strategyP1 = new Strategy(gameBoard, mPlayer1, updater, nThreads);

        LinkedList<Move> result = strategyP1.shuffleListOfPossMoves();

        assertEquals(5 + 7, result.size());

        assertTrue(result.get(0).getKill() != null);
        assertTrue(result.get(1).getKill() != null);
        assertTrue(result.get(2).getKill() != null);
        assertTrue(result.get(3).getKill() != null);
        assertTrue(result.get(4).getKill() != null);

    }

}

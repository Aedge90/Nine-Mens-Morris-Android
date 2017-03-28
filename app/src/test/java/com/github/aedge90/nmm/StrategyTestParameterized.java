package com.github.aedge90.nmm;


import android.test.mock.MockContext;
import android.widget.ProgressBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;


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
    public void computeMoveShouldFormPotentialMills () throws InterruptedException {

        Options.Color[][] mill5 =
                {{N , I , I , N , I , I , N },
                { I , I , I , I , I , I , I },
                { I , I , N , N , N , I , I },
                { N , I , N , I , P1, I , N },
                { I , I , N , N , N , I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(4);
        mPlayer2.setSetCount(4);

        Move result = strategy.computeMove(mPlayer1);

        assertThat(result.getDest(), anyOf(is(new Position(4,2)), is(new Position(4,4))));
    }

    @Test
    public void computeMoveShouldFormTwoPotentialMillsInOneMove () throws InterruptedException {

        //check if the bot prevents both potential mills instead of only one, in which case P1 could definitely close a mill

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

        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        Move result = strategy.computeMove(mPlayer1);

        assertEquals(new Position(4,2), result.getDest());

    }

    @Test
    public void computeMoveShouldPreventPotentialMill () throws InterruptedException {

        Options.Color[][] mill5 =
                {{N , I , I , N , I , I , N },
                { I , I , I , I , I , I , I },
                { I , I , N , N , N , I , I },
                { N , I , N , I , N , I , N },
                { I , I , N , N , P2, I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);

        mPlayer1.setSetCount(5);
        mPlayer2.setSetCount(4);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());

        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(4);
        mPlayer2.setSetCount(4);

        Move result = strategy.computeMove(mPlayer1);

        assertThat(result.getDest(), anyOf(is(new Position(4,2)), is(new Position(4,3)), is(new Position(3,4)), is(new Position(2,4))));

    }

    @Test
    public void computeMoveShouldPreventTwoPotentialMillsInOneMove () throws InterruptedException {

        //check if the bot prevents both potential mills instead of only one, in which case P1 could definitely close a mill

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

        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        Move result = strategy.computeMove(mPlayer1);

        assertEquals(new Position(4,2), result.getDest());

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(2);
        mPlayer2.setSetCount(2);

        Move result = strategy.computeMove(mPlayer1);

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
                { I , I , P2, N , N , I , I },
                { I , I , I , I , I , I , I },
                { P1, I , I , P2, I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove(mPlayer1);

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
                { I , I , P1, N , P2, I , I },
                { I , I , I , I , I , I , I },
                { P1, I , I , P2, I , I , P2}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result1 = strategy.computeMove(mPlayer1);

        assertEquals(new Position(2,3), result1.getDest());
        assertEquals(new Position(0,3), result1.getSrc());

        gameBoard.executeCompleteTurn(result1, mPlayer1);

        //just let black do the next move again, white cant do anything
        strategy = new Strategy(gameBoard, updater, nThreads);
        Move result2 = strategy.computeMove(mPlayer1);

        assertEquals(new Position(0,3), result2.getDest());
        assertEquals(new Position(2,3), result2.getSrc());

    }

    @Test
    public void computeMoveShouldCloseMillAndPreventOtherPlayersMill() throws InterruptedException {

        //its ok that the EASIER bot can not see that the other player can kill after his move
        if(mPlayer1.getDifficulty().equals(Options.Difficulties.EASIER)){
            return;
        }

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove(mPlayer1);

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
        if(mPlayer1.getDifficulty().equals(Options.Difficulties.EASIER)
                || mPlayer1.getDifficulty().equals(Options.Difficulties.EASY)
                || mPlayer1.getDifficulty().equals(Options.Difficulties.NORMAL)
                || mPlayer1.getDifficulty().equals(Options.Difficulties.ADVANCED)){
            return;
        }

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result1 = strategy.computeMove(mPlayer1);
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        Move result2 = strategy.computeMove(mPlayer2);
        gameBoard.executeCompleteTurn(result2, mPlayer2);

        Move result3 = strategy.computeMove(mPlayer1);
        gameBoard.executeCompleteTurn(result3, mPlayer1);

        Move result4 = strategy.computeMove(mPlayer2);
        gameBoard.executeCompleteTurn(result4, mPlayer2);

        Move result5 = strategy.computeMove(mPlayer1);
        gameBoard.executeCompleteTurn(result5, mPlayer1);

        assertEquals(3, gameBoard.getPositions(mPlayer2.getColor()).size());

    }

    @Test
    // P1 should at least try to prevent a mill, although he cant prevent that the P2 still can close
    // his mill in another way. Is ok that P2 does not make the perfect move on EASIER
    public void computeMoveShouldTryToPreventLoosingEvenIfItsImpossible() throws InterruptedException {

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result3 = strategy.computeMove(mPlayer1);

        gameBoard.executeCompleteTurn(result3, mPlayer1);

        assertThat(result3.getDest(), anyOf(is(new Position(2,2)), is(new Position(4,3))));
    }

    @Test
    public void computeMoveShouldCloseMillAndPreventOtherPlayersMillWhenJumping() throws InterruptedException {

        //its ok that the EASIER bot can not see that the other player can kill after his move
        if(mPlayer1.getDifficulty().equals(Options.Difficulties.EASIER)){
            return;
        }

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove(mPlayer1);
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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        LinkedList<Position> positionsP1Before = gameBoard.getPositions(mPlayer1.getColor());

        Move result1 = strategy.computeMove(mPlayer1);
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        strategy.computeMove(mPlayer2);
        //do not use this move, but compute it to check that it doesnt influence Player1s decision
        Move actualMove = new Move(new Position(4,4), new Position(3,4), null);
        mPlayer2.setPrevMove(actualMove);
        strategy.replaceLastMove(actualMove);
        gameBoard.executeCompleteTurn(actualMove, mPlayer2);

        Move result2 = strategy.computeMove(mPlayer1);
        gameBoard.executeCompleteTurn(result2, mPlayer1);

        LinkedList<Position> positionsP1After = gameBoard.getPositions(mPlayer1.getColor());

        assertNotEquals(positionsP1Before, positionsP1After);

    }

    @Test
    public void computeMoveShouldUndoHisMoveIfItClosesMill1() throws InterruptedException {

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result1 = strategy.computeMove(mPlayer1);
        assertEquals(new Position(0,3), result1.getSrc());
        assertEquals(new Position(0,0), result1.getDest());

        //change kill of the move, as it may be another equally evaluated kill, but we want to test with this one
        Move actualResult1 = new Move(result1.getDest(), result1.getSrc(), new Position(6,6));
        mPlayer1.setPrevMove(actualResult1);
        strategy.replaceLastMove(actualResult1);
        gameBoard.executeCompleteTurn(actualResult1, mPlayer1);

        Move result2 = strategy.computeMove(mPlayer2);
        Move actualResult2 = new Move(new Position(4,4), new Position(3,4), null);
        mPlayer2.setPrevMove(actualResult2);
        strategy.replaceLastMove(actualResult2);
        gameBoard.executeCompleteTurn(actualResult2, mPlayer2);

        Move result3 = strategy.computeMove(mPlayer1);
        gameBoard.executeCompleteTurn(result3, mPlayer1);

        assertEquals(new Position(0,0), result3.getSrc());
        assertEquals(new Position(0,3), result3.getDest());

    }

    @Test
    public void computeMoveShouldUndoHisMoveIfItClosesMill2() throws InterruptedException {

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result1 = strategy.computeMove(mPlayer1);
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        assertEquals(new Position(0,3), result1.getDest());

        Move result2 = strategy.computeMove(mPlayer2);
        Move actualResult2 = new Move(new Position(4,4), new Position(3,4), null);
        mPlayer2.setPrevMove(actualResult2);
        strategy.replaceLastMove(actualResult2);
        gameBoard.executeCompleteTurn(actualResult2, mPlayer2);

        Move result3 = strategy.computeMove(mPlayer1);
        gameBoard.executeCompleteTurn(result3, mPlayer1);

        assertEquals(new Position(0,0), result3.getDest());

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove(mPlayer1);

        assertEquals(new Position(4, 3), result.getDest());
        assertEquals(new Position(6, 3), result.getSrc());

    }

    //Test if especially bots on EASIER open their mill, as they cant see the gameboard after 2 moves
    @Test
    public void computeMoveShouldOpenMill() throws InterruptedException {

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove(mPlayer1);

        assertThat(result.getDest(), anyOf(is(new Position(6,3)), is(new Position(4,3))));

    }

    @Test
    public void computeMoveShouldNotOpenMill() throws InterruptedException {

        //its ok that the EASIER bot can not see that the other player can prevent his next mill
        if(mPlayer1.getDifficulty().equals(Options.Difficulties.EASIER)){
            return;
        }

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove(mPlayer1);

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
        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        for(int i = 0; i<30; i++){

            GameBoard gameBoardBefore1 = gameBoard.getCopy();

            //test if computeMove makes unallowed changed to gameBoard or players now
            Move result1 = strategy.computeMove(mPlayer1);
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
            Move result2 = strategy.computeMove(mPlayer2);
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
    public void computeMoveShouldReturn16DifferentMovesOverTime () throws InterruptedException {

        int nPos = 16;

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

        for(int i = 0; i < 2000; i++) {
            Strategy strategy = new Strategy(gameBoard, updater, nThreads);
            Move result = strategy.computeMove(mPlayer1);
            if(!list.contains(result)) {
                list.add(result);
            }
            //check after 100 iterations if list contains enough (or too much) elements and break
            if(i % 100 == 0 && list.size() >= nPos){
                break;
            }
        }

        assertEquals(nPos, list.size());

    }

    @Test
    public void computeMoveShouldReturn5DifferentKillMovesOverTime () throws InterruptedException {

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

        for(int i = 0; i < 2000; i++) {
            Strategy strategy = new Strategy(gameBoard, updater, nThreads);
            Move result = strategy.computeMove(mPlayer1);
            if(!list.contains(result)) {
                list.add(result);
            }
            //check after 50 iterations if list contains enough (or too much) elements and break
            if(i % 50 == 0 && list.size() >= 5){
                break;
            }
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

        Strategy strategy = new Strategy(gameBoard, updater, nThreads);

        LinkedList<Move> result = strategy.shuffleListOfPossMoves(gameBoard.possibleMoves(mPlayer1));

        assertEquals(5 + 7, result.size());

        assertTrue(result.get(0).getKill() != null);
        assertTrue(result.get(1).getKill() != null);
        assertTrue(result.get(2).getKill() != null);
        assertTrue(result.get(3).getKill() != null);
        assertTrue(result.get(4).getKill() != null);

    }

    @Test
    public void gameAgainstHumanShouldNotCrashIfBotStarts() {
        gameAgainstHumanShouldNotCrash(true);
    }

    @Test
    public void gameAgainstHumanShouldNotCrashIfHumanStarts() {
        gameAgainstHumanShouldNotCrash(false);
    }

    public void gameAgainstHumanShouldNotCrash(boolean botStarts) {

        try {

            GameBoard gameBoard = new Mill9();
            ProgressBar progBar = new ProgressBar(new MockContext());
            ProgressUpdater updater = new ProgressUpdater(progBar, new GameModeActivity());
            Strategy strategy = new Strategy(gameBoard, updater, nThreads);

            mPlayer1.setSetCount(9);
            mPlayer2.setSetCount(9);

            mPlayer2.setDifficulty(null); //now its a human

            if(botStarts) {
                Move firstMove = strategy.computeMove(mPlayer1);
                gameBoard.executeCompleteTurn(firstMove, mPlayer1);
            }

            for (int i = 0; i < 50; i++) {

                //simulate human and just take the first move and do not use the strategy
                Move result2 = gameBoard.possibleMoves(mPlayer2).getFirst();
                gameBoard.executeSetOrMovePhase(result2, mPlayer2);
                gameBoard.executeKillPhase(result2, mPlayer2);
                strategy.registerLastMove(result2, mPlayer2);
                mPlayer2.setPrevMove(result2);
                if (!gameBoard.getState(mPlayer2).equals(GameBoard.GameState.RUNNING)) {
                    if(!gameBoard.getState(mPlayer2).equals(GameBoard.GameState.REMIS)) {
                        break;
                    }
                }

                Move result1 = strategy.computeMove(mPlayer1);
                gameBoard.executeCompleteTurn(result1, mPlayer1);
                if (!gameBoard.getState(mPlayer1).equals(GameBoard.GameState.RUNNING)) {
                    if(!gameBoard.getState(mPlayer1).equals(GameBoard.GameState.REMIS)) {
                        break;
                    }
                }

            }
        } catch (Throwable e) {
            fail(e.getMessage());
        }

    }

}

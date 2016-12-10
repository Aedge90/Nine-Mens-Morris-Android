package own.projects.lemiroapp;


import android.test.mock.MockContext;
import android.widget.ProgressBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;


@RunWith(value = Parameterized.class)
public class StrategyTestParameterized {

    private final Options.Color P1;
    private final Options.Color P2;
    private final Options.Color N = Options.Color.NOTHING;
    private final Options.Color I = Options.Color.INVALID;

    private Player mPlayer1;
    private Player mPlayer2;

    // name attribute is optional, provide an unique name for test
    // multiple parameters, uses Collection<Object[]>
    @Parameterized.Parameters(name = "P1 is {0}")
    public static Collection<Object[] > data() {

        Object[][] player1List = new Player[Options.Difficulties.values().length*2][1];
        for(int i = 0; i < Options.Difficulties.values().length; i++) {

            Player playerBlack = new Player(Options.Color.BLACK);
            Player playerWhite = new Player(Options.Color.WHITE);
            playerBlack.setDifficulty(Options.Difficulties.values()[i]);
            playerWhite.setDifficulty(Options.Difficulties.values()[i]);
            playerBlack.setSetCount(5);
            playerWhite.setSetCount(5);

            player1List[i*2][0] = playerBlack;
            player1List[i*2+1][0] = playerWhite;
        }

        Collection<Object[]> parameters = Arrays.asList(player1List);

        return parameters;
    }

    // Inject paremeters via constructor, constructor is called before each test
    public StrategyTestParameterized(Player player1){

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
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater);

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
                { I , I , P1, N , P2, I , I },
                { I , I , I , I , I , I , I },
                { P1, I , I , P2, I , I , P2}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater);

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
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater);

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
        if(mPlayer1.getDifficulty().equals(Options.Difficulties.EASY)
                || mPlayer1.getDifficulty().equals(Options.Difficulties.NORMAL)
                || mPlayer1.getDifficulty().equals(Options.Difficulties.HARD)){
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
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategyPlayer1 = new Strategy(gameBoard, mPlayer1, updater);
        Strategy strategyPlayer2 = new Strategy(gameBoard, mPlayer2, updater);

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
    //in this scenario P1 makes a mistake, P2 should compute the perfect move, so that P1 cant prevent loosing
    //then P1 should at least try to prevent a mill, although he cant prevent that the P2 still can close
    //his mill in another way. Is ok that P2 does not make the perfect move on EASY
    public void computeMoveShouldTryToPreventLoosingEvenIfItsImpossible() throws InterruptedException {

        if(mPlayer2.getDifficulty() == Options.Difficulties.EASY){
            return;
        }

        Options.Color[][] mill5 =
                {{N , I , I , N , I , I , N },
                { I , I , I , I , I , I , I },
                { I , I , N , P2, P1, I , I },
                { N , I , N , I , P2, I , P1},
                { I , I , N , N , P2, I , I },
                { I , I , I , I , I , I , I },
                { P2, I , I , N , I , I , P1}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategyPlayer1 = new Strategy(gameBoard, mPlayer1, updater);
        Strategy strategyPlayer2 = new Strategy(gameBoard, mPlayer2, updater);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = new Move(new Position(6,0), new Position(4,2), new Position(4,3));

        gameBoard.executeCompleteTurn(result, mPlayer1);

        Move result2 = strategyPlayer2.computeMove();
        gameBoard.executeCompleteTurn(result2, mPlayer2);

        assertEquals(new Position(4,2), result2.getDest());

        Move result3 = strategyPlayer1.computeMove();
        gameBoard.executeCompleteTurn(result3, mPlayer1);

        assertThat(result3.getDest(), anyOf(is(new Position(2,2)), is(new Position(4,3))));
    }

    @Test
    public void computeMoveShouldCloseMillAndPreventOtherPlayersMillWhenJumping() throws InterruptedException {

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
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater);

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
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategyPlayer1 = new Strategy(gameBoard, mPlayer1, updater);
        Strategy strategyPlayer2 = new Strategy(gameBoard, mPlayer2, updater);

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
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result1 = strategy.computeMove();
        gameBoard.executeCompleteTurn(result1, mPlayer1);

        assertEquals(new Position(0,0), result1.getDest());

        gameBoard.executeCompleteTurn(new Move(new Position(4,4), new Position(3,4), null), mPlayer2);

        Move result2 = strategy.computeMove();
        gameBoard.executeCompleteTurn(result2, mPlayer1);

        assertEquals(new Position(0,3), result2.getDest());

    }

    @Test
    public void computeMoveShouldUndoHisMoveIfItClosesMill2() throws InterruptedException {

        Options.Color[][] mill5 =
                {{P1, I , I , P1, I , I , P1 },
                { I , I , I , I , I , I , I },
                { I , I , P1, P2, N , I , I },
                { N , I , N , I , N , I , P2},
                { I , I , P2, P2, N , I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , P1, I , I , P2}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater);

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
                {{P1, I , I , N , I , I , N},
                { I , I , I , I , I , I , I },
                { I , I , P2, P1, P2, I , I },
                { N , I , P1, I , N , I , P1},
                { I , I , P2, P1, P2, I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategy = new Strategy(gameBoard, mPlayer1, updater);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Move result = strategy.computeMove();

        assertEquals(new Position(4, 3), result.getDest());
        assertEquals(new Position(6, 3), result.getSrc());

    }

    @Test
    public void computeMoveDoesNotAlterPassedObjects() throws InterruptedException {

        GameBoard gameBoard = new Mill9();

        mPlayer1.setSetCount(9);
        mPlayer2.setSetCount(9);

        Player mPlayer1Before = new Player(mPlayer1);
        Player mPlayer2Before = new Player(mPlayer2);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategy strategyP1 = new Strategy(gameBoard, mPlayer1, updater);
        Strategy strategyP2 = new Strategy(gameBoard, mPlayer2, updater);

        for(int i = 0; i<14; i++){

            GameBoard gameBoardBefore1 = gameBoard.getCopy();

            //test if computeMove makes unallowed changed to gameBoard or players now
            Move result1 = strategyP1.computeMove();
            gameBoard.executeCompleteTurn(result1, mPlayer1);
            //computeMove should not alter anything but the last move of course

            //do the same but this time manually without computeMove to check if its the same
            gameBoardBefore1.executeCompleteTurn(result1, mPlayer1Before);
            assertEquals("round " + i, gameBoardBefore1.toString(), gameBoard.toString());

            //computeMove should not have altered anything but the setCount of P1 (was done in executeCompleteTurn)
            assertEquals("round " + i, mPlayer1Before, mPlayer1);
            assertEquals("round " + i, mPlayer2Before, mPlayer2);

            System.out.println(gameBoard);

            GameBoard gameBoardBefore2 = gameBoard.getCopy();
            Move result2 = strategyP2.computeMove();
            gameBoard.executeCompleteTurn(result2, mPlayer2);
            gameBoardBefore2.executeCompleteTurn(result2, mPlayer2Before);
            assertEquals("round " + i, gameBoardBefore2.toString(), gameBoard.toString());

            assertEquals("round " + i, mPlayer1Before, mPlayer1);
            assertEquals("round " + i, mPlayer2Before, mPlayer2);

            System.out.println(gameBoard);

        }

    }

    @Test
    public void computeEqualMovesShouldBeSameForAnyNumberOfThreads () throws InterruptedException {

        GameBoard gameBoard = new Mill9();

        mPlayer1.setSetCount(9);
        mPlayer2.setSetCount(9);

        //make 14 rounds and check if the results on all possible thread counts are the same
        for(int i = 0; i<14; i++){
            computeEqualMovesShouldBeSameForAnyNumberOfThreads_Turn(i, gameBoard, mPlayer1);
            computeEqualMovesShouldBeSameForAnyNumberOfThreads_Turn(i, gameBoard, mPlayer2);
        }

    }

    public void computeEqualMovesShouldBeSameForAnyNumberOfThreads_Turn (int round, GameBoard gameBoard, Player player) throws InterruptedException{

        int maxThreads = 16;

        LinkedList<Move> prevResult = new LinkedList<Move>();
        LinkedList<Move> result = new LinkedList<Move>();

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());

        for(int j = 0; j < maxThreads; j++) {
            int nThreads = j+1;
            Strategy strategy = new Strategy(gameBoard, player, updater, nThreads);
            System.out.println("starting computeEqualMoves, nThreads: " + nThreads + " round: " + round);
            result = strategy.computeEqualMoves();
            // dont use computeMove directly as the result may be different as the list from
            // computeEqualMoves may be in a different order for different nThreads, which is ok
            if(j > 0) {
                System.out.println(" prev: " + prevResult.size() + " res: " + result.size());
                if(prevResult.size() != result.size()){
                    System.out.println(gameBoard + " " + player);
                    System.out.println(prevResult + " " + "\nresult: " + result);
                }
                assertListsContainingSameMoves("round " + round + " nTreads: " + nThreads +
                        "; result was different from previous one", prevResult, result);
            }
            prevResult = result;
        }

        gameBoard.executeCompleteTurn(result.getFirst(), player);
    }

    public void assertListsContainingSameMoves (String message, LinkedList<Move> prevResult, LinkedList<Move> result) {
        assertEquals(message, prevResult.size(), result.size());
        for(Move prev : prevResult){
            assertTrue(message, result.contains(prev));
        }
    }

    @Test
    public void computeEqualMovesShouldBeOfSize1ForAnyNumberOfThreads () throws InterruptedException {

        Options.Color[][] mill5 =
                {{N , I , I , P1, I , I , P2},
                { I , I , I , I , I , I , I },
                { I , I , P1, N , P2, I , I },
                { P1, I , P1, I , N , I , N },
                { I , I , P2, N , N , I , I },
                { I , I , I , I , I , I , I },
                { P1, I , I , P2, I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);

        mPlayer1.setSetCount(9);
        mPlayer2.setSetCount(9);

        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());

        int maxThreads = 16;
        for(int j = 0; j < maxThreads; j++) {
            Strategy strategyP1 = new Strategy(gameBoard, mPlayer1, updater, 1);
            LinkedList<Move> result1 = strategyP1.computeEqualMoves();
            assertEquals(1, result1.size());
        }

    }
}

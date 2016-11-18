package own.projects.lemiroapp;


import android.test.mock.MockContext;
import android.widget.ProgressBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotEquals;


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
                { P1, I , P1, I , N , I , N},
                { I , I , P2, N , N , I , I },
                { I , I , I , I , I , I , I },
                { P1, I , I , P2, I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategie strategy = new Strategie(gameBoard, updater);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Zug result = strategy.computeMove(mPlayer1);

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
        Strategie strategy = new Strategie(gameBoard, updater);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Zug result1 = strategy.computeMove(mPlayer1);

        assertEquals(new Position(2,3), result1.getDest());
        assertEquals(new Position(0,3), result1.getSrc());

        gameBoard.executeCompleteTurn(result1, mPlayer1);

        //just let black do the next move again, white cant do anything
        Zug result2 = strategy.computeMove(mPlayer1);

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
        Strategie strategy = new Strategie(gameBoard, updater);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Zug result = strategy.computeMove(mPlayer1);

        assertEquals(new Position(0, 0), result.getDest());
        assertEquals(new Position(3, 0), result.getSrc());

        assertNotEquals(new Position(2, 4), result.getKill());

    }

    @Test
    public void computeMoveShouldCloseMillAndPreventOtherPlayersMillWhenJumping() throws InterruptedException {

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
        Strategie strategy = new Strategie(gameBoard, updater);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Zug result = strategy.computeMove(mPlayer1);

        assertEquals(new Position(6, 0), result.getDest());
        assertEquals(new Position(4, 2), result.getSrc());

        assertNotEquals(new Position(0, 6), result.getKill());

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
        Strategie strategy = new Strategie(gameBoard, updater);

        mPlayer1.setSetCount(0);
        mPlayer2.setSetCount(0);

        Zug result = strategy.computeMove(mPlayer1);

        assertEquals(new Position(4, 3), result.getDest());
        assertEquals(new Position(6, 3), result.getSrc());

    }

}

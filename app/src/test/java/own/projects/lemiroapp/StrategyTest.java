package own.projects.lemiroapp;

import android.widget.ProgressBar;

import org.junit.Before;
import org.junit.Test;
import android.test.mock.MockContext;
import static junit.framework.Assert.assertEquals;



public class StrategyTest {

    private Strategie strategy;

    private Spielfeld mGameboard;

    private Strategie mStrategy;

    @Before
    public void init() {
        mGameboard = new Mill5();
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());

        mStrategy = new Strategie(mGameboard, updater);
    }

    @Test
    public void bewertungShouldBe0() {

        Player playerBlack = new Player(Options.Color.BLACK);
        Player playerWhite = new Player(Options.Color.WHITE);
        playerBlack.setOtherPlayer(playerWhite);
        playerWhite.setOtherPlayer(playerBlack);

        mGameboard.setPos(new Position(0,0), Options.Color.BLACK);
        mGameboard.setPos(new Position(3,0), Options.Color.WHITE);
        mGameboard.setPos(new Position(6,6), Options.Color.BLACK);

        playerBlack.setSetCount(3);
        playerWhite.setSetCount(4);

        //result should be 500 +500 -1000 = 0 (500 for having own pieces on the gameboard, -1000 for enemies piece

        int result = mStrategy.bewertung(playerBlack);
        assertEquals(0, result);
    }

    @Test
    public void bewertungOfMillShouldBe500() {

        Player playerBlack = new Player(Options.Color.BLACK);
        Player playerWhite = new Player(Options.Color.WHITE);
        playerBlack.setOtherPlayer(playerWhite);
        playerWhite.setOtherPlayer(playerBlack);

        mGameboard.setPos(new Position(0,0), Options.Color.BLACK);
        mGameboard.setPos(new Position(3,0), Options.Color.WHITE);
        mGameboard.setPos(new Position(0,3), Options.Color.BLACK);
        mGameboard.setPos(new Position(6,0), Options.Color.WHITE);
        mGameboard.setPos(new Position(0,6), Options.Color.BLACK);

        playerBlack.setSetCount(2);
        playerWhite.setSetCount(3);

        //last move contains a kill, which is contained in the move and is also evaluated
        mGameboard.setPos(new Position(3,0), Options.Color.NOTHING);

        //result should be 500 +500 +500 -1000 = 500 (500 for having own pieces on the gameboard, -1000 for enemies piece

        int result = mStrategy.bewertung(playerBlack);
        assertEquals(500, result);
    }

}

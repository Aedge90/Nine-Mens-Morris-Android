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

    private Player mPlayerBlack;
    private Player mPlayerWhite;

    @Before
    public void beforeTests() {
        mGameboard = new Mill5();
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());

        mStrategy = new Strategie(mGameboard, updater);

        mPlayerBlack = new Player(Options.Color.BLACK);
        mPlayerWhite = new Player(Options.Color.WHITE);
        mPlayerBlack.setOtherPlayer(mPlayerWhite);
        mPlayerWhite.setOtherPlayer(mPlayerBlack);

    }

    @Test
    public void bewertungShouldBe0() {

        mGameboard.setPos(new Position(0,0), Options.Color.BLACK);
        mGameboard.setPos(new Position(3,0), Options.Color.WHITE);
        mGameboard.setPos(new Position(6,6), Options.Color.BLACK);

        mPlayerBlack.setSetCount(3);
        mPlayerWhite.setSetCount(4);

        //result should be 500 +500 -1000 = 0 (500 for having own pieces on the gameboard, -1000 for enemies piece

        int result = mStrategy.bewertung(mPlayerBlack);
        assertEquals(0, result);
    }

    @Test
    public void bewertungOfMillShouldBe500() {

        mGameboard.setPos(new Position(0,0), Options.Color.BLACK);
        mGameboard.setPos(new Position(3,0), Options.Color.WHITE);
        mGameboard.setPos(new Position(0,3), Options.Color.BLACK);
        mGameboard.setPos(new Position(6,0), Options.Color.WHITE);
        mGameboard.setPos(new Position(0,6), Options.Color.BLACK);

        mPlayerBlack.setSetCount(2);
        mPlayerWhite.setSetCount(3);

        //last move contains a kill, which is contained in the move and is also evaluated
        mGameboard.setPos(new Position(3,0), Options.Color.NOTHING);

        //result should be 500 +500 +500 -1000 = 500 (500 for having own pieces on the gameboard, -1000 for enemies piece

        int result = mStrategy.bewertung(mPlayerBlack);
        assertEquals(500, result);
    }

    @Test
    public void bewertungOfLoosingOrWinning() {

        mGameboard.setPos(new Position(0,0), Options.Color.BLACK);
        mGameboard.setPos(new Position(0,6), Options.Color.BLACK);
        mGameboard.setPos(new Position(0,6), Options.Color.BLACK);
        mGameboard.setPos(new Position(6,6), Options.Color.BLACK);
        mGameboard.setPos(new Position(6,3), Options.Color.BLACK);

        mGameboard.setPos(new Position(3,0), Options.Color.WHITE);
        mGameboard.setPos(new Position(3,2), Options.Color.WHITE);

        //white player has lost now

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        int result = mStrategy.bewertung(mPlayerBlack);
        assertEquals(mStrategy.MAX, result);

        result = mStrategy.bewertung(mPlayerWhite);
        assertEquals(mStrategy.MIN, result);
    }

}

package own.projects.lemiroapp;

import android.widget.ProgressBar;

import org.junit.Before;
import org.junit.Test;
import android.test.mock.MockContext;

import java.util.LinkedList;

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
        mPlayerBlack.setSetCount(5);
        mPlayerWhite.setSetCount(5);

    }

    @Test
    public void bewertungShouldBe0() {

        LinkedList<Zug> moves = new LinkedList<Zug>();
        moves.add(new Zug(null, null, new Position(0,0), null));
        moves.add(new Zug(null, null, new Position(3,0), null));
        moves.add(new Zug(null, null, new Position(6,6), null));

        executeMoveSeries(moves, mPlayerBlack);

        //result should be 500 +500 -1000 = 0 (500 for having own pieces on the gameboard, -1000 for enemies piece

        int result = mStrategy.bewertung(mPlayerBlack);
        assertEquals(0, result);
    }

    @Test
    public void bewertungOfMillShouldBe500() {

        LinkedList<Zug> moves = new LinkedList<Zug>();
        moves.add(new Zug(null, null, new Position(0,0), null));
        moves.add(new Zug(null, null, new Position(3,0), null));
        moves.add(new Zug(null, null, new Position(0,3), null));
        moves.add(new Zug(null, null, new Position(6,0), null));
        //last move contains a kill
        moves.add(new Zug(null, null, new Position(0,6), new Position(3,0)));

        executeMoveSeries(moves, mPlayerBlack);

        //result should be 500 +500 +500 -1000 = 500 (500 for having own pieces on the gameboard, -1000 for enemies piece

        int result = mStrategy.bewertung(mPlayerBlack);
        assertEquals(500, result);
    }

    @Test
    public void bewertungOfLoosingOrWinning() {

        //Game in which black player starts, and kills white player
        LinkedList<Zug> moves = new LinkedList<Zug>();
        moves.add(new Zug(null, null, new Position(0,0), null));
        moves.add(new Zug(null, null, new Position(3,0), null));
        moves.add(new Zug(null, null, new Position(0,6), null));
        moves.add(new Zug(null, null, new Position(3,2), null));
        moves.add(new Zug(null, null, new Position(0,3), new Position(3,0)));
        moves.add(new Zug(null, null, new Position(3,0), null));
        moves.add(new Zug(null, null, new Position(6,6), null));
        moves.add(new Zug(null, null, new Position(3,4), null));
        moves.add(new Zug(null, null, new Position(3,6), new Position(3,0)));
        moves.add(new Zug(null, null, new Position(3,0), null));
        moves.add(new Zug(new Position(6,3), new Position(6,6), null, null));
        moves.add(new Zug(new Position(4,4), new Position(3,4), null, null));
        moves.add(new Zug(new Position(6,6), new Position(6,3), null, new Position(3,0)));

        //white player has lost now

        executeMoveSeries(moves, mPlayerBlack);

        int result = mStrategy.bewertung(mPlayerBlack);
        assertEquals(mStrategy.MAX, result);

        result = mStrategy.bewertung(mPlayerWhite);
        assertEquals(mStrategy.MIN, result);
    }

    @Test
    public void addpossibleKillstoMove () {

    }

    public void executeMoveSeries (LinkedList<Zug> series, Player startingPlayer) {
        Player currentPlayer = startingPlayer;
        int nMoves = series.size();
        for(int i = 0; i<nMoves; i++){
            mGameboard.makeWholeMove(series.removeFirst(), currentPlayer);
            currentPlayer = currentPlayer.getOtherPlayer();
        }
    }
}

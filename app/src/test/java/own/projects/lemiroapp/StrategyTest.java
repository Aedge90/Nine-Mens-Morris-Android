package own.projects.lemiroapp;

import android.widget.ProgressBar;

import org.junit.Before;
import org.junit.Test;
import android.test.mock.MockContext;

import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;



public class StrategyTest {

    private Strategie strategy;

    private GameBoard mGameboard;

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
        moves.add(new Zug(new Position(0,0),null, null));
        moves.add(new Zug(new Position(3,0),null, null));
        moves.add(new Zug(new Position(6,6),null, null));

        executeMoveSeries(moves, mPlayerBlack);

        //result should be 500 +500 -1000 = 0 (500 for having own pieces on the gameboard, -1000 for enemies piece

        int result = mStrategy.bewertung(mPlayerBlack);
        assertEquals(0, result);
    }

    @Test
    public void bewertungOfMillShouldBe500() {

        LinkedList<Zug> moves = new LinkedList<Zug>();
        moves.add(new Zug(new Position(0,0), null, null));
        moves.add(new Zug(new Position(3,0), null, null));
        moves.add(new Zug(new Position(0,3), null, null));
        moves.add(new Zug(new Position(6,0), null, null));
        //last move contains a kill
        moves.add(new Zug(new Position(0,6), null, new Position(3,0)));

        executeMoveSeries(moves, mPlayerBlack);

        //result should be 500 +500 +500 -1000 = 500 (500 for having own pieces on the gameboard, -1000 for enemies piece

        int result = mStrategy.bewertung(mPlayerBlack);
        assertEquals(500, result);
    }

    @Test
    public void bewertungOfLoosingOrWinning() {

        //Game in which black player starts, and kills white player
        LinkedList<Zug> moves = new LinkedList<Zug>();
        moves.add(new Zug(new Position(0,0), null, null));
        moves.add(new Zug(new Position(3,0), null, null));
        moves.add(new Zug(new Position(0,6), null, null));
        moves.add(new Zug(new Position(3,2), null, null));
        moves.add(new Zug(new Position(0,3), null, new Position(3,0)));
        moves.add(new Zug(new Position(3,0), null, null));
        moves.add(new Zug(new Position(6,6), null, null));
        moves.add(new Zug(new Position(3,4), null, null));
        moves.add(new Zug(new Position(3,6), null, new Position(3,0)));
        moves.add(new Zug(new Position(3,0), null, null));
        moves.add(new Zug(new Position(6,3), new Position(6,6), null));
        moves.add(new Zug(new Position(4,4), new Position(3,4), null));
        moves.add(new Zug(new Position(6,6), new Position(6,3), new Position(3,0)));

        //white player has lost now

        executeMoveSeries(moves, mPlayerBlack);

        int result = mStrategy.bewertung(mPlayerBlack);
        assertEquals(mStrategy.MAX, result);

        result = mStrategy.bewertung(mPlayerWhite);
        assertEquals(mStrategy.MIN, result);
    }

    @Test
    public void addpossibleKillstoMove () {

        LinkedList<Zug> possibleMovessoFar = new LinkedList<Zug>();

        LinkedList<Zug> moves = new LinkedList<Zug>();
        moves.add(new Zug(new Position(0,0), null, null));
        moves.add(new Zug(new Position(3,0), null, null));
        moves.add(new Zug(new Position(0,6), null, null));
        moves.add(new Zug(new Position(3,2), null, null));

        executeMoveSeries(moves, mPlayerBlack);

        //black closes his mill, kill should be added to this move
        Zug killMove = new Zug(new Position(0,3), null, null);
        mStrategy.addpossibleKillstoMove(possibleMovessoFar, killMove, mPlayerBlack);

        Zug expected0 = new Zug(new Position(0,3), null, new Position(3,0));
        Zug expected1 = new Zug(new Position(0,3), null, new Position(3,2));

        assertEquals(2, possibleMovessoFar.size());
        assertEquals(expected0, possibleMovessoFar.get(0));
        assertEquals(expected1, possibleMovessoFar.get(1));

        killMove = possibleMovessoFar.get(1);

        //now (3,2) of white is actually killed
        mGameboard.executeCompleteTurn(killMove, mPlayerBlack);

        possibleMovessoFar = new LinkedList<Zug>();

        //now white sets to (6,6)
        Zug nextMove = new Zug(new Position(6,6), null, null);
        mStrategy.addpossibleKillstoMove(possibleMovessoFar, nextMove, mPlayerWhite);

        //assert that white can not kill
        assertEquals(1, possibleMovessoFar.size());
        assertEquals(nextMove, possibleMovessoFar.get(0));

    }

    public void executeMoveSeries (LinkedList<Zug> series, Player startingPlayer) {
        Player currentPlayer = startingPlayer;
        int nMoves = series.size();
        for(int i = 0; i<nMoves; i++){
            mGameboard.executeCompleteTurn(series.removeFirst(), currentPlayer);
            currentPlayer = currentPlayer.getOtherPlayer();
        }
    }

    @Test
    public void addpossibleKillstoMove_ShouldNotAddKill(){
        Options.Color WHITE = Options.Color.WHITE;
        Options.Color BLACK = Options.Color.BLACK;
        Options.Color NOTHING = Options.Color.NOTHING;
        Options.Color I = Options.Color.INVALID;

        Options.Color[][] field = {{BLACK, I, I,    BLACK ,I ,I ,    NOTHING},
                                  { I, I, I, I, I, I, I },
                                  { I, I,    WHITE, WHITE, BLACK,   I, I },
                                  {NOTHING,I,BLACK  ,I ,   NOTHING,I,WHITE},
                                  { I, I,    WHITE, BLACK ,NOTHING, I, I },
                                  { I, I, I, I, I, I, I },
                                  {WHITE, I, I,  NOTHING, I, I,      NOTHING}};

        mGameboard = new Mill5(field);

        LinkedList<Zug> possibleMovessoFar = new LinkedList<Zug>();

        Zug move = new Zug(new Position(6,6), null, null);

        mStrategy.addpossibleKillstoMove(possibleMovessoFar, move, mPlayerBlack);

        //assert that black can not kill in this scenario
        assertEquals(1, possibleMovessoFar.size());
        assertEquals(move, possibleMovessoFar.get(0));

    }
}

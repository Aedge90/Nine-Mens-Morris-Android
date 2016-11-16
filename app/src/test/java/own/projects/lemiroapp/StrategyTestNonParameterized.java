package own.projects.lemiroapp;

import android.graphics.Path;
import android.widget.ProgressBar;

import org.junit.Before;
import org.junit.Test;
import android.test.mock.MockContext;

import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;


public class StrategyTestNonParameterized {

    private Player mPlayerBlack;
    private Player mPlayerWhite;

    private final Options.Color B = Options.Color.BLACK;
    private final Options.Color W = Options.Color.WHITE;
    private final Options.Color N = Options.Color.NOTHING;
    private final Options.Color I = Options.Color.INVALID;

    @Before
    public void beforeTests() {

        mPlayerBlack = new Player(Options.Color.BLACK);
        mPlayerWhite = new Player(Options.Color.WHITE);
        mPlayerBlack.setOtherPlayer(mPlayerWhite);
        mPlayerWhite.setOtherPlayer(mPlayerBlack);
        mPlayerBlack.setSetCount(5);
        mPlayerWhite.setSetCount(5);

    }

    @Test
    public void bewertungMinimizingPlayeronNormalDiffShouldBe2() {

        Options.Color[][] mill5 =
                {{N, I, I, B, I, I, W},
                { I, I, I, I, I, I, I},
                { I, I, B, N, W, I, I},
                { B, I, B, I, N, I, N},
                { I, I, W, N, N, I, I},
                { I, I, I, I, I, I, I},
                { B, I, I, W, I, I, N}};

        GameBoard gameBoard = new Mill5(mill5);
        Strategie strategy = new Strategie(gameBoard,null);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        gameBoard.executeCompleteTurn(new Zug(new Position(0,0), new Position(3,0), new Position(4,2)), mPlayerBlack);
        gameBoard.executeCompleteTurn(new Zug(new Position(6,3), new Position(6,0), null), mPlayerWhite);
        gameBoard.executeCompleteTurn(new Zug(new Position(3,0), new Position(0,0), null), mPlayerBlack);

        //minimizing player evaluates now
        int result = strategy.bewertung(mPlayerWhite, strategy.possibleMoves(mPlayerWhite));

        assertEquals(2, result);
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

        GameBoard gameBoard = new Mill5();

        executeMoveSeries(gameBoard, moves, mPlayerBlack);

        Strategie strategy = new Strategie(gameBoard, null);

        LinkedList<Zug> possibleMoves = strategy.possibleMoves(mPlayerWhite);
        assertEquals(0, possibleMoves.size());

        //minimizing players worst case is MAX. black is the maximizing player
        int result = strategy.bewertung(mPlayerWhite, possibleMoves);
        assertEquals(strategy.MAX, result);
    }

    @Test
    public void addpossibleKillstoMove () {

        GameBoard gameBoard = new Mill5();
        Strategie strategy = new Strategie(gameBoard, null);

        LinkedList<Zug> possibleMovessoFar = new LinkedList<Zug>();

        LinkedList<Zug> moves = new LinkedList<Zug>();
        moves.add(new Zug(new Position(0,0), null, null));
        moves.add(new Zug(new Position(3,0), null, null));
        moves.add(new Zug(new Position(0,6), null, null));
        moves.add(new Zug(new Position(3,2), null, null));

        executeMoveSeries(gameBoard, moves, mPlayerBlack);

        //black closes his mill, kill should be added to this move
        Zug killMove = new Zug(new Position(0,3), null, null);
        strategy.addpossibleKillstoMove(possibleMovessoFar, killMove, mPlayerBlack);

        Zug expected0 = new Zug(new Position(0,3), null, new Position(3,0));
        Zug expected1 = new Zug(new Position(0,3), null, new Position(3,2));

        assertEquals(2, possibleMovessoFar.size());
        assertEquals(expected0, possibleMovessoFar.get(0));
        assertEquals(expected1, possibleMovessoFar.get(1));

        killMove = possibleMovessoFar.get(1);

        //now (3,2) of white is actually killed
        gameBoard.executeCompleteTurn(killMove, mPlayerBlack);

        possibleMovessoFar = new LinkedList<Zug>();

        //now white sets to (6,6)
        Zug nextMove = new Zug(new Position(6,6), null, null);
        strategy.addpossibleKillstoMove(possibleMovessoFar, nextMove, mPlayerWhite);

        //assert that white can not kill
        assertEquals(1, possibleMovessoFar.size());
        assertEquals(nextMove, possibleMovessoFar.get(0));

    }

    public void executeMoveSeries (GameBoard gameBoard, LinkedList<Zug> series, Player startingPlayer) {
        Player currentPlayer = startingPlayer;
        int nMoves = series.size();
        for(int i = 0; i<nMoves; i++){
            gameBoard.executeCompleteTurn(series.removeFirst(), currentPlayer);
            currentPlayer = currentPlayer.getOtherPlayer();
        }
    }

    @Test
    public void addpossibleKillstoMove_ShouldNotAddKill(){

        Options.Color[][] field = {{B, I, I, B, I, I, N},
                                  { I, I, I, I, I, I, I },
                                  { I, I, W, W, B, I, I },
                                  { N, I, B, I, N, I, W },
                                  { I, I, W, B, N, I, I },
                                  { I, I, I, I, I, I, I },
                                  { W, I, I, N, I, I, N}};

        GameBoard gameBoard = new Mill5(field);
        Strategie strategy = new Strategie(gameBoard, null);

        LinkedList<Zug> possibleMovessoFar = new LinkedList<Zug>();

        Zug move = new Zug(new Position(6,6), null, null);

        strategy.addpossibleKillstoMove(possibleMovessoFar, move, mPlayerBlack);

        //assert that black can not kill in this scenario
        assertEquals(1, possibleMovessoFar.size());
        assertEquals(move, possibleMovessoFar.get(0));

    }
}

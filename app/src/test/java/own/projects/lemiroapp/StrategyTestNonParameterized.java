package own.projects.lemiroapp;

import org.junit.Before;
import org.junit.Test;

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
    public void bewertungOfLoosingOrWinning() {

        //Game in which black player starts, and kills white player
        LinkedList<Move> moves = new LinkedList<Move>();
        moves.add(new Move(new Position(0,0), null, null));
        moves.add(new Move(new Position(3,0), null, null));
        moves.add(new Move(new Position(0,6), null, null));
        moves.add(new Move(new Position(3,2), null, null));
        moves.add(new Move(new Position(0,3), null, new Position(3,0)));
        moves.add(new Move(new Position(3,0), null, null));
        moves.add(new Move(new Position(6,6), null, null));
        moves.add(new Move(new Position(3,4), null, null));
        moves.add(new Move(new Position(3,6), null, new Position(3,0)));
        moves.add(new Move(new Position(3,0), null, null));
        moves.add(new Move(new Position(6,3), new Position(6,6), null));
        moves.add(new Move(new Position(4,4), new Position(3,4), null));
        moves.add(new Move(new Position(6,6), new Position(6,3), new Position(3,0)));

        //white player has lost now

        GameBoard gameBoard = new Mill5();

        executeMoveSeries(gameBoard, moves, mPlayerBlack);

        //black is the maximizing player
        Strategy strategy = new Strategy(gameBoard, mPlayerBlack, null);

        LinkedList<Move> possibleMoves = strategy.possibleMoves(mPlayerWhite);
        assertEquals(0, possibleMoves.size());

        //minimizing players worst case is MAX. black is the maximizing player
        int result = strategy.bewertung(mPlayerWhite, possibleMoves,0);
        assertEquals(strategy.MAX, result);
    }

    @Test
    public void addpossibleKillstoMove () {

        GameBoard gameBoard = new Mill5();
        Strategy strategyBlack = new Strategy(gameBoard, mPlayerBlack, null);
        Strategy strategyWhite = new Strategy(gameBoard, mPlayerWhite, null);

        LinkedList<Move> possibleMovessoFar = new LinkedList<Move>();

        LinkedList<Move> moves = new LinkedList<Move>();
        moves.add(new Move(new Position(0,0), null, null));
        moves.add(new Move(new Position(3,0), null, null));
        moves.add(new Move(new Position(0,6), null, null));
        moves.add(new Move(new Position(3,2), null, null));

        executeMoveSeries(gameBoard, moves, mPlayerBlack);

        //black closes his mill, kill should be added to this move
        Move killMove = new Move(new Position(0,3), null, null);
        strategyBlack.addpossibleKillstoMove(possibleMovessoFar, killMove, mPlayerBlack);

        Move expected0 = new Move(new Position(0,3), null, new Position(3,0));
        Move expected1 = new Move(new Position(0,3), null, new Position(3,2));

        assertEquals(2, possibleMovessoFar.size());
        assertEquals(expected0, possibleMovessoFar.get(0));
        assertEquals(expected1, possibleMovessoFar.get(1));

        killMove = possibleMovessoFar.get(1);

        //now (3,2) of white is actually killed
        gameBoard.executeCompleteTurn(killMove, mPlayerBlack);

        possibleMovessoFar = new LinkedList<Move>();

        //now white sets to (6,6)
        Move nextMove = new Move(new Position(6,6), null, null);
        strategyWhite.addpossibleKillstoMove(possibleMovessoFar, nextMove, mPlayerWhite);

        //assert that white can not kill
        assertEquals(1, possibleMovessoFar.size());
        assertEquals(nextMove, possibleMovessoFar.get(0));

    }

    public void executeMoveSeries (GameBoard gameBoard, LinkedList<Move> series, Player startingPlayer) {
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
        Strategy strategy = new Strategy(gameBoard, mPlayerBlack, null);

        LinkedList<Move> possibleMovessoFar = new LinkedList<Move>();

        Move move = new Move(new Position(6,6), null, null);

        strategy.addpossibleKillstoMove(possibleMovessoFar, move, mPlayerBlack);

        //assert that black can not kill in this scenario
        assertEquals(1, possibleMovessoFar.size());
        assertEquals(move, possibleMovessoFar.get(0));

    }

    @Test
    public void getPossibleMovesShouldHaveSize24(){

        GameBoard gameBoard = new Mill9();

        mPlayerBlack.setSetCount(9);

        Strategy strategy = new Strategy(gameBoard, mPlayerBlack, null);

        LinkedList<Move> moves = strategy.possibleMoves(mPlayerBlack);

        assertEquals(24, moves.size());

    }

    @Test
    public void getPossibleMovesShouldHaveSize56(){

        final Options.Color B = Options.Color.BLACK;
        final Options.Color W = Options.Color.WHITE;
        final Options.Color N = Options.Color.NOTHING;
        final Options.Color I = Options.Color.INVALID;

        Options.Color[][] mill9 =

                {{N, I, I, N, I, I, N},
                { I, N, I, B, I, N, I},
                { I, I, N, N, N, I, I},
                { N, N, N, I, N, N, N},
                { I, I, W, N, N, I, I},
                { I, W, I, B, I, N, I},
                { W, I, I, B, I, I, N}};

        GameBoard gameBoard = new Mill9(mill9);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        Strategy strategy = new Strategy(gameBoard, mPlayerBlack, null);

        LinkedList<Move> moves = strategy.possibleMoves(mPlayerBlack);

        //minus one, as one move is replaced with 3 moves containing different kill targets
        assertEquals(3*18 - 1 + 3, moves.size());

    }

}

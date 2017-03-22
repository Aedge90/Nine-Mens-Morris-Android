package com.github.aedge90.nmm;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class GameBoardTestNonParameterized {

    private GameBoard mGameBoard;
    private Player mPlayerWhite;
    private Player mPlayerBlack;

    private final Options.Color B = Options.Color.BLACK;
    private final Options.Color W = Options.Color.WHITE;
    private final Options.Color N = Options.Color.NOTHING;
    private final Options.Color I = Options.Color.INVALID;

    @Before
    public void setUp(){
        mGameBoard = new Mill9();
        mPlayerBlack = new Player(Options.Color.BLACK);
        mPlayerWhite = new Player(Options.Color.WHITE);
        mPlayerBlack.setOtherPlayer(mPlayerWhite);
        mPlayerWhite.setOtherPlayer(mPlayerBlack);
        mPlayerBlack.setSetCount(5);
        mPlayerWhite.setSetCount(5);
    }


    @Test
    public void executeCompleteTurn_WithSetMoveOnNonEmptyPosShouldThrowException(){

        GameBoard mGameBoard = new Mill9();

        Position p = new Position(6,6);
        try {
            mPlayerBlack.setSetCount(5);
            mPlayerWhite.setSetCount(5);
            mGameBoard.executeCompleteTurn(new Move(p, null, null), mPlayerBlack);
            mGameBoard.executeCompleteTurn(new Move(p, null, null), mPlayerWhite);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            String expectedMessage = "is trying to set to an occupied field by";
            if(!e.getMessage().contains(expectedMessage)){
                fail("expected message to contain: " + expectedMessage + "\n" + "but was: " + e.getMessage());
            }
        }


    }

    @Test
    public void executeCompleteTurn_WithMoveMoveOnNonEmptyPosShouldThrowException(){

        Position src1 = new Position(6,0);
        Position src2 = new Position(6,6);
        Position dest = new Position(6,3);

        try {
            mPlayerBlack.setSetCount(1);
            mPlayerWhite.setSetCount(1);
            mGameBoard.executeCompleteTurn(new Move(src1, null, null), mPlayerBlack);
            mGameBoard.executeCompleteTurn(new Move(src2, null, null), mPlayerWhite);
            mGameBoard.executeCompleteTurn(new Move(dest, src1, null), mPlayerBlack);
            mGameBoard.executeCompleteTurn(new Move(dest, src2, null), mPlayerWhite);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            String expectedMessage = "is trying to move to an occupied field by";
            if(!e.getMessage().contains(expectedMessage)){
                fail("expected message to contain: " + expectedMessage + "\n" + "but was: " + e.getMessage());
            }
        }

    }

    @Test
    public void executeCompleteTurn_KillOwnPieceShouldThrowException(){

        Position p = new Position(6,6);
        try {
            mPlayerBlack.setSetCount(5);
            mGameBoard.executeCompleteTurn(new Move(p, null, p), mPlayerBlack);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            String expectedMessageStart = "Trying to kill own piece of color";
            if(!e.getMessage().startsWith(expectedMessageStart)){
                fail("expected message to start with: " + expectedMessageStart + "\n" + "but was: " + e.getMessage());
            }
        }

    }


    @Test
    public void executeCompleteTurn_KillNotExistingPieceShouldThrowException(){

        Position set = new Position(0,0);
        Position kill = new Position(6,3);

        try {
            GameBoard emptyGameBoard = mGameBoard.getCopy();
            mPlayerBlack.setSetCount(5);
            emptyGameBoard.executeCompleteTurn(new Move(set, null, kill), mPlayerBlack);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            String expectedMessageEnd = "is trying to kill an empty field";
            if(!e.getMessage().endsWith(expectedMessageEnd)){
                fail("expected message to end with: " + expectedMessageEnd + "\n" + "but was: " + e.getMessage());
            }
        }

    }

    @Test
    public void getMill_ShouldBeNullForMill7(){

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, W, I, B, I},
                { I, I, I, I, I, I, I},
                { N, W, I, W, I, W, N},
                { I, I, I, I, I, I, I},
                { I, B, I, W, I, B, I},
                { B, I, I, N, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        Position[] mill1 = gameBoard.getMill(new Position(1,3), W);
        assertNull(mill1);
        Position[] mill2 = gameBoard.getMill(new Position(3,3), W);
        assertNull(mill2);
        Position[] mill3 = gameBoard.getMill(new Position(5,3), W);
        assertNull(mill3);

        Position[] mill4 = gameBoard.getMill(new Position(3,1), W);
        assertNull(mill4);
        Position[] mill5 = gameBoard.getMill(new Position(3,5), W);
        assertNull(mill5);

    }

    @Test
    public void isInMill_ShouldBeTrueForMill7 () {

        //Tests if isInMill is true even if there are FOUR neighbouring pieces in one direction

        Options.Color[][] mill7 =

                {{N, I, I, W, I, I, B},
                { I, W, I, N, I, W, I},
                { I, I, I, I, I, I, I},
                { B, W, I, W, I, N, W},
                { I, I, I, I, I, I, I},
                { I, B, I, W, I, N, I},
                { B, I, I, N, I, I, B}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        Move m1 = new Move(new Position(5,3), new Position(5,1), new Position(0,6));

        gameBoard.executeSetOrMovePhase(m1, mPlayerWhite);

        assertTrue(gameBoard.isInMill(m1.getDest(), mPlayerWhite.getColor()));

        Move m2 = new Move(new Position(3,1), new Position(1,1), new Position(6,6));

        gameBoard.executeSetOrMovePhase(m2, mPlayerWhite);

        assertTrue(gameBoard.isInMill(m2.getDest(), mPlayerWhite.getColor()));

    }

    @Test
    public void isInMill_ShouldBeFalseForMill7 () {

        Options.Color[][] mill7 =

                {{N, I, I, B, I, I, N},
                { I, N, I, N, I, N, I},
                { I, I, I, I, I, I, I},
                { B, W, I, W, I, N, N},
                { I, I, I, I, I, I, I},
                { I, N, I, B, I, N, I},
                { W, I, I, W, I, I, B}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(3);
        mPlayerWhite.setSetCount(3);

        Move m1 = new Move(new Position(5,3), null, null);

        assertFalse(gameBoard.isInMill(m1.getDest(), mPlayerWhite.getColor()));

    }

    @Test
    public void getPositionsShouldbeOfCorrectSize () {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, W, I, B, I},
                { I, I, I, I, I, I, I},
                { N, W, I, N, I, W, N},
                { I, I, I, I, I, I, I},
                { I, B, I, W, I, B, I},
                { B, I, I, N, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(1);
        mPlayerWhite.setSetCount(1);

        gameBoard.executeCompleteTurn(new Move(new Position(6,3), null, null), mPlayerWhite);
        assertEquals(5, gameBoard.getPositions(mPlayerWhite.getColor()).size());

        gameBoard.executeCompleteTurn(new Move(new Position(0,0), null, null), mPlayerBlack);
        assertEquals(5, gameBoard.getPositions(mPlayerBlack.getColor()).size());

        gameBoard.executeCompleteTurn(new Move(new Position(3,3), new Position(3,1), new Position(0,6)), mPlayerWhite);
        assertEquals(5, gameBoard.getPositions(mPlayerWhite.getColor()).size());
        assertEquals(4, gameBoard.getPositions(mPlayerBlack.getColor()).size());

        gameBoard.reverseCompleteTurn(new Move(new Position(3,3), new Position(3,1), new Position(0,6)), mPlayerWhite);
        assertEquals(5, gameBoard.getPositions(mPlayerWhite.getColor()).size());
        assertEquals(5, gameBoard.getPositions(mPlayerBlack.getColor()).size());

    }

    @Test
    public void preventedMillShouldReturnTrue1() {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, W, I, B, I},
                { I, I, I, I, I, I, I},
                { N, W, I, W, I, N, B},
                { I, I, I, I, I, I, I},
                { I, B, I, W, I, B, I},
                { B, I, I, N, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        assertTrue(gameBoard.preventedMill(new Position(5,3), mPlayerWhite));

    }

    @Test
    public void preventedMillShouldReturnFalse2() {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, W, I, N, I},
                { I, I, I, I, I, I, I},
                { N, W, I, W, I, N, B},
                { I, I, I, I, I, I, I},
                { I, B, I, W, I, B, I},
                { B, I, I, N, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        assertFalse(gameBoard.preventedMill(new Position(5,3), mPlayerWhite));

    }

    @Test
    public void preventedMillShouldReturnTrue2() {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, W, I, B, I},
                { I, I, I, I, I, I, I},
                { N, W, I, W, I, N, N},
                { I, I, I, I, I, I, I},
                { I, N, I, W, I, B, I},
                { B, I, I, N, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        assertTrue(gameBoard.preventedMill(new Position(5,3), mPlayerWhite));

    }

    @Test
    public void preventedMillShouldReturnFalse3() {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, W, I, B, I},
                { I, I, I, I, I, I, I},
                { N, W, I, W, I, N, N},
                { I, I, I, I, I, I, I},
                { I, B, I, W, I, B, I},
                { B, I, I, N, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        assertFalse(gameBoard.preventedMill(new Position(5,3), mPlayerWhite));

    }

    @Test
    public void isInNPotentialMillsShouldBeOne1 () {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, N, I, B, I},
                { I, I, I, I, I, I, I},
                { N, N, I, N, I, N, N},
                { I, I, I, I, I, I, I},
                { I, N, I, N, I, N, I},
                { N, I, I, W, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        assertEquals(1, gameBoard.isInNPotentialMills(new Position(6,6), mPlayerWhite.getColor()));

    }

    @Test
    public void isInNPotentialMillsShouldBeOne2 () {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, N, I, B, I},
                { I, I, I, I, I, I, I},
                { N, N, I, N, I, N, N},
                { I, I, I, I, I, I, I},
                { I, N, I, N, I, N, I},
                { N, I, I, N, I, I, W}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        assertEquals(1, gameBoard.isInNPotentialMills(new Position(3,6), mPlayerWhite.getColor()));

    }

    @Test
    public void isInNPotentialMillsShouldBeOne3 () {

        Options.Color[][] mill5 =
                {{N , I , I , N , I , I , N },
                { I , I , I , I , I , I , I },
                { I , I , N , N , N , I , I },
                { N , I , N , I , W , I , N },
                { I , I , N , N , N , I , I },
                { I , I , I , I , I , I , I },
                { N , I , I , B , I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);

        mPlayerBlack.setSetCount(4);
        mPlayerWhite.setSetCount(4);

        assertEquals(1, gameBoard.isInNPotentialMills(new Position(4,2), mPlayerWhite.getColor()));

    }

    @Test
    public void isInNPotentialMillsShouldBeOne4 () {

        //check if this returns 1 for Player White while the position is occupied by black
        //it should be 1 as the color of the position itself is not checked

        Options.Color[][] mill9 =
            {{N , I , I , N , I , I , N },
            { I , N , I , N , I , N , I },
            { I , I , W , N , N , I , I },
            { N , N , B , I , B , N , N },
            { I , I , N , N , W , I , I },
            { I , N , I , N , I , N , I },
            { N , I , I , N , I , I , N}};

        GameBoard gameBoard = new Mill9(mill9);

        mPlayerBlack.setSetCount(7);
        mPlayerWhite.setSetCount(8);

        assertEquals(1, gameBoard.isInNPotentialMills(new Position(4,3), mPlayerWhite.getColor()));


    }

    @Test
    public void isInNPotentialMillsShouldBeZero () {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, N, I, B, I},
                { I, I, I, I, I, I, I},
                { N, N, I, N, I, N, N},
                { I, I, I, I, I, I, I},
                { I, N, I, N, I, N, I},
                { B, I, I, W, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        assertEquals(0, gameBoard.isInNPotentialMills(new Position(6,6), mPlayerWhite.getColor()));

    }

    @Test
    public void isInPotentialMillShouldBeTwo () {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, N},
                { I, N, I, N, I, N, I},
                { I, I, I, I, I, I, I},
                { N, N, I, N, I, W, N},
                { I, I, I, I, I, I, I},
                { I, N, I, N, I, N, I},
                { N, I, I, N, I, I, W}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        assertEquals(2, gameBoard.isInNPotentialMills(new Position(6,3), mPlayerWhite.getColor()));

    }

    @Test
    public void isInNPotentialMillsShouldBeZeroForMill7 () {

        Options.Color[][] mill7 =

                {{N, I, I, N, I, I, W},
                { I, N, I, N, I, N, I},
                { I, I, I, I, I, I, I},
                { N, N, I, N, I, N, N},
                { I, I, I, I, I, I, I},
                { I, B, I, W, I, B, I},
                { N, I, I, B, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(5);
        mPlayerWhite.setSetCount(5);

        assertEquals(0, gameBoard.isInNPotentialMills(new Position(3,3), mPlayerWhite.getColor()));

    }

    @Test
    public void getStateShouldBeDraw () {

        Options.Color[][] mill7 =

                {{B, I, I, N, I, I, N},
                { I, N, I, W, I, B, I},
                { I, I, I, I, I, I, I},
                { W, W, I, N, I, B, N},
                { I, I, I, I, I, I, I},
                { I, W, I, W, I, B, I},
                { B, I, I, N, I, I, N}};

        GameBoard gameBoard = new Mill7(mill7);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        gameBoard.executeCompleteTurn(new Move(new Position(3,3), new Position(3,1), null), mPlayerWhite);
        gameBoard.executeCompleteTurn(new Move(new Position(3,6), new Position(0,6), null), mPlayerBlack);
        gameBoard.executeCompleteTurn(new Move(new Position(3,1), new Position(3,3), null), mPlayerWhite);
        gameBoard.executeCompleteTurn(new Move(new Position(0,6), new Position(3,6), null), mPlayerBlack);
        gameBoard.executeCompleteTurn(new Move(new Position(1,1), new Position(3,1), new Position(0,6)), mPlayerWhite);
        //now remisCount should be reset

        int i = 0;
        while(i <= GameBoard.REMISMAX){
            gameBoard.executeCompleteTurn(new Move(new Position(3,0), new Position(0,0), null), mPlayerBlack);
            i++;
            getStateShouldBeDraw_AssertDRAWOrRUNNING(i, gameBoard, mPlayerBlack);
            gameBoard.executeCompleteTurn(new Move(new Position(0,6), new Position(0,3), null), mPlayerWhite);
            i++;
            getStateShouldBeDraw_AssertDRAWOrRUNNING(i, gameBoard, mPlayerWhite);
            gameBoard.executeCompleteTurn(new Move(new Position(0,0), new Position(3,0), null), mPlayerBlack);
            i++;
            getStateShouldBeDraw_AssertDRAWOrRUNNING(i, gameBoard, mPlayerBlack);
            gameBoard.executeCompleteTurn(new Move(new Position(0,3), new Position(0,6), null), mPlayerWhite);
            i++;
            getStateShouldBeDraw_AssertDRAWOrRUNNING(i, gameBoard, mPlayerWhite);
        }

    }

    public void getStateShouldBeDraw_AssertDRAWOrRUNNING (int turnSinceLastKill, GameBoard gameBoard, Player executingPlayer) {
        if(turnSinceLastKill >= GameBoard.REMISMAX){
            assertEquals(GameBoard.GameState.REMIS, gameBoard.getState(executingPlayer));
        }else{
            assertEquals(GameBoard.GameState.RUNNING, gameBoard.getState(executingPlayer));
        }
    }

    @Test
    public void addpossibleKillstoMove () {

        GameBoard gameBoard = new Mill5();

        LinkedList<Move> possibleMovessoFar = new LinkedList<Move>();

        LinkedList<Move> moves = new LinkedList<Move>();
        moves.add(new Move(new Position(0,0), null, null));
        moves.add(new Move(new Position(3,0), null, null));
        moves.add(new Move(new Position(0,6), null, null));
        moves.add(new Move(new Position(3,2), null, null));

        executeMoveSeries(gameBoard, moves, mPlayerBlack);

        //black closes his mill, kill should be added to this move
        Move killMove = new Move(new Position(0,3), null, null);
        gameBoard.addpossibleKillstoMove(possibleMovessoFar, killMove, mPlayerBlack);

        Move expected0 = new Move(new Position(0,3), null, new Position(3,0));
        Move expected1 = new Move(new Position(0,3), null, new Position(3,2));

        assertEquals(2, possibleMovessoFar.size());
        assertThat( possibleMovessoFar.get(0), anyOf(is(expected0), is(expected1)));
        assertThat( possibleMovessoFar.get(1), anyOf(is(expected0), is(expected1)));

        killMove = possibleMovessoFar.get(1);

        //now (3,2) of white is actually killed
        gameBoard.executeCompleteTurn(killMove, mPlayerBlack);

        possibleMovessoFar = new LinkedList<Move>();

        //now white sets to (6,6)
        Move nextMove = new Move(new Position(6,6), null, null);
        gameBoard.addpossibleKillstoMove(possibleMovessoFar, nextMove, mPlayerWhite);

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

        LinkedList<Move> possibleMovessoFar = new LinkedList<Move>();

        Move move = new Move(new Position(6,6), null, null);

        gameBoard.addpossibleKillstoMove(possibleMovessoFar, move, mPlayerBlack);

        //assert that black can not kill in this scenario
        assertEquals(1, possibleMovessoFar.size());
        assertEquals(move, possibleMovessoFar.get(0));

    }

    @Test
    public void getPossibleMovesShouldHaveSize24(){

        GameBoard gameBoard = new Mill9();

        mPlayerBlack.setSetCount(9);

        LinkedList<Move> moves = gameBoard.possibleMoves(mPlayerBlack);

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

        LinkedList<Move> moves = gameBoard.possibleMoves(mPlayerBlack);

        //minus one, as one move is replaced with 3 moves containing different kill targets
        assertEquals(3*18 - 1 + 3, moves.size());

    }

    @Test
    public void getPossibleMovesShouldHaveKillsAtBeginning(){

        final Options.Color B = Options.Color.BLACK;
        final Options.Color W = Options.Color.WHITE;
        final Options.Color N = Options.Color.NOTHING;
        final Options.Color I = Options.Color.INVALID;

        Options.Color[][] mill5 =
                {{N , I , I , B , I , I , W },
                        { I , I , I , I , I , I , I },
                        { I , I , B , N , W , I , I },
                        { B , I , B , I , N , I , N },
                        { I , I , N , N , W , I , I },
                        { I , I , I , I , I , I , I },
                        { B , I , I , W , I , I , N}};

        GameBoard gameBoard = new Mill5(mill5);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        LinkedList<Move> moves = gameBoard.possibleMoves(mPlayerBlack);

        assertEquals(4 + 4, moves.size());

        assertTrue(moves.get(0).getKill() != null);
        assertTrue(moves.get(1).getKill() != null);
        assertTrue(moves.get(2).getKill() != null);
        assertTrue(moves.get(3).getKill() != null);

    }

}

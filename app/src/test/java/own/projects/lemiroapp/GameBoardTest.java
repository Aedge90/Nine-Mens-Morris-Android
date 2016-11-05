package own.projects.lemiroapp;

import org.junit.Test;

import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class GameBoardTest {

    @Test
    public void getPossibleMillX_DoesNotChangeYValue() {
        Spielfeld gameBoard = new Mill5();
        getPossibleMillX_DoesNotChangeYValue( gameBoard);
        gameBoard = new Mill7();
        getPossibleMillX_DoesNotChangeYValue( gameBoard);
        gameBoard = new Mill9();
        getPossibleMillX_DoesNotChangeYValue( gameBoard);
    }

    public void getPossibleMillX_DoesNotChangeYValue(Spielfeld gameBoard) {

        Position[] possibleMillX;

        //assert that y value does not change for a mill in x direction
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                //its okay to throw an exception for impossible positions as all positions are tried here
                try {
                    possibleMillX = gameBoard.getPossibleMillX(p);
                    //may be null if there are no possible mills in x direction for this position
                    if (possibleMillX != null) {
                        assertEquals(p.getY(), possibleMillX[0].getY());
                        assertEquals(p.getY(), possibleMillX[1].getY());
                        assertEquals(p.getY(), possibleMillX[2].getY());
                    }
                } catch (IllegalArgumentException e) {
                }

            }
        }
    }

    @Test
    public void getPossibleMillY_DoesNotChangeXValue() {
        Spielfeld gameBoard = new Mill5();
        getPossibleMillY_DoesNotChangeXValue(gameBoard);
        gameBoard = new Mill7();
        getPossibleMillY_DoesNotChangeXValue(gameBoard);
        gameBoard = new Mill9();
        getPossibleMillY_DoesNotChangeXValue(gameBoard);
    }

    public void getPossibleMillY_DoesNotChangeXValue(Spielfeld gameBoard) {

        Position[] possibleMillY;

        //assert that x value does not change for a mill in y direction
        for (int x=0; x<7; x++) {
            for (int y=0; y<7; y++) {
                Position p = new Position(x, y);
                //its okay to throw an exception for impossible positions as all positions are tried here
                try{
                    possibleMillY = gameBoard.getPossibleMillY(p);
                    //may be null if there are no possible mills in y direction for this position
                    if(possibleMillY != null) {
                        assertEquals(p.getX(), possibleMillY[0].getX());
                        assertEquals(p.getX(), possibleMillY[1].getX());
                        assertEquals(p.getX(), possibleMillY[2].getX());
                    }
                }catch(IllegalArgumentException e){
                }

            }
        }
    }


    @Test
    public void makeWholeMove_WithSetMoveOnNonEmptyPosShouldThrowException() {
        Spielfeld gameBoard = new Mill5();
        makeWholeMove_WithSetMoveOnNonEmptyPosShouldThrowException(gameBoard);
        gameBoard = new Mill7();
        makeWholeMove_WithSetMoveOnNonEmptyPosShouldThrowException(gameBoard);
        gameBoard = new Mill9();
        makeWholeMove_WithSetMoveOnNonEmptyPosShouldThrowException(gameBoard);
    }

    public void makeWholeMove_WithSetMoveOnNonEmptyPosShouldThrowException(Spielfeld gameBoard){

        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(gameBoard.isValid(p)) {
                    try {
                        Player playerBlack = new Player(Options.Color.BLACK);
                        Player playerWhite = new Player(Options.Color.WHITE);
                        playerBlack.setSetCount(5);
                        playerWhite.setSetCount(5);
                        gameBoard.executeCompleteTurn(new Zug(p, null, null), playerBlack);
                        gameBoard.executeCompleteTurn(new Zug(p, null, null), playerWhite);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

    @Test
    public void makeWholeMove_WithMoveMoveOnNonEmptyPosShouldThrowException() {
        Spielfeld gameBoard = new Mill5();
        makeWholeMove_WithMoveMoveOnNonEmptyPosShouldThrowException(gameBoard);
        gameBoard = new Mill7();
        makeWholeMove_WithMoveMoveOnNonEmptyPosShouldThrowException(gameBoard);
        gameBoard = new Mill9();
        makeWholeMove_WithMoveMoveOnNonEmptyPosShouldThrowException(gameBoard);
    }

    public void makeWholeMove_WithMoveMoveOnNonEmptyPosShouldThrowException(Spielfeld gameBoard){

        Player playerBlack = new Player(Options.Color.BLACK);
        Player playerWhite = new Player(Options.Color.WHITE);
        Position dest = new Position(6,3);
        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(gameBoard.isValid(p)) {
                    try {
                        gameBoard.executeCompleteTurn(new Zug(dest, p, null), playerBlack);
                        gameBoard.executeCompleteTurn(new Zug(dest, p, null), playerWhite);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

    @Test
    public void makeWholeMove_KillOwnPieceShouldThrowException() {
        Spielfeld gameBoard = new Mill5();
        makeWholeMove_KillOwnPieceShouldThrowException(gameBoard);
        gameBoard = new Mill7();
        makeWholeMove_KillOwnPieceShouldThrowException(gameBoard);
        gameBoard = new Mill9();
        makeWholeMove_KillOwnPieceShouldThrowException(gameBoard);
    }

    public void makeWholeMove_KillOwnPieceShouldThrowException(Spielfeld gameBoard){

        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(gameBoard.isValid(p)) {
                    try {
                        Player playerBlack = new Player(Options.Color.BLACK);
                        playerBlack.setSetCount(5);
                        gameBoard.executeCompleteTurn(new Zug(p, null, p), playerBlack);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }


    @Test
    public void makeWholeMove_KillNotExistingPieceShouldThrowException() {
        Spielfeld gameBoard = new Mill5();
        makeWholeMove_KillNotExistingPieceShouldThrowException(gameBoard);
        gameBoard = new Mill7();
        makeWholeMove_KillNotExistingPieceShouldThrowException(gameBoard);
        gameBoard = new Mill9();
        makeWholeMove_KillNotExistingPieceShouldThrowException(gameBoard);
    }

    public void makeWholeMove_KillNotExistingPieceShouldThrowException(Spielfeld gameBoard){

        Player playerBlack = new Player(Options.Color.BLACK);
        //TODO dont use 7 but LENGTH from Gameoard
        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(gameBoard.isValid(p)) {
                    try {
                        gameBoard.executeCompleteTurn(new Zug(null, null, p), playerBlack);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

    @Test
    public void reverseCompleteTurn_ShouldReverse(){

        Player playerBlack = new Player(Options.Color.BLACK);
        Player playerWhite = new Player(Options.Color.WHITE);
        playerBlack.setOtherPlayer(playerWhite);
        playerWhite.setOtherPlayer(playerBlack);
        playerBlack.setSetCount(0);
        playerWhite.setSetCount(0);

        Options.Color WHITE = Options.Color.WHITE;
        Options.Color BLACK = Options.Color.BLACK;
        Options.Color NOTHING = Options.Color.NOTHING;
        Options.Color I = Options.Color.INVALID;

        final Options.Color[][] before = {{BLACK, I, I,    BLACK ,I ,I ,    NOTHING},
                                          { I, I, I, I, I, I, I },
                                          { I, I,    WHITE, WHITE, BLACK,   I, I },
                                          {NOTHING,I,BLACK  ,I ,   NOTHING,I,WHITE},
                                          { I, I,    WHITE, BLACK ,NOTHING, I, I },
                                          { I, I, I, I, I, I, I },
                                          {WHITE, I, I,  NOTHING, I, I,      NOTHING}};

        final Spielfeld gameBoardBefore = new Mill5(before);

        //copy the original field, so we can compare it later
        Options.Color [][] field = new Options.Color[before.length][];
        for(int i = 0; i < before.length; i++) {
            field[i] = before[i].clone();
        }
        Spielfeld gameBoard = new Mill5(field);


        //TODO write a test for strategy which verifies that the number of possible Moves is correct... should be calculatable

        Strategie strategy = new Strategie(gameBoardBefore, null);

        LinkedList<Zug> allPossibleMoves =  strategy.possibleMoves(playerBlack);

        System.out.println(allPossibleMoves.size());

        for (int i = 0; i < allPossibleMoves.size(); i++) {
            gameBoard.executeCompleteTurn(allPossibleMoves.get(i), playerBlack);
            gameBoard.reverseCompleteTurn(allPossibleMoves.get(i), playerBlack);
            assertEqualGameboards(gameBoardBefore, gameBoard, allPossibleMoves.get(i));
        }

    }

    public void assertEqualGameboards(Spielfeld expected, Spielfeld actual, Zug z){
        for (int x = 0; x < expected.LENGTH; x++) {
            for (int y = 0; y < expected.LENGTH; y++) {
                if(!expected.getPos(x,y).equals(actual.getPos(x,y))){
                    fail("expected: " + expected.getPos(x,y) + ", actual: " + actual.getPos(x,y) + "; on position: (" + x + "," + y + ")" +
                    "\nmove was: " + z);
                }
            }
        }
    }

}

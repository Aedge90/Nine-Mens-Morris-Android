package own.projects.lemiroapp;

import org.junit.Before;
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

        Player playerBlack = new Player(Options.Color.BLACK);
        Player playerWhite = new Player(Options.Color.WHITE);
        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(gameBoard.isValid(p)) {
                    try {
                        gameBoard.makeWholeMove(new Zug(null, null, p, null), playerBlack);
                        gameBoard.makeWholeMove(new Zug(null, null, p, null), playerWhite);
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
                        gameBoard.makeWholeMove(new Zug(dest, p, null, null), playerBlack);
                        gameBoard.makeWholeMove(new Zug(dest, p, null, null), playerWhite);
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

        Player playerBlack = new Player(Options.Color.BLACK);
        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(gameBoard.isValid(p)) {
                    try {
                        gameBoard.makeWholeMove(new Zug(null, null, p, p), playerBlack);
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
        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(gameBoard.isValid(p)) {
                    try {
                        gameBoard.makeWholeMove(new Zug(null, null, null, p), playerBlack);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

}

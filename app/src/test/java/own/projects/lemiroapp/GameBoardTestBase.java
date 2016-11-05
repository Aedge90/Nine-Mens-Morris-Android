package own.projects.lemiroapp;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;


public abstract class GameBoardTestBase<T extends GameBoard> {

    private T mGameBoard;

    protected abstract T createInstance();

    @Before
    public void setUp() {
        mGameBoard = createInstance();
    }

    @Test
    public void getPossibleMillX_DoesNotChangeYValue() {

        Position[] possibleMillX;

        //assert that y value does not change for a mill in x direction
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                //its okay to throw an exception for impossible positions as all positions are tried here
                try {
                    possibleMillX = mGameBoard.getPossibleMillX(p);
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

        Position[] possibleMillY;

        //assert that x value does not change for a mill in y direction
        for (int x=0; x<7; x++) {
            for (int y=0; y<7; y++) {
                Position p = new Position(x, y);
                //its okay to throw an exception for impossible positions as all positions are tried here
                try{
                    possibleMillY = mGameBoard.getPossibleMillY(p);
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
    public void makeWholeMove_WithSetMoveOnNonEmptyPosShouldThrowException(){

        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(mGameBoard.isValid(p)) {
                    try {
                        Player playerBlack = new Player(Options.Color.BLACK);
                        Player playerWhite = new Player(Options.Color.WHITE);
                        playerBlack.setSetCount(5);
                        playerWhite.setSetCount(5);
                        mGameBoard.executeCompleteTurn(new Zug(p, null, null), playerBlack);
                        mGameBoard.executeCompleteTurn(new Zug(p, null, null), playerWhite);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

    @Test
    public void makeWholeMove_WithMoveMoveOnNonEmptyPosShouldThrowException(){

        Player playerBlack = new Player(Options.Color.BLACK);
        Player playerWhite = new Player(Options.Color.WHITE);
        Position dest = new Position(6,3);
        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(mGameBoard.isValid(p)) {
                    try {
                        mGameBoard.executeCompleteTurn(new Zug(dest, p, null), playerBlack);
                        mGameBoard.executeCompleteTurn(new Zug(dest, p, null), playerWhite);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

    @Test
    public void makeWholeMove_KillOwnPieceShouldThrowException(){

        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(mGameBoard.isValid(p)) {
                    try {
                        Player playerBlack = new Player(Options.Color.BLACK);
                        playerBlack.setSetCount(5);
                        mGameBoard.executeCompleteTurn(new Zug(p, null, p), playerBlack);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }


    @Test
    public void makeWholeMove_KillNotExistingPieceShouldThrowException(){

        Player playerBlack = new Player(Options.Color.BLACK);
        //TODO dont use 7 but LENGTH from Gameoard
        for (int x=0; x<7; x++) {
            for (int y = 0; y < 7; y++) {
                Position p = new Position(x, y);
                if(mGameBoard.isValid(p)) {
                    try {
                        mGameBoard.executeCompleteTurn(new Zug(null, null, p), playerBlack);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

}


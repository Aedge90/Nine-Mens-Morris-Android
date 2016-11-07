package own.projects.lemiroapp;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.fail;


public class GameBoardTestNonParameterized {

    private GameBoard mGameBoard;
    private Player mPlayerWhite;
    private Player mPlayerBlack;

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
    public void makeWholeMove_WithSetMoveOnNonEmptyPosShouldThrowException(){

        GameBoard mGameBoard = new Mill9();

        Position p = new Position(6,6);
        try {
            mPlayerBlack.setSetCount(5);
            mPlayerWhite.setSetCount(5);
            mGameBoard.executeCompleteTurn(new Zug(p, null, null), mPlayerBlack);
            mGameBoard.executeCompleteTurn(new Zug(p, null, null), mPlayerWhite);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            String expectedMessage = "is trying to set to an occupied field by";
            if(!e.getMessage().contains(expectedMessage)){
                fail("expected message to contain: " + expectedMessage + "\n" + "but was: " + e.getMessage());
            }
        }


    }

    @Test
    public void makeWholeMove_WithMoveMoveOnNonEmptyPosShouldThrowException(){

        Position src1 = new Position(6,0);
        Position src2 = new Position(6,6);
        Position dest = new Position(6,3);

        try {
            mPlayerBlack.setSetCount(1);
            mPlayerWhite.setSetCount(1);
            mGameBoard.executeCompleteTurn(new Zug(src1, null, null), mPlayerBlack);
            mGameBoard.executeCompleteTurn(new Zug(src2, null, null), mPlayerWhite);
            mGameBoard.executeCompleteTurn(new Zug(dest, src1, null), mPlayerBlack);
            mGameBoard.executeCompleteTurn(new Zug(dest, src2, null), mPlayerWhite);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            String expectedMessage = "is trying to move to an occupied field by";
            if(!e.getMessage().contains(expectedMessage)){
                fail("expected message to contain: " + expectedMessage + "\n" + "but was: " + e.getMessage());
            }
        }

    }

    @Test
    public void makeWholeMove_KillOwnPieceShouldThrowException(){

        Position p = new Position(6,6);
        try {
            mPlayerBlack.setSetCount(5);
            mGameBoard.executeCompleteTurn(new Zug(p, null, p), mPlayerBlack);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            String expectedMessageStart = "Trying to kill own piece of color";
            if(!e.getMessage().startsWith(expectedMessageStart)){
                fail("expected message to start with: " + expectedMessageStart + "\n" + "but was: " + e.getMessage());
            }
        }

    }


    @Test
    public void makeWholeMove_KillNotExistingPieceShouldThrowException(){

        Position set = new Position(0,0);
        Position kill = new Position(6,3);

        try {
            GameBoard emptyGameBoard = mGameBoard.getCopy();
            mPlayerBlack.setSetCount(5);
            emptyGameBoard.executeCompleteTurn(new Zug(set, null, kill), mPlayerBlack);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            String expectedMessageEnd = "is trying to kill an empty field";
            if(!e.getMessage().endsWith(expectedMessageEnd)){
                fail("expected message to end with: " + expectedMessageEnd + "\n" + "but was: " + e.getMessage());
            }
        }

    }
}

package own.projects.lemiroapp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(value = Parameterized.class)
public abstract class GameBoardTestBase<T extends GameBoard> {

    protected T mGameBoard;
    protected Player mPlayerWhite;
    protected Player mPlayerBlack;

    // Inject paremeters via constructor, constructor is called before each test
    public GameBoardTestBase(T gameBoard, Player playerBlack, Player playerWhite){
        //call copy constructors otherwise tests will change the parameters which should be the same for all tests
        mGameBoard = callCopyConstructor(gameBoard);
        mPlayerBlack = new Player(playerBlack);
        mPlayerWhite = new Player(playerWhite);
        //System.out.println(mGameBoard);
        //System.out.println(mPlayerBlack.getSetCount());
        //System.out.println(mPlayerWhite.getSetCount());
    }

    //creates a new object of type <T extends GameBoard>
    protected abstract T callCopyConstructor(T copyThis);

    //common code thats used to create the parameter for each subclass
    public static Object[] createPlayersandExecuteMoves(GameBoard gameBoard, int nMovesPerPlayer){

        Player playerBlack = new Player(Options.Color.BLACK);
        Player playerWhite = new Player(Options.Color.WHITE);
        playerBlack.setOtherPlayer(playerWhite);
        playerWhite.setOtherPlayer(playerBlack);
        playerBlack.setSetCount(5);
        playerWhite.setSetCount(5);

        //TODO write a test for strategy which verifies that the number of possible Moves is correct... should be calculatable

        Strategie strategy = new Strategie(gameBoard, null);

        //every player executes 10 turns
        for (int i = 0; i < nMovesPerPlayer; i++) {
            LinkedList<Zug> allPossibleMoves = strategy.possibleMoves(playerBlack);
            //just use the third possible move
            gameBoard.executeCompleteTurn(allPossibleMoves.get(2), playerBlack);
            allPossibleMoves = strategy.possibleMoves(playerWhite);
            gameBoard.executeCompleteTurn(allPossibleMoves.get(2), playerWhite);
        }

        return new Object[]{gameBoard, playerBlack, playerWhite};
    }


    @Test
    public void reverseCompleteTurn_ShouldReverse(){

        final GameBoard gameBoardBefore = callCopyConstructor(mGameBoard);

        Strategie strategy = new Strategie(mGameBoard, null);

        LinkedList<Zug> allPossibleMoves = strategy.possibleMoves(mPlayerBlack);

        for (int i = 0; i < allPossibleMoves.size(); i++) {
            mGameBoard.executeCompleteTurn(allPossibleMoves.get(i), mPlayerBlack);
            mGameBoard.reverseCompleteTurn(allPossibleMoves.get(i), mPlayerBlack);
            assertEqualGameboards(gameBoardBefore, mGameBoard, allPossibleMoves.get(i));
        }

    }

    public void assertEqualGameboards(GameBoard expected, GameBoard actual, Zug z){
        for (int x = 0; x < expected.LENGTH; x++) {
            for (int y = 0; y < expected.LENGTH; y++) {
                if(!expected.getPos(x,y).equals(actual.getPos(x,y))){
                    fail("expected: " + expected.getPos(x,y) + ", actual: " + actual.getPos(x,y) + "; on position: (" + x + "," + y + ")" +
                            "\nmove was: " + z);
                }
            }
        }
    }


    @Test
    public void getPossibleMillX_DoesNotChangeYValue() {

        Position[] possibleMillX;

        //assert that y value does not change for a mill in x direction
        for (int x = 0; x < GameBoard.LENGTH; x++) {
            for (int y = 0; y < GameBoard.LENGTH; y++) {
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

        for (int x=0; x < GameBoard.LENGTH; x++) {
            for (int y = 0; y < GameBoard.LENGTH; y++) {
                Position p = new Position(x, y);
                if(mGameBoard.isValid(p)) {
                    try {
                        mPlayerBlack.setSetCount(5);
                        mPlayerWhite.setSetCount(5);
                        mGameBoard.executeCompleteTurn(new Zug(p, null, null), mPlayerBlack);
                        mGameBoard.executeCompleteTurn(new Zug(p, null, null), mPlayerWhite);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

    @Test
    public void makeWholeMove_WithMoveMoveOnNonEmptyPosShouldThrowException(){

        Position dest = new Position(6,3);
        for (int x=0; x < GameBoard.LENGTH; x++) {
            for (int y = 0; y < GameBoard.LENGTH; y++) {
                Position p = new Position(x, y);
                if(mGameBoard.isValid(p)) {
                    try {
                        mPlayerBlack.setSetCount(0);
                        mPlayerWhite.setSetCount(0);
                        mGameBoard.executeCompleteTurn(new Zug(dest, p, null), mPlayerBlack);
                        mGameBoard.executeCompleteTurn(new Zug(dest, p, null), mPlayerWhite);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

    @Test
    public void makeWholeMove_KillOwnPieceShouldThrowException(){

        for (int x=0; x < GameBoard.LENGTH; x++) {
            for (int y = 0; y < GameBoard.LENGTH; y++) {
                Position p = new Position(x, y);
                if(mGameBoard.isValid(p)) {
                    try {
                        mPlayerBlack.setSetCount(5);
                        mGameBoard.executeCompleteTurn(new Zug(p, null, p), mPlayerBlack);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}

                    //TODO check which exception was thrown
                }
            }
        }

    }


    @Test
    public void makeWholeMove_KillNotExistingPieceShouldThrowException(){

        for (int x=0; x < GameBoard.LENGTH; x++) {
            for (int y = 0; y < GameBoard.LENGTH; y++) {
                Position p = new Position(x, y);
                if(mGameBoard.isValid(p)) {
                    try {
                        mGameBoard.executeCompleteTurn(new Zug(null, null, p), mPlayerBlack);
                        fail("Expected an IllegalArgumentException to be thrown");
                    } catch (IllegalArgumentException e) {}
                }
            }
        }

    }

}


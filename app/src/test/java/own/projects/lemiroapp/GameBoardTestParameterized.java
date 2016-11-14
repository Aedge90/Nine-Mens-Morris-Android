package own.projects.lemiroapp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(value = Parameterized.class)
public class GameBoardTestParameterized {

    private GameBoard mGameBoard;
    private Player mPlayerWhite;
    private Player mPlayerBlack;

    // name attribute is optional, provide an unique name for test
    // multiple parameters, uses Collection<Object[]>
    @Parameterized.Parameters(name = "Param{index}: {3}")
    public static Collection<Object[] > data() {

        Collection<Object[]> parameters = Arrays.asList(new Object[][]{
                createPlayersandExecuteMoves(new Mill5(), 0),
                createPlayersandExecuteMoves(new Mill7(), 0),
                createPlayersandExecuteMoves(new Mill9(), 0),
                createPlayersandExecuteMoves(new Mill5(), 10),
                createPlayersandExecuteMoves(new Mill7(), 10),
                createPlayersandExecuteMoves(new Mill9(), 10)
        });

        return parameters;
    }

    // Inject paremeters via constructor, constructor is called before each test
    public GameBoardTestParameterized(GameBoard gameBoard, Player playerBlack, Player playerWhite, String testDescription){
        //call copy constructors otherwise tests will change the parameters which should be the same for all tests
        mGameBoard = gameBoard.getCopy();
        mPlayerBlack = new Player(playerBlack);
        mPlayerWhite = new Player(playerWhite);
        //System.out.println(mGameBoard);
        //System.out.println(mPlayerBlack.getSetCount());
        //System.out.println(mPlayerWhite.getSetCount());
    }


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

        return new Object[]{gameBoard, playerBlack, playerWhite, "" + gameBoard.millMode + " after " + nMovesPerPlayer + " moves per player"};
    }


    @Test
    public void reverseCompleteTurn_ShouldReverse(){

        final GameBoard gameBoardBefore = mGameBoard.getCopy();

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


}

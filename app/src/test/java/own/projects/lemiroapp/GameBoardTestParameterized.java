package own.projects.lemiroapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

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

        StrategyRunnable strategyBlack = new StrategyRunnable(gameBoard, playerBlack ,null, 0, 1);
        StrategyRunnable strategyWhite = new StrategyRunnable(gameBoard, playerWhite ,null, 0, 1);

        //every player executes 10 turns
        for (int i = 0; i < nMovesPerPlayer; i++) {
            LinkedList<Move> allPossibleMoves = strategyBlack.possibleMoves(playerBlack);
            //just use the third possible move
            gameBoard.executeCompleteTurn(allPossibleMoves.get(2), playerBlack);
            allPossibleMoves = strategyWhite.possibleMoves(playerWhite);
            gameBoard.executeCompleteTurn(allPossibleMoves.get(2), playerWhite);
        }

        return new Object[]{gameBoard, playerBlack, playerWhite, ""  + " after " + nMovesPerPlayer + " moves per player"};
    }


    @Test
    public void reverseCompleteTurn_ShouldReverse(){

        final GameBoard gameBoardBefore = mGameBoard.getCopy();

        StrategyRunnable strategy = new StrategyRunnable(mGameBoard, mPlayerBlack, null, 0, 1);

        LinkedList<Move> allPossibleMoves = strategy.possibleMoves(mPlayerBlack);

        for (int i = 0; i < allPossibleMoves.size(); i++) {
            mGameBoard.executeCompleteTurn(allPossibleMoves.get(i), mPlayerBlack);
            mGameBoard.reverseCompleteTurn(allPossibleMoves.get(i), mPlayerBlack);
            assertEqualGameboards(gameBoardBefore, mGameBoard, allPossibleMoves.get(i));
        }

    }

    public void assertEqualGameboards(GameBoard expected, GameBoard actual, Move z){
        for (int x = 0; x < expected.LENGTH; x++) {
            for (int y = 0; y < expected.LENGTH; y++) {
                if(!expected.getColorAt(x,y).equals(actual.getColorAt(x,y))){
                    fail("expected: " + expected.getColorAt(x,y) + ", actual: " + actual.getColorAt(x,y) + "; on position: (" + x + "," + y + ")" +
                            "\nmove was: " + z);
                }
            }
        }
    }



}


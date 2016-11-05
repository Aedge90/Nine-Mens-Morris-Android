package own.projects.lemiroapp;

import org.junit.Test;

import java.util.LinkedList;

import static junit.framework.Assert.fail;


public class GameBoardMill5Test extends GameBoardTestBase<Mill5> {


    @Override
    protected Mill5 createInstance(){
        return new Mill5();
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

        final GameBoard gameBoardBefore = new Mill5(before);

        //copy the original field, so we can compare it later
        Options.Color [][] field = new Options.Color[before.length][];
        for(int i = 0; i < before.length; i++) {
            field[i] = before[i].clone();
        }
        GameBoard mGameBoard = new Mill5(field);


        //TODO write a test for strategy which verifies that the number of possible Moves is correct... should be calculatable

        Strategie strategy = new Strategie(gameBoardBefore, null);

        LinkedList<Zug> allPossibleMoves =  strategy.possibleMoves(playerBlack);

        System.out.println(allPossibleMoves.size());

        for (int i = 0; i < allPossibleMoves.size(); i++) {
            mGameBoard.executeCompleteTurn(allPossibleMoves.get(i), playerBlack);
            mGameBoard.reverseCompleteTurn(allPossibleMoves.get(i), playerBlack);
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

}

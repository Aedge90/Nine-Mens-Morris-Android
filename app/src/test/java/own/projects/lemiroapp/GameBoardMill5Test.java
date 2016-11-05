package own.projects.lemiroapp;

import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.fail;


public class GameBoardMill5Test extends GameBoardTestBase<Mill5> {

    //this constructor is called in several tests for all parameters below
    public GameBoardMill5Test(Mill5 gameBoard, Player playerBlack, Player playerWhite) {
        super(gameBoard, playerBlack, playerWhite);
    }

    @Override
    protected Mill5 callCopyConstructor(Mill5 copyThis) {
        return new Mill5(copyThis);
    }

    // name attribute is optional, provide an unique name for test
    // multiple parameters, uses Collection<Object[]>
    @Parameterized.Parameters(name = "Parameters {index}")
    public static Collection<Object[] > data() {

        Mill5 gameBoard0 = new Mill5();
        Object[] testParameters0 = createPlayersandExecuteMoves(gameBoard0, 0);

        Mill5 gameBoard1 = new Mill5();
        Object[] testParameters1 = createPlayersandExecuteMoves(gameBoard1, 10);

        List<Object[]> result = Arrays.asList(testParameters0, testParameters1);

        return result;
    }


}

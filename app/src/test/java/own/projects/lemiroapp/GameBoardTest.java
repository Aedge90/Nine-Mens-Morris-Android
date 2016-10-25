package own.projects.lemiroapp;

import org.junit.Before;
import org.junit.Test;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class GameBoardTest {

    private Spielfeld mGameBoard5;

    @Before
    public void initMocks() {
        mGameBoard5 = new Mill5();
    }

    @Test
    public void getPossibleMillX_ReturnsCorrectPositions() {
        Position[] possibleMillX = mGameBoard5.getPossibleMillX(new Position(4,4));
        assertEquals(new Position(2,4), possibleMillX[0]);
    }

}

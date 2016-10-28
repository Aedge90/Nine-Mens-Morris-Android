package own.projects.lemiroapp;

import android.util.Log;

import junit.framework.Assert;

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
        //arbitrary sample
        Position[] possibleMillX = mGameBoard5.getPossibleMillX(new Position(4, 4));
        assertEquals(new Position(2, 4), possibleMillX[0]);
        assertEquals(new Position(3, 4), possibleMillX[1]);
        assertEquals(new Position(4, 4), possibleMillX[2]);

        //assert that y value does not change for a mill in x direction
        for (int x=0; x<7; x++) {
            for (int y=0; y<7; y++) {
                Position p = new Position(x, y);
                //its okay to throw an exception for impossible positions as all positions are tried here
                try{
                    possibleMillX = mGameBoard5.getPossibleMillX(p);
                    //may be null if there are no possible mill in x direction for this position
                    if(possibleMillX != null) {
                        assertEquals(p.getY(), possibleMillX[0].getY());
                        assertEquals(p.getY(), possibleMillX[1].getY());
                        assertEquals(p.getY(), possibleMillX[2].getY());
                    }
                }catch(IllegalArgumentException e){
                }

            }
        }
    }
}

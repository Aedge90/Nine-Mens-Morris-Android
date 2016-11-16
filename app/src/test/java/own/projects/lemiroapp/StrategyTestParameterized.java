package own.projects.lemiroapp;


import android.test.mock.MockContext;
import android.widget.ProgressBar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;


@RunWith(value = Parameterized.class)
public class StrategyTestParameterized {

    private final Options.Color B = Options.Color.BLACK;
    private final Options.Color W = Options.Color.WHITE;
    private final Options.Color N = Options.Color.NOTHING;
    private final Options.Color I = Options.Color.INVALID;

    private Player mPlayerWhite;
    private Player mPlayerBlack;

    // name attribute is optional, provide an unique name for test
    // multiple parameters, uses Collection<Object[]>
    @Parameterized.Parameters(name = "Param{index}: {0}")
    public static Collection<Object[] > data() {

        Collection<Object[]> parameters = Arrays.asList(new Object[][]{
                {Options.Difficulties.EASY},
                {Options.Difficulties.NORMAL},
                {Options.Difficulties.HARD},
                {Options.Difficulties.HARDER},
                {Options.Difficulties.HARDEST}
        });

        return parameters;
    }

    // Inject paremeters via constructor, constructor is called before each test
    public StrategyTestParameterized(Options.Difficulties difficulty){

        mPlayerBlack = new Player(Options.Color.BLACK);
        mPlayerWhite = new Player(Options.Color.WHITE);
        mPlayerBlack.setOtherPlayer(mPlayerWhite);
        mPlayerWhite.setOtherPlayer(mPlayerBlack);
        mPlayerBlack.setSetCount(5);
        mPlayerWhite.setSetCount(5);
        mPlayerBlack.setDifficulty(difficulty);
        mPlayerWhite.setDifficulty(difficulty);

    }

    @Test
    public void computeMoveShouldCloseMill() throws InterruptedException {

        Options.Color[][] mill5 =
                {{N, I, I, B, I, I, W},
                { I, I, I, I, I, I, I},
                { I, I, B, N, W, I, I},
                { B, I, B, I, N, I, N},
                { I, I, W, N, N, I, I},
                { I, I, I, I, I, I, I},
                { B, I, I, W, I, I, N}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategie strategy = new Strategie(gameBoard, updater);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        Zug result = strategy.computeMove(mPlayerBlack);

        assertEquals(new Position(0, 0), result.getDest());
        assertEquals(new Position(3, 0), result.getSrc());

    }

    @Test
    public void computeMoveShouldWinAsNoMovesLeft() throws InterruptedException {

        Options.Color[][] mill5 =
                {{B, I, I, N, I, I, N},
                { I, I, I, I, I, I, I},
                { I, I, W, B, W, I, I},
                { N, I, B, I, N, I, B},
                { I, I, W, B, W, I, I},
                { I, I, I, I, I, I, I},
                { N, I, I, N, I, I, N}};

        GameBoard gameBoard = new Mill5(mill5);
        ProgressBar progBar = new ProgressBar(new MockContext());
        ProgressUpdater updater = new ProgressUpdater(progBar, new HumanVsBot());
        Strategie strategy = new Strategie(gameBoard, updater);

        mPlayerBlack.setSetCount(0);
        mPlayerWhite.setSetCount(0);

        Zug result = strategy.computeMove(mPlayerBlack);

        assertEquals(new Position(4, 3), result.getDest());
        assertEquals(new Position(6, 3), result.getSrc());

    }

}

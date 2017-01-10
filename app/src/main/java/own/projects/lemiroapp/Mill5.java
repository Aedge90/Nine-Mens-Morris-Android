package own.projects.lemiroapp;

import android.support.annotation.VisibleForTesting;

public class Mill5 extends GameBoard {

    @Override
    public void initField(){

       field = new GameBoardPosition[][] // first is y: [y][x]

                {{N, I, I, N, I, I, N},
                { I, I, I, I, I, I, I},
                { I, I, N, N, N, I, I},
                { N, I, N, I, N, I, N},
                { I, I, N, N, N, I, I},
                { I, I, I, I, I, I, I},
                { N, I, I, N, I, I, N}};

        initGameBoardPositions();

        //horizontal connections
        getGameBoardPosAt(0,0).connectRight(getGameBoardPosAt(3,0));
        getGameBoardPosAt(3,0).connectRight(getGameBoardPosAt(6,0));

        getGameBoardPosAt(2,2).connectRight(getGameBoardPosAt(3,2));
        getGameBoardPosAt(3,2).connectRight(getGameBoardPosAt(4,2));

        getGameBoardPosAt(0,3).connectRight(getGameBoardPosAt(2,3));
        getGameBoardPosAt(4,3).connectRight(getGameBoardPosAt(6,3));

        getGameBoardPosAt(2,4).connectRight(getGameBoardPosAt(3,4));
        getGameBoardPosAt(3,4).connectRight(getGameBoardPosAt(4,4));

        getGameBoardPosAt(0,6).connectRight(getGameBoardPosAt(3,6));
        getGameBoardPosAt(3,6).connectRight(getGameBoardPosAt(6,6));

        //vertical connections
        getGameBoardPosAt(0,0).connectDown(getGameBoardPosAt(0,3));
        getGameBoardPosAt(0,3).connectDown(getGameBoardPosAt(0,6));

        getGameBoardPosAt(2,2).connectDown(getGameBoardPosAt(2,3));
        getGameBoardPosAt(2,3).connectDown(getGameBoardPosAt(2,4));

        getGameBoardPosAt(3,0).connectDown(getGameBoardPosAt(3,2));
        getGameBoardPosAt(3,4).connectDown(getGameBoardPosAt(3,6));

        getGameBoardPosAt(4,2).connectDown(getGameBoardPosAt(4,3));
        getGameBoardPosAt(4,3).connectDown(getGameBoardPosAt(4,4));

        getGameBoardPosAt(6,0).connectDown(getGameBoardPosAt(6,3));
        getGameBoardPosAt(6,3).connectDown(getGameBoardPosAt(6,6));
    }
    
    Mill5() {
        initField();
    }

    @VisibleForTesting
    Mill5(Options.Color[][] inputField) {
        super(inputField);
    }

    @Override
    GameBoard getCopy() {
        return new Mill5(this);
    }

    //copy constructor
    Mill5(Mill5 other){
        super(other);
    }

    
}

package own.projects.lemiroapp;

import android.support.annotation.VisibleForTesting;

public class Mill9 extends GameBoard {

    @Override
    public void initField(){

        field = new GameBoardPosition[][] // first is y: [y][x]

                {{N, I, I, N, I, I, N},
                { I, N, I, N, I, N, I},
                { I, I, N, N, N, I, I},
                { N, N, N, I, N, N, N},
                { I, I, N, N, N, I, I},
                { I, N, I, N, I, N, I},
                { N, I, I, N, I, I, N}};

        initGameBoardPositions();

        //horizontal connections
        getGameBoardPosAt(0,0).connectRight(getGameBoardPosAt(3,0));
        getGameBoardPosAt(3,0).connectRight(getGameBoardPosAt(6,0));

        getGameBoardPosAt(1,1).connectRight(getGameBoardPosAt(3,1));
        getGameBoardPosAt(3,1).connectRight(getGameBoardPosAt(5,1));

        getGameBoardPosAt(2,2).connectRight(getGameBoardPosAt(3,2));
        getGameBoardPosAt(3,2).connectRight(getGameBoardPosAt(4,2));

        getGameBoardPosAt(0,3).connectRight(getGameBoardPosAt(1,3));
        getGameBoardPosAt(1,3).connectRight(getGameBoardPosAt(2,3));
        getGameBoardPosAt(4,3).connectRight(getGameBoardPosAt(5,3));
        getGameBoardPosAt(5,3).connectRight(getGameBoardPosAt(6,3));

        getGameBoardPosAt(2,4).connectRight(getGameBoardPosAt(3,4));
        getGameBoardPosAt(3,4).connectRight(getGameBoardPosAt(4,4));

        getGameBoardPosAt(1,5).connectRight(getGameBoardPosAt(3,5));
        getGameBoardPosAt(3,5).connectRight(getGameBoardPosAt(5,5));

        getGameBoardPosAt(0,6).connectRight(getGameBoardPosAt(3,6));
        getGameBoardPosAt(3,6).connectRight(getGameBoardPosAt(6,6));

        //vertical connections
        getGameBoardPosAt(0,0).connectDown(getGameBoardPosAt(0,3));
        getGameBoardPosAt(0,3).connectDown(getGameBoardPosAt(0,6));

        getGameBoardPosAt(1,1).connectDown(getGameBoardPosAt(1,3));
        getGameBoardPosAt(1,3).connectDown(getGameBoardPosAt(1,5));

        getGameBoardPosAt(2,2).connectDown(getGameBoardPosAt(2,3));
        getGameBoardPosAt(2,3).connectDown(getGameBoardPosAt(2,4));

        getGameBoardPosAt(3,0).connectDown(getGameBoardPosAt(3,1));
        getGameBoardPosAt(3,1).connectDown(getGameBoardPosAt(3,2));
        getGameBoardPosAt(3,4).connectDown(getGameBoardPosAt(3,5));
        getGameBoardPosAt(3,5).connectDown(getGameBoardPosAt(3,6));

        getGameBoardPosAt(4,2).connectDown(getGameBoardPosAt(4,3));
        getGameBoardPosAt(4,3).connectDown(getGameBoardPosAt(4,4));

        getGameBoardPosAt(5,1).connectDown(getGameBoardPosAt(5,3));
        getGameBoardPosAt(5,3).connectDown(getGameBoardPosAt(5,5));

        getGameBoardPosAt(6,0).connectDown(getGameBoardPosAt(6,3));
        getGameBoardPosAt(6,3).connectDown(getGameBoardPosAt(6,6));
    }

    Mill9() {
        initField();
    }

    @VisibleForTesting
    Mill9(Options.Color[][] inputField) {
        super(inputField);
    }

    @Override
    GameBoard getCopy() {
        return new Mill9(this);
    }

    //copy constructor
    Mill9(Mill9 other){
        super(other);
    }

}
package com.github.aedge90.nmm;

import android.support.annotation.VisibleForTesting;

public class Mill7 extends GameBoard {

    @Override
    public void initField() {

        field = new GameBoardPosition[][] // first is y: [y][x]

                {{N, I, I, N, I, I, N},
                { I, N, I, N, I, N, I},
                { I, I, I, I, I, I, I},
                { N, N, I, N, I, N, N},
                { I, I, I, I, I, I, I},
                { I, N, I, N, I, N, I},
                { N, I, I, N, I, I, N}};

        initGameBoardPositions();

        //horizontal connections
        getGameBoardPosAt(0, 0).connectRight(getGameBoardPosAt(3, 0));
        getGameBoardPosAt(3, 0).connectRight(getGameBoardPosAt(6, 0));

        getGameBoardPosAt(1, 1).connectRight(getGameBoardPosAt(3, 1));
        getGameBoardPosAt(3, 1).connectRight(getGameBoardPosAt(5, 1));

        getGameBoardPosAt(0, 3).connectRight(getGameBoardPosAt(1, 3));
        getGameBoardPosAt(1, 3).connectRight(getGameBoardPosAt(3, 3));
        getGameBoardPosAt(3, 3).connectRight(getGameBoardPosAt(5, 3));
        getGameBoardPosAt(5, 3).connectRight(getGameBoardPosAt(6, 3));

        getGameBoardPosAt(1, 5).connectRight(getGameBoardPosAt(3, 5));
        getGameBoardPosAt(3, 5).connectRight(getGameBoardPosAt(5, 5));

        getGameBoardPosAt(0, 6).connectRight(getGameBoardPosAt(3, 6));
        getGameBoardPosAt(3, 6).connectRight(getGameBoardPosAt(6, 6));

        //vertical connections
        getGameBoardPosAt(0, 0).connectDown(getGameBoardPosAt(0, 3));
        getGameBoardPosAt(0, 3).connectDown(getGameBoardPosAt(0, 6));

        getGameBoardPosAt(1, 1).connectDown(getGameBoardPosAt(1, 3));
        getGameBoardPosAt(1, 3).connectDown(getGameBoardPosAt(1, 5));

        getGameBoardPosAt(3, 0).connectDown(getGameBoardPosAt(3, 1));
        getGameBoardPosAt(3, 1).connectDown(getGameBoardPosAt(3, 3));
        getGameBoardPosAt(3, 3).connectDown(getGameBoardPosAt(3, 5));
        getGameBoardPosAt(3, 5).connectDown(getGameBoardPosAt(3, 6));

        getGameBoardPosAt(5, 1).connectDown(getGameBoardPosAt(5, 3));
        getGameBoardPosAt(5, 3).connectDown(getGameBoardPosAt(5, 5));

        getGameBoardPosAt(6, 0).connectDown(getGameBoardPosAt(6, 3));
        getGameBoardPosAt(6, 3).connectDown(getGameBoardPosAt(6, 6));
    }

    Mill7() {
        initField();
    }

    @VisibleForTesting
    Mill7(Options.Color[][] inputField) {
        super(inputField);
    }

    @Override
    GameBoard getCopy() {
        return new Mill7(this);
    }

    //copy constructor
    Mill7(Mill7 other) {
        initField();
        initGameBoardPositionsFrom(other);
    }


    //Mill7 is a special case so override the function here
    @Override
    Position[] getMillOrPartialMill(Position p, Options.Color player, boolean partial){

        Position[] mill = super.getMillOrPartialMill(p, player ,partial);

        Position middle = new Position(3,3);
        Position[] criticalPositions = {new Position(1,3), new Position(5,3), new Position(3,1), new Position(3,5)};

        //overwrite mill as it may not really be a mill for mill7
        if(p.equals(middle)) {
            mill = getMillWithPosAtCorner(getGameBoardPosAt(p), player, partial);
            if (mill != null) {
                return mill;
            }
        }
        for(Position criticalPos : criticalPositions) {
            if(p.equals(criticalPos)) {
                mill = getMillWithPosInMiddle(getGameBoardPosAt(p), player, partial);
                if (mill != null) {
                    return mill;
                }
            }
        }

        return mill;
    }

}
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

        Position[] mill = super.getMillOrPartialMill(p, player, partial);

        Position criticalPos = new Position(3,3);

        // do not just dismiss a mill with 3,3 in the middle. It may still be that there is a mill
        // depsite that, that has 3,3 at the corner, which is valid
        if(mill != null && (criticalPos.equals(mill[0]) || criticalPos.equals(mill[1]) || criticalPos.equals(mill[2]))){
            return getMillOrPartialMillForMill7(p, player, partial);
        }

        return mill;
    }

    Position[] getMillOrPartialMillForMill7(Position p, Options.Color player, boolean partial) {
        Position criticalPos = new Position(3,3);
        GameBoardPosition criticalGameBoardPos = getGameBoardPosAt(criticalPos);
        GameBoardPosition checkPos = getGameBoardPosAt(p);
        GameBoardPosition[] neighbors = criticalGameBoardPos.getNeighbors();
        if (checkPos.equals(criticalGameBoardPos)) {
            for(GameBoardPosition neighbor : neighbors) {
                //check if next two neighbors belong to player
                if (belongTo(neighbor.getOpposite(checkPos), neighbor, player, partial)) {
                    return new GameBoardPosition[]{neighbor.getOpposite(checkPos), neighbor, checkPos};
                }
            }
        }
        for(GameBoardPosition neighbor : neighbors) {
            if (checkPos.equals(neighbor)) {
                //check if checkPos is between two positions that belong to player
                if(belongTo(criticalGameBoardPos, checkPos.getOpposite(criticalGameBoardPos), player, partial)){
                    return new GameBoardPosition[] {criticalGameBoardPos, checkPos, checkPos.getOpposite(criticalGameBoardPos)};
                }
            }
        }
        return null;
    }

}
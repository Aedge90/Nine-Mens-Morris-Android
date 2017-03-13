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
        super(other);
    }


    //Mill7 is a special case so override the function here
    @Override
    Position[] getMill(Position p, Options.Color player){

        GameBoardPosition critical = getGameBoardPosAt(new Position(3,3));

        if(!critical.getColor().equals(player) && !critical.equals(getGameBoardPosAt(p))){
            //critical position is not occupied by player AND not checked, so no special computing needed
            return super.getMill(p, player);
        }

        //now check if one of the special mills is formed together with p
        Position[] result = null;
        GameBoardPosition[] neighbors = critical.getNeighbors();
        for(int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null && (getColorAt(neighbors[i]).equals(player)
                    || neighbors[i].equals(getGameBoardPosAt(p))) ) {
                GameBoardPosition neighborOfNeighbor = neighbors[i].getNeighbors()[i];
                if (neighborOfNeighbor != null && (getColorAt(neighborOfNeighbor).equals(player)
                        || neighborOfNeighbor.equals(getGameBoardPosAt(p))) ) {
                    result = new Position[]{new Position(neighborOfNeighbor), new Position(neighbors[i]), new Position(critical)};
                }
            }
        }
        if(result != null && (result[0].equals(p) || result[1].equals(p) || result[2].equals(p))){
            return result;
        }else{
            return null;
        }
    }

}
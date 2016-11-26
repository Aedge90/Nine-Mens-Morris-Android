package own.projects.lemiroapp;

import android.support.annotation.VisibleForTesting;

public class Mill7 extends GameBoard {

    @Override
    public void initField() {
        field = new GameBoardPosition[][] // ERSTES Y ZWEITES X

                {{N, I, I, N, I, I, N},
                { I, N, I, N, I, N, I},
                { I, I, I, I, I, I, I},
                { N, N, I, N, I, N, N},
                { I, I, I, I, I, I, I},
                { I, N, I, N, I, N, I},
                { N, I, I, N, I, I, N}};

        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                if (!(field[i][j] == I)) {
                    field[i][j] = new GameBoardPosition(j, i);
                    field[i][j].setColor(Options.Color.NOTHING);
                }
            }
        }

        //horizontal connections
        getPosAt(0, 0).connectRight(getPosAt(3, 0));
        getPosAt(3, 0).connectRight(getPosAt(6, 0));

        getPosAt(1, 1).connectRight(getPosAt(3, 1));
        getPosAt(3, 1).connectRight(getPosAt(5, 1));

        getPosAt(0, 3).connectRight(getPosAt(1, 3));
        getPosAt(1, 3).connectRight(getPosAt(3, 3));
        getPosAt(3, 3).connectRight(getPosAt(5, 3));
        getPosAt(5, 3).connectRight(getPosAt(6, 3));

        getPosAt(1, 5).connectRight(getPosAt(3, 5));
        getPosAt(3, 5).connectRight(getPosAt(5, 5));

        getPosAt(0, 6).connectRight(getPosAt(3, 6));
        getPosAt(3, 6).connectRight(getPosAt(6, 6));

        //vertical connections
        getPosAt(0, 0).connectDown(getPosAt(0, 3));
        getPosAt(0, 3).connectDown(getPosAt(0, 6));

        getPosAt(1, 1).connectDown(getPosAt(1, 3));
        getPosAt(1, 3).connectDown(getPosAt(1, 5));

        getPosAt(3, 0).connectDown(getPosAt(3, 1));
        getPosAt(3, 1).connectDown(getPosAt(3, 3));
        getPosAt(3, 3).connectDown(getPosAt(3, 5));
        getPosAt(3, 5).connectDown(getPosAt(3, 6));

        getPosAt(5, 1).connectDown(getPosAt(5, 3));
        getPosAt(5, 3).connectDown(getPosAt(5, 5));

        getPosAt(6, 0).connectDown(getPosAt(6, 3));
        getPosAt(6, 3).connectDown(getPosAt(6, 6));
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

        Position[] superMill = super.getMill(p, player);

        if (superMill == null){
            return null;
        }

        //find incorrectly detected mill, that contain the middle position in the middle of the mill
        if(superMill[0].getX() == 1 && superMill[0].getY() == 3){
            //return null if its the case, else the result from super is correct
            return null;
        }
        if(superMill[0].getX() == 3 && superMill[0].getY() == 1){
            return null;
        }

        return superMill;

    }

}
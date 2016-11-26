package own.projects.lemiroapp;

import android.support.annotation.VisibleForTesting;

public class Mill7 extends GameBoard {

    @Override
    public void initField() {
        field = new GameBoardPosition[][] // ERSTES Y ZWEITES X

                {{N, I, I, N, I, I, N},
                        {I, N, I, N, I, N, I},
                        {I, I, I, I, I, I, I},
                        {N, N, I, N, I, N, N},
                        {I, I, I, I, I, I, I},
                        {I, N, I, N, I, N, I},
                        {N, I, I, N, I, I, N}};

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

    @Override
    Position[] getPossibleMillX(Position p) {
        assertValidandNotNull(p);
        Position[] millX = new Position[3];
        if (p.getY() == 0 && (p.getX() == 0 || p.getX() == 3 || p.getX() == 6)) {
            millX[0] = new Position(0, 0);
            millX[1] = new Position(3, 0);
            millX[2] = new Position(6, 0);
        } else if (p.getY() == 6 && (p.getX() == 0 || p.getX() == 3 || p.getX() == 6)) {
            millX[0] = new Position(0, 6);
            millX[1] = new Position(3, 6);
            millX[2] = new Position(6, 6);
        } else if (p.getY() == 1 && (p.getX() == 1 || p.getX() == 3 || p.getX() == 5)) {
            millX[0] = new Position(1, 1);
            millX[1] = new Position(3, 1);
            millX[2] = new Position(5, 1);
        } else if (p.getY() == 5 && (p.getX() == 1 || p.getX() == 3 || p.getX() == 5)) {
            millX[0] = new Position(1, 5);
            millX[1] = new Position(3, 5);
            millX[2] = new Position(5, 5);
        } else if (p.getY() == 3 && (p.getX() == 0 || p.getX() == 1 || p.getX() == 3)) {
            millX[0] = new Position(0, 3);
            millX[1] = new Position(1, 3);
            millX[2] = new Position(3, 3);
        } else {
            return null;
        }
        return millX;
    }

    ;

    @Override
    Position[] getPossibleMillY(Position p) {
        assertValidandNotNull(p);
        Position[] millY = new Position[3];
        if (p.getX() == 0 && (p.getY() == 0 || p.getY() == 3 || p.getY() == 6)) {
            millY[0] = new Position(0, 0);
            millY[1] = new Position(0, 3);
            millY[2] = new Position(0, 6);
        } else if (p.getX() == 6 && (p.getY() == 0 || p.getY() == 3 || p.getY() == 6)) {
            millY[0] = new Position(6, 0);
            millY[1] = new Position(6, 3);
            millY[2] = new Position(6, 6);
        } else if (p.getX() == 1 && (p.getY() == 1 || p.getY() == 3 || p.getY() == 5)) {
            millY[0] = new Position(1, 1);
            millY[1] = new Position(1, 3);
            millY[2] = new Position(1, 5);
        } else if (p.getX() == 5 && (p.getY() == 1 || p.getY() == 3 || p.getY() == 5)) {
            millY[0] = new Position(5, 1);
            millY[1] = new Position(5, 3);
            millY[2] = new Position(5, 5);
        } else if (p.getX() == 3 && (p.getY() == 0 || p.getY() == 1 || p.getY() == 3)) {
            millY[0] = new Position(3, 0);
            millY[1] = new Position(3, 1);
            millY[2] = new Position(3, 3);
        } else {
            return null;
        }
        return millY;
    }

    ;

    @Override
        // for x or y == 3 there is another possible mill
    boolean inMill7(Position p, Options.Color player) {
        Position[] millX = new Position[3];
        if (p.getY() == 3 && (p.getX() == 3 || p.getX() == 5 || p.getX() == 6)) {
            millX[0] = new Position(3, 3);
            millX[1] = new Position(5, 3);
            millX[2] = new Position(6, 3);
            int count = 0;
            for (int i = 0; i < 3; i++) {
                if (getColorAt(millX[i]).equals(player)) {
                    count++;
                }
            }
            if (count == 3) {
                return true;
            }
        }
        Position[] millY = new Position[3];
        if (p.getX() == 3 && (p.getY() == 3 || p.getY() == 5 || p.getY() == 6)) {
            millY[0] = new Position(3, 3);
            millY[1] = new Position(3, 5);
            millY[2] = new Position(3, 6);
            int count = 0;
            for (int i = 0; i < 3; i++) {
                if (getColorAt(millY[i]).equals(player)) {
                    count++;
                }
            }
            if (count == 3) {
                return true;
            }
        }
        return false;
    }

    @Override
        // for x or y == 3 there is another possible mill
    Position[] getMill7(Position p, Options.Color player) {
        Position[] millX = new Position[3];
        if (p.getY() == 3 && (p.getX() == 3 || p.getX() == 5 || p.getX() == 6)) {
            millX[0] = new Position(3, 3);
            millX[1] = new Position(5, 3);
            millX[2] = new Position(6, 3);
            int count = 0;
            for (int i = 0; i < 3; i++) {
                if (!millX[i].equals(p)) {
                    if (getColorAt(millX[i]).equals(player)) {
                        count++;
                    }
                }
                if (count == 2) {
                    return millX;
                }
            }
        }
        Position[] millY = new Position[3];
        if (p.getX() == 3 && (p.getY() == 3 || p.getY() == 5 || p.getY() == 6)) {
            millY[0] = new Position(3, 3);
            millY[1] = new Position(3, 5);
            millY[2] = new Position(3, 6);
            int count = 0;
            for (int i = 0; i < 3; i++) {
                if (!millY[i].equals(p)) {
                    if (getColorAt(millY[i]).equals(player)) {
                        count++;
                    }
                }
                if (count == 2) {
                    return millY;
                }
            }
        }
        return null;
    }


}
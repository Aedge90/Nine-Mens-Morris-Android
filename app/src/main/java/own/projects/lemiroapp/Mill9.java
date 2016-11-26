package own.projects.lemiroapp;

import android.support.annotation.VisibleForTesting;

public class Mill9 extends GameBoard {

	@Override
	public void initField(){
		field = new GameBoardPosition[][] // ERSTES Y ZWEITES X

                {{N, I, I, N, I, I, N},
                { I, N, I, N, I, N, I},
                { I, I, N, N, N, I, I},
                { N, N, N, I, N, N, N},
                { I, I, N, N, N, I, I},
                { I, N, I, N, I, N, I},
                { N, I, I, N, I, I, N}};

		for(int i = 0; i < LENGTH; i++){
			for(int j = 0; j < LENGTH; j++){
				if(!(field[i][j] == I)) {
					field[i][j] = new GameBoardPosition(j,i);
					field[i][j].setColor(Options.Color.NOTHING);
				}
			}
		}

		//horizontal connections
		getPosAt(0,0).connectRight(getPosAt(3,0));
		getPosAt(3,0).connectRight(getPosAt(6,0));

		getPosAt(1,1).connectRight(getPosAt(3,1));
		getPosAt(3,1).connectRight(getPosAt(5,1));

        getPosAt(2,2).connectRight(getPosAt(3,2));
        getPosAt(3,2).connectRight(getPosAt(4,2));

        getPosAt(0,3).connectRight(getPosAt(1,3));
        getPosAt(1,3).connectRight(getPosAt(2,3));
        getPosAt(4,3).connectRight(getPosAt(5,3));
        getPosAt(5,3).connectRight(getPosAt(6,3));

        getPosAt(2,4).connectRight(getPosAt(3,4));
        getPosAt(3,4).connectRight(getPosAt(4,4));

		getPosAt(1,5).connectRight(getPosAt(3,5));
		getPosAt(3,5).connectRight(getPosAt(5,5));

		getPosAt(0,6).connectRight(getPosAt(3,6));
		getPosAt(3,6).connectRight(getPosAt(6,6));

		//vertical connections
		getPosAt(0,0).connectDown(getPosAt(0,3));
		getPosAt(0,3).connectDown(getPosAt(0,6));

		getPosAt(1,1).connectDown(getPosAt(1,3));
		getPosAt(1,3).connectDown(getPosAt(1,5));

        getPosAt(2,2).connectDown(getPosAt(2,3));
        getPosAt(2,3).connectDown(getPosAt(2,4));

        getPosAt(3,0).connectDown(getPosAt(3,1));
        getPosAt(3,1).connectDown(getPosAt(3,2));
        getPosAt(3,4).connectDown(getPosAt(3,5));
        getPosAt(3,5).connectDown(getPosAt(3,6));

        getPosAt(4,2).connectDown(getPosAt(4,3));
        getPosAt(4,3).connectDown(getPosAt(4,4));

		getPosAt(5,1).connectDown(getPosAt(5,3));
		getPosAt(5,3).connectDown(getPosAt(5,5));

		getPosAt(6,0).connectDown(getPosAt(6,3));
		getPosAt(6,3).connectDown(getPosAt(6,6));
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
	
	@Override
	Position[] getPossibleMillY(Position p) {
		assertValidandNotNull(p);
		Position[] millY = new Position[3];
		if(p.getX() == 0 && (p.getY() == 0 || p.getY() == 3 || p.getY() == 6)){
			millY[0] = new Position(0,0);
			millY[1] = new Position(0,3);
			millY[2] = new Position(0,6);
		}else if(p.getX() == 6 && (p.getY() == 0 || p.getY() == 3 || p.getY() == 6)){
			millY[0] = new Position(6,0);
			millY[1] = new Position(6,3);
			millY[2] = new Position(6,6);
		}else if(p.getX() == 1 && (p.getY() == 1 || p.getY() == 3 || p.getY() == 5 )){
			millY[0] = new Position(1,1);
			millY[1] = new Position(1,3);
			millY[2] = new Position(1,5);
		}else if(p.getX() == 5 && (p.getY() == 1 || p.getY() == 3 || p.getY() == 5 )){
			millY[0] = new Position(5,1);
			millY[1] = new Position(5,3);
			millY[2] = new Position(5,5);
		}else if(p.getX() == 2 && (p.getY() == 2 || p.getY() == 3 || p.getY() == 4)){
			millY[0] = new Position(2,2);
			millY[1] = new Position(2,3);
			millY[2] = new Position(2,4);
		}else if(p.getX() == 4 && (p.getY() == 2 || p.getY() == 3 || p.getY() == 4)){
			millY[0] = new Position(4,2);
			millY[1] = new Position(4,3);
			millY[2] = new Position(4,4);
		}else if(p.getX() == 3 && (p.getY() == 0 || p.getY() == 1 || p.getY() == 2)){
			millY[0] = new Position(3,0);
			millY[1] = new Position(3,1);
			millY[2] = new Position(3,2);
		}else if(p.getX() == 3 && (p.getY() == 4 || p.getY() == 5 || p.getY() == 6)){
			millY[0] = new Position(3,4);
			millY[1] = new Position(3,5);
			millY[2] = new Position(3,6);
		}
		return millY;
	}

	@Override
	Position[] getPossibleMillX(Position p) {
		assertValidandNotNull(p);
		Position[] millX = new Position[3];
		if(p.getY() == 0 && (p.getX() == 0 || p.getX() == 3 || p.getX() == 6)){
			millX[0] = new Position(0,0);
			millX[1] = new Position(3,0);
			millX[2] = new Position(6,0);
		}else if(p.getY() == 6 && (p.getX() == 0 || p.getX() == 3 || p.getX() == 6)){
			millX[0] = new Position(0,6);
			millX[1] = new Position(3,6);
			millX[2] = new Position(6,6);
		}else if(p.getY() == 1 && (p.getX() == 1 || p.getX() == 3 || p.getX() == 5 )){
			millX[0] = new Position(1,1);
			millX[1] = new Position(3,1);
			millX[2] = new Position(5,1);
		}else if(p.getY() == 5 && (p.getX() == 1 || p.getX() == 3 || p.getX() == 5 )){
			millX[0] = new Position(1,5);
			millX[1] = new Position(3,5);
			millX[2] = new Position(5,5);
		}else if(p.getY() == 2 && (p.getX() == 2 || p.getX() == 3 || p.getX() == 4)){
			millX[0] = new Position(2,2);
			millX[1] = new Position(3,2);
			millX[2] = new Position(4,2);
		}else if(p.getY() == 4 && (p.getX() == 2 || p.getX() == 3 || p.getX() == 4)){
			millX[0] = new Position(2,4);
			millX[1] = new Position(3,4);
			millX[2] = new Position(4,4);
		}else if(p.getY() == 3 && (p.getX() == 0 || p.getX() == 1 || p.getX() == 2)){
			millX[0] = new Position(0,3);
			millX[1] = new Position(1,3);
			millX[2] = new Position(2,3);
		}else if(p.getY() == 3 && (p.getX() == 4 || p.getX() == 5 || p.getX() == 6)){
			millX[0] = new Position(4,3);
			millX[1] = new Position(5,3);
			millX[2] = new Position(6,3);
		}
		return millX;
	}
    
}
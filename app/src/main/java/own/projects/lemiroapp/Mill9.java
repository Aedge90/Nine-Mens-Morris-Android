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

}
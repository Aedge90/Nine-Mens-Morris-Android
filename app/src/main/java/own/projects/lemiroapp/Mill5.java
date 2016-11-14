package own.projects.lemiroapp;

import android.support.annotation.VisibleForTesting;

public class Mill5 extends GameBoard {

	private Options.Color[][] mill5 = // ERSTES Y ZWEITES X
		{{N, I, I, N, I, I, N},
		{ I, I, I, I, I, I, I},
		{ I, I, N, N, N, I, I},
		{ N, I, N, I, N, I, N},
		{ I, I, N, N, N, I, I},
		{ I, I, I, I, I, I, I},
		{ N, I, I, N, I, I, N}};

	
	Mill5() {
        field = mill5;
        millMode =  Options.MillMode.MILL5;

		GameBoardPosition N00 = new GameBoardPosition(0,0);
		GameBoardPosition N30 = new GameBoardPosition(3,0);
		GameBoardPosition N60 = new GameBoardPosition(6,0);

		GameBoardPosition N22 = new GameBoardPosition(2,2);
		GameBoardPosition N32 = new GameBoardPosition(3,2);
		GameBoardPosition N42 = new GameBoardPosition(4,2);

		GameBoardPosition N03 = new GameBoardPosition(0,3);
		GameBoardPosition N23 = new GameBoardPosition(2,3);
		GameBoardPosition N43 = new GameBoardPosition(4,3);
		GameBoardPosition N63 = new GameBoardPosition(6,3);

		GameBoardPosition N24 = new GameBoardPosition(2,4);
		GameBoardPosition N34 = new GameBoardPosition(3,4);
		GameBoardPosition N44 = new GameBoardPosition(4,4);

		GameBoardPosition N06 = new GameBoardPosition(0,6);
		GameBoardPosition N36 = new GameBoardPosition(3,6);
		GameBoardPosition N66 = new GameBoardPosition(6,6);

	}

    @VisibleForTesting
    Mill5(Options.Color[][] field) {
        super(field);
        millMode =  Options.MillMode.MILL5;
    }

    @Override
    GameBoard getCopy() {
        return new Mill5(this);
    }

    //copy constructor
    Mill5(Mill5 other){
        super(other);
    }

	@Override
	Position[] getPossibleMillX(Position p){
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
		}else if(p.getY() == 2 && (p.getX() == 2 || p.getX() == 3 || p.getX() == 4)){
			millX[0] = new Position(2,2);
			millX[1] = new Position(3,2);
			millX[2] = new Position(4,2);
		}else if(p.getY() == 4 && (p.getX() == 2 || p.getX() == 3 || p.getX() == 4)){
			millX[0] = new Position(2,4);
			millX[1] = new Position(3,4);
			millX[2] = new Position(4,4);
		}else if(p.getY() == 3){
			//there is no mill in x direction for this position
			return null;
		}
		return millX;
	};
	
	@Override
	Position[] getPossibleMillY(Position p){
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
		}else if(p.getX() == 2 && (p.getY() == 2 || p.getY() == 3 || p.getY() == 4)){
			millY[0] = new Position(2,2);
			millY[1] = new Position(2,3);
			millY[2] = new Position(2,4);
		}else if(p.getX() == 4 && (p.getY() == 2 || p.getY() == 3 || p.getY() == 4)){
			millY[0] = new Position(4,2);
			millY[1] = new Position(4,3);
			millY[2] = new Position(4,4);
		}else if(p.getX() == 3){
			//there is no mill in y direction
			return null;
		}
		return millY;
	};
	

	@Override
	Zug moveUp(Position p) {
		if (p.getX() == 3	&& p.getY() > 3) {
			for (int i =  (p.getY()- 1); i > 3; i--)
				if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
					return new Zug(new Position(p.getX(), i), new Position(p),  null);
				else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
					break;

		} else {
			for (int i =  (p.getY()- 1); i >= 0; i--)
				if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
					return new Zug(new Position(p.getX(), i), new Position(p),  null);
				else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
					break;
		}
		return null;
	}

	@Override
	Zug moveDown(Position p) {
		if (p.getX() == 3 && p.getY() < 3) {
			for (int i =  (p.getY()+ 1); i < 3; i++)
				if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
					return new Zug(new Position(p.getX(), i), new Position(p),  null);
				else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
					break;

		} else {
			for (int i =  (p.getY()+ 1); i <= 6; i++)
				if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
					return new Zug(new Position(p.getX(), i), new Position(p),  null);
				else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
					break;
		}
		return null;
	}

	@Override
	Zug moveRight(Position p) {
		if (p.getY() == 3 && p.getX() < 3) {
			for (int i =  (p.getX()+ 1); i < 3; i++)
				if (this.getPos(i, p.getY()).equals(Options.Color.NOTHING))
					return new Zug(new Position(i, p.getY()), new Position(p),  null);
				else if (!this.getPos(i, p.getY()).equals(Options.Color.INVALID))
					break;
		} else {
			for (int i =  (p.getX()+ 1); i <= 6; i++)
				if (this.getPos(i, p.getY()).equals(Options.Color.NOTHING))
					return new Zug(new Position(i, p.getY()), new Position(p),  null);
				else if (!this.getPos(i, p.getY()).equals(Options.Color.INVALID))
					break;
		}
		return null;
	}

	@Override
	Zug moveLeft(Position p) {
		if (p.getY() == 3 && p.getX() > 3) {
			for (int i =  (p.getX()- 1); i > 3; i--)
				if (this.getPos(i, p.getY()).equals(Options.Color.NOTHING))
					return new Zug(new Position(i, p.getY()), new Position(p), null);
				else if (!this.getPos(i, p.getY()).equals(Options.Color.INVALID))
					break;

		} else {
			for (int i =  (p.getX()- 1); i >= 0; i--)
				if (this.getPos(i, p.getY()).equals(Options.Color.NOTHING))
					return new Zug(new Position(i, p.getY()), new Position(p), null);
				else if (!this.getPos(i, p.getY()).equals(Options.Color.INVALID))
					break;
		}
		return null;
	}
	
}

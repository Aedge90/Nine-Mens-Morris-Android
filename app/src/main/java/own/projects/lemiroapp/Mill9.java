package own.projects.lemiroapp;

import android.support.annotation.VisibleForTesting;

public class Mill9 extends GameBoard {
	
	private Options.Color[][] mill9 = // ERSTES Y ZWEITES X
		{{O, N, N, O, N, N, O },
		{ N, O, N, O, N, O, N },
		{ N, N, O, O, O, N, N },
		{ O, O, O, N, O, O, O },
		{ N, N, O, O, O, N, N },
		{ N, O, N, O, N, O, N },
		{ O, N, N, O, N, N, O }};

	Mill9() {
		field = mill9;
		millMode =  Options.MillMode.MILL9;
	}

	@VisibleForTesting
	Mill9(Options.Color[][] field) {
		super(field);
		millMode =  Options.MillMode.MILL9;
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

	@Override
	Zug moveUp(Position p) {
		if (p.getX() == 3	&& p.getY() > 3) {
			for (int i =  (p.getY()- 1); i > 3; i--)
				if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
					return new Zug(new Position(p.getX(), i), new Position(p), null);
				else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
					break;

		} else {
			for (int i =  (p.getY()- 1); i >= 0; i--)
				if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
					return new Zug(new Position(p.getX(), i), new Position(p), null);
				else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
					break;
		}
		return new Zug(null, null, null);
	}

	@Override
	Zug moveDown(Position p) {
		if (p.getX() == 3 && p.getY() < 3) {
			for (int i =  (p.getY()+ 1); i < 3; i++)
				if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
					return new Zug(new Position(p.getX(), i), new Position(p), null);
				else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
					break;

		} else {
			for (int i =  (p.getY()+ 1); i <= 6; i++)
				if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
					return new Zug(new Position(p.getX(), i), new Position(p), null);
				else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
					break;
		}
		return new Zug(null, null, null);
	}

	@Override
	Zug moveRight(Position p) {
		if (p.getY() == 3	&& p.getX() < 3) {
			for (int i =  (p.getX()+ 1); i < 3; i++)
				if (this.getPos(i, p.getY()).equals(Options.Color.NOTHING))
					return new Zug(new Position(i, p.getY()), new Position(p), null);
				else if (!this.getPos(i, p.getY()).equals(Options.Color.INVALID))
					break;
		} else {
			for (int i =  (p.getX()+ 1); i <= 6; i++)
				if (this.getPos(i, p.getY()).equals(Options.Color.NOTHING))
					return new Zug(new Position(i, p.getY()), new Position(p), null);
				else if (!this.getPos(i, p.getY()).equals(Options.Color.INVALID))
					break;
		}
		return new Zug(null, null, null);
	}

	@Override
	Zug moveLeft(Position p) {
		if (p.getY() == 3	&& p.getX() > 3) {
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
		return new Zug(null, null, null);
	}

}
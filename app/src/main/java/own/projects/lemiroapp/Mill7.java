package own.projects.lemiroapp;

public class Mill7 extends Spielfeld{

	private Options.Color[][] mill7 = // ERSTES Y ZWEITES X
		{{O, N, N, O, N, N, O },
		{ N, O, N, O, N, O, N },
		{ N, N, N, N, N, N, N },
		{ O, O, N, O, N, O, O },
		{ N, N, N, N, N, N, N },
		{ N, O, N, O, N, O, N },
		{ O, N, N, O, N, N, O }};
	
	Mill7() {
		this.field = mill7;
		millMode =  Options.MillMode.MILL7;
	}
	
	@Override
	Position[] getPossibleMillX(Position p){
		if(p == null){
			throw new IllegalArgumentException("getPossibleMillX: p is not valid");
		}
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
		}else if(p.getY() == 3 && (p.getX() == 0 || p.getX() == 1 || p.getX() == 3)){
			millX[0] = new Position(0,3);
			millX[1] = new Position(1,3);
			millX[2] = new Position(3,3);
		}else{
			return null;
		}
		return millX;
	};
	
	@Override
	Position[] getPossibleMillY(Position p){
		if(p == null){
			throw new IllegalArgumentException("getPossibleMillY: p is not valid");
		}
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
		}else if(p.getX() == 3 && (p.getY() == 0 || p.getY() == 1 || p.getY() == 3)){
			millY[0] = new Position(3,0);
			millY[1] = new Position(3,1);
			millY[2] = new Position(3,3);
		}else{
			return null;
		}
		return millY;
	};
	
	@Override
	// for x or y == 3 there is another possible mill
	boolean inMill7(Position p, Options.Color player) {
		Position[] millX = new Position[3];
		if(p.getY() == 3 && (p.getX() == 3 || p.getX() == 5 || p.getX() == 6)){
			millX[0] = new Position(3,3);
			millX[1] = new Position(5,3);
			millX[2] = new Position(6,3);
			int count = 0;
			for(int i = 0; i<3; i++){
				if(getPos(millX[i]).equals(player)){
					count ++;
				}
			}
			if(count == 3){
				return true;
			}
		}
		Position[] millY = new Position[3];
		if(p.getX() == 3 && (p.getY() == 3 || p.getY() == 5 || p.getY() == 6)){
			millY[0] = new Position(3,3);
			millY[1] = new Position(3,5);
			millY[2] = new Position(3,6);
			int count = 0;
			for(int i = 0; i<3; i++){
				if(getPos(millY[i]).equals(player)){
					count ++;
				}
			}
			if(count == 3){
				return true;
			}
		}
		return false;
	}
	
	@Override
	// for x or y == 3 there is another possible mill
		Position[] getMill7(Position p, Options.Color player) {
			Position[] millX = new Position[3];
			if(p.getY() == 3 && (p.getX() == 3 || p.getX() == 5 || p.getX() == 6)){
				millX[0] = new Position(3,3);
				millX[1] = new Position(5,3);
				millX[2] = new Position(6,3);
				int count = 0;
				for(int i = 0; i<3; i++){
					if(!millX[i].equals(p)){
						if(getPos(millX[i]).equals(player)){
							count ++;
						}
					}
					if(count == 2){
						return millX;
					}
				}
			}
			Position[] millY = new Position[3];
			if(p.getX() == 3 && (p.getY() == 3 || p.getY() == 5 || p.getY() == 6)){
				millY[0] = new Position(3,3);
				millY[1] = new Position(3,5);
				millY[2] = new Position(3,6);
				int count = 0;
				for(int i = 0; i<3; i++){
					if(!millY[i].equals(p)){
						if(getPos(millY[i]).equals(player)){
							count ++;
						}
					}
					if(count == 2){
						return millY;
					}
				}
			}
			return null;
		}
	
	

	@Override
	Zug moveUp(Position p) {

		for (int i =  (p.getY()- 1); i >= 0; i--)
			if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
				return new Zug(new Position(p.getX(), i), new Position(p), null,  null);
			else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
				break;

		return new Zug(null, null, null,  null);
	}

	@Override
	Zug moveDown(Position p) {

		for (int i =  (p.getY()+ 1); i <= 6; i++)
			if (this.getPos(p.getX(), i).equals(Options.Color.NOTHING))
				return new Zug(new Position(p.getX(), i), new Position(p), null,  null);
			else if (!this.getPos(p.getX(), i).equals(Options.Color.INVALID))
				break;

		return new Zug(null, null, null,  null);
	}

	@Override
	Zug moveRight(Position p) {

		for (int i =  (p.getX()+ 1); i <= 6; i++)
			if (this.getPos(i, p.getY()).equals(Options.Color.NOTHING))
				return new Zug(new Position(i, p.getY()), new Position(p), null,  null);
			else if (!this.getPos(i, p.getY()).equals(Options.Color.INVALID))
				break;

		return new Zug(null, null, null,  null);
	}

	@Override
	Zug moveLeft(Position p) {

		for (int i =  (p.getX()- 1); i >= 0; i--)
			if (this.getPos(i, p.getY()).equals(Options.Color.NOTHING))
				return new Zug(new Position(i, p.getY()), new Position(p), null,  null);
			else if (!this.getPos(i, p.getY()).equals(Options.Color.INVALID))
				break;

		return new Zug(null, null, null,  null);
	}
}

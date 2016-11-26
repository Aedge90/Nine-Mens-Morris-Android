package own.projects.lemiroapp;

import java.util.LinkedList;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

public abstract class GameBoard {

	final static int LENGTH = 7;
	GameBoardPosition[][] field;
	final GameBoardPosition N = new GameBoardPosition(0,0);
	final GameBoardPosition I = null;

    GameBoard(){}

    @VisibleForTesting
    GameBoard(Options.Color[][] inputField) {
        initField();
		if (inputField.length != LENGTH || inputField[0].length != LENGTH){
			throw new IllegalArgumentException("Constructor called with wrong size of array");
		}
		for(int y = 0; y < LENGTH; y++){
			for(int x = 0; x < LENGTH; x++){
				if(getPosAt(x,y) == I && !inputField[y][x].equals(Options.Color.INVALID)) {
					throw new IllegalArgumentException("Constructor called with invalid input field");
				}
				if(getPosAt(x,y) != I && getPosAt(x,y).equals(N)){
                    GameBoardPosition pos = new GameBoardPosition(x, y);
                    if(inputField[y][x].equals(Options.Color.BLACK)) {
                        pos.setColor(Options.Color.BLACK);
                    }else if (inputField[y][x].equals(Options.Color.WHITE)){
                        pos.setColor(Options.Color.WHITE);
                    }else if (inputField[y][x].equals(Options.Color.NOTHING)){
                        pos.setColor(Options.Color.NOTHING);
                    }
                    setPosAt(x, y, pos);
				}
			}
		}
    }

    //copy constructor
    GameBoard(GameBoard other){
        //copy the field from other
		for(int i = 0; i < LENGTH; i++){
			for(int j = 0; j < LENGTH; j++){
				field[i][j] = other.field[i][j];
			}
		}
    }

    public abstract void initField();

    @VisibleForTesting
    abstract GameBoard getCopy();

    //asserts this Position is allowed. In case of an invalid Position (or null) an Exception is thrown
    void assertValidandNotNull(Position p) {
        if (p == null) {
            throw new IllegalArgumentException("getPossibleMillX: p is null");
        }
        if (field[p.getY()][p.getX()] == I) {
            throw new IllegalArgumentException("getPossibleMillX: p is not valid");
        }
    }
	
	LinkedList<Position> getPositions(Options.Color color) {
        LinkedList<Position> positions = new LinkedList<Position>();
        for (int x=0; x < LENGTH; x++) {
            for (int y = 0; y < LENGTH; y++) {
                if(getColorAt(x,y).equals(color)) {
                    positions.add(new Position(x,y));
                }
            }
        }
        return positions;
	}

    GameBoardPosition getPosAt(int x, int y){
        return field[y][x];
    }

    GameBoardPosition getPosAt(Position pos) {
        if (pos == null){
            Log.e("GameBoard", "Error: getGameBoardPositionAt: Position was null!");
        }
        return getPosAt(pos.getX(), pos.getY());
    }

    Options.Color getColorAt(int x, int y) {
        if(field[y][x] == I){
            return Options.Color.INVALID;
        }
		return field[y][x].getColor();
	}

    Options.Color getColorAt(Position pos) {
		if (pos == null){
			Log.e("GameBoard", "Error: getColorAt: Position was null!");
		}
		return getColorAt(pos.getX(), pos.getY());
	}
	
	private void setColorAt(Position pos, Options.Color color) {
        field[pos.getY()][pos.getX()].setColor(color);
	}

    private void setPosAt(int x, int y, GameBoardPosition pos) {
        field[y][x] = pos;
    }
	
	private void makeMove(Position src, Position dest, Options.Color color) {
		field[src.getY()][src.getX()].setColor(Options.Color.NOTHING);
		field[dest.getY()][dest.getX()].setColor(color);
	}

    //this executes only the setting or moving phase of a player, regardless if a kill is contained in move
    //necessary to make is separate as the user can only add the kill after this move was done
    void executeSetOrMovePhase(Zug move, Player player) {
        if(player.getSetCount() > 0){
            if(!getColorAt(move.getDest()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to set to an occupied field by: " + getColorAt(move.getDest()));
            }
            setColorAt(move.getDest(), player.getColor());
            player.setSetCount(player.getSetCount() - 1);
        }else{
            if(!getColorAt(move.getDest()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to move to an occupied field by: " + getColorAt(move.getDest()));
            }
            if(getColorAt(move.getSrc()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to move from an empty field, which is: " + getColorAt(move.getSrc()));
            }
            makeMove(move.getSrc(), move.getDest(), player.getColor());
        }
    }

    //this executes a kill if the move contains one
    void executeKillPhase(Zug move, Player player){
        if(move.getKill() != null){
            if(getColorAt(move.getKill()).equals(player.getColor())){
                throw new IllegalArgumentException("Trying to kill own piece of color: " + player.getColor());
            }
            if(getColorAt(move.getKill()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to kill an empty field");
            }
            setColorAt(move.getKill(), Options.Color.NOTHING);
        }
    }

    //this executes the complete turn of a player, including setting or moving and killing
	void executeCompleteTurn(Zug move, Player player){
        executeSetOrMovePhase(move, player);
        executeKillPhase(move, player);
	}

    //undoes a complete turn of a player, including setting or moving and killing
	public void reverseCompleteTurn(Zug move, Player player) {
		if(move.getSrc() == null && move.getDest() != null){
			setColorAt(move.getDest(), Options.Color.NOTHING);
            player.setSetCount(player.getSetCount() + 1);
		}else{
			makeMove(move.getDest(), move.getSrc(), player.getColor());
		}
		if(move.getKill() != null){
			setColorAt(move.getKill(), player.getOtherPlayer().getColor());
		}	
	}

    //returns true if two pieces of same color are found, that form a mill together with position
    //assumes that position is of the same color that is passed here !
	boolean inMill(Position p, Options.Color player) {
        if(null != getMill(p, player)){
            //mill was found
            return true;
        }else{
            return false;
        }
    }

	Position[] getMill(Position p, Options.Color player) {
		
		Position[] millX = getPossibleMillX(p);
		
		if(millX != null){ //its null here if there is no possible mill in x direction
			int count = 0;
			for(int i = 0; i<3; i++){
				if(!millX[i].equals(p)){
					if(getColorAt(millX[i]).equals(player)){
						count ++;
					}
				}
				if(count == 2){
					return millX;
				}
			}
		}
		
		Position[] millY = getPossibleMillY(p);
		if(millY != null){ //its null here if there is no possible mill in y direction
			int count = 0;
			for(int i = 0; i<3; i++){
				if(!millY[i].equals(p)){
					if(getColorAt(millY[i]).equals(player)){
						count ++;
					}
				}
				if(count == 2){
					return millY;
				}
			}
		}
		
		if(this.getClass() == Mill7.class){
			return getMill7(p, player);
		}
		
		return null;
	}

	//is any move possible?
	boolean movesPossible(Options.Color player, int setCount) {
		if(setCount > 0){
			return true;
		}
		boolean jump = false;
		if (getPositions(player).size() == 3){
			jump = true;
		}

		if (!jump){
			for (Position p : getPositions(player)) {
				if (moveUp(p) != null)
					return true;
				if (moveDown(p) != null)
					return true;
				if (moveRight(p) != null)
					return true;
				if (moveLeft(p) != null)
					return true;
			}
		} else {
			return true;
		}
		return false;

	}
	
	//is move dest possible?
	boolean movePossible(Position src, Position dest){
		if(!getColorAt(dest).equals(Options.Color.NOTHING)){
			return false;
		}
		if(getPositions(getColorAt(src)).size() == 3){
			return true;
		}

		if(moveUp(src) != null && dest.equals(moveUp(src).getDest())){
			return true;
		}
		if(moveDown(src) != null && dest.equals(moveDown(src).getDest())) {
			return true;
		}
		if(moveRight(src) != null && dest.equals(moveRight(src).getDest())) {
			return true;
		}
		if(moveLeft(src) != null && dest.equals(moveLeft(src).getDest())) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		String print = "";

        for(int y = 0; y < LENGTH; y++) {
            String line1 = "";
            String line2 = "";
            String line3 = "";
            for (int x = 0; x < LENGTH; x++) {
                char[] pos1 = {' ',' ',' ', ' ', ' '};
                char[] pos2 = {' ',' ',' ', ' ', ' '};
                char[] pos3 = {' ',' ',' ', ' ', ' '};
                if(getColorAt(x,y).equals(Options.Color.INVALID)){
                    line1 += new String(pos1);
                    line2 += new String(pos2);
                    line3 += new String(pos3);
                    continue;
                }
                if(getColorAt(x,y).equals(Options.Color.BLACK)){
                    pos2[2] = 'B';
                }else if(getColorAt(x,y).equals(Options.Color.WHITE)){
                    pos2[2] = 'W';
                }else if(getColorAt(x,y).equals(Options.Color.NOTHING)){
                    pos2[2] = 'N';
                }
                if(getPosAt(x,y).getLeft() != null){
                    pos2[0] = '-';
                    pos2[1] = '-';
                }
                if(getPosAt(x,y).getUp() != null){
                    pos1[2] = '|';
                }
                if(getPosAt(x,y).getRight() != null){
                    pos2[3] = '-';
                    pos2[4] = '-';
                }
                if(getPosAt(x,y).getDown() != null){
                    pos3[2] = '|';
                }
                line1 += new String(pos1);
                line2 += new String(pos2);
                line3 += new String(pos3);
            }
            line1 += '\n';
            line2 += '\n';
            line3 += '\n';
            print += line1 + line2 + line3;
        }
		return print;
	}
	
	//the following methods are implemented by subclasses
	Position[] getPossibleMillX(Position p){
		return null;
	};
	Position[] getPossibleMillY(Position p){
		return null;
	};
	Zug moveUp(Position p) {
		return null;
	}
	Zug moveDown(Position p) {
		return null;
	}
	Zug moveLeft(Position p) {
		return null;
	}
	Zug moveRight(Position p) {
		return null;
	}
	
	Position[] getMill7(Position p, Options.Color player) {
		return null;
	}
	
	boolean inMill7(Position p, Options.Color player) {
		return false;
	}
}

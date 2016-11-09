package own.projects.lemiroapp;

import java.util.LinkedList;

import android.graphics.Path;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

public abstract class GameBoard {

	final static int LENGTH = 7;
	Options.Color[][] field;
	final Options.Color O = Options.Color.NOTHING;
	final Options.Color N = Options.Color.INVALID;
	Options.MillMode millMode;

    GameBoard(){}

    @VisibleForTesting
    GameBoard(Options.Color[][] field) {
        this.field = field;
    }

    //copy constructor
    GameBoard(GameBoard other){
        //copy the field from other
        field = new Options.Color[other.field.length][];
        for(int i = 0; i < other.field.length; i++) {
            field[i] = other.field[i].clone();
        }
        millMode = other.millMode;
    }

    @VisibleForTesting
    abstract GameBoard getCopy();


    //checks if this Position is allowed or an invalid Position
    boolean isValid(Position p){
        if (field[p.getY()][p.getX()] == N) {
            return false;
        }else{
            return true;
        }
    }

    //asserts this Position is allowed. In case of an invalid Position (or null) an Exception is thrown
    void assertValidandNotNull(Position p) {
        if (p == null) {
            throw new IllegalArgumentException("getPossibleMillX: p is null");
        }
        if (field[p.getY()][p.getX()] == N) {
            throw new IllegalArgumentException("getPossibleMillX: p is not valid");
        }
    }
	
	LinkedList<Position> getPositions(Options.Color color) {
        LinkedList<Position> positions = new LinkedList<Position>();
        for (int x=0; x < LENGTH; x++) {
            for (int y = 0; y < LENGTH; y++) {
                if(getPos(x,y).equals(color)) {
                    positions.add(new Position(x,y));
                }
            }
        }
        return positions;
	}
	
	Options.MillMode getMillVar(){
		return millMode;
	}
	
	Options.Color getPos(int x, int y) {
		return field[y][x];
	}

	Options.Color getPos(Position pos) {
		if (pos == null){
			Log.e("GameBoard", "Error: getPos: Position was null!");
		}
		return getPos(pos.getX(), pos.getY());
	}
	
	private void setPos(Position pos, Options.Color color) {
        field[pos.getY()][pos.getX()] = color;
	}
	
	private void makeMove(Position src, Position dest, Options.Color color) {
		field[src.getY()][src.getX()] = Options.Color.NOTHING;
		field[dest.getY()][dest.getX()] = color;
	}

    //this executes only the setting or moving phase of a player, regardless if a kill is contained in move
    //necessary to make is separate as the user can only add the kill after this move was done
    void executeSetOrMovePhase(Zug move, Player player) {
        if(player.getSetCount() > 0){
            if(!getPos(move.getDest()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to set to an occupied field by: " + getPos(move.getDest()));
            }
            setPos(move.getDest(), player.getColor());
            player.setSetCount(player.getSetCount() - 1);
        }else{
            if(!getPos(move.getDest()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to move to an occupied field by: " + getPos(move.getDest()));
            }
            if(getPos(move.getSrc()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to move from an empty field, which is: " + getPos(move.getSrc()));
            }
            makeMove(move.getSrc(), move.getDest(), player.getColor());
        }
    }

    //this executes a kill if the move contains one
    void executeKillPhase(Zug move, Player player){
        if(move.getKill() != null){
            if(getPos(move.getKill()).equals(player.getColor())){
                throw new IllegalArgumentException("Trying to kill own piece of color: " + player.getColor());
            }
            if(getPos(move.getKill()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to kill an empty field");
            }
            setPos(move.getKill(), Options.Color.NOTHING);
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
			setPos(move.getDest(), Options.Color.NOTHING);
            player.setSetCount(player.getSetCount() + 1);
		}else{
			makeMove(move.getDest(), move.getSrc(), player.getColor());
		}
		if(move.getKill() != null){
			setPos(move.getKill(), player.getOtherPlayer().getColor());
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
					if(getPos(millX[i]).equals(player)){
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
					if(getPos(millY[i]).equals(player)){
						count ++;
					}
				}
				if(count == 2){
					return millY;
				}
			}
		}
		
		if(millMode.equals(Options.MillMode.MILL7)){
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
		if(!getPos(dest).equals(Options.Color.NOTHING)){
			return false;
		}
		if(getPositions(getPos(src)).size() == 3){
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
		print += "    0 1 2 3 4 5 6\n------------------\n";
		for (int y = 0; y < field.length; y++) {
			print += y + " | ";
			for (int x = 0; x < field[y].length; x++) {
				print += !getPos(x, y).equals(Options.Color.INVALID) ? getPos(x, y) + " " : "  ";

			}
			print += '\n';
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

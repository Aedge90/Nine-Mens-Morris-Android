package own.projects.lemiroapp;

import java.util.LinkedList;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

public abstract class GameBoard {

	final static int LENGTH = 7;
	final static GameBoardPosition N = new GameBoardPosition(0,0);
	final static GameBoardPosition I = null;

    GameBoardPosition[][] field;

    LinkedList<Position> allValidPositions = new LinkedList<Position>();

    static enum GameState {
        RUNNING, DRAW, WON_NO_MOVES, WON_KILLED_ALL
    };

    GameBoard(){}

    @VisibleForTesting
    GameBoard(Options.Color[][] inputField) {
        initField();
		if (inputField.length != LENGTH || inputField[0].length != LENGTH){
			throw new IllegalArgumentException("Constructor called with wrong size of array");
		}
		for(int y = 0; y < LENGTH; y++){
			for(int x = 0; x < LENGTH; x++){
				if(getGameBoardPosAt(x,y) == I && !inputField[y][x].equals(Options.Color.INVALID)) {
					throw new IllegalArgumentException("Constructor called with invalid input field");
				}
				if(getGameBoardPosAt(x,y) != I){
                    if(inputField[y][x].equals(Options.Color.BLACK)) {
                        getGameBoardPosAt(x,y).setColor(Options.Color.BLACK);
                    }else if (inputField[y][x].equals(Options.Color.WHITE)){
                        getGameBoardPosAt(x,y).setColor(Options.Color.WHITE);
                    }else if (inputField[y][x].equals(Options.Color.NOTHING)){
                        getGameBoardPosAt(x,y).setColor(Options.Color.NOTHING);
                    }
				}
			}
		}
    }

    //copy constructor
    GameBoard(GameBoard other){
        field = new GameBoardPosition[LENGTH][LENGTH];
        //copy the field from other
		for(int i = 0; i < LENGTH; i++){
			for(int j = 0; j < LENGTH; j++){
                if(other.field[i][j] != null) {
                    field[i][j] = new GameBoardPosition(other.field[i][j]);
                    allValidPositions.add(new Position(field[i][j]));
                }
			}
		}
    }

    void initGameBoardPositions(){
        for(int i = 0; i < LENGTH; i++){
            for(int j = 0; j < LENGTH; j++){
                if(!(field[i][j] == I)) {
                    field[i][j] = new GameBoardPosition(j,i);
                    field[i][j].setColor(Options.Color.NOTHING);
                    allValidPositions.add(new Position(field[i][j]));
                }
            }
        }
    }

    public abstract void initField();

    @VisibleForTesting
    abstract GameBoard getCopy();

    public LinkedList<Position> getPositions(Options.Color player) {
        LinkedList<Position> result = new LinkedList<Position>();
        for(Position p : allValidPositions){
            if(getGameBoardPosAt(p).getColor().equals(player)) {
                result.add(p);
            }
        }
        return result;
    }

    GameBoardPosition getGameBoardPosAt(int x, int y){
        return field[y][x];
    }

    GameBoardPosition getGameBoardPosAt(Position pos) {
        if (pos == null){
            Log.e("GameBoard", "Error: getGameBoardPositionAt: Position was null!");
        }
        return getGameBoardPosAt(pos.getX(), pos.getY());
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
	
	private void makeSetMove(Position dest, Options.Color color) {
        field[dest.getY()][dest.getX()].setColor(color);
	}

    private void makeKillMove(Position dest) {
        field[dest.getY()][dest.getX()].setColor(Options.Color.NOTHING);
    }
	
	private void makeMove(Position src, Position dest, Options.Color color) {
		field[src.getY()][src.getX()].setColor(Options.Color.NOTHING);
		field[dest.getY()][dest.getX()].setColor(color);
	}

    //this executes only the setting or moving phase of a player, regardless if a kill is contained in move
    //necessary to make is separate as the user can only add the kill after this move was done
    void executeSetOrMovePhase(Move move, Player player) {
        if(player.getSetCount() > 0){
            if(!getColorAt(move.getDest()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to set to an occupied field by: " + getColorAt(move.getDest()));
            }
            makeSetMove(move.getDest(), player.getColor());
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
    void executeKillPhase(Move move, Player player){
        if(move.getKill() != null){
            if(getColorAt(move.getKill()).equals(player.getColor())){
                throw new IllegalArgumentException("Trying to kill own piece of color: " + player.getColor());
            }
            if(getColorAt(move.getKill()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to kill an empty field");
            }
            makeKillMove(move.getKill());
        }
    }

    //this executes the complete turn of a player, including setting or moving and killing
	void executeCompleteTurn(Move move, Player player){
        executeSetOrMovePhase(move, player);
        executeKillPhase(move, player);
	}

    //undoes a complete turn of a player, including setting or moving and killing
	public void reverseCompleteTurn(Move move, Player player) {
		if(move.getSrc() == null && move.getDest() != null){
			makeKillMove(move.getDest());
            player.setSetCount(player.getSetCount() + 1);
        }else{
			makeMove(move.getDest(), move.getSrc(), player.getColor());
		}
		if(move.getKill() != null){
			makeSetMove(move.getKill(), player.getOtherPlayer().getColor());
		}	
	}

    //returns true if move opens a mill and the mill can not be denied by the opponent in the next move
    boolean opensMillSafely (Move move, Player player){
        if(move.getSrc() == null){
            return false;
        }
        if(!inMill(move.getSrc(), player.getColor())){
            return false;
        }
        //check if enemy could move a piece into the mill in the next move
        if(player.getOtherPlayer().getSetCount() > 0 || getPositions(player.getOtherPlayer().getColor()).size() == 3){
            return false;
        }
        GameBoardPosition[] neighbors = getGameBoardPosAt(move.getSrc()).getNeighbors();
        for(int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null && getColorAt(neighbors[i]).equals(player.getOtherPlayer().getColor())) {
                return false;
            }
        }
        return true;
    }

    boolean preventsMill (Position p, Player player){
        Position[] preventedMill = getMill(p, player.getOtherPlayer().getColor());
        if(preventedMill == null){
            return false;
        }
        //check if enemy could actually move a piece into the mill in the next move --> prevented mill
        if(player.getOtherPlayer().getSetCount() > 0 || getPositions(player.getOtherPlayer().getColor()).size() == 3){
            return true;
        }
        GameBoardPosition[] neighbors = getGameBoardPosAt(p).getNeighbors();
        for(int i = 0; i < neighbors.length; i++) {
            //check if enemy could actually move a piece that is not part of the mill to form his mill
            if(!preventedMill[0].equals(neighbors[i]) && !preventedMill[1].equals(neighbors[i]) && !preventedMill[2].equals(neighbors[i])) {
                if (neighbors[i] != null && getColorAt(neighbors[i]).equals(player.getOtherPlayer().getColor())) {
                    return true;
                }
            }
        }
        return false;
    }

    //returns true if two pieces of color player are found, that form a mill together with position
	boolean inMill(Position p, Options.Color player) {
        if(null != getMill(p, player)){
            //mill was found
            return true;
        }else{
            return false;
        }
    }

    //if two pieces of color player are found, that form a mill together with position
    //an array containing the two pieces and position is returned, else null is returned
	Position[] getMill(Position p, Options.Color player) {
        GameBoardPosition[] neighbors = getGameBoardPosAt(p).getNeighbors();
        for(int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null && getColorAt(neighbors[i]).equals(player)) {
                GameBoardPosition neighborOfNeighbor = neighbors[i].getNeighbors()[i];
                if (neighborOfNeighbor != null && getColorAt(neighborOfNeighbor).equals(player)) {
                    return new Position[]{new Position(neighborOfNeighbor), new Position(neighbors[i]), new Position(p)};
                }
                GameBoardPosition oppositeNeighbor = getGameBoardPosAt(p).getOpposite(neighbors[i]);
                if (neighbors[i] != null && getColorAt(neighbors[i]).equals(player) &&
                        oppositeNeighbor != null && getColorAt(oppositeNeighbor).equals(player)) {
                    return new Position[]{new Position(neighbors[i]), new Position(p), new Position(oppositeNeighbor)};
                }
            }
        }
		return null;
	}

	//is any move possible?
	boolean movesPossible(Options.Color player, int setCount) {
        LinkedList<Position> positionsOfPlayer = getPositions(player);
		if(setCount > 0){
			return true;
		}
		boolean jump = false;
		if (positionsOfPlayer.size() == 3){
			jump = true;
		}

		if (!jump){
			for (Position p : positionsOfPlayer) {
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

	Move moveUp(Position p) {
        Position dest = getGameBoardPosAt(p).getUp();
        if(dest != null && getColorAt(dest).equals(Options.Color.NOTHING)){
            return new Move(new Position(dest), new Position(p), null);
        }
		return null;
	}

	Move moveDown(Position p) {
        Position dest = getGameBoardPosAt(p).getDown();
        if(dest != null && getColorAt(dest).equals(Options.Color.NOTHING)){
            return new Move(new Position(dest), new Position(p), null);
        }
        return null;
	}

	Move moveLeft(Position p) {
        Position dest = getGameBoardPosAt(p).getLeft();
        if(dest != null && getColorAt(dest).equals(Options.Color.NOTHING)){
            return new Move(new Position(dest), new Position(p), null);
        }
        return null;
	}

	Move moveRight(Position p) {
        Position dest = getGameBoardPosAt(p).getRight();
        if(dest != null && getColorAt(dest).equals(Options.Color.NOTHING)){
            return new Move(new Position(dest), new Position(p), null);
        }
        return null;
	}

    public LinkedList<Position> getAllValidPositions() {
        return allValidPositions;
    }

    GameState getState(Player player) {

        //TODO initialize remi count correctly
        int remiCount = 20;

        if(getPositions(Options.Color.BLACK).size() == 3 && getPositions(Options.Color.WHITE).size() == 3){
            remiCount --;
            if(remiCount == 0){
                return GameState.DRAW;
            }
        }

        //only the other player can have lost as its impossible for maxPlayer to commit suicide
        if(!movesPossible(player.getOtherPlayer().getColor(), player.getOtherPlayer().getSetCount())){
            return GameState.WON_NO_MOVES;
        }else if ((getPositions(player.getOtherPlayer().getColor()).size() < 3 && player.getOtherPlayer().getSetCount() <= 0)) {
            return GameState.WON_KILLED_ALL;
        }

        return GameState.RUNNING;
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
                if(getGameBoardPosAt(x,y).getLeft() != null){
                    pos2[0] = '-';
                    pos2[1] = '-';
                }
                if(getGameBoardPosAt(x,y).getUp() != null){
                    pos1[2] = '|';
                }
                if(getGameBoardPosAt(x,y).getRight() != null){
                    pos2[3] = '-';
                    pos2[4] = '-';
                }
                if(getGameBoardPosAt(x,y).getDown() != null){
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

}

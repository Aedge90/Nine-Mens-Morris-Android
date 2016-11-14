package own.projects.lemiroapp;

import java.util.LinkedList;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

public class Strategie {

	GameBoard field;
	Zug move;
	int startDepth;
	private ProgressUpdater up;
    private Player maxPlayer;
	static final int MAX = 100000;
    static final int MIN = -100000;

	Strategie(GameBoard field, ProgressUpdater up) {
		this.field = field;
		this.up = up;
	}
	
	private void addnonJumpMoves(LinkedList<Zug> moves, Player player){
		for (int i = 0; i<field.getPositions(player.getColor()).size(); i++) {
			//no iterator here to avoid concurrent modification exception
			Position p = field.getPositions(player.getColor()).get(i);
			if (field.moveUp(p) != null)
				addpossibleKillstoMove(moves, field.moveUp(p), player);
			if (field.moveDown(p) != null)
				addpossibleKillstoMove(moves, field.moveDown(p), player);
			if (field.moveRight(p) != null)
				addpossibleKillstoMove(moves, field.moveRight(p), player);
			if (field.moveLeft(p) != null)
				addpossibleKillstoMove(moves, field.moveLeft(p), player);
		}
	}

	private void addJumpMoves(LinkedList<Zug> moves, Player player){
		for (int x = 0; x < field.LENGTH; x++) {
			for (int y = 0; y < field.LENGTH; y++) {
				if (field.getPos(x, y).equals(Options.Color.NOTHING)) {
					for (int i = 0; i<field.getPositions(player.getColor()).size(); i++) {
						//no iterator here to avoid concurrent modification exception
						Position p = field.getPositions(player.getColor()).get(i);
						addpossibleKillstoMove(moves, new Zug(new Position(x, y), p, null), player);
					}
				}
			}
		}
	}
	
	private void addSetMoves(LinkedList<Zug> moves, Player player){
		for (int x = 0; x < field.LENGTH; x++) {
			for (int y = 0; y < field.LENGTH; y++) {
				if (field.getPos(x, y).equals(Options.Color.NOTHING)) {
					addpossibleKillstoMove(moves, new Zug(new Position(x, y), null, null), player);
				}
			}
		}
	}

    @VisibleForTesting
	void addpossibleKillstoMove(LinkedList<Zug> possibleMovessoFar, Zug move, Player player){
			boolean inMill = false;
            field.executeSetOrMovePhase(move, player);
			inMill = field.inMill(move.getDest(), player.getColor());
            field.reverseCompleteTurn(move, player);
            //player has a mill after doing this move --> he can kill a piece of the opponent
			if(inMill){
				int added = 0;
				for (Position kill : field.getPositions(player.getOtherPlayer().getColor())) {
					if(!field.inMill(kill, player.getOtherPlayer().getColor())){
						Zug killMove = new Zug(move.getDest(), move.getSrc(), kill);
						possibleMovessoFar.add(killMove);
						added++;
					}
				}
                //no pieces to kill because all are in a mill --> do it again but now add all pieces
                //as you are allowed to kill if all pieces are part of a mill
				if(added == 0){
					for (Position kill2 : field.getPositions(player.getOtherPlayer().getColor())) {
						Zug killMove = new Zug(move.getDest(), move.getSrc(), kill2);
						possibleMovessoFar.add(killMove);
					}
				}
			}else{
				possibleMovessoFar.add(move);
			}
	}

	//returns a list of moves that the player is able to do
	LinkedList<Zug> possibleMoves(Player player) {
		LinkedList<Zug> poss = new LinkedList<Zug>();
        //do not compute possible moves if the player has lost, otherwise it breaks the evaluation
        //as a state AFTER loosing would be evaluated instead of the final state after the final kill
        if(field.getPositions(player.getColor()).size() < 3 && player.getSetCount() <= 0){
            return poss;
        }
		if(player.getSetCount() > 0){
			addSetMoves(poss, player);
		}else{
			boolean jump = false;
			if (field.getPositions(player.getColor()).size() <= 3){
				jump = true;
			}
			if (!jump) {
				addnonJumpMoves(poss, player);
			} else {
				addJumpMoves(poss, player);
			}
		}
		return poss;
	}

	//maximizing player has got to return higher values for better situations
    //minimizing player has got to return lower values the better his situation
    @VisibleForTesting
	int bewertung(Player player, LinkedList<Zug> moves) {

        if (moves.size() == 0) {
            //worst case: player can not make any moves --> game is lost
            //or player has less than 3 pieces and has no pieces left to set --> game is lost
            if (player.equals(maxPlayer)) {
                //Log.i("Strategie", "bewertung: size: " + moves.size() + " ret: " + MIN);
                return MIN;
            }else{
                //Log.i("Strategie", "bewertung: size: " + moves.size() + " ret: " + MAX);
                return MAX;
            }
        }

		int ret = 0;
		//ret += field.getPositions(player.getColor()).size() * 500;
		//ret += field.getPositions(player.getOtherPlayer().getColor()).size() * (-1000);

        //simply try to always have more pieces than the enemy. This should motivate to kill to make ret bigger
        ret = field.getPositions(player.getColor()).size() - field.getPositions(player.getOtherPlayer().getColor()).size();



        //Log.i("Strategie", "bewertung: ret: " + ret);

        if (player.equals(maxPlayer)) {
            return ret;
        }else{
            return -ret;
        }
	}

	//setCountMax: number of stones to set for max player
	//setCountMin: analog
	private int max(int depth, int alpha, int beta, Player player, int bewertung) throws InterruptedException {
		if(Thread.interrupted()){
			throw new InterruptedException("Computation of Bot Move was interrupted!");
		}
		LinkedList<Zug> moves = possibleMoves(player);
		if(depth == startDepth){
			up.initialize(moves.size());
		}
        //evaluate every move and add evaluations together rather than just evaluating the last state of the gameboard.
        //By this we avoid the bot not killing because the evaluation of a kill in a move in the future
        //will be the same as a kill in the current move, thus sometimes the bot will not kill in his current move
		if (depth != startDepth){
			bewertung += bewertung(player, moves);
		}
		//end reached or no more moves available, maybe because he is trapped or because he lost
		if (depth == 0 || moves.size() == 0){
			return bewertung;
		}
		int maxWert = alpha;
		for (Zug z : moves) {
			if(depth == startDepth){
				up.update();
			}
			field.executeCompleteTurn(z, player);
			int wert = min(depth-1, maxWert, beta, player.getOtherPlayer(), bewertung);
			field.reverseCompleteTurn(z, player);
			if (wert > maxWert) {
                Log.i("Strategie", "bewertung was: " + wert + "  " + z + " depth:  " + depth );
				maxWert = wert;
				if (maxWert >= beta)             
					break;
				if (depth == startDepth)
					move = z;
			}
		}
		return maxWert;
	}

	private int min(int depth, int alpha, int beta, Player player, int bewertung) throws InterruptedException {
		LinkedList<Zug> moves = possibleMoves(player);
		bewertung += bewertung(player, moves);
		if (depth == 0 || moves.size() == 0){
			return bewertung;
		}
		int minWert = beta;
		for (Zug z : moves) {
			field.executeCompleteTurn(z, player);
			int wert = max(depth-1, alpha, minWert, player.getOtherPlayer(), bewertung);
			field.reverseCompleteTurn(z, player);
			if (wert < minWert) {
				minWert = wert;
				if (minWert <= alpha){ 
					break;
				}
			}
		}
		return minWert;

	}

	public Zug computeMove(Player player) throws InterruptedException {
				
		startDepth = player.getDifficulty().ordinal() + 2;
		//decrease startDepth if there are too much possible moves to save time
		int actualDepth = startDepth;
		if(startDepth > 4){
			if (field.getPositions(player.getColor()).size() <= 3 || field.getPositions(player.getOtherPlayer().getColor()).size() <= 3){
				startDepth = 4;
			}
		}
		
		Log.i("Strategie", "computeMove started for Player " + player.getColor() + " startDepth: " + startDepth);

        move = null;

        maxPlayer = player;

		max(startDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, player,0);
		
		up.reset();
		
		//restore old startDepth
		startDepth = actualDepth;
		
		return move;
	}

}
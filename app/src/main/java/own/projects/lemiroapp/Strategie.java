package own.projects.lemiroapp;

import java.util.LinkedList;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

public class Strategie {

	GameBoard field;
	Zug move;
	int startDepth;
	private ProgressUpdater up;
	static final int MAX = Integer.MAX_VALUE;
	static final int MIN = Integer.MIN_VALUE;

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
        Log.i("Stratgie" , player.getColor() + " " + poss.get(0));
		return poss;
	}

	//player: player to evaluate
    @VisibleForTesting
	int bewertung(Player player) {
		
		if (field.getPositions(player.getColor()).size() < 3 && player.getSetCount() <= 0) {
			//worst case: player has less than 3 pieces and has no pieces left to set --> game is lost
			return MIN;
		}
		if (field.getPositions(player.getOtherPlayer().getColor()).size() < 3 && player.getOtherPlayer().getSetCount() <= 0) {
			//best case: other player has less than 3 pieces and has no pieces left to set --> he has lost
			return MAX;
		}

        //TODO think about this problem: if player could win, he would proceed to make moves until the depth is reached
        //TODO only in the end, he would realize (in bewertung()) that he could win although he could win earlier

		int ret = 0;
		ret += field.getPositions(player.getColor()).size() * 500;
		ret += field.getPositions(player.getOtherPlayer().getColor()).size() * (-1000);

		//TODO dont compute moves again although it has been computed before
		LinkedList<Zug> moves = possibleMoves(player);
		if (moves.size() == 0) {
            //worst case: player can not make any moves --> game is lost
			return MIN;
		}

		/*
		//this is probably false, as we only evaluate the current situation. This would mean the other player can
		//move this round which isnt true
		//also it should be enough to say that its a worst case if you cant move anymore
		//as the minimizing player will use that
		moves = possibleMoves(player.getOtherPlayer());
		if (moves.size() == 0) {
			return MAX;
		}
		*/

		return ret;
	}

	//setCountMax: number of stones to set for max player
	//setCountMin: analog
	private int max(int depth, int alpha, int beta, Player player) throws InterruptedException {
		if(Thread.interrupted()){
			throw new InterruptedException("Computation of Bot Move was interrupted!");
		}
		LinkedList<Zug> moves = possibleMoves(player);
		if(depth == startDepth){
			up.initialize(moves.size());
		}
        //end reached or no more moves available, maybe because he is trapped or because he lost
		if (depth == 0 || moves.size() == 0){
			int bewertung = bewertung(player);
			return bewertung;
		}
		int maxWert = alpha;
		for (Zug z : moves) {
			if(depth == startDepth){
				up.update();
			}
			field.executeCompleteTurn(z, player);
			int wert = min(depth-1, maxWert, beta, player.getOtherPlayer());
			field.reverseCompleteTurn(z, player);
			if (wert > maxWert) {
				maxWert = wert;
				if (maxWert >= beta)             
					break;
				if (depth == startDepth)
					move = z;
			}
		}
		return maxWert;
	}

	private int min(int depth, int alpha, int beta, Player player) throws InterruptedException {
		LinkedList<Zug> moves = possibleMoves(player);
		if (depth == 0 || moves.size() == 0){
			int bewertung = bewertung(player);
			return bewertung;
		}
		int minWert = beta;
		for (Zug z : moves) {
			field.executeCompleteTurn(z, player);
			int wert = max(depth-1, alpha, minWert, player.getOtherPlayer());
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

		max(startDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, player);
		
		up.reset();
		
		//restore old startDepth
		startDepth = actualDepth;
		
		return move;
	}

}
package own.projects.lemiroapp;

import java.util.LinkedList;

import android.util.Log;
import android.widget.ProgressBar;

public class Strategie {

	Spielfeld field;
	Zug move;
	int startDepth;
	private ProgressUpdater up;
	private static final int BEST = Integer.MAX_VALUE - 10;
	private static final int WORST = Integer.MIN_VALUE + 10;

	Strategie(Spielfeld field, ProgressUpdater up) {
		this.field = field;
		this.up = up;
	}

	private Options.Color getOtherPlayer(Options.Color player){
		if(player.equals(Options.Color.BLACK)){
			return Options.Color.WHITE;
		}else if(player.equals(Options.Color.WHITE)){
			return Options.Color.BLACK;
		}else{
			throw new IllegalArgumentException("player not found!");
		}
	}
	
	private void addnonJumpMoves(LinkedList<Zug> moves,Options.Color player){
		for (int i = 0; i<field.getPositions(player).size(); i++) { 
			//no iterator here to avoid concurrent modification exception
			Position p = field.getPositions(player).get(i);
			if (field.moveUp(p).isValid())
				addpossibleKillstoMove(moves, field.moveUp(p), player);
			if (field.moveDown(p).isValid())
				addpossibleKillstoMove(moves, field.moveDown(p), player);
			if (field.moveRight(p).isValid())
				addpossibleKillstoMove(moves, field.moveRight(p), player);
			if (field.moveLeft(p).isValid())
				addpossibleKillstoMove(moves, field.moveLeft(p), player);
		}
	}

	private void addJumpMoves(LinkedList<Zug> moves, Options.Color player){
		for (int x = 0; x < field.LENGTH; x++) {
			for (int y = 0; y < field.LENGTH; y++) {
				if (field.getPos(x, y).equals(Options.Color.NOTHING)) {
					for (int i = 0; i<field.getPositions(player).size(); i++) { 
						//no iterator here to avoid concurrent modification exception
						Position p = field.getPositions(player).get(i);
						addpossibleKillstoMove(moves, new Zug(new Position(x, y), p, null, null), player);
					}
				}
			}
		}
	}
	
	private void addSetMoves(LinkedList<Zug> moves, Options.Color player){
		for (int x = 0; x < field.LENGTH; x++) {
			for (int y = 0; y < field.LENGTH; y++) {
				if (field.getPos(x, y).equals(Options.Color.NOTHING)) {
					addpossibleKillstoMove(moves, new Zug(null, null,new Position(x, y), null), player);
				}
			}
		}
	}

	private void addpossibleKillstoMove(LinkedList<Zug> possibleMovessoFar, Zug move, Options.Color player){
			boolean inMill = false;
			if(move.getSet()!= null){
				field.setPos(move.getSet(), player); // set to check if mill
				inMill = field.inMill(move.getSet(), player);
				field.setPos(move.getSet(), Options.Color.NOTHING); //undo set
			}else{
				field.makeMove(move.getSrc(), move.getDest(), player); // make move to check if mill
				inMill = field.inMill(move.getDest(), player);
				field.makeMove(move.getDest(), move.getSrc(), player);
			}
			if(inMill){
				int added = 0;
				for (Position kill : field.getPositions(getOtherPlayer(player))) {
					if(!field.inMill(kill, getOtherPlayer(player))){
						Zug killMove = new Zug(move.getDest(), move.getSrc(), move.getSet(), kill);
						possibleMovessoFar.add(killMove);
						added++;
					}
				}
				if(added == 0){
					for (Position kill2 : field.getPositions(getOtherPlayer(player))) {
						Zug killMove = new Zug(move.getDest(), move.getSrc(), move.getSet(), kill2);
						possibleMovessoFar.add(killMove);
					}
				}
			}else{
				possibleMovessoFar.add(move);
			}
	}
	
	LinkedList<Zug> possibleMoves(Options.Color player, int setCount) {
		LinkedList<Zug> poss = new LinkedList<Zug>(); // pos : possibility
		if(setCount > 0){
			addSetMoves(poss, player);
		}else{
			boolean jump = false;
			if (field.getPositions(player).size() <= 3){
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

	//setCount: setCount of Player to evaluate
	//setCountOther: setCount of other Player
	int bewertung(Options.Color player, int setCount, int setCountOther) {
		
		if (field.getPositions(player).size() < 3 && setCount <= 0)
			return WORST;
		if (field.getPositions(getOtherPlayer(player)).size() < 3 && setCountOther <= 0)
			return BEST ;
		
		int ret = 0;
		ret += field.getPositions(player).size() * 500;
		ret += field.getPositions(getOtherPlayer(player)).size() * (-1000);

		LinkedList<Zug> moves = possibleMoves(player, setCount);
		if (moves.size() == 0)
			return WORST;
		moves = possibleMoves(getOtherPlayer(player), setCountOther);
		if (moves.size() == 0)
			return BEST ;
		
		return ret;
	}

	//setCountMax: number of stones to set for max player
	//setCountMin: analog
	int max(int depth, int alpha, int beta, Options.Color player, final int setCountMax, final int setCountMin) throws InterruptedException {
		if(Thread.interrupted()){
			throw new InterruptedException("Computation of Bot Move was interrupted!");
		}
		LinkedList<Zug> moves = possibleMoves(player, setCountMax);
		if(depth == startDepth){
			up.initialize(moves.size());
		}
		if (depth == 0 || moves.size() == 0){
			int bewertung = bewertung(player, setCountMax, setCountMin);
			return bewertung;
		}
		int maxWert = alpha;
		for (Zug z : moves) {
			if(depth == startDepth){
				up.update();
			}
			field.makeWholeMove(z, player);
			int wert = min(depth-1, maxWert, beta, getOtherPlayer(player), setCountMax-1, setCountMin);    
			field.reverseWholeMove(z, player);
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

	int min(int depth, int alpha, int beta, Options.Color player, final int setCountMax, final int setCountMin) throws InterruptedException {
		LinkedList<Zug> moves = possibleMoves(player, setCountMin);
		if (depth == 0 || moves.size() == 0){
			int bewertung = bewertung( player, setCountMin, setCountMax);
			return bewertung;
		}
		int minWert = beta;
		for (Zug z : moves) {
			field.makeWholeMove(z, player);
			int wert = max(depth-1, alpha, minWert, getOtherPlayer(player),setCountMax, setCountMin-1); 
			field.reverseWholeMove(z, player);
			if (wert < minWert) {
				minWert = wert;
				if (minWert <= alpha){ 
					break;       
				}
			}
		}
		return minWert;

	}

	Zug computeMove(Options.Color player, Options.Difficulties hisDifficulty, final int setCountMax, final int setCountMin) throws InterruptedException {
				
		startDepth = hisDifficulty.ordinal() + 2;
		//decrease startDepth if there are too much possible moves to save time
		int actualDepth = startDepth;
		if(startDepth > 4){
			if (field.getPositions(player).size() <= 3 || field.getPositions(getOtherPlayer(player)).size() <= 3){
				startDepth = 4;
			}
		}
		
		Log.i("Strategie", "computeMove started for Player " + player + " setCountMax: " + setCountMax + " setCountMin: " + setCountMin + " startDepth: " + startDepth);
		
		move = new Zug(null, null, null, null);

		max(startDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, player, setCountMax, setCountMin);
		
		up.reset();
		
		//restore old startDepth
		startDepth = actualDepth;
		
		return move;
	}

}
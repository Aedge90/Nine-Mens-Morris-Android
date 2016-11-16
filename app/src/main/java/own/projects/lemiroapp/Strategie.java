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
	//not Int.Max as the evaluation function would create overflows
	static final int MAX = 100000;
    static final int MIN = -100000;
    LinkedList<Zug> movesToEvaluate;

	Strategie(GameBoard field, ProgressUpdater up) {
		this.field = field;
		this.up = up;
        this.movesToEvaluate = new LinkedList<Zug>();
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

	// maximizing player has got to return higher values for better situations
    // minimizing player has got to return lower values the better his situation
    @VisibleForTesting
	int bewertung(Player player, LinkedList<Zug> moves, int depth) {

        if (moves.size() == 0) {
            //worst case: player can not make any moves --> game is lost
            //or player has less than 3 pieces and has no pieces left to set --> game is lost
            if (player.equals(maxPlayer)) {
                //multiply with an number (which is bigger than 1) depending on depth
                //necessary as the evaluation has to be higher if the player can win after fewer moves
                return MIN * (depth + 1);
            }else{
                return MAX * (depth + 1);
            }
        }

		int ret = 0;
        //evaluate how often the players can kill, and prefer kills that are in the near future
        int weight = 512;
        for(int i = 0; i < movesToEvaluate.size(); i++){
            if (movesToEvaluate.get(i).getKill() != null) {
                if(i % 2 == 0) {    //even numbers are moves of the maximizing player
                    ret += weight;
                }else{
                    ret -= weight;
                }
            }
            // next weight will be half the weight
            // this has to be done so players wont do the same move over and over again
            // as they would not choose a path in which they kill but the other player kills in a
            // distant future (which is seen in higher difficulties, when he can make jump moves)
            // thus lowers the evaluation drastically and the game is stalled
            // also this prefers kill in the near future, so they are done now and not later
            // as could be the case if alle were weighted equally
            weight /= 2;
        }

        /*
        if(movesToEvaluate.get(0).getDest().equals(new Position(0,0))){
            for(int i = 0; i < movesToEvaluate.size(); i++) {
                System.out.println(movesToEvaluate.get(i) + "  "  + ret);
            }
            System.out.println();
        }*/

        return ret;

	}

	private int max(int depth, int alpha, int beta, Player player) throws InterruptedException {
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

		//end reached or no more moves available, maybe because he is trapped or because he lost
		if (depth == 0 || moves.size() == 0){
            int bewertung = bewertung(player, moves, depth);
            return bewertung;
		}
		int maxWert = alpha;
		for (Zug z : moves) {
			if(depth == startDepth){
				up.update();
			}
			field.executeCompleteTurn(z, player);
            movesToEvaluate.add(z);
			int wert = min(depth-1, maxWert, beta, player.getOtherPlayer());
            movesToEvaluate.remove(z);
			field.reverseCompleteTurn(z, player);
            if (wert > maxWert) {
				maxWert = wert;
				if (maxWert >= beta) {
                    break;
                }
				if (depth == startDepth) {
                    System.out.println("new move was found: " + z + " wert: " + wert);
                    move = z;
                }
			}
		}
		return maxWert;
	}

	private int min(int depth, int alpha, int beta, Player player) throws InterruptedException {
		LinkedList<Zug> moves = possibleMoves(player);
		if (depth == 0 || moves.size() == 0){
            int bewertung = bewertung(player, moves, depth);
            return bewertung;
		}
		int minWert = beta;
		for (Zug z : moves) {
			field.executeCompleteTurn(z, player);
            movesToEvaluate.add(z);
			int wert = max(depth-1, alpha, minWert, player.getOtherPlayer());
            movesToEvaluate.remove(z);
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

		max(startDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, player);
		
		up.reset();
		
		//restore old startDepth
		startDepth = actualDepth;
		
		return move;
	}

}
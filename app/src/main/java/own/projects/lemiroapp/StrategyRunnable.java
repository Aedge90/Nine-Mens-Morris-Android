package own.projects.lemiroapp;

import java.util.LinkedList;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

public class StrategyRunnable implements Runnable{

	GameBoard field;
	Move resultMove;
    int resultEvaluation;
    Move prevMove;
	int startDepth;
	private ProgressUpdater up;
    private Player maxPlayer;
	//not Int.Max as the evaluation function would create overflows
	static final int MAX = 100000;
    static final int MIN = -100000;
    LinkedList<Move> movesToEvaluate;

    final int threadNr;
    int nThreads;

	StrategyRunnable(ProgressUpdater up, int threadNr, int nThreads) {
		this.up = up;
        this.movesToEvaluate = new LinkedList<Move>();
        this.threadNr = threadNr;
        this.nThreads = nThreads;
	}

	public void updateState(GameBoard field, Player player){
		//copy these so every thread has its own one to avoid concurrency problems
		this.field = field.getCopy();
		this.maxPlayer = new Player(player);
	}

    @Override
    public void run() {
        try {
            computeMove();
        }catch ( InterruptedException e ) {
            Log.d("computeMove Thread " + threadNr, "Interrupted!");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
	
	private void addnonJumpMoves(LinkedList<Move> moves, Player player){
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

	private void addJumpMoves(LinkedList<Move> moves, Player player){
		for (int x = 0; x < field.LENGTH; x++) {
			for (int y = 0; y < field.LENGTH; y++) {
				if (field.getColorAt(x, y).equals(Options.Color.NOTHING)) {
					for (int i = 0; i<field.getPositions(player.getColor()).size(); i++) {
						//no iterator here to avoid concurrent modification exception
						Position p = field.getPositions(player.getColor()).get(i);
						addpossibleKillstoMove(moves, new Move(new Position(x, y), p, null), player);
					}
				}
			}
		}
	}
	
	private void addSetMoves(LinkedList<Move> moves, Player player){
		for (int x = 0; x < field.LENGTH; x++) {
			for (int y = 0; y < field.LENGTH; y++) {
				if (field.getColorAt(x, y).equals(Options.Color.NOTHING)) {
					addpossibleKillstoMove(moves, new Move(new Position(x, y), null, null), player);
				}
			}
		}
	}

    @VisibleForTesting
	void addpossibleKillstoMove(LinkedList<Move> possibleMovessoFar, Move move, Player player){
			boolean inMill = false;
            field.executeSetOrMovePhase(move, player);
			inMill = field.inMill(move.getDest(), player.getColor());
            field.reverseCompleteTurn(move, player);
            //player has a mill after doing this resultMove --> he can kill a piece of the opponent
			if(inMill){
				int added = 0;
				for (Position kill : field.getPositions(player.getOtherPlayer().getColor())) {
					if(!field.inMill(kill, player.getOtherPlayer().getColor())){
						Move killMove = new Move(move.getDest(), move.getSrc(), kill);
						possibleMovessoFar.add(killMove);
						added++;
					}
				}
                //no pieces to kill because all are in a mill --> do it again but now add all pieces
                //as you are allowed to kill if all pieces are part of a mill
				if(added == 0){
					for (Position kill2 : field.getPositions(player.getOtherPlayer().getColor())) {
						Move killMove = new Move(move.getDest(), move.getSrc(), kill2);
						possibleMovessoFar.add(killMove);
					}
				}
			}else{
				possibleMovessoFar.add(move);
			}
	}

	//returns a list of moves that the player is able to do
	LinkedList<Move> possibleMoves(Player player) {
		LinkedList<Move> poss = new LinkedList<Move>();
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
	int bewertung(Player player, LinkedList<Move> moves, int depth) {

        int ret = 0;

        if (moves.size() == 0) {
            if(movesToEvaluate.size() > 1) {
                field.reverseCompleteTurn(movesToEvaluate.getLast(), player.getOtherPlayer());
                //check if the loosing player, prevented a mill in his last resultMove (which is the size-2th resultMove)
                if (field.inMill(movesToEvaluate.get(movesToEvaluate.size() - 2).getDest(), player.getOtherPlayer().getColor())) {
                    //evaluate this better, as it looks stupid if he does not try to prevent one mill even if the other player
                    //can close another mill despite that
                    ret = 1;
                }
                field.executeCompleteTurn(movesToEvaluate.getLast(), player.getOtherPlayer());
            }
            //worst case: player can not make any moves --> game is lost
            //or player has less than 3 pieces and has no pieces left to set --> game is lost
            if (player.equals(maxPlayer)) {
                //multiply with an number (which is >= 1) depending on depth
                //necessary as the evaluation has to be higher if the player can win after fewer moves
                //you may think depth is always 0 here, but it can be higher
                ret = MIN * (depth + 1) + ret;
            }else{
                ret = MAX * (depth + 1) - ret;
            }
            return ret;
        }

        //evaluate how often the players can kill, and prefer kills that are in the near future
        int weight = 2048;
        for(int i = 0; i < movesToEvaluate.size(); i++){
            if (movesToEvaluate.get(i).getKill() != null) {
                if(i % 2 == 0) {    //even numbers are moves of the maximizing player
                    ret += weight;
                }else{
                    ret -= weight;
                }
            }
            // next weight will be half the weight
            // this has to be done so players wont do the same resultMove over and over again
            // as they would not choose a path in which they kill but the other player kills in a
            // distant future (which is seen in higher difficulties, when he can make jump moves)
            // thus lowers the evaluation drastically and the game is stalled
            // also this prefers kill in the near future, so they are done now and not later
            // as could be the case if alle were weighted equally
            weight /= 2;
        }

        //evaluate undoing a resultMove, as its probably of no use. If it is, the other evaluation should overwrite this
        //this should break endless undoing and redoing of moves if all have the same evaluation so far
        if(prevMove != null){
            // closing and opening a mill should not be downgraded. Ignore setting phase
            if (prevMove.getKill() != null || movesToEvaluate.get(0).getKill() != null || prevMove.getSrc() == null){
                //do nothing
            }else if (prevMove.getSrc().equals(movesToEvaluate.get(0).getDest())
                    && prevMove.getDest().equals(movesToEvaluate.get(0).getSrc())) {
                ret -= 1;
            }
        }

        return ret;

	}

	private int max(int depth, int alpha, int beta, Player player) throws InterruptedException {
		if(Thread.interrupted()){
            throw new InterruptedException("Computation of Bot " + player + " was interrupted!");
		}
		LinkedList<Move> moves = possibleMoves(player);
		//end reached or no more moves available, maybe because he is trapped or because he lost
		if (depth == 0 || moves.size() == 0){
            int bewertung = bewertung(player, moves, depth);
            return bewertung;
		}
		int maxWert = alpha;
		for (Move z : moves) {
			field.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
			int wert = min(depth-1, maxWert, beta, player.getOtherPlayer());
            movesToEvaluate.removeLast();
			field.reverseCompleteTurn(z, player);
            if (wert > maxWert) {
				maxWert = wert;
				if (maxWert >= beta) {
                    break;
                }
			}
		}
		return maxWert;
	}

    //same as max, but slightly modified to distribute work among threads
    private void maxKickoff(int depth, int alpha, int beta, Player player, int threadNr) throws InterruptedException{
        LinkedList<Move> moves = possibleMoves(player);
        if(threadNr == 0) {
            up.initialize(moves.size());
        }
        int maxWert = alpha;
        for (int i = 0; i < moves.size(); i++) {
            if(i % nThreads == threadNr) {
                Move z = moves.get(i);
                // update and dont care that its concurrent. loosing an increment would not be noticable
                // and wont break the computation as its just a visualization of the progress
                up.update();
                field.executeCompleteTurn(z, player);
                movesToEvaluate.addLast(z);
                int wert = min(depth - 1, maxWert, beta, player.getOtherPlayer());
                movesToEvaluate.removeLast();
                field.reverseCompleteTurn(z, player);
                if (wert > maxWert) {
                    maxWert = wert;
                    if (maxWert >= beta) {
                        break;
                    }
                    //System.out.println("new resultMove was found: " + z + " wert: " + wert);
                    resultMove = z;
                    resultEvaluation = wert;
                }
            }
        }
    }

	private int min(int depth, int alpha, int beta, Player player) throws InterruptedException {
        if(Thread.interrupted()){
            throw new InterruptedException("Computation of Bot " + player + " was interrupted!");
        }
		LinkedList<Move> moves = possibleMoves(player);
		if (depth == 0 || moves.size() == 0){
            int bewertung = bewertung(player, moves, depth);
            return bewertung;
		}
		int minWert = beta;
		for (Move z : moves) {
			field.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
			int wert = max(depth-1, alpha, minWert, player.getOtherPlayer());
            movesToEvaluate.removeLast();
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

	private void computeMove() throws InterruptedException {
				
		startDepth = maxPlayer.getDifficulty().ordinal() + 2;
		//decrease startDepth if there are too much possible moves to save time
		int actualDepth = startDepth;
		if(startDepth > 4){
			if (field.getPositions(maxPlayer.getColor()).size() <= 3 || field.getPositions(maxPlayer.getOtherPlayer().getColor()).size() <= 3){
				startDepth = 4;
			}
		}

		Log.i("StrategyRunnable " + threadNr, "computeMove started for Player " + maxPlayer.getColor() + " startDepth: " + startDepth);

        prevMove = resultMove;

        resultMove = null;
        resultEvaluation = MIN;

        maxKickoff(startDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, maxPlayer, threadNr);

        if(threadNr == 0) {
            up.reset();
        }

		//restore old startDepth
		startDepth = actualDepth;

	}

    public Move getResultMove(){
        return resultMove;
    }

    public int getResultEvaluation(){
        return resultEvaluation;
    }

}

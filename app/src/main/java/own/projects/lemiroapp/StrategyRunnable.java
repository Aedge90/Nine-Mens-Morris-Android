package own.projects.lemiroapp;

import java.util.LinkedList;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

public class StrategyRunnable implements Runnable{

    //runnable will only work with local copies of the global gameboard and player objects
	private final GameBoard globalGameBoard;
    private GameBoard localGameBoard;
    private final Player globalMaxPlayer;
    private Player localMaxPlayer;

	private int startDepth;
	private final ProgressUpdater up;
	//not Int.Max as the evaluation function would create overflows
	static final int MAX = 100000;
    static final int MIN = -100000;
    private LinkedList<Move> movesToEvaluate;
    private Move prevMove;

    private final int threadNr;
    static int maxWertKickoff;
    static Move resultMove;
    static int resultEvaluation;
    static LinkedList<Move> possibleMovesKickoff;

	StrategyRunnable(final GameBoard gameBoard, final Player maxPlayer, final ProgressUpdater up, final int threadNr) {
        this.globalGameBoard = gameBoard;
        this.globalMaxPlayer = maxPlayer;
		this.up = up;
        this.movesToEvaluate = new LinkedList<Move>();
        this.threadNr = threadNr;
	}

	public void updateState(){
		//copy these so every thread has its own one to avoid concurrency problems
        this.localGameBoard = globalGameBoard.getCopy();
        //copy BOTH!!! the maxPlayer and the other player
		this.localMaxPlayer = new Player(globalMaxPlayer);
        Player other = new Player(globalMaxPlayer.getOtherPlayer());
        other.setOtherPlayer(localMaxPlayer);
        this.localMaxPlayer.setOtherPlayer(other);
	}

    @Override
    public void run() {
        try {
            updateState();
            computeMove();
        }catch ( InterruptedException e ) {
            Log.d("computeMove Thread " + threadNr, "Interrupted!");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
	
	private void addnonJumpMoves(LinkedList<Move> moves, Player player){
		for (Position p : localGameBoard.getPositions(player.getColor())) {
			if (localGameBoard.moveUp(p) != null) {
                addpossibleKillstoMove(moves, localGameBoard.moveUp(p), player);
            }
			if (localGameBoard.moveDown(p) != null) {
                addpossibleKillstoMove(moves, localGameBoard.moveDown(p), player);
            }
			if (localGameBoard.moveRight(p) != null) {
                addpossibleKillstoMove(moves, localGameBoard.moveRight(p), player);
            }
			if (localGameBoard.moveLeft(p) != null) {
                addpossibleKillstoMove(moves, localGameBoard.moveLeft(p), player);
            }
		}
	}

	private void addJumpMoves(LinkedList<Move> moves, Player player){
		for (Position all : localGameBoard.getAllValidPositions()) {
            if (localGameBoard.getColorAt(all).equals(Options.Color.NOTHING)) {
                for (Position own: localGameBoard.getPositions(player.getColor())) {
                    addpossibleKillstoMove(moves, new Move(all, own, null), player);
                }
            }
		}
	}
	
	private void addSetMoves(LinkedList<Move> moves, Player player){
        for (Position all : localGameBoard.getAllValidPositions()) {
            if (localGameBoard.getColorAt(all).equals(Options.Color.NOTHING)) {
                addpossibleKillstoMove(moves, new Move(all, null, null), player);
            }
		}
	}

    @VisibleForTesting
	void addpossibleKillstoMove(LinkedList<Move> possibleMovessoFar, Move move, Player player){
			boolean inMill = false;
            localGameBoard.executeSetOrMovePhase(move, player);
			inMill = localGameBoard.inMill(move.getDest(), player.getColor());
            localGameBoard.reverseCompleteTurn(move, player);
            //player has a mill after doing this move --> he can kill a piece of the opponent
			if(inMill){
				int added = 0;
				for (Position kill : localGameBoard.getPositions(player.getOtherPlayer().getColor())) {
					if(!localGameBoard.inMill(kill, player.getOtherPlayer().getColor())){
						Move killMove = new Move(move.getDest(), move.getSrc(), kill);
                        // using add first is important, so the kill moves will be at the beginning of the list
                        // by that its more likely that the alpha beta algorithms does more cutoffs
						possibleMovessoFar.addFirst(killMove);
						added++;
					}
				}
                //no pieces to kill because all are in a mill --> do it again but now add all pieces
                //as you are allowed to kill if all pieces are part of a mill
				if(added == 0){
					for (Position kill2 : localGameBoard.getPositions(player.getOtherPlayer().getColor())) {
						Move killMove = new Move(move.getDest(), move.getSrc(), kill2);
						possibleMovessoFar.addFirst(killMove);
					}
				}
			}else{
				possibleMovessoFar.add(move);
			}
	}

	//returns a list of moves that the player is able to do
	LinkedList<Move> possibleMoves(Player player) {
		LinkedList<Move> poss = new LinkedList<Move>();
		int nPositions = localGameBoard.getPositions(player.getColor()).size();
        //do not compute possible moves if the player has lost, otherwise it breaks the evaluation
        //as a state AFTER loosing would be evaluated instead of the final state after the final kill
        if(nPositions < 3 && player.getSetCount() <= 0){
            return poss;
        }
		if(player.getSetCount() > 0){
			addSetMoves(poss, player);
		}else{
			boolean jump = false;
			if (nPositions <= 3){
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
	int evaluation(Player player, LinkedList<Move> moves, int depth) {

        int ret = 0;

        if (moves.size() == 0) {
            if(movesToEvaluate.size() > 1) {
                localGameBoard.reverseCompleteTurn(movesToEvaluate.getLast(), player.getOtherPlayer());
                //check if the loosing player, prevented a mill in his last move (which is the size-2th move)
                if (localGameBoard.inMill(movesToEvaluate.get(movesToEvaluate.size() - 2).getDest(), player.getOtherPlayer().getColor())) {
                    //evaluate this better, as it looks stupid if he does not try to prevent one mill even if the other player
                    //can close another mill despite that
                    ret = 1;
                }
                localGameBoard.executeCompleteTurn(movesToEvaluate.getLast(), player.getOtherPlayer());
            }
            //worst case: player can not make any moves --> game is lost
            //or player has less than 3 pieces and has no pieces left to set --> game is lost
            if (player.equals(localMaxPlayer)) {
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
        int i = 0;
        for(Move move : movesToEvaluate){
            if(i % 2 == 0) {    //even numbers are moves of the maximizing player
                ret += evaluateMove(move, weight);
            }else{
                ret -= evaluateMove(move, weight);
            }
            // next weight will be half the weight
            // this has to be done so players wont do the same move over and over again
            // as they would not choose a path in which they kill but the other player kills in a
            // distant future (which is seen in higher difficulties, when he can make jump moves)
            // thus lowers the evaluation drastically and the game is stalled
            // also this prefers kills in the near future, so they are done now and not later
            // as could be the case if all were weighted equally
            weight /= 2;
            i++;
        }

        //evaluate undoing a move, as its probably of no use. If it is, the other evaluation should overwrite this
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

    private int evaluateMove(Move move, int weight){
        if (move.getKill() != null) {
            return weight;
        }else{
            return 0;
        }
    }

	private int max(int depth, int alpha, int beta, Player player) throws InterruptedException {
		if(Thread.interrupted()){
            throw new InterruptedException("Computation of Bot " + player + " was interrupted!");
		}
		LinkedList<Move> moves = possibleMoves(player);
		//end reached or no more moves available, maybe because he is trapped or because he lost
		if (depth == 0 || moves.size() == 0){
            int bewertung = evaluation(player, moves, depth);
            return bewertung;
		}
		int maxWert = alpha;
		for (Move z : moves) {
			localGameBoard.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
			int wert = min(depth-1, maxWert, beta, player.getOtherPlayer());
            movesToEvaluate.removeLast();
			localGameBoard.reverseCompleteTurn(z, player);
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
    private void maxKickoff(int depth, Player player) throws InterruptedException{

        while(true) {

            Move z;
            synchronized (StrategyRunnable.class) {
                if(possibleMovesKickoff.size() > 0) {
                    z = possibleMovesKickoff.removeFirst();
                }else{
                    break;
                }
            }

            localGameBoard.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
            int wert = min(depth - 1, maxWertKickoff, Integer.MAX_VALUE, player.getOtherPlayer());
            movesToEvaluate.removeLast();
            localGameBoard.reverseCompleteTurn(z, player);

            synchronized (StrategyRunnable.class) {
                if (wert > maxWertKickoff) {
                    maxWertKickoff = wert;
                    resultMove = z;
                    resultEvaluation = wert;
                }
            }

            up.increment();
        }
    }

	private int min(int depth, int alpha, int beta, Player player) throws InterruptedException {
        if(Thread.interrupted()){
            throw new InterruptedException("Computation of Bot " + player + " was interrupted!");
        }
		LinkedList<Move> moves = possibleMoves(player);
		if (depth == 0 || moves.size() == 0){
            int bewertung = evaluation(player, moves, depth);
            return bewertung;
		}
		int minWert = beta;
		for (Move z : moves) {
			localGameBoard.executeCompleteTurn(z, player);
            movesToEvaluate.addLast(z);
			int wert = max(depth-1, alpha, minWert, player.getOtherPlayer());
            movesToEvaluate.removeLast();
			localGameBoard.reverseCompleteTurn(z, player);
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
				
		startDepth = localMaxPlayer.getDifficulty().ordinal() + 2;
		//decrease startDepth if there are too much possible moves to save time
		int actualDepth = startDepth;
		if(startDepth > 4){
			if (localGameBoard.getPositions(localMaxPlayer.getColor()).size() <= 3
                    || localGameBoard.getPositions(localMaxPlayer.getOtherPlayer().getColor()).size() <= 3){
				startDepth = 4;
			}
		}

        maxKickoff(startDepth, localMaxPlayer);

		//restore old startDepth
		startDepth = actualDepth;

	}

    public void setPreviousMove(Move prevMove) {
       this.prevMove = prevMove;
    }

}

package own.projects.lemiroapp;

import java.util.LinkedList;

public class Strategy {

    private int nThreads;
    private Thread[] threads;
    private StrategyRunnable[] runnables;

    GameBoard gameBoard;
    private Player maxPlayer;

    Strategy(GameBoard field, Player player, ProgressUpdater up) {
        this.gameBoard = field;
        this.maxPlayer = player;
        this.nThreads = 4; //TODO decide number
        this.threads = new Thread[nThreads];
        this.runnables = new StrategyRunnable[nThreads];
        for (int i = 0; i < nThreads; i++){
            runnables[i] = new StrategyRunnable(gameBoard, maxPlayer, up, i, nThreads);
        }
    }

    private LinkedList<Move> waitForandGetResult () throws InterruptedException{
        LinkedList<Move> resultingMoves = new LinkedList<Move>();
        int maxEvaluation = Integer.MIN_VALUE;
        for (int i = 0; i < nThreads; i++){
            threads[i].join();
            if(runnables[i].getResultEvaluation() > maxEvaluation){
                maxEvaluation = runnables[i].getResultEvaluation();
                resultingMoves = new LinkedList<Move>();
                resultingMoves.add(runnables[i].getResultMove());
            }else if (runnables[i].getResultEvaluation() == maxEvaluation){
                maxEvaluation = runnables[i].getResultEvaluation();
                //store moves with equal evaluation
                resultingMoves.add(runnables[i].getResultMove());
            }
        }
        return resultingMoves;
    }

    public Move computeMove() throws InterruptedException {
        for (int i = 0; i < nThreads; i++){
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }
        LinkedList<Move> resultingMoves = waitForandGetResult();
        // TODO choose a random move,
        // TODO for that StrategyRunnable also needs adjustment, so that it stores a list rather than one result move
        Move result = resultingMoves.get(0);
        for (int i = 0; i < nThreads; i++) {
            //runnables need to know which move was chosen
            runnables[i].setPreviousMove(result);
        }
        return result;
    }
}

package own.projects.lemiroapp;

import java.util.LinkedList;

public class Strategy {

    private int nThreads;
    private Thread[] threads;
    private StrategyRunnable[] runnables;

    GameBoard field;
    private Player maxPlayer;

    Strategy(GameBoard field, Player player, ProgressUpdater up) {
        this.field = field;
        this.maxPlayer = player;
        this.nThreads = 4; //TODO decide number
        this.threads = new Thread[nThreads];
        this.runnables = new StrategyRunnable[nThreads];
        for (int i = 0; i < nThreads; i++){
            runnables[i] = new StrategyRunnable(up, i, nThreads);
            threads[i] = new Thread(runnables[i]);
        }
    }

    private LinkedList<Move> waitForandGetResult () throws InterruptedException{
        LinkedList<Move> resultingMoves = new LinkedList<Move>();
        int maxEvaluation = StrategyRunnable.MIN;
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
            runnables[i].updateState(field, maxPlayer);
            threads[i].start();
        }
        LinkedList<Move> resultingMoves = waitForandGetResult();
        //TODO choose a random move
        return resultingMoves.get(0);
    }
}

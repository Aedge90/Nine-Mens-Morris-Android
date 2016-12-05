package own.projects.lemiroapp;

import java.util.LinkedList;

public class Strategy {

    int nThreads;
    Thread[] threads;
    StrategyRunnable[] runnables;

    Strategy(GameBoard field, Player player, ProgressUpdater up) {
        this.nThreads = 4; //TODO decide number
        this.threads = new Thread[nThreads];
        for (int i = 0; i < nThreads; i++){
            runnables[i] = new StrategyRunnable(field, player, up, i, nThreads);
            threads[i] = new Thread(runnables[i]);
        }
    }

    private void startThreads(){
        for (int i = 0; i < nThreads; i++){
            threads[i].start();
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
        startThreads();
        LinkedList<Move> resultingMoves = waitForandGetResult();
        //TODO choose a random move
        return resultingMoves.get(0);
    }
}

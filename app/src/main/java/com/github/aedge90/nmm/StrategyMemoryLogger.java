package com.github.aedge90.nmm;

public class StrategyMemoryLogger extends StrategyMemory {

    //just for debugging
    int nExecutedEval = 0;
    int nSkippedEval = 0;
    int nExecutedPossMoveCalc = 0;
    int nSkippedPossMoveCalc = 0;

    @Override
    public void addSkippedEvaluation() {
        nSkippedEval++;
    }

    @Override
    public void addExecutedEvaluation() {
        nExecutedEval++;
    }

    @Override
    public void addSkippedPossMoveCalculation() {
        nSkippedPossMoveCalc++;
    }


    @Override
    public void addExecutedPossMoveCalculation() {
        nExecutedPossMoveCalc++;
    }

    public void resetLog () {
        nSkippedEval = 0;
        nExecutedEval = 0;
        nExecutedPossMoveCalc = 0;
        nSkippedPossMoveCalc = 0;
    }


}

package com.github.aedge90.nmm;

public class StrategyMemoryLogger extends StrategyMemory {

    @Override
    public void logSkipped () {
        nSkippedEval++;
        nTotalEval++;
    }

    @Override
    public void logTotal () {
        nTotalEval++;
    }

    public void resetLog () {
        nSkippedEval = 0;
        nTotalEval = 0;
    }
}

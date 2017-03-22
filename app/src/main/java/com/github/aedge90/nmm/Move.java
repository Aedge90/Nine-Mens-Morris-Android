package com.github.aedge90.nmm;

public class Move {

    private final Position src;
    private final Position dest; //if only dest is set, then this is a move in the set phase
    private final Position kill;

    private double evaluation;
    
    Move(Position dest, Position src, Position kill){
        this.evaluation = 0;
        this.src = src;
        this.dest = dest;
        this.kill = kill;
        if (!isPossible()){
            throw new IllegalArgumentException("Move: invalid arguments for constructor: " + toString() );
        }
    }

    Move(Move other){
        this.evaluation = other.evaluation;
        this.src = other.src;
        this.dest = other.dest;
        this.kill = other.kill;
    }
    
    public Position getSrc(){
        return src;
    }
    
    public Position getDest(){
        return dest;
    }
    
    public Position getKill() {
        return kill;
    }

    public double getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(double evaluation) {
        this.evaluation = evaluation;
    }

    @Override
    public String toString(){
        return "src: " + src + " dest: " + dest + " kill: " + kill;
    }
    
    private boolean isPossible(){
        if(src == null && dest == null && kill == null){
            return false;
        }
        if(src == null && dest == null && kill != null){
            return false;
        }
        if(src != null && dest == null){
            return false;
        }
        if(src != null && src.equals(dest)){
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (src != null ? !src.equals(move.src) : move.src != null) return false;
        if (dest != null ? !dest.equals(move.dest) : move.dest != null) return false;
        return kill != null ? kill.equals(move.kill) : move.kill == null;

    }

    @Override
    public int hashCode() {
        int result = src != null ? src.hashCode() : 0;
        result = 31 * result + (dest != null ? dest.hashCode() : 0);
        result = 31 * result + (kill != null ? kill.hashCode() : 0);
        return result;
    }
}

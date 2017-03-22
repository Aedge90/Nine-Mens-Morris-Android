package com.github.aedge90.nmm;

import java.io.Serializable;

/**
 * Representation of a Player
 */

public class Player implements Serializable {

    //the number of times this player can freely place a piece anywhere on the field
    //at the beginning of a game
    private int setCount;

    private Move prevMove = null;

    //the color of this player (black or white)
    private final Options.Color color;

    //the difficulty of this player, may be null if its a human
    private Options.Difficulties difficulty = null;

    //reference to the other player
    private Player otherPlayer;

    public Player (Options.Color color) {
        this.color = color;
    }

    //copy constructor. does not deepcopy the other player !!!!!
    public Player (Player other){
        this.setCount = other.setCount;
        if(other.getPrevMove() != null) {
            this.prevMove = new Move(other.prevMove);
        }
        this.color = other.color;
        this.difficulty = other.difficulty;
        this.otherPlayer = other.otherPlayer;
    }

    public void setSetCount (int setCount) {
        if(setCount < 0){
            throw new IllegalArgumentException("setSetCount: setCount of player: " + getColor() + " may not be set below 0");
        }
        this.setCount = setCount;
    }

    public void setPrevMove(Move prevMove) {
        this.prevMove = prevMove;
    }

    public void setOtherPlayer (Player otherPlayer) {
        if(this == otherPlayer){
            throw new IllegalArgumentException("setOtherPlayer: other player may not reference the same player");
        }
        if(this.getColor().equals(otherPlayer.getColor())){
            throw new IllegalArgumentException("setOtherPlayer: other player may not have the same color");
        }
        this.otherPlayer = otherPlayer;
    }

    public void setDifficulty (Options.Difficulties difficulty) {
        this.difficulty = difficulty;
    }

    public int getSetCount () {
        return this.setCount;
    }

    public Move getPrevMove() {
        return prevMove;
    }

    public Player getOtherPlayer () {
        return this.otherPlayer;
    }

    public Options.Color getColor (){
        return color;
    }

    public Options.Difficulties getDifficulty () {
        return this.difficulty;
    }



    @Override
    public String toString(){
        return "Player " + color + " on " + difficulty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return color == player.color;

    }

    @Override
    public int hashCode() {
        return color.hashCode();
    }
}

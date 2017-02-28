package com.github.aedge90.nmm;

import java.io.Serializable;

/**
 * Representation of a Player
 */

public class Player implements Serializable {

    //the number of times this player can freely place a piece anywhere on the field
    //at the beginning of a game
    private int setCount;

    //the color of this player (black or white)
    private final Options.Color color;

    //the difficulty of this player, may be null if its a human
    private Options.Difficulties difficulty = null;

    //reference to the other player
    private Player otherPlayer;

    public Player (Options.Color color) {
        this.color = color;
    }

    //copy constructor
    public Player (Player other){
        this.setCount = other.setCount;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (setCount != player.setCount) return false;
        if (color != player.color) return false;
        if (difficulty != player.difficulty) return false;
        return otherPlayer != null ? otherPlayer.equals(player.otherPlayer) : player.otherPlayer == null;

    }

    @Override
    public int hashCode() {
        int result = setCount;
        result = 31 * result + color.hashCode();
        result = 31 * result + (difficulty != null ? difficulty.hashCode() : 0);
        result = 31 * result + (otherPlayer != null ? otherPlayer.hashCode() : 0);
        return result;
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

}

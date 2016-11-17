package own.projects.lemiroapp;

import android.graphics.Path;

/**
 * Representation of a Player
 */

public class Player {

    //the number of times this player can freely place a piece anywhere on the field
    //at the beginning of a game
    private int setCount;

    //the color of this player (black or white)
    private Options.Color color;

    //the difficulty of this player, may be null if it a human
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

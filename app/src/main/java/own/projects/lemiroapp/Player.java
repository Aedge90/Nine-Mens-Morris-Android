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

    public void setSetCount (int setCount) {
        this.setCount = setCount;
    }

    public void setOtherPlayer (Player otherPlayer) {
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

}

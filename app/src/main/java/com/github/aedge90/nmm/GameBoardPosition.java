package com.github.aedge90.nmm;


public class GameBoardPosition extends Position {

    private GameBoardPosition left;
    private GameBoardPosition right;
    private GameBoardPosition up;
    private GameBoardPosition down;
    private Options.Color color;

    GameBoardPosition(int x, int y) {
        super(x, y);
    }

    public GameBoardPosition getLeft() {
        return left;
    }

    public GameBoardPosition getRight() {
        return right;
    }

    public GameBoardPosition getUp() {
        return up;
    }

    public GameBoardPosition getDown() {
        return down;
    }

    public GameBoardPosition[] getNeighbors () {
        return new GameBoardPosition[]{left,right,up,down};
    }

    public GameBoardPosition getOpposite(GameBoardPosition pos){
        if(pos.equals(left)){
            return right;
        }else if(pos.equals(right)){
            return left;
        }else if(pos.equals(up)){
            return down;
        }else if(pos.equals(down)){
            return up;
        }
        return null;
    }

    public void setColor(Options.Color color) {
        this.color = color;
    }

    public void connectRight(GameBoardPosition right) {
        this.right = right;
        right.left = this;
    }

    public void connectDown(GameBoardPosition down) {
        this.down = down;
        down.up = this;
    }

    public Options.Color getColor() {
        return this.color;
    }

}

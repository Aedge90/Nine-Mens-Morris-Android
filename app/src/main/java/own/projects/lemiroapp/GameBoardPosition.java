package own.projects.lemiroapp;


public class GameBoardPosition extends Position {

    private GameBoardPosition left;
    private GameBoardPosition right;
    private GameBoardPosition up;
    private GameBoardPosition down;

    GameBoardPosition(int x, int y) {
        super(x, y);
    }

    GameBoardPosition(Position pos) {
        super(pos);
    }

    public void setLeft(GameBoardPosition left) {
        this.left = left;
    }

    public void setRight(GameBoardPosition right) {
        this.right = right;
    }

    public void setUp(GameBoardPosition up) {
        this.up = up;
    }

    public void setDown(GameBoardPosition down) {
        this.down = down;
    }
}

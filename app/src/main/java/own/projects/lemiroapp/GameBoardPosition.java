package own.projects.lemiroapp;


public class GameBoardPosition extends Position {

    private GameBoardPosition left;
    private GameBoardPosition right;
    private GameBoardPosition up;
    private GameBoardPosition down;
    private Options.Color color;

    GameBoardPosition(int x, int y) {
        super(x, y);
    }

    GameBoardPosition(Position pos) {
        super(pos);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GameBoardPosition that = (GameBoardPosition) o;

        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (right != null ? !right.equals(that.right) : that.right != null) return false;
        if (up != null ? !up.equals(that.up) : that.up != null) return false;
        return down != null ? down.equals(that.down) : that.down == null;

    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        result = 31 * result + (up != null ? up.hashCode() : 0);
        result = 31 * result + (down != null ? down.hashCode() : 0);
        return result;
    }

}

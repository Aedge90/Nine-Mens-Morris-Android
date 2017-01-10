package own.projects.lemiroapp;

public class Position {

    private final int x;
    private final int y;
    
    Position(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    Position(Position pos){
        this.x = pos.getX();
        this.y = pos.getY();
    }
    
    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    }
    
    @Override
    public String toString(){
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object pos){
        if (pos == null) {
            return false;
        }
        if (!Position.class.isAssignableFrom(pos.getClass())) {
            return false;
        }
        final Position other = (Position) pos;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }
}

package own.projects.lemiroapp;

public class Zug {

	private Position src;
	private Position dest; //if only dest is set, then this is a move in the set phase
	private Position kill;
	
	Zug(Position dest, Position src, Position kill){
		this.src = src;
		this.dest = dest;
		this.kill = kill;
        if (!isPossible()){
            throw new IllegalArgumentException("Zug: invalid arguments for constructor: " + toString() );
        }
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

        Zug zug = (Zug) o;

        if (src != null ? !src.equals(zug.src) : zug.src != null) return false;
        if (dest != null ? !dest.equals(zug.dest) : zug.dest != null) return false;
        return kill != null ? kill.equals(zug.kill) : zug.kill == null;

    }

    @Override
    public int hashCode() {
        int result = src != null ? src.hashCode() : 0;
        result = 31 * result + (dest != null ? dest.hashCode() : 0);
        result = 31 * result + (kill != null ? kill.hashCode() : 0);
        return result;
    }
}

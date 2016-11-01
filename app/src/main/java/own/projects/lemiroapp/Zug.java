package own.projects.lemiroapp;

public class Zug {

	private Position set; //used during Set Phase
	private Position src;
	private Position dest;
	private Position kill;
	
	Zug(Position dest, Position src, Position set, Position kill){
		this.src = src;
		this.dest = dest;
		this.kill = kill;
		this.set = set;
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
	
	public Position getSet(){
		return set;
	}
	
	@Override
	public String toString(){
		return "src: " + src + " dest: " + dest + " kill: " + kill + " set: " + set;
	}

	public boolean isValid(){
		if(src != null || dest != null || kill != null || set != null){
			return true;
		}else{
			return false;
		}
	}
	
	private boolean isPossible(){
		if(set != null && (dest != null || src != null)){
			return false;
		}
        if(src == null && dest == null && kill != null && set == null){
            return false;
        }
        if(src != null && dest == null){
            return false;
        }
        if(src == null && dest !=null){
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

        if (set != null ? !set.equals(zug.set) : zug.set != null) return false;
        if (src != null ? !src.equals(zug.src) : zug.src != null) return false;
        if (dest != null ? !dest.equals(zug.dest) : zug.dest != null) return false;
        return kill != null ? kill.equals(zug.kill) : zug.kill == null;

    }

    @Override
    public int hashCode() {
        int result = set != null ? set.hashCode() : 0;
        result = 31 * result + (src != null ? src.hashCode() : 0);
        result = 31 * result + (dest != null ? dest.hashCode() : 0);
        result = 31 * result + (kill != null ? kill.hashCode() : 0);
        return result;
    }
}

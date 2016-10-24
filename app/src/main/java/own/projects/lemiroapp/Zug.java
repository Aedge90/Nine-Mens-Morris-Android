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
	
	void changeTo (Zug m){
		this.dest = m.getDest();
		this.src = m.getSrc();
		this.kill = m.getKill();
	}

	public void setSrc(Position src){
		this.src = src;
		checkState();
	}
	
	public void setDest(Position dest){
		this.dest = dest;
		checkState();
	}
	
	public void setKill(Position kill) {
		this.kill = kill;
		checkState();
	}

	public void setSet(Position set) {
		this.set = set;
		checkState();
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
	
	//only for asserting that there are no set and move moves mixed up
	private void checkState(){
		if(set != null && (dest != null || src != null)){
			throw new IllegalStateException("Illegal State in Zug!" + this);
		}
	}
	
}

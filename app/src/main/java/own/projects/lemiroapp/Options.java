package own.projects.lemiroapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Options implements Parcelable {

	protected GameMode gameMode;
	protected static enum GameMode {
		HUMANBOT, BOTBOT, HUMANHUMAN //and vllt HUMANVIABTOOTH
	}
	protected MillMode millMode;
	protected static enum MillMode {
		MILL5, MILL7, MILL9
	};
	protected Difficulties difficulty1;
	protected  Difficulties difficulty2;
	protected static enum Difficulties {
		EASY, NORMAL, HARD, HARDER, HARDEST
	};
	protected Color whoStarts;
	protected Color colorPlayer1;
	protected Color colorPlayer2;
	protected static enum Color {
		WHITE, BLACK, RED, GREEN, NOTHING, INVALID
	};
	
	public Options() {

		this.gameMode = null;
		this.millMode = null;
		this.difficulty1 = null;
		this.difficulty2 = null;
		this.whoStarts = null;
		this.colorPlayer1 = null;
		this.colorPlayer2 = null;
		
	}
	
	public Options(Parcel in) {
		Options o = in.readParcelable(null);
		this.gameMode = o.gameMode;
		this.millMode = o.millMode;
		this.difficulty1 = o.difficulty1;
		this.difficulty2 = o.difficulty2;
		this.whoStarts = o.whoStarts;
		this.colorPlayer1 = o.colorPlayer1;
		this.colorPlayer2 = o.colorPlayer2;
	}
	
	@Override
	public String toString(){
		return "gameMode = " + gameMode + "millMode = " + millMode + "difficulty1 = " + difficulty1 + "difficulty2 = "
				+ difficulty2 + "whoStarts = " + whoStarts + "color = " + colorPlayer1 + "otherColor = " +colorPlayer2;
	}
	
	/* everything below here is for implementing Parcelable */

    // 99.9% of the time you can just ignore this
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
	public void writeToParcel(Parcel dest, int flags) {
    	dest.writeValue(gameMode);
    	dest.writeValue(millMode);
    	dest.writeValue(difficulty1);
    	dest.writeValue(difficulty2);
    	dest.writeValue(whoStarts);
    	dest.writeValue(colorPlayer1);
    	dest.writeValue(colorPlayer2);
	}

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Options> CREATOR = new Parcelable.Creator<Options>() {
        public Options createFromParcel(Parcel source) {
        	final Options o = new Options();
            o.gameMode = (GameMode) source.readValue(Options.class.getClassLoader());
            o.millMode = (MillMode) source.readValue(Options.class.getClassLoader());
            o.difficulty1 = (Difficulties) source.readValue(Options.class.getClassLoader());
            o.difficulty2 = (Difficulties) source.readValue(Options.class.getClassLoader());
            o.whoStarts = (Color) source.readValue(Options.class.getClassLoader());
            o.colorPlayer1 = (Color) source.readValue(Options.class.getClassLoader());
            o.colorPlayer2 = (Color) source.readValue(Options.class.getClassLoader());
            return o;
        }

        public Options[] newArray(int size) {
        	throw new UnsupportedOperationException();
        }
    };
    
}

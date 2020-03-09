import java.io.Serializable.*;

public class Participant implements Serializable {

	private static final long serialVersionUID = 1L;
	
	int index;
	String name;
	
	public Participant(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	publis int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
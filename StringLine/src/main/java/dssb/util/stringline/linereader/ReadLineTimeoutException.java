package dssb.util.stringline.linereader;

public class ReadLineTimeoutException extends LineReaderException {
	
	/** */
	private static final long serialVersionUID = -6152198069546709271L;
	
	private final String part;
	
	public ReadLineTimeoutException(String part) {
		super(null, null);
		this.part = part;
	}
	
	public String getPart() {
		return part;
	}
	
}

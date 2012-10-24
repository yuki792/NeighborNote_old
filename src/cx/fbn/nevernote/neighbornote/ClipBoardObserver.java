package cx.fbn.nevernote.neighbornote;

public class ClipBoardObserver {
	private String SourceGuid;
	
	public ClipBoardObserver(){
		SourceGuid = new String();
	}
	
	public void setCopySourceGuid(String guid){
		SourceGuid = guid;
	}
	
	public String getSourceGuid(){
		return SourceGuid;
	}

}

package cx.fbn.nevernote.neighbornote;

import com.trolltech.qt.gui.QApplication;

public class ClipBoardObserver {
	private String SourceGuid;
	private boolean internalFlg;
	
	public ClipBoardObserver(){
		SourceGuid = new String("");
		internalFlg = false;
		QApplication.clipboard().dataChanged.connect(this, "clipboardDataChanged()");
	}
	
	public void setCopySourceGuid(String guid){
		SourceGuid = guid;
		internalFlg = true;
	}
	
	public String getSourceGuid(){
		if(SourceGuid == ""){
			return null;
		}
		return SourceGuid;
	}
	
	@SuppressWarnings("unused")
	private void clipboardDataChanged(){
		if(!internalFlg){
			SourceGuid = "";
		}
		internalFlg = false;
	}
}

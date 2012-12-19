// ICHANGED
package cx.fbn.nevernote.neighbornote;

import com.trolltech.qt.gui.QWidget;

public class CompositeRensoElementItem extends QWidget {
	private final String noteGuid;
	private final String noteTitle;
	
	public CompositeRensoElementItem(String noteGuid, String noteTitle) {
		this.noteGuid = new String(noteGuid);
		this.noteTitle = new String(noteTitle);
	}

	public String getNoteGuid() {
		return noteGuid;
	}

	public String getNoteTitle() {
		return noteTitle;
	}
}

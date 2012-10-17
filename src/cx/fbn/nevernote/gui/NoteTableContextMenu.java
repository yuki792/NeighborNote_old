// ICHANGED
package cx.fbn.nevernote.gui;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMouseEvent;

public class NoteTableContextMenu extends QMenu {
	private final TableView parent;
	
	public NoteTableContextMenu(TableView tableView) {
		this.parent = tableView;
	}

	@Override
	protected void mousePressEvent(QMouseEvent event){
		super.mousePressEvent(event);
		
		int x = event.x();
		int y = event.y();
		
		if(x < 0 || this.width() < x){
			parent.restoreCurrentNoteGuid();
		}else if(y < 0 || this.height() < y){
			parent.restoreCurrentNoteGuid();
		}
	}
	
	@Override
	protected void keyPressEvent(QKeyEvent event){
		super.keyPressEvent(event);
		
		if(event.key() == Qt.Key.Key_Escape.value()){
			parent.restoreCurrentNoteGuid();
		}
	}
}

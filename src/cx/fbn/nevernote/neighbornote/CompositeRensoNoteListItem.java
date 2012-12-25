// ICHANGED
package cx.fbn.nevernote.neighbornote;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPalette;

import cx.fbn.nevernote.sql.DatabaseConnection;

public class CompositeRensoNoteListItem extends RensoNoteListItem {
	private boolean selected;
	
	public CompositeRensoNoteListItem(Note note, int relationPoints, boolean isStared, DatabaseConnection c) {
		super(note, relationPoints, isStared, c);
		selected = false;
	}
	
	@Override
	protected void enterEvent(QEvent e){
		if (!selected) {
			QPalette p = new QPalette();
			p.setColor(QPalette.ColorRole.Window, new QColor(225, 235, 255));
			this.setPalette(p);
		}
	}

	@Override
	protected void leaveEvent(QEvent e){
		if (!selected) {
			setDefaultBackground();
		}
	}
	
	@Override
	protected void mousePressEvent(QMouseEvent e) {
		// TODO 左ボタンの時だけー
		super.mousePressEvent(e);
		test();
	}
	
	@Override
	protected void mouseDoubleClickEvent(QMouseEvent e) {
		// もしかして、右ダブルクリックにも反応するのか？？
		System.out.println("だぶるくりっくー");
		
		super.mouseDoubleClickEvent(e);
		test();
	}
	
	private void test() {
		selected = !selected;
		
		if (selected) {
			QPalette p = new QPalette();
			p.setColor(QPalette.ColorRole.Window, new QColor(165, 175, 255));
			this.setPalette(p);
		} else {
			setDefaultBackground();
		}
	}
}

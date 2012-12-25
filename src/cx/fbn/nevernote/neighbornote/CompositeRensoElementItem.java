// ICHANGED
package cx.fbn.nevernote.neighbornote;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QWidget;

public class CompositeRensoElementItem extends QWidget {
	private final String noteGuid;
	private final String noteTitle;
	// private boolean selected;
	
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
	
	/*
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
		// 左クリックの時だけ
		if (e.button() == Qt.MouseButton.LeftButton) {
			super.mousePressEvent(e);
			itemSelectionChanged();
		}
	}
	
	@Override
	protected void mouseDoubleClickEvent(QMouseEvent e) {
		// 左ダブルクリックの時だけ
		if (e.button() == Qt.MouseButton.LeftButton) {
			super.mouseDoubleClickEvent(e);
			itemSelectionChanged();
		}
	}
	
	private void itemSelectionChanged() {
		selected = !selected;
		
		if (selected) {
			QPalette p = new QPalette();
			p.setColor(QPalette.ColorRole.Window, new QColor(165, 175, 255));
			this.setPalette(p);
		} else {
			setDefaultBackground();
		}
	}
	
	private void setDefaultBackground() {
		QPalette p = new QPalette();
		p.setColor(QPalette.ColorRole.Window, new QColor(255, 255, 255));
		this.setPalette(p);
	}
	*/
	
	@Override
	protected void paintEvent(QPaintEvent event){
		QPainter painter = new QPainter(this);

		// 枠線
		painter.setPen(QColor.lightGray);
		painter.drawLine(0, rect().height() - 1, rect().width() - 1, rect().height() - 1);
		
		// 項目の中身
		painter.setPen(QColor.black);
		painter.drawText(0, 0, size().width(), size().height(), Qt.AlignmentFlag.AlignLeft.value(), noteTitle);
		
		painter.end();
	}
}

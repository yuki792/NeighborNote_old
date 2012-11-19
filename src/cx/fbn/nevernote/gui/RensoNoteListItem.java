// ICHANGED
package cx.fbn.nevernote.gui;

import java.text.SimpleDateFormat;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class RensoNoteListItem extends QWidget{
	private final DatabaseConnection conn;
	String noteGuid;
	String noteTitle;
	int relationPoints;
	String noteUpdated;
	String tagNames;
	
	public RensoNoteListItem(Note note, int relationPoints, DatabaseConnection c){
		
		this.conn = c;
		this.noteGuid = new String(note.getGuid());
		
		this.noteTitle = new String(note.getTitle());
		this.relationPoints = relationPoints;
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.noteUpdated = new StringBuilder(simple.format(note.getUpdated())).toString();
		
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < note.getTagNames().size(); i++) {
			sb.append(note.getTagNames().get(i));
			if(i + 1 < note.getTagNames().size()){
				sb.append(Global.tagDelimeter + " ");
			}
		}

		this.tagNames = new String(sb);
		
		QPalette p = new QPalette();
		p.setColor(QPalette.ColorRole.Window, new QColor(255, 255, 255));
		this.setPalette(p);
		this.setAutoFillBackground(true);
		this.setBackgroundRole(QPalette.ColorRole.Window);
	}
	
	@Override
	protected void paintEvent(QPaintEvent event){
		QPainter painter = new QPainter(this);

		// 枠線
		painter.setPen(QColor.lightGray);
		painter.drawLine(0, rect().height() - 1, rect().width() - 1, rect().height() - 1);
		
		// 項目名
		// painter.setPen(QColor.blue);
		// painter.setFont(new QFont("Arial", 8));
		// painter.drawText(3, 3, 50, 15, Qt.AlignmentFlag.AlignRight.value(), tr("Title:"));
		// painter.drawText(3, 23, 50, 15, Qt.AlignmentFlag.AlignRight.value(), tr("Relation:"));
		// painter.drawText(3, 43, 50, 15, Qt.AlignmentFlag.AlignRight.value(), tr("Updated:"));
		// painter.drawText(3, 63, 50, 15, Qt.AlignmentFlag.AlignRight.value(), tr("Tags:"));
		
		// 項目の中身
		painter.setPen(QColor.black);
		QFont titleFont = new QFont("Arial", 10);
		titleFont.setBold(true);
		QFont normalFont = new QFont("Arial", 10);
		painter.setFont(titleFont);
		painter.drawText(85, 3, size().width() - 55, 20, Qt.AlignmentFlag.AlignLeft.value(), noteTitle);
		painter.setFont(normalFont);
		painter.drawText(85, 23, size().width() - 55, 20, Qt.AlignmentFlag.AlignLeft.value(), String.valueOf(relationPoints) + tr(" points"));
		painter.drawText(85, 43, size().width() - 55, 20, Qt.AlignmentFlag.AlignLeft.value(), noteUpdated);
		painter.drawText(85, 63, size().width() - 55, 20, Qt.AlignmentFlag.AlignLeft.value(), tagNames);
		
		// サムネイル
		QImage img;
		String thumbnailName = Global.getFileManager().getResDirPath("thumbnail-" + noteGuid + ".png");
		QFile thumbnail = new QFile(thumbnailName);
		if (!thumbnail.exists()) {
			img = new QImage();
			img.loadFromData(conn.getNoteTable().getThumbnail(noteGuid));
		} else {
			img = new QImage(thumbnailName);
		}
		painter.drawImage(2, 2, img, 0, 0, 80, rect().height() - 6);
		painter.setPen(QColor.lightGray);
		painter.drawRect(2, 2, 80, rect().height() - 6);
		
		painter.end();
	}
	
	@Override
	protected void enterEvent(QEvent e){
		QPalette p = new QPalette();
		p.setColor(QPalette.ColorRole.Window, new QColor(225, 235, 255));
		this.setPalette(p);
	}
	
	@Override
	protected void leaveEvent(QEvent e){
		QPalette p = new QPalette();
		p.setColor(QPalette.ColorRole.Window, new QColor(255, 255, 255));
		this.setPalette(p);
	}
	
	@Override
	protected void mousePressEvent(QMouseEvent e) {
		QPalette p = new QPalette();
		p.setColor(QPalette.ColorRole.Window, new QColor(165, 175, 255));
		this.setPalette(p);
		
		super.mousePressEvent(e);
	}
}

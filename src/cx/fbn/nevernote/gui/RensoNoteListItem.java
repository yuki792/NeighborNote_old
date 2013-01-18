// ICHANGED
package cx.fbn.nevernote.gui;

import java.text.SimpleDateFormat;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QTextOption;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class RensoNoteListItem extends QWidget{
	private final DatabaseConnection conn;
	private final String noteGuid;
	private final String noteTitle;
	private final int relationPoints;
	private final String noteCreated;
	private final String tagNames;
	private String noteContent;
	private final RensoNoteList parent;
	private final boolean isStared;
	private final int allPointSum;
	
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	public RensoNoteListItem(Note note, int relationPoints, boolean isStared, int allPointSum, DatabaseConnection c, RensoNoteList parent){
		
		this.conn = c;
		this.parent = parent;
		this.isStared = isStared;
		this.allPointSum = allPointSum;
		this.noteGuid = new String(note.getGuid());
		
		this.noteTitle = new String(note.getTitle());
		this.relationPoints = relationPoints;
		SimpleDateFormat simple = new SimpleDateFormat("yyyy/MM/dd");
		this.noteCreated = new StringBuilder(simple.format(note.getCreated())).toString();
		
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < note.getTagNames().size(); i++) {
			sb.append(note.getTagNames().get(i));
			if(i + 1 < note.getTagNames().size()){
				sb.append(Global.tagDelimeter + " ");
			}
		}

		this.tagNames = new String(sb);
		
		// this.noteContent = new String(note.getContent());
		this.noteContent = conn.getNoteTable().getNoteContentNoUTFConversion(note.getGuid());
		this.noteContent = this.noteContent.replaceAll("<.+?>", "");
		this.noteContent = this.noteContent.replaceAll("\\s{2,}", " ");
		String kaigyo = System.getProperty("line.separator");
		this.noteContent = this.noteContent.replaceAll(kaigyo, "");
		
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
		
		// 項目の中身
		// フォント設定
		painter.setPen(QColor.black);
		QFont titleFont = new QFont();
		titleFont.setPixelSize(15);
		titleFont.setBold(true);
		QFont normalFont = new QFont();
		normalFont.setPixelSize(12);
		
		// タイトル
		painter.setFont(titleFont);
		painter.drawText(85, 3, size().width() - 130, 20, Qt.AlignmentFlag.AlignLeft.value(), noteTitle);
		// 関連度
		double ratio = (double)relationPoints / allPointSum;
		int green = (int) (255 * (1.0 - ratio));
		painter.setPen(new QColor(255, green, 0));
		painter.drawText(size().width() - 40, 3, 40, 20, Qt.AlignmentFlag.AlignRight.value(), String.valueOf((int)(ratio * 100)) + "%");
		// ノート作成日時
		painter.setFont(normalFont);
		painter.setPen(new QColor(60, 65, 255));
		painter.drawText(85, 23, 75, 17, Qt.AlignmentFlag.AlignLeft.value(), noteCreated);
		// タグ
		painter.setPen(QColor.black);
		painter.drawText(165, 23, size().width() - 165, 17, Qt.AlignmentFlag.AlignLeft.value(), tagNames);
		// ノート内容
		QTextOption option = new QTextOption();
		option.setAlignment(Qt.AlignmentFlag.AlignLeft);
		option.setUseDesignMetrics(true);
		painter.drawText(new QRectF(85, 40, width() - 85, 40), noteContent, option);
		
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
		painter.drawImage(2, 4, img, 0, 0, 80, rect().height() - 10);
		painter.setPen(QColor.lightGray);
		painter.drawRect(2, 4, 80, rect().height() - 10);
		
		// スター
		if (isStared) {
			QImage starImage = new QImage(iconPath+"star.png");
			starImage = starImage.scaled(30, 30, Qt.AspectRatioMode.IgnoreAspectRatio, Qt.TransformationMode.SmoothTransformation);
			painter.drawImage(0, 0, starImage, 0, 0, starImage.width(), starImage.height());
		}
		
		painter.end();
	}

	@Override
	protected void enterEvent(QEvent e){
		if (!parent.isContextMenuVisible()) {
			QPalette p = new QPalette();
			p.setColor(QPalette.ColorRole.Window, new QColor(225, 235, 255));
			this.setPalette(p);
		}
	}
	
	@Override
	protected void leaveEvent(QEvent e){
		if (!parent.isContextMenuVisible()) {
			setDefaultBackground();
		}
	}
	
	@Override
	protected void mousePressEvent(QMouseEvent e) {
		QPalette p = new QPalette();
		p.setColor(QPalette.ColorRole.Window, new QColor(165, 175, 255));
		this.setPalette(p);
		
		super.mousePressEvent(e);
	}
	
	public void setDefaultBackground() {
		QPalette p = new QPalette();
		p.setColor(QPalette.ColorRole.Window, new QColor(255, 255, 255));
		this.setPalette(p);
	}
}

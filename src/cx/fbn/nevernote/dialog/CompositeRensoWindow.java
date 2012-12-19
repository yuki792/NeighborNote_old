// ICHANGED
package cx.fbn.nevernote.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;

import cx.fbn.nevernote.gui.CompositeRensoElementList;
import cx.fbn.nevernote.gui.CompositeRensoNoteList;
import cx.fbn.nevernote.gui.RensoDockWidget;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class CompositeRensoWindow extends QDialog {
	private final RensoDockWidget parent;
	private final DatabaseConnection conn;
	private final CompositeRensoElementList compositeRensoElementList;
	private final CompositeRensoNoteList compositeRensoNoteList;
	private final QPushButton deleteButton;
	private final QPushButton allClearButton;
	private final QPushButton openButton;
	private final QPushButton exitButton;
	private final QLabel elementLabel;
	private final QLabel resultLabel;
	private final QLabel explainLabel;
	private final QLabel rightArrowLabel;
	
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	public CompositeRensoWindow(RensoDockWidget parent, DatabaseConnection conn) {
		super(parent);
		this.parent = parent;
		this.conn = conn;
		this.setWindowTitle(tr("Composite Renso Search"));
		
		compositeRensoElementList = new CompositeRensoElementList(this.conn, this);
		compositeRensoNoteList = new CompositeRensoNoteList(this.conn, this);
		compositeRensoNoteList.setFixedSize(300, 400);
		
		deleteButton = new QPushButton(tr("Delete Slected Notes"));
		getDeleteButton().setEnabled(false);
		allClearButton = new QPushButton(tr("All Clear"));
		getAllClearButton().setEnabled(false);
		openButton = new QPushButton(tr("Open Selected Notes"));
		getOpenButton().setEnabled(false);
		exitButton = new QPushButton(tr("Exit"));
		exitButton.setFixedWidth(100);
		elementLabel = new QLabel(tr("Element Notes:"));
		resultLabel = new QLabel(tr("Search Result:"));
		explainLabel = new QLabel(tr("Please drop notes in this list."));
		QImage arrowImage = new QImage(iconPath+"greenRightArrow.png");
		rightArrowLabel = new QLabel();
		rightArrowLabel.setPixmap(QPixmap.fromImage(arrowImage));
		
		initialize();
	}
	
	private void initialize() {
		connectButtons();
		layoutSettings();
	}
	
	private void connectButtons() {
		getDeleteButton().clicked.connect(this, "deleteButtonClicked()");
		getAllClearButton().clicked.connect(this, "allClearButtonClicked()");
		getOpenButton().clicked.connect(this, "openButtonClicked()");
		exitButton.clicked.connect(this, "reject()");
	}
	
	private void layoutSettings() {		
		QHBoxLayout hElementButtonLayout = new QHBoxLayout();
		hElementButtonLayout.addWidget(getDeleteButton());
		hElementButtonLayout.addWidget(getAllClearButton());
		
		QVBoxLayout vElementLayout = new QVBoxLayout();
		vElementLayout.addWidget(explainLabel);
		vElementLayout.addSpacing(10);
		vElementLayout.addWidget(elementLabel);
		vElementLayout.addWidget(compositeRensoElementList);
		vElementLayout.addLayout(hElementButtonLayout);
		
		QVBoxLayout vResultLayout = new QVBoxLayout();
		vResultLayout.addWidget(resultLabel);
		vResultLayout.addWidget(compositeRensoNoteList);
		vResultLayout.addWidget(getOpenButton());
		
		QHBoxLayout hLayout1 = new QHBoxLayout();
		hLayout1.addLayout(vElementLayout);
		hLayout1.addWidget(rightArrowLabel);
		hLayout1.addLayout(vResultLayout);
		
		QHBoxLayout hLayout2 = new QHBoxLayout();
		hLayout2.setAlignment(new Qt.Alignment(Qt.AlignmentFlag.AlignRight));
		hLayout2.addWidget(exitButton);
		
		QVBoxLayout vLayout1 = new QVBoxLayout();
		vLayout1.addLayout(hLayout1);
		vLayout1.addSpacing(20);
		vLayout1.addLayout(hLayout2);
		
		setLayout(vLayout1);
	}

	@Override
	public void reject() {
		parent.getCompositeSearchButton().setChecked(false);
		super.reject();
	}
	
	@SuppressWarnings("unused")
	private void deleteButtonClicked() {
		compositeRensoElementList.deleteSelectedItems();
	}
	
	@SuppressWarnings("unused")
	private void allClearButtonClicked() {
		compositeRensoElementList.clearItems();
	}
	
	@SuppressWarnings("unused")
	private void openButtonClicked() {
		// TODO
		System.out.println(compositeRensoNoteList.selectedItems());
	}
	
	public void executeCompositeRenso() {
		HashMap<String, String> elements = compositeRensoElementList.getListItems();
		List<String> elementGuids = new ArrayList<String>();
		
		Collection<String> guids = elements.keySet();
		Iterator<String> iterator = guids.iterator();
		while (iterator.hasNext()) {
			elementGuids.add(iterator.next());
		}
		compositeRensoNoteList.compositeRensoSearch(elementGuids);
	}

	public QPushButton getDeleteButton() {
		return deleteButton;
	}

	public QPushButton getAllClearButton() {
		return allClearButton;
	}

	public QPushButton getOpenButton() {
		return openButton;
	}
}

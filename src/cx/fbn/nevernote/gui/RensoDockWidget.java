// ICHANGED
package cx.fbn.nevernote.gui;

import com.trolltech.qt.gui.QDockWidget;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.NeverNote;
import cx.fbn.nevernote.dialog.CompositeRensoWindow;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class RensoDockWidget extends QDockWidget {
	private final NeverNote parent;
	private final DatabaseConnection conn;
	private final QPushButton compositeSearchButton;
	private final RensoNoteList rensoNoteList;
	private final CompositeRensoWindow compositeRensoWindow;
	
	public RensoDockWidget(NeverNote parent, DatabaseConnection conn) {
		this.parent = parent;
		this.conn = conn;
		this.setParent(this.getParent());
		this.setWindowTitle(tr("Renso Note List"));
		compositeRensoWindow = new CompositeRensoWindow(this, conn);
		
		compositeSearchButton = new QPushButton();
		getCompositeSearchButton().setText(tr("Composite Renso Search"));
		getCompositeSearchButton().setCheckable(true);
		getCompositeSearchButton().clicked.connect(this, "compositeSearchButtonClicked()");
		
		rensoNoteList = new RensoNoteList(conn, this.getParent());
		getRensoNoteList().itemPressed.connect(this.getParent(), "rensoNoteItemPressed(QListWidgetItem)");
		
		QVBoxLayout vLayout = new QVBoxLayout();
		vLayout.addWidget(getRensoNoteList());
		vLayout.addSpacing(20);
		vLayout.addWidget(getCompositeSearchButton());
		vLayout.addSpacing(20);
		
		QWidget widgetGroup = new QWidget();
		widgetGroup.setLayout(vLayout);
		this.setWidget(widgetGroup);
	}

	public RensoNoteList getRensoNoteList() {
		return rensoNoteList;
	}
	
	@SuppressWarnings("unused")
	private void compositeSearchButtonClicked() {
		if (getCompositeSearchButton().isChecked()) {
			compositeRensoWindow.show();
			compositeRensoWindow.raise();
	        compositeRensoWindow.activateWindow();
		} else {
			if (compositeRensoWindow.isVisible()) {
				compositeRensoWindow.raise();
				compositeSearchButton.setChecked(true);
			}
		}
	}

	public QPushButton getCompositeSearchButton() {
		return compositeSearchButton;
	}

	public NeverNote getParent() {
		return parent;
	}
}

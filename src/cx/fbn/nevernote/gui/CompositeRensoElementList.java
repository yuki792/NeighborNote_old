// ICHANGED
package cx.fbn.nevernote.gui;

import java.util.HashMap;
import java.util.List;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;

import cx.fbn.nevernote.dialog.CompositeRensoWindow;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class CompositeRensoElementList extends QListWidget {
	private final DatabaseConnection conn;
	private final CompositeRensoWindow parent;
	
	private final HashMap<String, String> listItems;	// <Guid, Title>
	
	public CompositeRensoElementList(DatabaseConnection conn, CompositeRensoWindow parent) {
		this.conn = conn;
		this.parent = parent;
		
		listItems = new HashMap<String, String>();
		
		setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		setAcceptDrops(true);
		
	}

	@Override
	protected boolean dropMimeData(int index, QMimeData data, Qt.DropAction action) {
		System.out.println("drop");
		// ノートがリストにドロップされた場合(NotebookTreeWidgetのdropMimeData()を参考)
		if (data.hasFormat("application/x-nevernote-note")) {
			QByteArray d = data.data("application/x-nevernote-note");
			String s = d.toString();
			String noteGuidArray[] = s.split(" ");
			for (String elementGuid : noteGuidArray) {
				String guid = elementGuid.trim();
				Note n = conn.getNoteTable().getNote(guid, false, false, false, false, false);
				
				addElement(n.getTitle(), guid);
			}
			enableDeleteButtons();
			parent.executeCompositeRenso();
			return true;
		}
		return false;
	}
	
	private void addElement(String title, String guid) {
		// まだリストに追加されていなければ、追加
		if (!listItems.containsKey(guid)) {
			addItem(title);
			listItems.put(guid, title);
		}
	}

	public void deleteSelectedItems() {
		List<QListWidgetItem> selectedItems = selectedItems();
		List<QModelIndex> selectedIndexes = selectedIndexes();
		for (int i = 0; i < selectedItems.size(); i++) {
			String title = selectedItems.get(i).text();
			takeItem(selectedIndexes.get(i).row());	// リストから削除
			listItems.remove(title);
		}
		enableDeleteButtons();
	}

	public void clearItems() {
		clear();
		listItems.clear();
		enableDeleteButtons();
	}
	
	private void enableDeleteButtons() {
		if (count() > 0) {
			parent.getDeleteButton().setEnabled(true);
			parent.getAllClearButton().setEnabled(true);
		} else {
			parent.getDeleteButton().setEnabled(false);
			parent.getAllClearButton().setEnabled(false);
		}
	}

	public HashMap<String, String> getListItems() {
		return listItems;
	}
}

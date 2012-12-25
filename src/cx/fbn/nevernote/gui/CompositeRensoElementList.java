// ICHANGED
package cx.fbn.nevernote.gui;

import java.util.HashMap;
import java.util.List;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;

import cx.fbn.nevernote.dialog.CompositeRensoWindow;
import cx.fbn.nevernote.neighbornote.CompositeRensoElementItem;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class CompositeRensoElementList extends QListWidget {
	private final DatabaseConnection conn;
	private final CompositeRensoWindow parent;
	
	private final HashMap<QListWidgetItem, String> listItems;	// <Item, Guid>
	private final HashMap<String, CompositeRensoElementItem> listTrueItems;	// <Guid, TrueItem>
	
	public CompositeRensoElementList(DatabaseConnection conn, CompositeRensoWindow parent) {
		this.conn = conn;
		this.parent = parent;
		
		listItems = new HashMap<QListWidgetItem, String>();
		listTrueItems = new HashMap<String, CompositeRensoElementItem>();
		
		setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		setAcceptDrops(true);
		
	}

	@Override
	protected boolean dropMimeData(int index, QMimeData data, Qt.DropAction action) {
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
			// parent.executeCompositeRenso();
			return true;
		}
		return false;
	}
	
	// オーバーロードメソッド
	public void addElement(String guid) {
		Note n = conn.getNoteTable().getNote(guid, false, false, false, false, false);
		addElement(n.getTitle(), guid);
	}
	
	private void addElement(String title, String guid) {
		// まだリストに追加されていなければ、追加
		if (!listItems.containsValue(guid)) {
			QListWidgetItem item = new QListWidgetItem();
			CompositeRensoElementItem myItem = new CompositeRensoElementItem(guid, title);
			addItem(item);
			setItemWidget(item, myItem);
			listItems.put(item, guid);
			listTrueItems.put(guid, myItem);
			enableButtons();
		}
	}

	public void deleteSelectedItems() {
		List<QListWidgetItem> selectedItems = selectedItems();
		for (int i = 0; i < selectedItems.size(); i++) {
			int row = row(selectedItems.get(i));
			takeItem(row);	// リストから削除
			String deletedGuid = listItems.remove(selectedItems.get(i));
			listTrueItems.remove(deletedGuid);
		}
		enableButtons();
	}

	public void clearItems() {
		clear();
		listItems.clear();
		listTrueItems.clear();
		enableButtons();
	}
	
	// 削除、全クリア、検索ボタン有効（非有効）化
	private void enableButtons() {
		if (count() >= 1) {
			parent.getDeleteButton().setEnabled(true);
			parent.getAllClearButton().setEnabled(true);
		} else {
			parent.getDeleteButton().setEnabled(false);
			parent.getAllClearButton().setEnabled(false);
		}
		if (count() >= 2) {
			parent.getSearchButton().setEnabled(true);
		} else {
			parent.getSearchButton().setEnabled(false);
		}
	}

	public HashMap<QListWidgetItem, String> getListItems() {
		return listItems;
	}
}

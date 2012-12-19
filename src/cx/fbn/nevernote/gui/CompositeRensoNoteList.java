// ICHANGED
package cx.fbn.nevernote.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QListWidgetItem;

import cx.fbn.nevernote.dialog.CompositeRensoWindow;
import cx.fbn.nevernote.neighbornote.CompositeRensoNoteListItem;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class CompositeRensoNoteList extends RelationCalculator {
	private final DatabaseConnection conn;
	private final CompositeRensoWindow parent;
	
	private final HashMap<QListWidgetItem, String> compositeRensoNoteListItems;
	private final List<CompositeRensoNoteListItem> compositeRensoNoteListTrueItems;
	
	public CompositeRensoNoteList(DatabaseConnection conn, CompositeRensoWindow parent) {
		super(conn);
		this.conn = conn;
		this.parent = parent;
		
		compositeRensoNoteListItems = new HashMap<QListWidgetItem, String>();
		compositeRensoNoteListTrueItems = new ArrayList<CompositeRensoNoteListItem>();
		
		setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
	}
	
	public void compositeRensoSearch(List<String> elementGuids) {		
		clear();
		compositeRensoNoteListItems.clear();
		compositeRensoNoteListTrueItems.clear();
		
		if (elementGuids == null) {
			return;
		}
		
		// エレメントノートから複合連想ノートを算出して表示
		// <関連ノートのGuid, 関連度>>
		List<HashMap<String, Integer>> rensoNoteListList = new ArrayList<HashMap<String, Integer>>();
		for (int i = 0; i < elementGuids.size(); i++) {
			rensoNoteListList.add(calculateRensoNotes(elementGuids.get(i)));
		}
		HashMap<String, Integer> compositeRensoNotes = new HashMap<String, Integer>();	// エレメントノートの連想ノートリストの合成
		HashMap<String, Integer> ReferencedCount = new HashMap<String, Integer>();	// いくつのノートから連想ノートリストとして登録されていたか
		for (int i = 0; i < rensoNoteListList.size(); i++) {
			HashMap<String, Integer> rensoNotes = rensoNoteListList.get(i);
			Collection<String> guids = rensoNotes.keySet();
			Iterator<String> guidIterator = guids.iterator();
			while (guidIterator.hasNext()) {
				String guid = guidIterator.next();
				if (compositeRensoNotes.containsKey(guid)) {
					int point = compositeRensoNotes.get(guid);
					point += rensoNotes.get(guid);
					compositeRensoNotes.put(guid, point);
					ReferencedCount.put(guid, ReferencedCount.get(guid) + 1);
				} else {
					compositeRensoNotes.put(guid, rensoNotes.get(guid));
					ReferencedCount.put(guid, 1);
				}
			}
		}
		// ここで、1つのノートからしか連想ノートリストとして登録されていないノートを削除
		Collection<String> guids = compositeRensoNotes.keySet();
		Iterator<String> guidIterator = guids.iterator();
		List<String> removeGuids = new ArrayList<String>();
		while (guidIterator.hasNext()) {
			String guid = guidIterator.next();
			if (ReferencedCount.get(guid) <= 1) {
				removeGuids.add(guid);
			}
		}
		for (int i = 0; i < removeGuids.size(); i++) {
			String guid = removeGuids.get(i);
			compositeRensoNotes.remove(guid);
			ReferencedCount.remove(guid);
		}
		
		// 完成（参照数に応じた重み付けはまだ）
		System.out.println(compositeRensoNotes);
		addSearchResult(compositeRensoNotes);
		
		enableOpenButton();
	}
	
	private void addSearchResult(HashMap<String, Integer> History) {
		// 引数保護のためディープコピー
		HashMap<String, Integer> copyHistory = new HashMap<String, Integer>();
		copyHistory.putAll(History);
		
		while (!copyHistory.isEmpty()) {
			int maxNum = -1;
			String maxGuid = new String();
			Iterator<String> it = copyHistory.keySet().iterator();
			
			while (it.hasNext()) {
				String nextGuid = it.next();
				int tmpNum = copyHistory.get(nextGuid);
				// 最大ノート探索
				if (tmpNum > maxNum) {
					maxNum = tmpNum;
					maxGuid = nextGuid;
				}
			}
			// 次の最大値探索で邪魔なので最大値をHashMapから削除
			copyHistory.remove(maxGuid);

			// 関連度最大のノートがアクティブか確認
			Note maxNote = conn.getNoteTable().getNote(maxGuid, true, false,
					false, false, true);
			boolean isNoteActive = false;
			if(maxNote != null) {
				isNoteActive = maxNote.isActive();
			}
			
			// 存在していて、かつ関連度0でなければノート情報を取得して連想ノートリストに追加
			if (isNoteActive && maxNum > 0) {
				QListWidgetItem item = new QListWidgetItem();
				CompositeRensoNoteListItem myItem = new CompositeRensoNoteListItem(maxNote, maxNum, false, conn);
				item.setSizeHint(new QSize(0, 90));
				this.addItem(item);
				this.setItemWidget(item, myItem);
				compositeRensoNoteListItems.put(item, maxGuid);
				compositeRensoNoteListTrueItems.add(myItem);
			}
		}
	}
	
	// リストのアイテムから対象ノートのguidを取得
	public String getNoteGuid(QListWidgetItem item) {
		return compositeRensoNoteListItems.get(item);
	}
	
	private void enableOpenButton() {
		if (count() > 0) {
			parent.getOpenButton().setEnabled(true);
		} else {
			parent.getOpenButton().setEnabled(false);
		}
	}
}

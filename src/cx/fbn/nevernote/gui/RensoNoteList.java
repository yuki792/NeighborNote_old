// ICHANGED
package cx.fbn.nevernote.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;

import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class RensoNoteList extends QListWidget {
	private final DatabaseConnection conn;
	private final ApplicationLogger logger;
	private final HashMap<QListWidgetItem, String> rensoNoteListItems;

	public RensoNoteList(DatabaseConnection c) {
		logger = new ApplicationLogger("rensoNoteList.log");
		logger.log(logger.HIGH, "Setting up rensoNoteList");

		conn = c;
		rensoNoteListItems = new HashMap<QListWidgetItem, String>();
		
		logger.log(logger.HIGH, "rensoNoteList setup complete");
	}

	public void refreshRensoNoteList(String guid) {
		logger.log(logger.HIGH, "Entering RensoNoteList.refreshRensoNoteList");

		this.clear();
		rensoNoteListItems.clear();

		if (!this.isEnabled()) {
			return;
		}

		HashMap<String, Integer> mergedHistory = new HashMap<String, Integer>();
		
		// browseHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> browseHistory = conn.getHistoryTable().getBehaviorHistory("browse", guid);
		mergedHistory = mergeHistory(browseHistory, new HashMap<String, Integer>());
		
		// copy&pasteHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> copyAndPasteHistory = conn.getHistoryTable().getBehaviorHistory("copy & paste", guid);
		mergedHistory = mergeHistory(copyAndPasteHistory, mergedHistory);
		
		addRensoNoteList(mergedHistory);

		logger.log(logger.HIGH, "Leaving RensoNoteList.refreshRensoNoteList");
	}
	
	
	private HashMap<String, Integer> mergeHistory(HashMap<String, Integer> History1, HashMap<String, Integer> History2){
		HashMap<String, Integer> mergedHistory = new HashMap<String, Integer>();
		
		mergedHistory.putAll(History1);
		
		Set<String> keySet = History2.keySet();
		Iterator<String> hist2_iterator = keySet.iterator();
		while(hist2_iterator.hasNext()){
			String key = hist2_iterator.next();
			if(mergedHistory.containsKey(key)){
				mergedHistory.put(key, mergedHistory.get(key) + History2.get(key));
			}else {
				mergedHistory.put(key, History2.get(key));
			}
		}

		return mergedHistory;
	}
	
	private void addRensoNoteList(HashMap<String, Integer> History){
		// 引数をディープコピー
		HashMap<String, Integer> copyHistory = new HashMap<String, Integer>();
		Set<String> keySet = History.keySet();
		Iterator<String> iterator = keySet.iterator();
		
		while(iterator.hasNext()){
			String key = iterator.next();
			copyHistory.put(key, History.get(key));
		}
		
		// 操作回数が多い順に取り出して連想ノートリストに追加
		while (!copyHistory.isEmpty()) {
			int maxNum = 0;
			String maxGuid = new String();
			Iterator<String> it = copyHistory.keySet().iterator();
			while (it.hasNext()) {
				String nextGuid = it.next();
				int tmpNum = copyHistory.get(nextGuid);
				if (tmpNum > maxNum) {
					maxNum = tmpNum;
					maxGuid = nextGuid;
				}
			}
			// 次の最大値探索で邪魔なので最大値をHashMapから削除
			copyHistory.remove(maxGuid);

			// 関連度最大のノートがアクティブか確認
			Note maxNote = conn.getNoteTable().getNote(maxGuid, false, false,
					false, false, true);
			boolean isNoteActive = maxNote.isActive();

			// 存在していれば、ノート情報を取得して連想ノートリストに追加
			if (isNoteActive) {
				QListWidgetItem item = new QListWidgetItem();
				RensoNoteListItem myItem = new RensoNoteListItem(maxNote, maxNum);
				item.setSizeHint(new QSize(0, 85));
				this.addItem(item);
				this.setItemWidget(item, myItem);
				rensoNoteListItems.put(item, maxGuid);
			}
		}
	}

	// リストのアイテムから対象ノートのguidを取得
	public String getNoteGuid(QListWidgetItem item) {
		return rensoNoteListItems.get(item);
	}
	
}

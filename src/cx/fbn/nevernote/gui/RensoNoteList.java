// ICHANGED
package cx.fbn.nevernote.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QMenu;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.NeverNote;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class RensoNoteList extends QListWidget {
	private final DatabaseConnection conn;
	private final ApplicationLogger logger;
	private final HashMap<QListWidgetItem, String> rensoNoteListItems;
	
	private QAction openNewTabAction;
	private QAction excludeNoteAction;
	private final NeverNote parent;

	public RensoNoteList(DatabaseConnection c, NeverNote p) {
		logger = new ApplicationLogger("rensoNoteList.log");
		logger.log(logger.HIGH, "Setting up rensoNoteList");

		conn = c;
		this.parent = p;
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
		addWeight(browseHistory, Global.getBrowseWeight());
		mergedHistory = mergeHistory(browseHistory, new HashMap<String, Integer>());
		
		// copy&pasteHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> copyAndPasteHistory = conn.getHistoryTable().getBehaviorHistory("copy & paste", guid);
		addWeight(copyAndPasteHistory, Global.getCopyPasteWeight());
		mergedHistory = mergeHistory(copyAndPasteHistory, mergedHistory);
		
		// addNewNoteHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> addNewNoteHistory = conn.getHistoryTable().getBehaviorHistory("addNewNote", guid);
		addWeight(addNewNoteHistory, Global.getAddNewNoteWeight());
		mergedHistory = mergeHistory(addNewNoteHistory, mergedHistory);
		
		// rensoItemClickHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> rensoItemClickHistory = conn.getHistoryTable().getBehaviorHistory("rensoItemClick", guid);
		addWeight(rensoItemClickHistory, Global.getRensoItemClickWeight());
		mergedHistory = mergeHistory(rensoItemClickHistory, mergedHistory);
		
		addRensoNoteList(mergedHistory);

		logger.log(logger.HIGH, "Leaving RensoNoteList.refreshRensoNoteList");
	}
	
	// 操作回数に重み付けする
	private void addWeight(HashMap<String, Integer> history, int weight){
		Set<String> keySet = history.keySet();
		Iterator<String> hist_iterator = keySet.iterator();
		while(hist_iterator.hasNext()){
			String key = hist_iterator.next();
			history.put(key, history.get(key) * weight);
		}
	}
	
	// 引数1と引数2をマージしたハッシュマップを返す
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
		// 引数保護のためディープコピー
		HashMap<String, Integer> copyHistory = new HashMap<String, Integer>();
		copyHistory.putAll(History);
		
		// 操作回数が多い順に取り出して連想ノートリストに追加
		while (!copyHistory.isEmpty()) {
			int maxNum = -1;
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
			Note maxNote = conn.getNoteTable().getNote(maxGuid, true, false,
					false, false, true);
			boolean isNoteActive = false;
			if(maxNote != null) {
				isNoteActive = maxNote.isActive();
			}
			
			// 存在していて、かつ関連度0でなければノート情報を取得して連想ノートリストに追加
			if (isNoteActive && maxNum > 0) {
				QListWidgetItem item = new QListWidgetItem();
				RensoNoteListItem myItem = new RensoNoteListItem(maxNote, maxNum, conn);
				item.setSizeHint(new QSize(0, 90));
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
	
	// 関連ノートリストの右クリックメニュー
	@Override
	public void contextMenuEvent(QContextMenuEvent event){
		QMenu menu = new QMenu(this);
		
		// 新しいタブで開くアクション生成
		openNewTabAction = new QAction(tr("Open in New Tab"), this);
		openNewTabAction.setToolTip(tr("Open this note in new tab"));
		openNewTabAction.triggered.connect(parent, "openNewTabFromRNL()");
		
		// このノートを除外するアクション生成
		excludeNoteAction = new QAction(tr("Exclude"), this);
		excludeNoteAction.setToolTip(tr("Exclude this note from RensoNoteList"));
		excludeNoteAction.triggered.connect(parent, "excludeNote()");
		
		menu.addAction(openNewTabAction);
		menu.addAction(excludeNoteAction);
		
		menu.exec(event.globalPos());
	}
	
}

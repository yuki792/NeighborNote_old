// ICHANGED
package cx.fbn.nevernote.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.trolltech.qt.gui.QListWidget;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class RelationCalculator extends QListWidget {
	private final DatabaseConnection conn;
	
	public RelationCalculator(DatabaseConnection conn) {
		this.conn = conn;
	}

	protected HashMap<String, Integer> calculateRensoNotes(String guid) {
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
		
		// sameTagHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> sameTagHistory = conn.getHistoryTable().getBehaviorHistory("sameTag", guid);
		addWeight(sameTagHistory, Global.getSameTagWeight());
		mergedHistory = mergeHistory(sameTagHistory, mergedHistory);
		
		// sameNotebookNoteHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> sameNotebookHistory = conn.getHistoryTable().getBehaviorHistory("sameNotebook", guid);
		addWeight(sameNotebookHistory, Global.getSameNotebookWeight());
		mergedHistory = mergeHistory(sameNotebookHistory, mergedHistory);
		
		return mergedHistory;
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
}

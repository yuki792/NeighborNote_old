// ICHANGED
package cx.fbn.nevernote.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.MouseButton;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QMenu;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.NeverNote;
import cx.fbn.nevernote.neighbornote.RensoNoteListItem;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class RensoNoteList extends RelationCalculator {
	private final DatabaseConnection conn;
	private final ApplicationLogger logger;
	private final HashMap<QListWidgetItem, String> rensoNoteListItems;
	private final List<RensoNoteListItem> rensoNoteListTrueItems;
	private String rensoNotePressedItemGuid;
	
	private final QAction openNewTabAction;
	private final QAction starAction;
	private final QAction unstarAction;
	private final QAction excludeNoteAction;
	private final NeverNote parent;
	private final QMenu menu;

	public RensoNoteList(DatabaseConnection c, NeverNote p) {
		super(c);
		logger = new ApplicationLogger("rensoNoteList.log");
		logger.log(logger.HIGH, "Setting up rensoNoteList");

		conn = c;
		this.parent = p;
		rensoNoteListItems = new HashMap<QListWidgetItem, String>();
		rensoNoteListTrueItems = new ArrayList<RensoNoteListItem>();
		
		this.itemPressed.connect(this, "rensoNoteItemPressed(QListWidgetItem)");
		
		// コンテキストメニュー作成
		menu = new QMenu(this);
		// 新しいタブで開くアクション生成
		openNewTabAction = new QAction(tr("Open in New Tab"), this);
		openNewTabAction.setToolTip(tr("Open this note in new tab"));
		openNewTabAction.triggered.connect(parent, "openNewTabFromRNL()");
		// スターをつけるアクション生成
		starAction = new QAction(tr("Star"), this);
		starAction.setToolTip(tr("Star this item"));
		starAction.triggered.connect(parent, "starNote()");
		// スターを外すアクション生成
		unstarAction = new QAction(tr("Unstar"), this);
		unstarAction.setToolTip(tr("Unstar this item"));
		unstarAction.triggered.connect(parent, "unstarNote()");
		// このノートを除外するアクション生成
		excludeNoteAction = new QAction(tr("Exclude"), this);
		excludeNoteAction.setToolTip(tr("Exclude this note from RensoNoteList"));
		excludeNoteAction.triggered.connect(parent, "excludeNote()");
		// コンテキストメニューに登録
		menu.addAction(openNewTabAction);
		menu.addAction(excludeNoteAction);
		menu.aboutToHide.connect(this, "contextMenuHidden()");
		
		logger.log(logger.HIGH, "rensoNoteList setup complete");
	}

	public void refreshRensoNoteList(String guid) {
		logger.log(logger.HIGH, "Entering RensoNoteList.refreshRensoNoteList");

		this.clear();
		rensoNoteListItems.clear();
		rensoNoteListTrueItems.clear();

		if (!this.isEnabled()) {
			return;
		}
		if (guid == null || guid.equals("")) {
			return;
		}
		
		HashMap<String, Integer> mergedHistory = calculateRensoNotes(guid);
		addRensoNoteList(mergedHistory);

		logger.log(logger.HIGH, "Leaving RensoNoteList.refreshRensoNoteList");
	}
	
	private void addRensoNoteList(HashMap<String, Integer> History){		
		// 引数保護のためディープコピー
		HashMap<String, Integer> copyHistory = new HashMap<String, Integer>();
		copyHistory.putAll(History);
		
		// 連想ノートリストアイテムの最大表示数まで繰り返す
		for (int i = 0; i < Global.getRensoListItemMaximum(); i++) {
			// 操作回数が多い順に取り出して連想ノートリストに追加
			if (!copyHistory.isEmpty()) {
				int maxNum = -1;
				String maxGuid = new String();
				Iterator<String> it = copyHistory.keySet().iterator();
				
				while (it.hasNext()) {
					String nextGuid = it.next();
					int tmpNum = copyHistory.get(nextGuid);
					
					// スター付きを見つけたら、その時点でそのノートを最大ノートとして表示させる
					boolean isStared;
					String currentNoteGuid = new String(parent.getCurrentNoteGuid());
					isStared = conn.getStaredTable().existNote(currentNoteGuid, nextGuid);
					if (isStared) {
						maxNum = tmpNum;
						maxGuid = nextGuid;
						break;
					}
					
					// スター付きでないなら、最大ノート探索する
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
					// スター付きか確認
					boolean isStared;
					String currentNoteGuid = new String(parent.getCurrentNoteGuid());
					isStared = conn.getStaredTable().existNote(currentNoteGuid, maxGuid);
					
					QListWidgetItem item = new QListWidgetItem();
					RensoNoteListItem myItem = new RensoNoteListItem(maxNote, maxNum, isStared, conn, this);
					item.setSizeHint(new QSize(0, 90));
					this.addItem(item);
					this.setItemWidget(item, myItem);
					rensoNoteListItems.put(item, maxGuid);
					rensoNoteListTrueItems.add(myItem);
				} else {
					break;
				}
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
		// STAR, UNSTARがあれば、一度消す
		List<QAction> menuActions = new ArrayList<QAction>(menu.actions());
		if (menuActions.contains(starAction)) {
			menu.removeAction(starAction);
		}
		if (menuActions.contains(unstarAction)) {
			menu.removeAction(unstarAction);
		}
		
		// 対象アイテムがスター付きなら「UNSTAR」、スター無しなら「STAR」を追加
		String currentNoteGuid = parent.getCurrentNoteGuid();
		boolean isExist = conn.getStaredTable().existNote(currentNoteGuid, rensoNotePressedItemGuid);
		if (isExist) {
			menu.insertAction(excludeNoteAction, unstarAction);
		} else {
			menu.insertAction(excludeNoteAction, starAction);
		}
		
		// コンテキストメニューを表示
		menu.exec(event.globalPos());
	}
	
	// コンテキストメニューが表示されているかどうか
	public boolean isContextMenuVisible() {
		return menu.isVisible();
	}
	
	// コンテキストメニューが閉じられた時
	@SuppressWarnings("unused")
	private void contextMenuHidden() {
		for (int i = 0; i < rensoNoteListTrueItems.size(); i++) {
			RensoNoteListItem item = rensoNoteListTrueItems.get(i);
			item.setDefaultBackground();
		}
	}
	
	// ユーザが連想ノートリストのアイテムを選択した時の処理
	@SuppressWarnings("unused")
	private void rensoNoteItemPressed(QListWidgetItem current) {
		rensoNotePressedItemGuid = null;
		// 右クリックだったときの処理
		if (QApplication.mouseButtons().isSet(MouseButton.RightButton)) {
			rensoNotePressedItemGuid = getNoteGuid(current);
		}
	}
}

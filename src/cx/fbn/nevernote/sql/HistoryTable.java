// ICHANGED
package cx.fbn.nevernote.sql;

import java.util.HashMap;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class HistoryTable {
	private final ApplicationLogger logger;
	private final DatabaseConnection db;

	// コンストラクタ
	public HistoryTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}

	// テーブル作成
	public void createTable() {
		// TODO 自動生成されたメソッド・スタブ
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		logger.log(logger.HIGH, "Historyテーブルを作成しています...");
		if (!query
				.exec("Create table History (id integer primary key auto_increment, behaviorType varchar,"
						+ "guid1 varchar, guid2 varchar)"))
			logger.log(logger.HIGH, "Historyテーブル作成失敗!!!");
	}

	// テーブルをドロップ
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		query.exec("Drop table History");
	}

	// Historyテーブルにアイテムを1つ追加
	public void addHistory(String behaviorType, String guid1, String guid2) {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		query.prepare("Insert Into History (behaviorType, guid1, guid2) Values(:behaviorType, :guid1, :guid2)");
		query.bindValue(":behaviorType", behaviorType);
		query.bindValue(":guid1", guid1);
		query.bindValue(":guid2", guid2);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Historyテーブルへのアイテム追加に失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}

	// Historyテーブルから引数ノートと関連のあるノートのguidと回数をゲット（操作種別：browse）
	public HashMap<String, Integer> getBrowseHistory(String guid) {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		HashMap<String, Integer> browseHist = new HashMap<String, Integer>();

		// guid1=guidの履歴一覧を取得
		query.prepare("Select guid2 from History where behaviorType='browse' and guid1=:guid1");
		query.bindValue(":guid1", guid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM,
					"HistoryテーブルからbehaviorType=browseかつguid1=guidのアイテム取得失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
		// HashMapに記録
		while (query.next()) {
			// すでにHashMapに登録されていたら、回数を+1
			String key = query.valueString(0);
			if (browseHist.containsKey(key)) {
				browseHist.put(key, browseHist.get(key) + 1);
			} else { // そうでないなら新規登録
				browseHist.put(key, 1);
			}
		}

		// guid2=guidの履歴一覧を取得
		query.prepare("Select guid1 from History where behaviorType='browse' and guid2=:guid2");
		query.bindValue(":guid2", guid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM,
					"HistoryテーブルからbehaviorType=browseかつguid2=guidのアイテム取得失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
		// HashMapに記録
		while (query.next()) {
			// すでにHashMapに登録されていたら、回数を+1
			String key = query.valueString(0);
			if (browseHist.containsKey(key)) {
				browseHist.put(key, browseHist.get(key) + 1);
			} else { // そうでないなら新規登録
				browseHist.put(key, 1);
			}
		}
		return browseHist;
	}
}

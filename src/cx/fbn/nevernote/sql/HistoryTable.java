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
	
	// HistoryテーブルのGuidを更新
	public void updateHistoryGuid(String newGuid, String oldGuid){
		NSqlQuery histQuery = new NSqlQuery(db.getBehaviorConnection());
		boolean check = false;
		
		histQuery.prepare("Delete from history where (guid1=:oldGuid1 and guid2=:newGuid1) or (guid1=:newGuid2 and guid2=:oldGuid2)");
		histQuery.bindValue(":oldGuid1", oldGuid);
		histQuery.bindValue(":newGuid1", newGuid);
		histQuery.bindValue(":oldGuid2", oldGuid);
		histQuery.bindValue(":newGuid2", newGuid);
		check = histQuery.exec();
		if(!check){
			logger.log(logger.MEDIUM, "historyテーブルの重複削除で失敗");
			logger.log(logger.MEDIUM, histQuery.lastError());
		}
		
		histQuery.prepare("Update history set guid1=:newGuid where guid1=:oldGuid");
		histQuery.bindValue(":newGuid", newGuid);
		histQuery.bindValue(":oldGuid", oldGuid);
		check = histQuery.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "historyテーブルのguid1のところでguid更新失敗");
			logger.log(logger.MEDIUM, histQuery.lastError());
		}
		histQuery.prepare("Update history set guid2=:newGuid where guid2=:oldGuid");
		histQuery.bindValue(":newGuid", newGuid);
		histQuery.bindValue(":oldGuid", oldGuid);
		check = histQuery.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "historyテーブルのguid2のところでguid更新失敗");
			logger.log(logger.MEDIUM, histQuery.lastError());
		}
	}

	// Historyテーブルから引数ノートと関連のあるノートのguidと回数をゲット
	public HashMap<String, Integer> getBehaviorHistory(String behaviorType, String guid) {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		HashMap<String, Integer> behaviorHist = new HashMap<String, Integer>();

		// guid1=guidの履歴一覧を取得
		query.prepare("Select guid2 from History where behaviorType='" + behaviorType + "' and guid1=:guid1");
		query.bindValue(":guid1", guid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM,
					"HistoryテーブルからbehaviorType=" + behaviorType + "かつguid1=" + guid + "のアイテム取得失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
		// HashMapに記録
		while (query.next()) {
			// すでにHashMapに登録されていたら、回数を+1
			String key = query.valueString(0);
			if (behaviorHist.containsKey(key)) {
				behaviorHist.put(key, behaviorHist.get(key) + 1);
			} else { // そうでないなら新規登録
				behaviorHist.put(key, 1);
			}
		}

		// guid2=guidの履歴一覧を取得
		query.prepare("Select guid1 from History where behaviorType='" + behaviorType + "' and guid2=:guid2");
		query.bindValue(":guid2", guid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM,
					"HistoryテーブルからbehaviorType=" + behaviorType + "かつguid2=" + guid + "のアイテム取得失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
		// HashMapに記録
		while (query.next()) {
			// すでにHashMapに登録されていたら、回数を+1
			String key = query.valueString(0);
			if (behaviorHist.containsKey(key)) {
				behaviorHist.put(key, behaviorHist.get(key) + 1);
			} else { // そうでないなら新規登録
				behaviorHist.put(key, 1);
			}
		}
		return behaviorHist;
	}

	public void duplicateHistory(String newGuid, String oldGuid) {
		NSqlQuery histQuery = new NSqlQuery(db.getBehaviorConnection());

		// guid1 = oldGuidの履歴一覧を取得
		histQuery.prepare("Select behaviorType, guid2 from History where guid1=:oldGuid");
		histQuery.bindValue(":oldGuid", oldGuid);
		if(!histQuery.exec()){
			logger.log(logger.MEDIUM, "Historyテーブルからguid1=" + oldGuid + "のアイテム取得失敗");
			logger.log(logger.MEDIUM, histQuery.lastError());
		}
		// guid1 = newGuidの履歴として複製
		while(histQuery.next()){
			String behaviorType = histQuery.valueString(0);
			String guid2 = histQuery.valueString(1);
			
			addHistory(behaviorType, newGuid, guid2);
		}
		
		// guid2 = oldGuidの履歴一覧を取得
		histQuery.prepare("Select behaviorType, guid1 from History where guid2=:oldGuid");
		histQuery.bindValue(":oldGuid", oldGuid);
		if(!histQuery.exec()){
			logger.log(logger.MEDIUM, "Historyテーブルからguid2=" + oldGuid + "のアイテム取得失敗");
			logger.log(logger.MEDIUM,  histQuery.lastError());
		}
		// guid2 = newGuidの履歴として複製
		while(histQuery.next()){
			String behaviorType = histQuery.valueString(0);
			String guid1 = histQuery.valueString(1);
			
			addHistory(behaviorType, guid1, newGuid);
		}
	}
	
	public void expungeHistory(String guid) {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		boolean check;
		
		query.prepare("Delete from History where guid1=:guid1 or guid2=:guid2");
		query.bindValue(":guid1", guid);
		query.bindValue(":guid2", guid);
		
		check = query.exec();
		if(!check){
			logger.log(logger.MEDIUM, "historyテーブルからguid=" + guid + "のデータ削除に失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
}

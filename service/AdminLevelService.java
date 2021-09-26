package org.sfmis.adminstr.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.sfmis.adminstr.auth.Authenticated;
import org.sfmis.adminstr.model.AdminLevel;
import org.sfmis.adminstr.model.AdminLocalLevel;
import org.sfmis.adminstr.model.AdminLocalLevelType;
import org.sfmis.adminstr.util.DB;
import org.sfmis.adminstr.util.DbResponse;
import org.sfmis.adminstr.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AdminLevelService  extends AutoService{
	
	@Autowired
	DB db;

	@Autowired
	Authenticated auth;
	
	public ResponseEntity<Map<String, Object>> index() {
		String sql = "";
		int perPage = (request("rows") == null | (request("rows")).isBlank()) ? 10 : Integer.parseInt(request("rows"));
		int page = (request("page") == null | (request("page")).isBlank()) ? 1 : Integer.parseInt(request("page"));
		if (perPage > 100) {
			perPage = 100;
		}
		String condition = "";

		if (!request("search").isEmpty()) {
			List<String> searchbles = AdminLevel.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE N'%" + db.esc(request("search")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		 sql = "select (select levelId, code,levelOrder,nameNp,nameEn,disabled,approved from  adminstr.adminLevel where disabled=? and approved=? "+condition+" order by code ASC offset "
				+ ((page - 1) * perPage) + " rows fetch next " + perPage + " rows only for json auto) as rows";
		Map<String, Object> result = new HashMap<>();
		List<Tuple> rsTuple = db.getResultList(sql,Arrays.asList(1,1));
		sql = "select count(levelId) from adminstr.adminLevel where 1=1 "+condition;
		Tuple totalRows = db.getSingleResult(sql);
		if (rsTuple != null) {
			result.put("rows", rsTuple.get(0).get(0));
			result.put("currentPage", page);
			result.put("perPage", perPage);
			result.put("total", totalRows.get(0));
			return Messenger.getMessenger().setData(result).success();
		} else {
			return Messenger.getMessenger().error();
		}

	}

	
	public ResponseEntity<Map<String, Object>> store() {
		
		String sql = "";
		
			AdminLevel model = new AdminLevel();
		model.loadData(document);
		sql = "INSERT INTO adminstr.adminLevel (levelId,code,nameEn,nameNp,levelOrder,approved,disabled,entryDate, enterby) VALUES (dbo.newidint(),?,?,?,?,?,?,GETDATE(),?)";
			DbResponse rowEffect = db.execute(sql, Arrays.asList(model.code, model.nameEn, model.nameNp,model.levelOrder, model.approved,
				model.disabled, auth.getUserId()));
			if (rowEffect.getErrorNumber() == 0) {
				return Messenger.getMessenger().success();
				
			} else {
				return Messenger.getMessenger().error();
			}
	}

	

	public ResponseEntity<Map<String, Object>> edit(String id) {

		
		String sql = "select levelId ,code,nameNp,nameEn,levelOrder as levelOrder,disabled,approved from  adminstr.adminLevel where levelId = ? for json auto";
		Tuple result = db.getSingleResult(sql, Arrays.asList(id));
		if (result == null) {
			return Messenger.getMessenger().setMessage("Invalid Request").error();
		} else {
			return Messenger.getMessenger().setData(result.get(0)).success();
		}
	}
	
	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect ;
		AdminLevel model = new AdminLevel();
		model.loadData(document);

		if(shouldUpdatePartially()) {
			String sql = "UPDATE adminstr.adminLevel set code=? levelOrder=?, approved=?,disabled= ? where levelId=?";
			 rowEffect = db.execute(sql,
					Arrays.asList(model.code, model.levelOrder, model.approved, model.disabled, id));
		
		}
		else {
			String sql = "UPDATE adminstr.adminLevel set code=?,nameEn=?, nameNp=?, levelOrder=?, approved=?,disabled= ? where levelId=?";
			 rowEffect = db.execute(sql,
					Arrays.asList(model.code, model.nameEn, model.nameNp,model.levelOrder, model.approved, model.disabled, id));
		
		}
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}
		
	}
	
	public ResponseEntity<Map<String, Object>> destroy(String id) {

		if (!isBeingUsed("adminstr.adminLevel", id)) {
			String sql = "delete from adminstr.adminLevel where levelId  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}
	}
	else {
		return Messenger.getMessenger().setMessage("Deletion not Allowed").error();
	}
}
	
	public ResponseEntity<Map<String, Object>> getAdminLevel() {
		System.out.println("we r in serciv");
		String sql = "select levelId,code,nameNp,nameEn from adminstr.adminLevel";
		List<Tuple> level = db.getResultList(sql);

		List<Map<String, Object>> list = new ArrayList<>();
		if (!level.isEmpty()) {
			for (Tuple t : level) {
				Map<String, Object> mapLevel = new HashMap<>();
				mapLevel.put("code", t.get("code"));
				mapLevel.put("nameNp", t.get("nameNp"));
				mapLevel.put("nameEn", t.get("nameEn"));
				mapLevel.put("levelId", t.get("levelId"));
				list.add(mapLevel);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	
}

package org.sfmis.adminstr.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.sfmis.adminstr.auth.Authenticated;
import org.sfmis.adminstr.model.AdminFederal;
import org.sfmis.adminstr.model.AdminLevel;
import org.sfmis.adminstr.model.AdminLocalLevel;
import org.sfmis.adminstr.util.DB;
import org.sfmis.adminstr.util.DbResponse;
import org.sfmis.adminstr.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AdminFederalService extends AutoService {

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
			List<String> searchbles = AdminFederal.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE N'%" + db.esc(request("search")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		 sql = "select (select  id,code,nameEn,nameNp,shortNameEn, shortNameNp, isTsa,approved,disabled from  adminstr.adminFederal where disabled=? and approved=? "+condition+" order by code ASC offset "
				+ ((page - 1) * perPage) + " rows fetch next " + perPage + " rows only for json auto) as rows";
		Map<String, Object> result = new HashMap<>();
		List<Tuple> rsTuple = db.getResultList(sql,Arrays.asList(0,1));
		sql = "select count(id) from adminstr.adminFederal where 1=1 "+condition;
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

	public void create() {

	}

	public ResponseEntity<Map<String, Object>> store() {

	
		AdminFederal model = new AdminFederal();
	model.loadData(document);
	String sql = "INSERT INTO adminstr.adminFederal (id,adminLevel,code,nameEn,nameNp,shortNameEn, shortNameNp, isTsa,approved,disabled) VALUES (dbo.newidint(),?,?,?,?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.adminLevel,model.code, model.nameEn, model.nameNp,model.shortNameEn,model.shortNameNp,model.isTsa, model.approved,
			model.disabled));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}

	}

	

	public ResponseEntity<Map<String, Object>> edit(String id) {

		
		String sql = "select id,code,nameNp,nameEn,shortNameNp,shortNameEn,isTsa,disabled,approved from  adminstr.adminFederal where id = ? for json auto";
		Tuple result = db.getSingleResult(sql, Arrays.asList(id));
		if (result == null) {
			return Messenger.getMessenger().setMessage("Invalid Request").error();
		} else {
			return Messenger.getMessenger().setData(result.get(0)).success();
		}
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		
		
		AdminFederal model = new AdminFederal();
		model.loadData(document);
		DbResponse rowEffect;
		if (shouldUpdatePartially()) {
			String sql = "UPDATE adminstr.adminFederal set   approved=?,disabled= ? where id=?";
			 rowEffect = db.execute(sql,
					Arrays.asList( model.approved, model.disabled, id));
		
		}
		else {
			String sql = "UPDATE adminstr.adminFederal set code=?,nameEn=?, nameNp=?,shortNameNp=?,shortNameEn=?,isTsa=?,  approved=?,disabled= ? where id=?";
			 rowEffect = db.execute(sql,
					Arrays.asList(model.code, model.nameEn, model.nameNp,model.shortNameNp,model.shortNameEn,model.isTsa, model.approved, model.disabled, id));
		
		}

			if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {


		if (!isBeingUsed("adminstr.adminFederal", id)) {
		String sql = "delete from adminstr.adminFederal where id  = ?";
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
	
	public ResponseEntity<Map<String, Object>> getFederal() {
		String sql = "select id,code,nameNp,nameEn from adminstr.adminFederal where disabled=? and approved=?  ";
		List<Tuple> federal = db.getResultList(sql, Arrays.asList(0, 1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!federal.isEmpty()) {
			for (Tuple t : federal) {
				Map<String, Object> mapFederal = new HashMap<>();
				mapFederal.put("code", t.get("code"));
				mapFederal.put("nameNp", t.get("nameNp"));
				mapFederal.put("nameEn", t.get("nameEn"));
				mapFederal.put("id", t.get("id"));
				list.add(mapFederal);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

}

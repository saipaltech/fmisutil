package org.sfmis.adminstr.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.sfmis.adminstr.auth.Authenticated;
import org.sfmis.adminstr.model.AdminLocalLevel;
import org.sfmis.adminstr.model.AdminLocalLevelType;
import org.sfmis.adminstr.util.DB;
import org.sfmis.adminstr.util.DbResponse;
import org.sfmis.adminstr.util.LangService;
import org.sfmis.adminstr.util.Messenger;
import org.sfmis.adminstr.util.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AdminLocalLevelTypeService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;
	@Autowired
	ValidationService validationService;



	@Autowired
	LangService langService;


	public ResponseEntity<Map<String, Object>> index() {
		int perPage = (request("rows") == null | (request("rows")).isBlank()) ? 10 : Integer.parseInt(request("rows"));
		int page = (request("page") == null | (request("page")).isBlank()) ? 1 : Integer.parseInt(request("page"));
		if (perPage > 100) {
			perPage = 100;
		}
		String condition = "";

		if (!request("search").isEmpty()) {
			List<String> searchbles = AdminLocalLevelType.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE N'%" + db.esc(request("search")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		String sql = "select (select lgTypeId, code,nameNp,nameEn,disabled,approved from  adminstr.adminLocalLevelType where disabled=? and approved=? "+condition+" order by code ASC offset "
				+ ((page - 1) * perPage) + " rows fetch next " + perPage + " rows only for json auto) as rows";

		Map<String, Object> result = new HashMap<>();
		List<Tuple> rsTuple = db.getResultList(sql,Arrays.asList(0,1));
		sql = "select count(lgTypeId) from adminstr.adminLocalLevelType where 1=1 "+condition;
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
		AdminLocalLevelType model = new AdminLocalLevelType();
		model.loadData(document);
		String sql = "INSERT INTO adminstr.adminLocalLevelType (lgTypeId,code,nameEn,nameNp,approved,disabled,entryDate, enterby) VALUES (dbo.newidint(),?,?,?,?,?,GETDATE(),?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.code, model.nameEn, model.nameNp, model.approved,
				model.disabled, auth.getUserId()));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {
		String sql = "select lgTypeId,code,nameNp,nameEn,disabled,approved from  adminstr.adminLocalLevelType where lgTypeId = ? for json auto";
		Tuple result = db.getSingleResult(sql, Arrays.asList(id));
		if (result == null) {
			return Messenger.getMessenger().setMessage("Invalid Request").error();
		} else {
			return Messenger.getMessenger().setData(result.get(0)).success();
		}
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		AdminLocalLevelType model = new AdminLocalLevelType();
		model.loadData(document);

		if(shouldUpdatePartially()) {
			String sql = "UPDATE adminstr.adminLocalLevelType set code=?, approved=?,disabled= ? where lgTypeId=?";
			 rowEffect = db.execute(sql,
					Arrays.asList(model.code,  model.approved, model.disabled, id));
		
		}else {
			String sql = "UPDATE adminstr.adminLocalLevelType set code=?,nameEn=?, nameNp=?, approved=?,disabled= ? where lgTypeId=?";
			 rowEffect = db.execute(sql,
					Arrays.asList(model.code, model.nameEn, model.nameNp, model.approved, model.disabled, id));
		
		}
			if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		if (!isBeingUsed("adminstr.adminLocalLevelType", id)) {
		String sql = "delete from adminstr.adminLocalLevelType where lgTypeId = ?";
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
	
	public ResponseEntity<Map<String, Object>> getType() {
		String sql = "select lgTypeId,code,nameNp,nameEn from adminstr.adminLocalLevelType where disabled=? and approved=?";
		List<Tuple> province = db.getResultList(sql,Arrays.asList(0,1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!province.isEmpty()) {
			for (Tuple t : province) {
				Map<String, Object> mapProvince = new HashMap<>();
				mapProvince.put("code", t.get("code"));
				mapProvince.put("nameNp", t.get("nameNp"));
				mapProvince.put("nameEn", t.get("nameEn"));
				mapProvince.put("lgTypeId", t.get("lgTypeId"));
				list.add(mapProvince);

			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}

	}
}

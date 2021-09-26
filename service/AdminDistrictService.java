package org.sfmis.adminstr.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.sfmis.adminstr.auth.Authenticated;
import org.sfmis.adminstr.model.AdminDistrict;
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
public class AdminDistrictService extends AutoService {

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
			List<String> searchbles = AdminDistrict.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE N'%" + db.esc(request("search")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		String sql = "select (select districtId as id, code,cbsCode,provinceId,nameNp,nameEn,isEqAffected,noOfConstituency,disabled,approved from  adminstr.adminDistrict where disabled=? and approved=? "+condition+" order by code ASC offset "
				+ ((page - 1) * perPage) + " rows fetch next " + perPage + " rows only for json auto) as rows";
		Map<String, Object> result = new HashMap<>();
		List<Tuple> rsTuple = db.getResultList(sql, Arrays.asList(0, 1));
		sql = "select count(districtId) from adminstr.adminDistrict where 1=1 "+condition;
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
		AdminDistrict model = new AdminDistrict();
		model.loadData(document);
		String sql = "INSERT INTO adminstr.adminDistrict (districtId,code,cbsCode,provinceId,nameNp,nameEn,isEqAffected,noOfConstituency,disabled,approved,entryDate, enterby) VALUES (dbo.newidint(),?,?,?,?,?,?,?,?,?,GETDATE(),?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(model.code, model.cbsCode, model.provinceId, model.nameEn, model.nameNp,
						model.isEqAffected, model.noOfConstituency, model.approved, model.disabled, auth.getUserId()));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {
		String sql = "select districtId as id,code,nameNp,nameEn,disabled,approved from  adminstr.adminDistrict where districtId = ? for json auto";
		Tuple result = db.getSingleResult(sql, Arrays.asList(id));
		if (result == null) {
			return Messenger.getMessenger().setMessage("Invalid Request").error();
		} else {
			return Messenger.getMessenger().setData(result.get(0)).success();
		}
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		AdminDistrict model = new AdminDistrict();
		model.loadData(document);
		DbResponse rowEffect;
		if (shouldUpdatePartially()) {
			String sql = "UPDATE adminstr.adminDistrict set code=?, cbsCode=?, provinceId=?, noOfConstituency=?, approved=?,disabled= ? where districtId=?";
			rowEffect = db.execute(sql, Arrays.asList(model.code, model.cbsCode, model.provinceId,
					model.noOfConstituency, model.approved, model.disabled, id));
		} else {
			String sql = "UPDATE adminstr.adminDistrict set code=?, cbsCode=?, provinceId=?,nameEn=?, nameNp=?, noOfConstituency=?, approved=?,disabled= ? where districtId=?";
			rowEffect = db.execute(sql, Arrays.asList(model.code, model.cbsCode, model.provinceId,
					model.nameEn, model.nameNp, model.noOfConstituency, model.approved, model.disabled, id));
		}
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		if (!isBeingUsed("adminstr.adminDistrict", id)) {
			String sql = "delete from adminstr.adminDistrict where districtId = ?";
			DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
			if (rowEffect.getErrorNumber() == 0) {
				return Messenger.getMessenger().success();
			} else {
				return Messenger.getMessenger().error();
			}
		}
		return Messenger.getMessenger().setMessage("Deletion not Allowed").error();

	}

	public ResponseEntity<Map<String, Object>> getDistrict(String provinceId) {
		String sql = "select districtId,code,nameNp,nameEn from adminstr.adminDistrict where disabled=? and approved=? and provinceId=? ";
		List<Tuple> province = db.getResultList(sql, Arrays.asList(0, 1,provinceId));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!province.isEmpty()) {
			for (Tuple t : province) {
				Map<String, Object> mapProvince = new HashMap<>();
				mapProvince.put("code", t.get("code"));
				mapProvince.put("nameNp", t.get("nameNp"));
				mapProvince.put("nameEn", t.get("nameEn"));
				mapProvince.put("districtId", t.get("districtId"));
				list.add(mapProvince);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
	public ResponseEntity<Map<String, Object>> getDistrict() {
		String sql = "select districtId,code,nameNp,nameEn from adminstr.adminDistrict where disabled=? and approved=?  ";
		List<Tuple> district = db.getResultList(sql, Arrays.asList(0, 1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!district.isEmpty()) {
			for (Tuple t : district) {
				Map<String, Object> mapDistrict = new HashMap<>();
				mapDistrict.put("code", t.get("code"));
				mapDistrict.put("nameNp", t.get("nameNp"));
				mapDistrict.put("nameEn", t.get("nameEn"));
				mapDistrict.put("districtId", t.get("districtId"));
				list.add(mapDistrict);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	

}

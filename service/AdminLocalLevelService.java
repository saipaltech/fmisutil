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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AdminLocalLevelService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

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
			List<String> searchbles = AdminLocalLevel.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE N'%" + db.esc(request("search")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		String sql = "select (Select vcid ,code,provinceid,districtid,lgType,nameNp,nameEn,numberOfWard,population,area,istsa,disabled,approved from  adminstr.adminLocalLevel where disabled=? and approved=? "
				+ condition + " order by code ASC offset " + ((page - 1) * perPage) + " rows fetch next " + perPage
				+ " rows only for json auto) as rows";

		Map<String, Object> result = new HashMap<>();
		List<Tuple> rsTuple = db.getResultList(sql,Arrays.asList(0,1));

		sql = "select count(vcid) from adminstr.adminLocalLevel where 1=1 " + condition;
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

		String sql = "";

		AdminLocalLevel model = new AdminLocalLevel();
		model.loadData(document);

		sql = "INSERT INTO adminstr.adminLocalLevel (vcid,adminLevel,code,sourceCode,provinceId,districtId,lgType,nameNp,nameEn,"
				+ "numberOfWard,population,area,istsa, approved,disabled,enterby,enterydate) VALUES (dbo.newidint(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,GETDATE())";

		System.out.println(sql);
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(model.adminLevel,model.code, model.sourceCode, model.provinceId, model.districtId,
						model.lgType, model.nameNp, model.nameEn, model.numberOfWard, model.population, model.area,
						model.istsa, model.approved, model.disabled, auth.getUserId()));
		System.out.println(rowEffect.getMessage());
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}

	public void show(String vcid) {

	}

	public ResponseEntity<Map<String, Object>> edit(String vcid) {
		String sql = "select vcid ,adminLevel,code,sourceCode,provinceId,districtId,lgType,nameNp,nameEn,numberOfWard,population,area,istsa,disabled,approved from  adminstr.adminLocalLevel where vcid = ? for json auto";
		Tuple result = db.getSingleResult(sql, Arrays.asList(vcid));
		if (result == null) {
			return Messenger.getMessenger().setMessage("Invalid Request").error();
		} else {
			return Messenger.getMessenger().setData(result.get(0)).success();
		}
	}

	public ResponseEntity<Map<String, Object>> update(String vcid) {
		DbResponse rowEffect;
		AdminLocalLevel model = new AdminLocalLevel();
		model.loadData(document);
		if (shouldUpdatePartially()) {

			String sql = "UPDATE adminstr.adminLocalLevel set sourceCode=?,nameNp=?, approved=?,disabled=?  where vcid=?";

			rowEffect = db.execute(sql,
					Arrays.asList(model.sourceCode, model.nameNp, model.approved, model.disabled, vcid));

		} else {
			String sql = "UPDATE adminstr.adminLocalLevel set  code=?,adminLevel=?,sourceCode=?,provinceId=?,districtId=?,nameNp=?,nameEn=?,numberOfWard=?,population=?,area=?,istsa=?, approved=?,disabled=?  where vcid=?";
			rowEffect = db.execute(sql,
					Arrays.asList(model.code, model.adminLevel,model.sourceCode, model.provinceId, model.districtId, model.nameNp,
							model.nameEn, model.numberOfWard, model.population, model.area, model.istsa, model.approved,
							model.disabled, vcid));

		}

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String vcid) {
		if (!isBeingUsed("adminstr.adminLocalLevel", vcid)) {
			String sql = "delete from adminstr.adminLocalLevel where vcid =? ";
			DbResponse rowEffect = db.execute(sql, Arrays.asList(vcid));
			if (rowEffect.getErrorNumber() == 0) {
				return Messenger.getMessenger().success();

			} else {
				return Messenger.getMessenger().error();
			}
		} else {
			return Messenger.getMessenger().setMessage("Deletion not Allowed").error();
		}

	}
	public ResponseEntity<Map<String, Object>> getLocalLevel() {
		System.out.println("we r in serciv");
		String sql = "select vcid,code,nameNp,nameEn from adminstr.adminLocalLevel";
		List<Tuple> localLevel = db.getResultList(sql);

		List<Map<String, Object>> list = new ArrayList<>();
		if (!localLevel.isEmpty()) {
			for (Tuple t : localLevel) {
				Map<String, Object> mapLocalLevel = new HashMap<>();
				mapLocalLevel.put("code", t.get("code"));
				mapLocalLevel.put("nameNp", t.get("nameNp"));
				mapLocalLevel.put("nameEn", t.get("nameEn"));
				mapLocalLevel.put("vcid", t.get("vcid"));
				list.add(mapLocalLevel);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getLocalLevelByDistrict(String dId) {
		String sql = "select vcid,code,nameNp,nameEn from adminstr.adminLocalLevel where approved=? and disabled=? and districtId=?";
		List<Tuple> localLevel = db.getResultList(sql,Arrays.asList(1,0,dId));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!localLevel.isEmpty()) {
			for (Tuple t : localLevel) {
				Map<String, Object> mapLocalLevel = new HashMap<>();
				mapLocalLevel.put("code", t.get("code"));
				mapLocalLevel.put("nameNp", t.get("nameNp"));
				mapLocalLevel.put("nameEn", t.get("nameEn"));
				mapLocalLevel.put("vcid", t.get("vcid"));
				list.add(mapLocalLevel);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

}

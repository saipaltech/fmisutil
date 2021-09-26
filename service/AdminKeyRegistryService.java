package org.sfmis.adminstr.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.sfmis.adminstr.auth.Authenticated;
import org.sfmis.adminstr.model.AdminFederal;
import org.sfmis.adminstr.model.AdminKeyRegistry;
import org.sfmis.adminstr.util.DB;
import org.sfmis.adminstr.util.DbResponse;
import org.sfmis.adminstr.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
public class AdminKeyRegistryService extends AutoService{

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;
	
	public ResponseEntity<Map<String, Object>> index() {
		int perPage = (request("rows") == null | (request("rows")).isBlank()) ? 10 : Integer.parseInt(request("rows"));
		int page = (request("page") == null | (request("page")).isBlank()) ? 1 : Integer.parseInt(request("page"));
		if (perPage > 100) {
			perPage = 100;
		}
		String sql = "select (select id,tableName,fieldName,serviceName,serviceTable,primaryKey from  adminstr.keyRegistry  order by serviceName ASC offset "
				+ ((page - 1) * perPage) + " rows fetch next " + perPage + " rows only for json auto) as rows";
		System.out.println(sql);
		Map<String, Object> result = new HashMap<>();
		List<Tuple> rsTuple = db.getResultList(sql);
		
		sql = "select count(id) from adminstr.keyRegistry";
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

		
		AdminKeyRegistry model = new AdminKeyRegistry();
	model.loadData(document);
	String sql = "INSERT INTO adminstr.keyRegistry (tableName,fieldName,serviceName,serviceTable,primaryKey,enterBy) VALUES (?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.tableName,model.fieldName,model.serviceName,model.serviceTable,model.primaryKey,auth.getUserId()));
	if (rowEffect.getErrorNumber() == 0) {
		return Messenger.getMessenger().success();
		
	} else {
		return Messenger.getMessenger().error();
	}

	}
	
	public ResponseEntity<Map<String, Object>> check() {
		String tableName = request("tableName");
		String primaryKey = request("primaryKey");
		if (isBeingUsed(tableName, primaryKey)) {
			return Messenger.getMessenger().setMessage("Cannot be deleted.").error();
		} else {
			return Messenger.getMessenger().setMessage("Can be deleted").success();
		}
	}
	
	public ResponseEntity<Map<String, Object>> destroy(String primaryKey) {
	String sql = "delete from adminstr.keyRegistry where primaryKey  = ? and tableName=? and id = (select min(id) from adminstr.keyRegistry where primaryKey  = ? and tableName=?) ";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(primaryKey));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}

	}
}


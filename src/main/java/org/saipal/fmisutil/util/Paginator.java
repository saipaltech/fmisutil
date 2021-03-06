package org.saipal.fmisutil.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.fmisutil.ApplicationContextProvider;

public class Paginator {
	private String selections;
	private String body;
	private String countFiled="";
	private String groupby="";

	private int perPage = 10;
	private int pageNo = 1;
	private int maxPerPage = 100;

	private List<Object> params;
	private String orderField = "";

	public Paginator select(String selections) {
		this.selections = selections;
		return this;
	}
	public Paginator setCountField(String countField) {
		this.countFiled = countField;
		return this;
	}
	public Paginator setGroupBy(String groupby) {
		this.groupby = groupby;
		return this;
	}

	public Paginator sqlBody(String body) {
		this.body = body;
		return this;
	}

	public Paginator setOrderBy(String orderField) {
		this.orderField = orderField;
		return this;
	}

	public Paginator setPerPage(String perPage) {
		try {
			this.perPage = Integer.parseInt(perPage);
		} catch (NumberFormatException e) {
			// do nothing
		}
		return this;
	}

	public Paginator setPageNo(String pageNo) {
		try {
			this.pageNo = Integer.parseInt(pageNo);
		} catch (NumberFormatException e) {
			// do nothing
		}
		return this;
	}

	public Paginator setMaxPerPage(int maxPerPage) {
		this.maxPerPage = maxPerPage;
		return this;
	}

	public Paginator setQueryParms(List<Object> params) {
		this.params = params;
		return this;
	}

	public Map<String, Object> paginate() {
		String countSql = "";
		String paginateSql = "";
		Map<String, Object> result = new HashMap<>();
		List<Map<String, Object>> rows = new ArrayList<>();

		DB db = ApplicationContextProvider.getBean(DB.class);
		// total sql
		String sel = "*";
		if(!countFiled.isBlank()) {
			sel = this.countFiled;
		}
		if (groupby.isBlank()) {
			countSql = "select count("+sel+") as total " + body;
		} else {
			countSql = "select count("+sel+") as total from (select "+selections +" "+ body + " "+groupby+") as a";
		}
		Tuple totalResp;
		// add perpage & limit
		if (this.perPage > this.maxPerPage) {
			this.perPage = maxPerPage;
		}
		int offset = ((pageNo - 1) * perPage);
		paginateSql = "select " + selections + " " + body;
		
		if(!groupby.isBlank()) {
			paginateSql +=" "+ groupby;
		}
		
		if (!orderField.isBlank()) {
			paginateSql += " order by " + orderField;
		}
		paginateSql += " limit " + offset + "," + perPage;
		if (params != null) {
			totalResp = db.getSingleResult(countSql, params);
		} else {
			totalResp = db.getSingleResult(countSql);
		}
		// check if rcord exists or not
		BigInteger totalRecords = (BigInteger) totalResp.get(0);
		if (totalRecords.compareTo(BigInteger.ZERO) == 1) {
			if (params != null) {
				rows = db.getResultListMap(paginateSql, params);
			} else {
				rows = db.getResultListMap(paginateSql);
			}
		}
		result.put("data", rows);
		result.put("currentPage", pageNo);
		result.put("perPage", perPage);
		result.put("total", totalRecords);
		return result;
	}

}

package org.saipal.fmisutil.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Tuple;

import org.saipal.fmisutil.ApplicationContextProvider;
import org.saipal.fmisutil.parser.Element;
import org.saipal.fmisutil.parser.RequestParser;
import org.saipal.fmisutil.util.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AutoService {

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected DB db;

	@Autowired
	public RequestParser document;

	public Element $(String elementId) {
		return document.getElementById(elementId);
	}

	public String getSyncId() {
		return document.getSyncId();
	}

	public RequestParser document() {
		return ApplicationContextProvider.getBean(RequestParser.class);
	}

	/**
	 * converts the given number into readable string format with precision
	 * 
	 * @param num       number to be converted
	 * @param precision precision required in output string
	 * @return readble string format with given precision
	 */
	public String displayNum(double num, int precision) {
		return String.format("%." + precision + "f", num);
	}

	/**
	 * converts the given number into readable string format with default precision
	 * 2
	 * 
	 * @param num number to be converted
	 * @return readble string format with given precision
	 */
	public String displayNum(double num) {
		return displayNum(num, 2);
	}

	public Element dbid(String id) {
		return document.getElementById(id);
	}

	public String request(String param) {
		return document.getElementById(param).value;
	}

	public boolean elemEmptyOrZero(String elemId) {
		return (document.getElementById(elemId).value.isEmpty() || document.getElementById(elemId).value.equals("0"));

	}

	public boolean isEmptyOrZero(String val) {
		return val.trim().isBlank() || val.trim().equals("0");
	}

	public boolean elemEmptyOrZeroAlert(String elemId, String msg) {
		if (document.getElementById(elemId).value.isBlank() || document.getElementById(elemId).value.equals("0")) {
			document.alert(msg);
			document.getElementById(elemId).focus();
			return true;
		}
		return false;
	}

	public boolean elemEmptyAlert(String elemId, String msg) {
		if (document.getElementById(elemId).value.isBlank()) {
			document.alert(msg);
			document.getElementById(elemId).focus();
			return true;
		}
		return false;

	}

	public double val(String p) {
		double ret = 0;
		try {
			ret = Double.parseDouble(p.trim());
		} catch (NumberFormatException ex) {
			ret = Double.parseDouble(db.getSingleResult("select dbo.val(N'" + p + "')").get(0) + "");
		}
		return ret;
	}

	public String trim(String str) {
		return str.trim();
	}

	public String replace(String str, String oldStr, String newStr) {
		return str.replace(oldStr, newStr);
	}

	public double ccur(Object num) {
		try {
			if (num instanceof Double) {
				return (double) num;
			} else if (num instanceof Integer) {
				return (Integer) num;
			} else {
				return Double.parseDouble(num + "");
			}
		} catch (NumberFormatException ex) {
			document.js(num + " is not number");
			ex.printStackTrace();
		}
		return 0.0;
	}

	public long cint(Object num) {
		try {
			if (num instanceof Long) {
				return (Long) num;
			} else {
				return (long) Double.parseDouble(num + "");
			}
		} catch (NumberFormatException ex) {
			document.js(num + " is not number");
			ex.printStackTrace();
		}
		return 0;
	}

	public boolean isInt(Object obj) {
		if (obj instanceof String) {
			try {
				Long.parseLong(obj + "");
				return true;
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public boolean isNumber(Object obj) {
		if (obj instanceof Integer || obj instanceof Double || obj instanceof Float || obj instanceof Long) {
			return true;
		}
		if (obj instanceof String) {
			try {
				Double.parseDouble(obj + "");
				return true;
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public boolean isEmail(String email) {
		String pattern = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
		return email.matches(pattern);
	}

	public long newidint() {
		return Long.parseLong(db.newIdInt());
	}

	public String[] split(String str1, String str2) {
		return str1.split(str2);
	}

	public String value(Object obj) {
		return obj == null ? "" : obj + "";
	}

	public boolean isBeingUsed(String table, String pkey) {
		String sql = "select id,tableName,serviceName,primaryKey from  dbo.keyRegistry where tableName=? and primaryKey = ?  for json auto";
		java.util.List<Tuple> result = db.getResultList(sql, Arrays.asList(table, pkey));
		if (result != null && result.size() > 0) {
			return true;
		}
		return false;
	}

	public boolean shouldUpdatePartially() {
		return false;
	}

	public List<Tuple> getSelections(String table, String fields, String order) {
		String sql = "select " + fields + " from " + table;
		if (order != null) {
			sql += " order by " + order;
		}
		List<Tuple> list = db.getResultList(sql);
		return list;

	}

	public List<Tuple> getSelections(String table, String fields, String where, String order) {
		String sql = "select " + fields + " from " + table;
		if (where != null) {
			sql += " where " + where;
		}
		if (order != null) {
			sql += " order by " + order;
		}
		List<Tuple> list = db.getResultList(sql);
		return list;
	}

	public List<Tuple> getSelections(String table, String fields, String where, String groupby, String having,
			String order) {
		String sql = "select " + fields + " from " + table;
		if (where != null) {
			sql += " where " + where;
		}
		if (groupby != null) {
			sql += " group by " + groupby;
		}
		if (having != null) {
			sql += " having " + having;
		}
		if (order != null) {
			sql += " order by " + order;
		}
		List<Tuple> list = db.getResultList(sql);
		return list;
	}
}

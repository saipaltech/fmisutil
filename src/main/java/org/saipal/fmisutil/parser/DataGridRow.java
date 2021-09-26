package org.saipal.fmisutil.parser;

import java.util.ArrayList;
import java.util.List;

public class DataGridRow {
	List<String> values;
	List<String> keys;

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public List<String> getKeys() {
		return keys;
	}

	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	public DataGridRow() {
		values = new ArrayList<>();
		keys = new ArrayList<>();
	}

	public void addValue(String key, String value) {
		values.add(value);
		keys.add(key);
	}

	public String getValue(String key) {
		return values.get(keys.indexOf(key));
	}

	public String getValue(int colIndex) {
		return values.get(colIndex);
	}
}

package org.saipal.fmisutil.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.fmisutil.ApplicationContextProvider;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class RequestParser {

	org.slf4j.Logger log = LoggerFactory.getLogger(RequestParser.class);

	public void setRequestParser(HttpServletRequest request) {
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		if (MediaType.APPLICATION_JSON.toString().equals(request.getContentType())) {
			parseJsonRequest(request, ds);
			parseQueryParams(request, ds);
		} else {
			parseFormData(request, ds);
		}
	}

	private void parseQueryParams(HttpServletRequest request, DocumentStore ds) {
		if (request.getQueryString() != null) {
			String data = request.getQueryString();
			String[] fragments = data.split("&");
			for (String it : fragments) {
				String[] dt = it.split("=");
				Element element = new Element();
				element.setId(dt[0]);
				element.setName(dt[0]);
				element.setRp(this);
				ds.elements.put(dt[0], element);
				if (dt.length == 2) {
					element.setValueAuto(dt[1]);
				} else {
					element.setValueAuto("");
				}
			}
		}
	}

	private void parseJsonRequest(HttpServletRequest request, DocumentStore ds) {
		Map<String, Integer> gridMap = new HashMap<String, Integer>();
		try {
			byte[] reqByteArray = request.getInputStream().readAllBytes();
			if (reqByteArray.length > 0) {
				String reqBody = new String(reqByteArray, 0, reqByteArray.length, request.getCharacterEncoding());
				// conform the request is json
				if (reqBody.trim().startsWith("{")) {
					try {
						JSONObject jsonBody = new JSONObject(reqBody);
						int objLen = jsonBody.names().length();
						for (int i = 0; i < objLen; i++) {
							String k = jsonBody.names().getString(i);
							Object o = jsonBody.get(k);
							String v = "";
							if (o instanceof JSONArray) {
								JSONArray arr = jsonBody.getJSONArray(k);
								if (arr.length() == 1) {
									v = arr.getString(0);
								} else if (arr.length() > 1) {
									v = arr.toString();
								}
							} else if (o instanceof JSONObject) {
								if (((JSONObject) o).length() == 1) {
									v = (String) jsonBody.getJSONObject(k)
											.get(jsonBody.getJSONObject(k).names().getString(0));
								}
								v = o.toString();
							} else {
								//System.out.println(o.toString());
								v = jsonBody.getString(k);
								
							}
							Element element = new Element();
							element.setId(k);
							element.setName(k);
							element.setValueAuto(v);
							element.setRp(this);
							ds.elements.put(k, element);
							// check if grid exists
							if (k.contains("_value") || k.contains("_rowid") || k.contains("_colname")
									|| k.contains("_datatype")) {
								String gridName = k.split("_")[0];
								if (gridMap.containsKey(gridName)) {
									gridMap.put(gridName, gridMap.get(gridName) + 1);
								} else {
									gridMap.put(gridName, 1);
								}
							}
						}
						parseGrids(gridMap, ds);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseFormData(HttpServletRequest request, DocumentStore ds) {
		Map<String, Integer> gridMap = new HashMap<String, Integer>();
		for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			String k = entry.getKey();
			String[] v = entry.getValue();
			Element element = new Element();
			element.setId(k);
			element.setName(k);
			if (v.length == 1) {
				element.setValueAuto(v[0]);
			} else {
				String s = Arrays.toString(v);
				s = s.substring(1, s.length() - 1);
				element.setValueAuto(s);
			}

			element.setRp(this);
			ds.elements.put(k, element);
			// check if grid exists
			if (k.contains("_value") || k.contains("_rowid") || k.contains("_colname") || k.contains("_datatype")) {
				String gridName = k.split("_")[0];
				if (gridMap.containsKey(gridName)) {
					gridMap.put(gridName, gridMap.get(gridName) + 1);
				} else {
					gridMap.put(gridName, 1);
				}
			}
		}
		// ds.addElements(elements);

		// parse all the grids
		parseGrids(gridMap, ds);
	}

	private void parseGrids(Map<String, Integer> gridMap, DocumentStore ds) {
		if (gridMap.size() > 0) {
			for (Entry<String, Integer> ent : gridMap.entrySet()) {
				if (ent.getValue() == 4) {
					String gid = ent.getKey();
					String cols = getElementById(gid + "_colname").value;
					String dtypes = getElementById(gid + "_datatype").value;
					DataGrid dg = new DataGrid(gid, cols, dtypes);
					dg.setRp(this);
					ds.grids.put(gid, dg);
				}
			}
			// ds.addGrids(grids);
		}
	}

	public Element getElementById(String id) {
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		// Element el = ds.getElement(id);
		Element el = ds.elements.get(id);
		if (el == null) {
			el = new Element();
			el.setId(id);
			el.setName(id);
			el.setRp(this);
			// ds.addElement(el);
			ds.elements.put(id, el);
		}
		return el;
	}

	public DataGrid getGrid(String id) {
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		return ds.grids.get(id);
		// return ds.getGrid(id);
	}

	public String getSyncId() {
		return "id";
	}

//	public void setSyncId(String syncId) {
//		this.syncId = syncId;
//	}

	public void alert(String msg) {
		opToJs("alert", msg);
	}

	public void js(String jscript) {
		opToJs("js", jscript);
	}

	// Added new function Begin
	public void setNestedExecution(boolean p) {
		opToJs("nested", p);
	}

	public boolean getNestedExecution() {
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		return ds.nestedExecution;
	}

	public void setRecursiveExecution(boolean p) {
		opToJs("recursive", p);
	}

	public boolean getRecursiveExecution() {
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		return ds.recursiveExecution;
	}

	// Added new function End
	public void addGridRow(String gridName, String value, String index) {
		opToJs("addGridRow", gridName + "::" + value + "::" + index);
	}

	public void additem(String elementId, Object text, Object value) {
		opToJs(elementId, "addItem", text + "::" + value);
	}

	public void addoptiongroup(String elementId, Object groupId, Object groupName) {
		opToJs(elementId, "addoptiongroup", groupId + "::" + groupName);
	}

	public void addgroupitem(String elementId, Object text, Object value, Object groupid) {
		opToJs(elementId, "addgroupitem", text + "::" + value + "::" + groupid);
	}

	public void removeall(String elementId) {
		opToJs(elementId, "removeAll", null);
	}

	public void resetItem(String elementId) {
		removeall(elementId);
		additem(elementId, "............", "0");
	}

	public String getJs() {
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		return ds.jsLog.toString();
	}

	protected void opToJs(String operation, boolean parms) {
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		if ("nested".equals(operation)) {
			ds.nestedExecution = parms;

		} else if ("recursive".equals(operation)) {
			ds.recursiveExecution = parms;
		}
	}

	protected void opToJs(String elementid, String operation, String params) {
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		//String opJs = "";

		/*
		 * if(jsa.length() == 0) { try { jsa.put(elementlist);
		 * elementlist.put("elements", elemarray); } catch (JSONException e) {
		 * e.printStackTrace(); } }
		 */
		if ("addItem".equals(operation)) {
			if (params != null) {
				String[] pms = params.split("::");
				if (pms.length > 1) {
					String js = "additem('" + elementid + "','" + pms[0].replace("'", "\\'") + "','" + pms[1] + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], pms[1]);
				} else {
					String js = "additem('" + elementid + "','" + pms[0].replace("'", "\\'") + "','" + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], "");
				}
			}
		} else if ("removeItem".equals(operation)) {
			if (params != null) {
				String js = "removeitem('" + elementid + "','" + params + "');\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "removeitem", params);
			}
		} else if ("src".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').src = '" + params + "';\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "src", params);
			}
		} else if ("removeAll".equals(operation)) {
			String js = "removeall('" + elementid + "');\n";
			ds.jsLog.append(js);
			// insetToDb(js);
			setJSON(elementid, "removeall", "true");
		} else if ("setDisabled".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').disabled = " + params + ";\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "disabled", params);

			}
		} else if ("setValue".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').value = '" + params.replace("'", "\\'")
						+ "';\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "value", params);

			}
		} else if ("focus".equals(operation)) {
			String js = "document.getElementById('" + elementid + "').focus();\n";
			ds.jsLog.append(js);
			// insetToDb(js);
		} else if ("submit".equals(operation)) {
			String js = "document.getElementById('" + elementid + "').submit();\n";
			ds.jsLog.append(js);
			// insetToDb(js);
		} else if ("reset".equals(operation)) {
			String js = "document.getElementById('" + elementid + "').reset();\n";
			ds.jsLog.append(js);
			// insetToDb(js);
		} else if ("setClassName".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').classname = '" + params + "';\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "classname", params);
			}
		} else if ("setChecked".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').checked = " + params + ";\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "checked", params);

			}
		} else if ("setInnerHTML".equals(operation)) {
			if (params != null) {
				String js = "document.getElementById('" + elementid + "').innerHTML = '"
						+ params.replace("\n", "").replace("\r", "") + "';\n";
				ds.jsLog.append(js);
				// insetToDb(js);
				setJSON(elementid, "innerHTML", params);

			}
		} else if ("alert".equals(operation)) {
			if (params != null) {
				String js = "alert('" + params.replace("'", "\\'").replaceAll("\n", " ") + "');\n";
				ds.jsLog.append(js);
				// insetToDb(js);
			}
		} else if ("js".equals(operation)) {
			if (params != null) {
				String js = params + "\n";
				ds.jsLog.append(js);
				// insetToDb(js);
			}
		} else if ("addoptiongroup".equals(operation)) {
			if (params != null) {
				String[] pms = params.split("::");
				if (pms.length > 1) {
					String js = "addoptiongroup('" + elementid + "','" + pms[0] + "','" + pms[1] + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], pms[1]);
				} else {
					String js = "addoptiongroup('" + elementid + "','" + pms[0] + "','" + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], "");
				}

			}
		} else if ("addgroupitem".equals(operation)) {
			if (params != null) {
				String[] pms = params.split("::");
				if (pms.length > 2) {
					String js = "additem('" + elementid + "','" + pms[0] + "','" + pms[1] + "','','" + pms[2] + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					// setJSON(elementid, operation, pms[0], pms[1], pms[2]);
				} else {
					String js = "additem('" + elementid + "','" + pms[0] + "','" + pms[1] + "','" + "');\n";
					ds.jsLog.append(js);
					// insetToDb(js);
					setJSON(elementid, operation, pms[0], pms[1], "");
				}
			}
		} else if ("addGridRow".equals(operation)) {
			String[] pms = params.split("::");
			if (pms.length == 3) {
				String js = pms[0] + ".addrow(\"" + pms[1] + "\"," + pms[2] + ");";
				ds.jsLog.append(js);
				// insetToDb(js);
			}
		}
	}

	protected void opToJs(String operation, String params) {
		opToJs("", operation, params);
	}

	// JSON Begin
	protected void setJSON(String elementid, String operation, String params) {
		// json
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		if (ds.json.has(elementid)) {
			try {
				JSONObject el = ds.json.getJSONObject(elementid);
				el.put(operation, params);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			JSONObject el = new JSONObject();
			try {
				el.put(operation, params);
				ds.json.put(elementid, el);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// end json
	}

	protected void setJSON(String elementid, String operation, String parmtext, String parmvalue) {
		// json
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		if ("addItem".equals(operation)) {
			if (ds.json.has(elementid)) {
				try {
					JSONObject el = ds.json.getJSONObject(elementid);
					if (el.has("listitem")) {
						JSONObject listitems = el.getJSONObject("listitem");
						listitems.put(parmtext, parmvalue);
					} else {
						JSONObject listitems = new JSONObject();
						listitems.put(parmtext, parmvalue);
						el.put("listitem", listitems);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				JSONObject el = new JSONObject();
				JSONObject listitems = new JSONObject();
				try {
					listitems.put(parmtext, parmvalue);
					el.put("listitem", listitems);
					ds.json.put(elementid, el);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		if ("addoptiongroup".equals(operation)) {
			if (ds.json.has(elementid)) {
				try {
					JSONObject el = ds.json.getJSONObject(elementid);
					if (el.has("listitem")) {
						JSONObject groups = el.getJSONObject("listitem");
						JSONObject groupitems = new JSONObject();
						groupitems.put("name", parmvalue);
						groupitems.put("items", new JSONObject());
						groups.put(parmtext, groupitems);
					} else {
						JSONObject groups = new JSONObject();
						JSONObject groupitems = new JSONObject();
						groupitems.put("name", parmvalue);
						groupitems.put("items", new JSONObject());
						groups.put(parmtext, groupitems);
						el.put("listitem", groups);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				JSONObject el = new JSONObject();
				JSONObject groups = new JSONObject();
				try {
					JSONObject groupitems = new JSONObject();
					groupitems.put("name", parmvalue);
					groupitems.put("items", new JSONObject());
					groups.put(parmtext, groupitems);
					el.put("listitem", groups);
					ds.json.put(elementid, el);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		// end json
	}
	// JSON End

	protected void setJSON(String elementid, String operation, String parmtext, String parmvalue, String groupid) {
		// json
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		if ("addgroupitem".equals(operation)) {
			if (ds.json.has(elementid)) {
				try {
					JSONObject el = ds.json.getJSONObject(elementid);
					if (el.has("listitem")) {
						JSONObject listitems = el.getJSONObject("listitem");
						JSONObject group;
						if (groupid.equals("")) {
							group = listitems.getJSONObject(listitems.keys().next() + "");
						} else {
							group = listitems.getJSONObject(groupid);
						}
						JSONObject items = group.getJSONObject("items");
						items.put(parmtext, parmvalue);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		// end json
	}

	public String getJSON() {
		DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
		return ds.json.toString();
	}

//	public void clearData() {
//		// DocumentStore ds = ApplicationContextProvider.getBean(DocumentStore.class);
//		// ds.cleanAll();
//	}
}
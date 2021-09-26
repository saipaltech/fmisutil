package org.saipal.fmisutil.parser;

import java.util.HashMap;
import java.util.Map;
//import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class DocumentStore {
	Map<String, Element> elements = new HashMap<>();
	Map<String, DataGrid> grids = new HashMap<>();

	protected JSONObject json = new JSONObject();

	protected StringBuilder jsLog = new StringBuilder();
	protected boolean nestedExecution=true;
	protected boolean recursiveExecution=true;
}

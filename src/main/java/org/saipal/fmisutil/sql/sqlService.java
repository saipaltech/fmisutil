package org.saipal.fmisutil.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.RecordSet;
import org.springframework.stereotype.Component;


@Component
public class sqlService extends AutoService {

	public Map<String,Object> open() {
		String sql=document.getElementById("sql").getValue();
		String pargs=document.getElementById("args").getValue("");
		RecordSet rs=new RecordSet();
		if(!pargs.isBlank()) {
			
			if(!pargs.startsWith("[")) {
				pargs="["+pargs+"]";
			}
			List<String> args=new ArrayList<String>();
			JSONArray a=null;
			try {
				a=new JSONArray(pargs);
				for(int i=0;i<a.length();i++) {
					args.add(a.getString(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			rs.open(sql, args,db.getConnection());
		}
		else {
			rs.open(sql,db.getConnection());
		}

		return rs.toREST();
	}
	
}

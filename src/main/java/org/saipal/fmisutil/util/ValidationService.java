package org.saipal.fmisutil.util;



import java.util.HashMap;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;




@Component
public class ValidationService {

	@Autowired
	RequestParser doc;
	
	public Validator validate(Map<String,String> rules) {
		Map<String,Object> data = new HashMap<>();
		for(String field: rules.keySet()) {
			data.put(field,doc.getElementById(field).value);
		}
		// data & rules are ready
		Validator vld = new Validator();
		vld.validate(data, rules);
		return vld;
	}
	
	public Validator validate(Map<String,String> rules, Map<String,String> overrides) {
		Map<String,Object> data = new HashMap<>();
		for(String field: rules.keySet()) {
			data.put(field,doc.getElementById(field).value);
		}
		// data & rules are ready
		Validator vld = new Validator();
		vld.validate(data, rules,overrides);
		return vld;
	}
}

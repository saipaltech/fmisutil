package org.saipal.fmisutil.sql;

import org.saipal.fmisutil.sql.sqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;;
@RestController
public class sqlController {
	@Autowired
	private sqlService record;
	@PostMapping("/openSQL")
	public Map<String,Object> openSQL() {
		return record.open();
	}
}

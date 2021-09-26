package org.saipal.fmisutil.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class Authrepo {
	public String userId;
	public String orgId;
	public String appId;
	public String token;
	public String adminId;
	public Map<String,Object> extraInfo = new HashMap<>();
}

package org.saipal.fmisutil.auth;


import org.saipal.fmisutil.ApplicationContextProvider;
import org.saipal.fmisutil.util.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Authenticated {
	
	@Autowired
	DB db;

	public Authrepo getAuthRequest() {
		return ApplicationContextProvider.getBean(Authrepo.class);
	}
	
	public String getToken() {
		return getAuthRequest().token;
	}

	public void setToken(String token) {
		getAuthRequest().token = token;
	}
	
	public String getAdminId() {
		return getAuthRequest().adminId;
	}

	public void setAdminId(String adminId) {
		getAuthRequest().adminId = adminId;
	}

	public String getUserId() {
		return getAuthRequest().userId;
	}

	public void setUserId(String userId) {
		getAuthRequest().userId = userId;
	}

	public String getOrgId() {
		return getAuthRequest().orgId;
	}

	public void setOrgId(String userId) {
		getAuthRequest().orgId = userId;
	}

	public void setAppId(String appId) {
		getAuthRequest().appId = appId;
	}

	public String getAppId() {
		return getAuthRequest().appId;
	}

	public void setExtraInfo(String key, Object value) {
		getAuthRequest().extraInfo.put(key, value);
	}

	public Object getExtraInfo(String key) {
		return getAuthRequest().extraInfo.get(key);
	}
	public void setLang(String value) {
		getAuthRequest().extraInfo.put("lang", value);
	}

	public String getLang() {
		Object lang = getAuthRequest().extraInfo.get("lang");
		return (lang==null)?"Np":lang+"";
	}
}

package org.saipal.fmisutil.auth;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.fmisutil.ApplicationContextProvider;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.LangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Authenticated {
	
	@Autowired
	DB db;

	public String getToken() {
		return getAuthRequest().token;
	}

	public void setToken(String token) {
		getAuthRequest().token = token;
	}
	public String getJti() {
		return getAuthRequest().jti;
	}

	public void setJti(String jti) {
		getAuthRequest().jti = jti;
	}
	
	public Authrepo getAuthRequest() {
		return ApplicationContextProvider.getBean(Authrepo.class);
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

	public String getUserTypeId() {
		String sql = "select usertype from hr_user_info where uid=? and projectid=? and userstatus=1";
		Tuple tpl = db.getSingleResult(sql, Arrays.asList(getUserId(), getAppId()));
		if (tpl != null) {
			return tpl.get("usertype") + "";
		}
		return "";
	}

	public void setLang(String lng) {
		setUserstate("language", lng);
	}

	public String getLang() {
		String lang = getUserState("language");
		if (lang == null) {
			return "Np";
		}
		return lang;
	}

	public void setUserstate(String key, String value) {
		String sql = "delete from user_state where userid=? and state_key=? and sessionid=?";
		db.execute(sql, Arrays.asList(getUserId(), key,getJti(),getUserId(), key, value,getJti()));
		sql = "insert into user_state(userid,state_key,state_value,sessionid) values (?,?,?,?)";
		db.execute(sql,Arrays.asList(getUserId(),key,value,getJti()));
		setExtraInfo(key, value);
	}
	
	public void setUserstate(Map<String,String> data) {
		List<List<Object>> params = new ArrayList<>();
		List<List<Object>> paramd = new ArrayList<>();
		if(data!=null && data.size()>0) {
			for(String key:data.keySet()) {
				paramd.add(Arrays.asList(getUserId(),key,getJti()));
				params.add(Arrays.asList(getUserId(),key,data.get(key),getJti()));
				//params.add(Arrays.asList(getUserId(),key,getJti(),getUserId(),key,data.get(key),getJti()));
			}
			String sql = "delete from user_state where userid=? and state_key=? and sessionid=?";
			db.executeBulk(sql, paramd);
			sql = " insert into user_state(userid,state_key,state_value,sessionid) values (?,?,?,?)";
			db.executeBulk(sql, params);
			for(String key : data.keySet()) {
				setExtraInfo(key, data.get(key));
			}
		}
	}
	
	public void setUserstateOnLogin(String userid,String jti,Map<String,String> data) {
		List<List<Object>> params = new ArrayList<>();
		List<List<Object>> paramd = new ArrayList<>();
		if(data!=null && data.size()>0) {
			for(String key:data.keySet()) {
				paramd.add(Arrays.asList(userid,key,jti));
				params.add(Arrays.asList(userid,key,data.get(key),jti));
				//params.add(Arrays.asList(getUserId(),key,getJti(),getUserId(),key,data.get(key),getJti()));
			}
			String sql = "delete from user_state where userid=? and state_key=? and sessionid=?";
			db.executeBulk(sql, paramd);
			sql = " insert into user_state(userid,state_key,state_value,sessionid) values (?,?,?,?)";
			db.executeBulk(sql, params);
			for(String key : data.keySet()) {
				setExtraInfo(key, data.get(key));
			}
		}
	}

	public String getUserState(String key) {
		/*String sql = "select state_value from user_state where userid=? and state_key=? and sessionid=?";
		Tuple tp = db.getSingleResult(sql, Arrays.asList(getUserId(), key,getJti()));
		if(tp == null) {
			return null;
		}
		return tp.get("state_value") + "";
		*/
		return getExtraInfo(key)+"";
	}
	
	public void removeUserstate(String key) {
		String sql = "delete from user_state where userid=? and state_key=? and sessionid=?";
		db.execute(sql, Arrays.asList(getUserId(), key,getJti()));
	}

	public void clearAllStates() {
		String sql = "delete from user_state where userid=? and sessionid=?";
		db.execute(sql, Arrays.asList(getUserId(),getJti()));
	}

	public String getAdminLevel() {
		String sql = "select adminlevel from admin_org_str where orgidint=?";
		Tuple tpl = db.getSingleResult(sql, Arrays.asList(getOrgId()));
		if (tpl != null) {
			return tpl.get("adminlevel") + "";
		}
		return "";
	}
	public String getAdminId() {
		String sql = "select adminid from admin_org_str where orgidint=?";
		Tuple tpl = db.getSingleResult(sql, Arrays.asList(getOrgId()));
		if (tpl != null) {
			return tpl.get("adminid") + "";
		}
		return "";
	}

	public void setAllUserStates() {
		String sql = "select state_key, state_value from user_state where userid=? and sessionid=?";
		List<Tuple> tp = db.getResultList(sql, Arrays.asList(getUserId(),getJti()));
		if(tp != null) {
			if(tp.size() > 0 ) {
				for(Tuple t:tp) {
					setExtraInfo(t.get("state_key")+"", t.get("state_value"));
				}
			}
		}
	}
}

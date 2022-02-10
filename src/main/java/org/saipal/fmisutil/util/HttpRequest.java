package org.saipal.fmisutil.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class HttpRequest {
	private Map<String, String> header = new HashMap<>();
	private Map<String, String> params = new HashMap<>();
	private String stringParam = "";

	private void setDefaultHeaders() {
		if (!header.containsKey("Accept")) {
			header.put("Accept", "application/json");
		}
		if (!header.containsKey("Content-Type")) {
			header.put("Content-Type", "application/json");
		}
		if (!header.containsKey("Content-Encoding")) {
			header.put("Content-Encoding", "UTF-8");
		}
		
	}

	public HttpRequest removeHeader(String key) {
		this.header.remove(key);
		return this;
	}

	public HttpRequest setHeader(String key, String value) {
		this.header.put(key, value);
		return this;
	}

	public HttpRequest setParam(String key, String value) {
		this.params.put(key, value);
		return this;
	}
	
	public HttpRequest setParam(String param) {
		this.stringParam = param;
		return this;
	}

	public JSONObject get(String url) {
		setDefaultHeaders();
		if (params.size() > 0) {
			try {
				URIBuilder builder = new URIBuilder(url);
				for (String key : params.keySet()) {
					builder.setParameter(key, params.get(key));
				}
				url = builder.build().toString();
			} catch (Exception e) {
				// do nothing
			}
		}
		HttpGet http = new HttpGet(url);
		if (header.size() > 0) {
			for (String key : header.keySet()) {
				http.setHeader(key, header.get(key));
			}
		}
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			CloseableHttpResponse response = httpclient.execute(http);
//			System.out.println(header);
//			System.out.println(http);
//			System.out.println(params);
			String resp = EntityUtils.toString(response.getEntity());
			JSONObject data = new JSONObject();
			if(resp.startsWith("{")) {
				data.put("data",new JSONObject(resp));
			}else if(resp.startsWith("[")) {
				data.put("data",new JSONArray(resp));
			}else {
				data.put("data", resp);
			}
			data.put("status_code", response.getCode());
			return data;
		} catch (Exception e) {
			HashMap<String, String> res = new HashMap<String, String>();
			res.put("Exception thrown (Local) ", e.getMessage());
			return new JSONObject(res);
		}
	}

	public JSONObject post(String url) {
		setDefaultHeaders();
		HttpPost http = new HttpPost(url);
		if (header.size() > 0) {
			for (String key : header.keySet()) {
				http.setHeader(key, header.get(key));
			}
		}
		if(!stringParam.isBlank()) {
			http.setEntity(new StringEntity(stringParam));
		}else if (params.size() > 0) {
			if (header.get("Content-Type") == "application/json") {
				// System.out.println(new JSONObject(params).toString());
				http.setEntity(new StringEntity((new JSONObject(params)).toString(), ContentType.APPLICATION_JSON));
			} else {
				List<NameValuePair> listOfParams = new ArrayList<>();
				for (String key : params.keySet()) {
					listOfParams.add(new BasicNameValuePair(key, params.get(key)));
				}
				// System.out.println(listOfParams.toString());
				http.setEntity(new UrlEncodedFormEntity(listOfParams));
			}
		}
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			CloseableHttpResponse response = httpclient.execute(http);
			String resp = EntityUtils.toString(response.getEntity()).trim();
			JSONObject data = new JSONObject();
			if(resp.startsWith("{")) {
				data.put("data",new JSONObject(resp));
			}else if(resp.startsWith("[")) {
				data.put("data",new JSONArray(resp));
			}else {
				data.put("data", resp);
			}
			data.put("status_code", response.getCode());
			return data;
		} catch (Exception e) {
			HashMap<String, String> res = new HashMap<String, String>();
			res.put("Exception thrown (Local) ", e.getMessage());
			return new JSONObject(res);
		}
	}

}

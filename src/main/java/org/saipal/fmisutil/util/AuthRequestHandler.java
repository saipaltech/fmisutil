package org.saipal.fmisutil.util;

import org.saipal.fmisutil.auth.Authenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthRequestHandler {

	@Autowired
	RestTemplate rt;

	@Autowired
	Authenticated auth;
	
	private HttpHeaders getHeaders () {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(auth.getToken());
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	} 

	public CommonResponse post(String url, Object data) {
		HttpEntity<Object> requestEntity = new HttpEntity<>(data, getHeaders());
		return rt.postForEntity(url, requestEntity, CommonResponse.class).getBody(); 
	}

	public CommonResponse get(String url) {
		HttpEntity<Object> requestEntity = new HttpEntity<>(null,getHeaders());
		return rt.exchange(url, HttpMethod.GET,requestEntity,CommonResponse.class).getBody();
	}
	
	public CommonResponse put(String url, Object data) {
		HttpEntity<Object> requestEntity = new HttpEntity<>(data, getHeaders());
		ResponseEntity<CommonResponse> resp = rt.exchange(url, HttpMethod.PUT, requestEntity, CommonResponse.class);
		return resp.getBody();
	}
	
	public CommonResponse delete(String url, Object data) {
		HttpEntity<Object> requestEntity = new HttpEntity<>(data, getHeaders());
		ResponseEntity<CommonResponse> resp = rt.exchange(url, HttpMethod.DELETE, requestEntity, CommonResponse.class);
		return resp.getBody();
	}

}

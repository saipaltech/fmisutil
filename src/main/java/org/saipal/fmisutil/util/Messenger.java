package org.saipal.fmisutil.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Messenger {

	private Map<String, Object> info = new HashMap<>();

	public static Messenger getMessenger() {
		return (new Messenger());
	}

	public Messenger setMessage(String message) {
		info.put("message", message);
		return this;
	}
	
	public Messenger setMessage(Map<String, Object> message) {
		info.put("message", message);
		return this;
	}

	public Messenger setData(Object data) {
		info.put("data", data);
		return this;
	}

	public Messenger setStatus(int status) {
		info.put("status", status);
		return this;
	}

	public ResponseEntity<Map<String, Object>> success() {
		setStatus(1);
		if (info.get("message") == null) {
			setMessage("Operation Successful.");
		}
		return ResponseEntity.ok(info);
	}

	public ResponseEntity<Map<String, Object>> error() {
		setStatus(0);
		if (info.get("message") == null) {
			setMessage("Operation Failed.");
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(info);
	}

	private Map<String, Object> info() {
		setStatus(3);
		if (info.get("message") == null) {
			setMessage("Operation Successful.");
		}
		return info;
	}

	private Map<String, Object> warning() {
		setStatus(2);
		if (info.get("message") == null) {
			setMessage("Operation succeeded with warning.");
		}
		return info;
	}

	public ResponseEntity<Map<String, Object>> getMessage() {
		HttpStatus status = HttpStatus.OK;
		if ((int) info.get("status") == 0) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return ResponseEntity.status(status).body(info);
	}
}

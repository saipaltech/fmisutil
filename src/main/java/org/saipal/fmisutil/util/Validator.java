package org.saipal.fmisutil.util;



import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
	private Map<String, Map<String, String>> validationMessages = new HashMap<>();
	
	public Boolean isFailed() {
		if(validationMessages.size() > 0) {
			return true;
		}
		return false;
	}
	
	public Map<String, Map<String, String>> getErrorMessages(){
		return validationMessages;
	}

	public void validate(Map<String, Object> data, Map<String, String> rules) {
		if (rules.size() > 0) {
			for (String field : rules.keySet()) {
				String[] rlArray = parseValidations(rules.get(field));
				if (rlArray != null) {
					for (String rlel : rlArray) {
						if (data != null) {
							String value = (String) data.get(field);
							if ((value == null | value.isBlank()) && rules.get(field).contains("optional")) {
								continue;
							}
							handleValidation(field, data.get(field), rlel.trim());
						}
					}
				}
			}
		}
	}

	public void validate(Map<String, Object> data, Map<String, String> rules, Map<String, String> overrided) {
		if (overrided.size() > 0) {
			for (String e : overrided.keySet()) {
				rules.put(e, overrided.get(e));
			}
		}
		validate(data, rules);
	}

	private String[] parseValidations(String rules) {
		if (!rules.isBlank()) {
			String[] rls = rules.split("\\|");
			return rls;
		}
		return null;
	}

	private void handleValidation(String field, Object value, String rule) {
		if ("required".equals(rule)) {
			handleRequired(field,value, rule);
		}
		if (rule.contains("min:")) {
			handleMin(field, value,  rule);
		}

		if (rule.contains("max:")) {
			handleMax(field,value, rule);
		}
		if ("email".equals(rule)) {
			handleEmail(field, value, rule);
		}

	}

	private void handleMax(String field, Object value, String rule) {
		
	}

	private void handleEmail(String field, Object value, String rule) {
		String val = (String) value;
		final String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(val);
		if (!matcher.matches()) {
			addFailureMessages(field, rule, "Invalid Email Address");
		}
	}

	private void handleMin(String field, Object value, String rule) {
		// TODO Auto-generated method stub

	}

	private void handleRequired(String field, Object value, String rule) {
		String val = (String) value;
		if(val.isBlank()) {
			addFailureMessages(field, rule, "The {} is required.");
		}
	}

	private String getFieldName(String field) {
		String label = field.substring(0, 1).toUpperCase() + field.substring(1);
		return label;
	}

	private void addFailureMessages(String field, String rule, String message) {
		if (validationMessages == null) {
			Map<String, String> msgs = new HashMap<>();
			validationMessages = new HashMap<>();
			validationMessages.put(field, msgs);
		}
		message = message.replace("{}", getFieldName(field));
		if (validationMessages.containsKey(field)) {
			validationMessages.get(field).put(rule, message);
		} else {
			Map<String, String> msgs = new HashMap<>();
			msgs.put(rule, message);
			validationMessages.put(field, msgs);
		}
	}

}

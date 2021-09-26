package org.saipal.fmisutil.parser;


public class Element {
	private String id;
	private String name;
	private boolean disabled;
	public String value;
	private String innerHTML;
	private boolean checked;
	private String className;
	private RequestParser rp;

	public Element() {
		checked = false;
		className = "";
		disabled = false;
		value = "";
		innerHTML = "";
	}

	public void setClassName(String className) {
		rp.opToJs(getId(), "setClassName", className);
		this.className = className;
	}

	public void setChecked(boolean checked) {
		if (checked) {
			rp.opToJs(getId(), "setChecked", Boolean.toString(checked));
		}
		this.checked = checked;
	}

	public boolean isChecked() {
		return checked;
	}

	public String getClassName() {
		return className;
	}

	public RequestParser getRp() {
		return rp;
	}

	public void setRp(RequestParser rp) {
		this.rp = rp;
	}

	public Element addItem(String text, String value) {
		rp.opToJs(getId(), "addItem", text + "::" + value);
		return this;
	}

	public String getInnerHTML() {
		return innerHTML;
	}

	public void setInnerHTML(String innerHTML) {
		rp.opToJs(getId(), "setInnerHTML", innerHTML);
		this.innerHTML = innerHTML;
	}

	public void setSrc(String innerHTML) {
		rp.opToJs(getId(), "src", innerHTML);
	}

	public Element removeItem(String value) {
		rp.opToJs(getId(), "removeItem", value);
		return this;
	}

	public Element removeAll() {
		rp.opToJs(getId(), "removeAll", null);
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDisabled() {
		return disabled;
	}

	protected void setDisabledAuto(boolean disabled) {
		this.disabled = disabled;
	}

	public void setDisabled(boolean disabled) {
		rp.opToJs(getId(), "setDisabled", disabled + "");
		setDisabledAuto(disabled);
	}

	public String getValue() {
		return value;
	}

	public String getValue(String defaultValue) {
		if (value.isBlank())
			return defaultValue;
		else
			return value;
	}

	public void setValue(Object value) {
		rp.opToJs(getId(), "setValue", value + "");
		setValueAuto(value + "");
	}

	// New Function Begin
	public String value() {
		return this.getValue();
	}

	public void value(String value) {
		this.setValue(value);
	}

	public String innerHTML() {
		return this.getInnerHTML();
	}

	public void innerHTML(String html) {
		this.setInnerHTML(html);
	}

	public boolean disabled() {
		return this.disabled;
	}

	public void disabled(boolean d) {
		this.setDisabled(d);
	}

	public String className() {
		return this.getClassName();
	}

	public void className(String cname) {
		this.setClassName(cname);
	}

	public boolean checked() {
		return this.isChecked();
	}

	public void checked(boolean c) {
		this.setChecked(c);
	}

	// New function End
	protected void setValueAuto(String value) {
		this.value = value;
	}

	public String getZeroIfEmpty() {
		return value.isBlank() ? "0" : value;
	}

	public void focus() {
		rp.opToJs(getId(), "focus", null);
	}

	public void submit() {
		rp.opToJs(getId(), "submit", null);
	}

	public void reset() {
		rp.opToJs(getId(), "reset", null);
	}

}

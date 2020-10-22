package org.camunda.bpm.piviz.result;

public class Element {

	private String id;
	
	private ElementStatus status;

	public Element(String id, ElementStatus status) {
		this.id = id;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ElementStatus getStatus() {
		return status;
	}

	public void setStatus(ElementStatus status) {
		this.status = status;
	}
	
}

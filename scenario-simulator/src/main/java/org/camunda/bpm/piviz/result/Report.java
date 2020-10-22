package org.camunda.bpm.piviz.result;

import java.util.LinkedList;
import java.util.List;

public class Report {

	private List<Element> elements = new LinkedList<>();
	
	public List<Element> getElements() {
		return elements;
	}
	
	public void setElements(final List<Element> elements) {
		this.elements = elements;
	}
	
	public Report addElement(final Element element) {
		elements.add(element);
		return this;
	}
}

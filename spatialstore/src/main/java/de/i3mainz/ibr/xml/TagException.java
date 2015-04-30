package de.i3mainz.ibr.xml;

public class TagException extends RuntimeException {
	
	public TagException(String name, int kindOf) {
		super("Invalid mode to classify tag " + name + ": " + kindOf);
	}
	
	public TagException(String name, int kindOf, String[] attributes) {
		super("Invalid attributes in tag " + name + ": " + attributes.length);
	}
	
	public TagException(String name, String[] nodes) {
		super("Invalid nodes in tag " + name + ": " + nodes.length);
	}
	
}

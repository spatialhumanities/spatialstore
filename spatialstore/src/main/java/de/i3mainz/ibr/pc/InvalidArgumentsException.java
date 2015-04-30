package de.i3mainz.ibr.pc;

public class InvalidArgumentsException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public InvalidArgumentsException() {
		super();
	}
	
	public InvalidArgumentsException(String message) {
		super(message);
	}

}

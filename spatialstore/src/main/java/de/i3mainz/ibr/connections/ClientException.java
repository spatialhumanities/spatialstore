package de.i3mainz.ibr.connections;

public class ClientException extends Exception {
	
	private int code;
	
	public ClientException(String message, int code) {
		super(message);
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
}
package de.i3mainz.ibr.connections;

import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.core.Response;

public class Config {
	
	public static final int XML_TAG = 0;
	public static final int XML_TAG_OPEN = 1;
	public static final int XML_TAG_CLOSE = 2;
	
	
	public static final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final Properties config = new Properties();//edit Felix: darf nicht direkt aufgerufen werden, damit Servervar-Tag entfernt werden kann.
	static {
		try {
			config.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static final String url = config.getProperty("gv_rest");
	
	public static String getProperty(String key) {
		return config.getProperty(key).replace("EXTERNAL_SERVER_ADDRESS", "");
	}
	
	public static void warn(String message) {
		System.out.println(message);
	}
	
	private static String getMessage(Exception exception) {
		String message = xml + "<error><message>" + exception.getMessage() + "</message>";
		for (StackTraceElement element : exception.getStackTrace()) {
			message += "<element>" + element.getClassName() + " / " + element.getLineNumber() + "</element>";
			if (element.getClassName().equals("de.i3mainz.ibr.rest.RestResource"))
				break;
		}
		return message;
	}
	
	public static Response getResult(ClientException exception) {
		return Response.status(exception.getCode()).entity(getMessage(exception)+"</error>").header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static Response getResult(Exception exception) {
		return Response.serverError().entity(getMessage(exception)+"</error>").header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static Response getResult(String file, String name) {
		return Response.ok(file).header("Content-Disposition","attachment; filename="+name).header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static Response getResult(byte[] data) {
		return Response.ok(data).header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static Response getResult(String message) {
		return Response.ok(message).header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static Response getResult() {
		return Response.noContent().header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static String xmlAttribute(String name, String value) {
		return ' ' + name + '=' + '"' + value + '"';
	}
	
	public static String xmlAttribute(String name, int value) {
		return ' ' + name + '=' + '"' + value + '"';
	}
	
}

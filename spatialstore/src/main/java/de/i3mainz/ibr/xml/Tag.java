package de.i3mainz.ibr.xml;

import de.i3mainz.ibr.connections.Config;
import de.i3mainz.ibr.geometry.Util;
import java.sql.ResultSet;
import java.sql.SQLException;

class Tag {
	
	public static final int TAG = 0; // <tag />
	public static final int OPEN = 1; // <tag>
	public static final int CLOSE = 2; // </tag>
	
	public static final String HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	
	private static String create(String name, int kindOf, String ... attributes) {
		if (kindOf == CLOSE)
			return "</"+name+">";
		String tag = "<"+name;
		if (attributes.length%2 != 0)
			throw new TagException(name,kindOf,attributes);
		for (int i=0; i<attributes.length; i+=2) {
			tag += ' ' + attributes[i] + '=' + '"' + attributes[i+1] + '"';
		}
		if (kindOf == OPEN)
			return tag + ">";
		if (kindOf == TAG)
			return tag + "/>";
		throw new TagException(name,kindOf);
	}
	
	private static String textNode(String name, String text) {
		return "<"+name+">" + text + "</"+name+">";
	}
	
	public static String spatialstore(int kindOf) {
		return create("spatialstore",kindOf);
	}
	
	public static String transformation(ResultSet result) throws SQLException {
		String tag = create("transformation",OPEN);
		tag += textNode("srccrs",result.getString("srcCRS"));
		tag += textNode("dstcrs",result.getString("dstCRS"));
		tag += textNode("params",result.getString("param"));
		return tag + create("transformation",CLOSE);
	}
	
	public static String spatialcontext(ResultSet result, int kindOf) throws SQLException {
		String id = result.getString("name");
		String href = Config.url + "/" + result.getString("name");
		String place = result.getString("place");
		String date = result.getDate("date").toString();
		return create("spatialcontext",kindOf,"id",id,"href",href,"place",place,"date",date);
	}
	
	public static String floorplan(String spatialcontext, ResultSet result, int kindOf) throws SQLException {
		String id = result.getString("id");
		String tag = create("media",kindOf,"id",id);
		if (kindOf == OPEN) {
			tag += textNode("mediatype","groundplan");
			tag += textNode("description",result.getString("description"));
			tag += textNode("filename",result.getString("filename"));
		}
		return tag;
	}
	
	public static String viewpoint(String spatialcontext, ResultSet result, int kindOf) throws SQLException {
		String id = result.getString("name");
		String href = Config.url + "/" + spatialcontext + "/viewpoints/" +result.getString("name");
		return create("viewpoint",kindOf,"id",id,"href",href);
	}
	
	public static String panorama(ResultSet result, int kindOf) throws SQLException {
		String id = result.getString("id");
		String tag = create("panorama",kindOf,"id",id);
		if (kindOf == OPEN) {
			tag += textNode("structuraltype",result.getString("type"));
			tag += textNode("kindof",result.getString("kindOf"));
		}
		return tag;
	}
	
	public static String panoramaimages(ResultSet result) throws SQLException {
		String tag = create("images",OPEN);
		while (result.next()) {
			tag += textNode("img",result.getString("img"));
		}
		return tag + create("images",CLOSE);
	}
	
	public static String feature(String spatialcontext, ResultSet result, int kindOf) throws SQLException {
		String id = result.getString("id");
		String href = Config.url + "/" + spatialcontext + "/features/" + result.getString("id");
		String tag = create("feature",kindOf,"id",id,"href",href);
		if (kindOf == OPEN) {
			tag += textNode("geom",result.getString(1));
			tag += textNode("size",String.valueOf(Util.wktToFeature(result.getString(1)).getSize()));
			tag += textNode("unit",Util.wktToFeature(result.getString(1)).getUnit());
			tag += textNode("creator",result.getString("creator"));
			tag += textNode("lastModify",result.getString("date"));
		}
		return tag;
	}
	
}

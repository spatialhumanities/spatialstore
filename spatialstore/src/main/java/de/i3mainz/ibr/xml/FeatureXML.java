package de.i3mainz.ibr.xml;

import de.i3mainz.ibr.connections.ClientException;
import de.i3mainz.ibr.connections.Config;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ws.rs.core.Response;

public class FeatureXML {
	
	public static Response getFeatures(String spatialcontext) {
		try (Database db = new Database()) {
			String message = Tag.HEAD;
			message += Tag.spatialstore(Tag.OPEN);
			ResultSet result = db.getSpatialcontext(spatialcontext);
			if (!result.next())
				throw new ClientException("spatialcontext "+spatialcontext+" does not exist",404);
			message += Tag.spatialcontext(result,Tag.OPEN);
			message += features(spatialcontext,db);
			message += Tag.spatialcontext(result,Tag.CLOSE);
			if (result.next())
				Config.warn("more than one spatialcontext with name "+spatialcontext+" in the database");
			message += Tag.spatialstore(Tag.CLOSE);
			db.close();
			return Config.getResult(message);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}
	
	public static Response getFeature(String spatialcontext, int fid) {
		try (Database db = new Database()) {
			String message = Tag.HEAD;
			message += Tag.spatialstore(Tag.OPEN);
			ResultSet result = db.getSpatialcontext(spatialcontext);
			if (!result.next())
				throw new ClientException("spatialcontext "+spatialcontext+" does not exist",404);
			message += Tag.spatialcontext(result,Tag.OPEN);
			message += feature(spatialcontext,fid,db);
			message += Tag.spatialcontext(result,Tag.CLOSE);
			if (result.next())
				Config.warn("more than one spatialcontext with name "+spatialcontext+" in the database");
			message += Tag.spatialstore(Tag.CLOSE);
			db.close();
			return Config.getResult(message);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}
	
	private static String features(String spatialcontext, Database db) throws SQLException {
		ResultSet result = db.getFeatures(spatialcontext);
		String message = "<resource id=\"features\">";
		while (result.next()) {
			message += Tag.feature(spatialcontext,result,Tag.TAG);
		}
		return message + "</resource>";
	}
	
	private static String feature(String spatialcontext, int fid, Database db) throws SQLException, ClientException {
		ResultSet result = db.getFeature(spatialcontext,fid);
		String message = "<resource id=\"features\">";
		if (!result.next())
			throw new ClientException("feature "+fid+" does not exist",404);
		message += Tag.feature(spatialcontext,result,Tag.OPEN);
		ResultSet viewpoints = db.getFeatureViewpoints(fid); // TODO: Reihenfolge - Aufnahmestandpunkt zuerst!
		while (viewpoints.next())
			message += Tag.viewpoint(spatialcontext,viewpoints,Tag.TAG);
		message += Tag.feature(spatialcontext,result,Tag.CLOSE);
		if (result.next())
				Config.warn("more than one feature with id "+fid+" in the database");
		return message + "</resource>";
	}
	
}

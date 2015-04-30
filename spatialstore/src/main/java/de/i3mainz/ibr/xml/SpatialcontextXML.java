package de.i3mainz.ibr.xml;

import de.i3mainz.ibr.connections.ClientException;
import de.i3mainz.ibr.connections.Config;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ws.rs.core.Response;

public class SpatialcontextXML {
	
	public static Response getSpatialcontexts() {
		try (Database db = new Database()) {
			String message = Tag.HEAD;
			message += Tag.spatialstore(Tag.OPEN);
			ResultSet result = db.getSpatialcontexts();
			while (result.next()) {
				message += Tag.spatialcontext(result,Tag.TAG);
			}
			message += Tag.spatialstore(Tag.CLOSE);
			db.close();
			return Config.getResult(message);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}
	
	public static Response getSpatialcontext(String spatialcontext) {
		try (Database db = new Database()) {
			String message = Tag.HEAD;
			message += Tag.spatialstore(Tag.OPEN);
			ResultSet result = db.getSpatialcontext(spatialcontext);
			if (!result.next())
				throw new ClientException("spatialcontext "+spatialcontext+" does not exist",404);
			message += Tag.spatialcontext(result,Tag.OPEN);
			message += Tag.transformation(result);
			message += features(spatialcontext,db);
			message += floorplans(spatialcontext,db);
			message += viewpoints(spatialcontext,db);
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
			message += Tag.feature(spatialcontext,result,Tag.OPEN);
			ResultSet viewpointResult = db.getFeatureViewpoints(result.getInt("id"));
			while (viewpointResult.next())
				message += Tag.viewpoint(spatialcontext,viewpointResult,Tag.TAG);
			message += Tag.feature(spatialcontext,result,Tag.CLOSE);
		}
		return message + "</resource>";
	}
	
	private static String floorplans(String spatialcontext, Database db) throws SQLException {
		ResultSet result = db.getFloorplans(spatialcontext);
		String message = "<resource id=\"media\">";
		while (result.next()) {
			message += Tag.floorplan(spatialcontext,result,Tag.OPEN);
			message += Tag.transformation(result);
			ResultSet viewpointResult = db.getFloorplanViewpoints(result.getInt("id"));
			while (viewpointResult.next())
				message += Tag.viewpoint(spatialcontext,viewpointResult,Tag.TAG);
			message += Tag.floorplan(spatialcontext,result,Tag.CLOSE);
		}
		return message + "</resource>";
	}
	
	private static String viewpoints(String spatialcontext, Database db) throws SQLException {
		ResultSet result = db.getViewpoints(spatialcontext);
		String message = "<resource id=\"viewpoints\">";
		while (result.next()) {
			message += Tag.viewpoint(spatialcontext,result,Tag.OPEN);
			message += Tag.transformation(result);
			ResultSet panoramaResult = db.getViewpointPanoramas(result.getInt("id"));
			while (panoramaResult.next()) {
				message += Tag.panorama(panoramaResult,Tag.OPEN);
				message += Tag.transformation(panoramaResult);
				message += Tag.panoramaimages(db.getPanoramaImages(panoramaResult.getInt("id")));
				message += Tag.panorama(panoramaResult,Tag.CLOSE);
			}
			ResultSet featureResult = db.getViewpointFeatures(result.getInt("id"));
			while (featureResult.next())
				message += Tag.feature(spatialcontext,featureResult,Tag.TAG);
			ResultSet floorplanResult = db.getViewpointFloorplans(result.getInt("id"));
			while (floorplanResult.next())
				message += Tag.floorplan(spatialcontext,floorplanResult,Tag.TAG);
			message += Tag.viewpoint(spatialcontext,result,Tag.CLOSE);
		}
		return message + "</resource>";
	}
	
}

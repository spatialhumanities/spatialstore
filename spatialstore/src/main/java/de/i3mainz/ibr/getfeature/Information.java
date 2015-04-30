package de.i3mainz.ibr.getfeature;

import de.i3mainz.ibr.connections.ClientException;
import de.i3mainz.ibr.connections.Config;
import de.i3mainz.ibr.connections.Pointcloud;
import de.i3mainz.ibr.geometry.Angle;
import de.i3mainz.ibr.geometry.Point;
import de.i3mainz.ibr.geometry.Transformation;
import java.sql.ResultSet;
import javax.ws.rs.core.Response;

public class Information {
	
	public static Response getPoint(String spatialcontext, String viewpoint, double azim, double elev) {
		try (Database db = new Database()) {
			Angle angle = new Angle(azim,elev);
			ResultSet result = db.getViewpoint(spatialcontext,viewpoint);
			if (!result.next())
				throw new ClientException("viewpoint "+viewpoint+" in spatialcontext "+spatialcontext+" does not exist",404);
			String filename = Config.getProperty("pointcloudpath") + result.getString("filename");
			Transformation ptrans = new Transformation(result.getString("pointcloud_trans"));
			if (result.next())
				Config.warn("more than one viewpoint in spatialcontext "+spatialcontext+" with name "+viewpoint+" in the database");
			Point p = null;
			try (Pointcloud pc = new Pointcloud(filename,ptrans)) {
				p = pc.getPoint(angle);
				pc.close();
			} catch (Exception e) {
				throw e;
			}
			String message = Config.xml;
			message += "<log>";
			if (p != null) {
				message += "<x>" + p.getX() + "</x>";
				message += "<y>" + p.getY() + "</y>";
				message += "<z>" + p.getZ() + "</z>";
				message += "<r>" + 0 + "</r>";
				message += "<g>" + 0 + "</g>";
				message += "<b>" + 0 + "</b>";
				message += "<i>" + 0 + "</i>";
			}
			message += "</log>";
			return Config.getResult(message);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}
	
}

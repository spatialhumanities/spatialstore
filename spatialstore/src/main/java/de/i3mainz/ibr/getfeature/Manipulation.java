package de.i3mainz.ibr.getfeature;

import de.i3mainz.ibr.connections.ClientException;
import de.i3mainz.ibr.connections.Config;
import de.i3mainz.ibr.connections.Pointcloud;
import de.i3mainz.ibr.geometry.Angle;
import de.i3mainz.ibr.geometry.GeoFeature;
import de.i3mainz.ibr.geometry.PlanarPolygon;
import de.i3mainz.ibr.geometry.Point;
import de.i3mainz.ibr.geometry.Transformation;
import de.i3mainz.ibr.geometry.Util;
import de.i3mainz.ibr.pc.PTGInteraction;
import de.i3mainz.ibr.xml.FeatureXML;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;

public class Manipulation {

	private static final double POINTCLOUD_TOLERANCE = 0.1;
	private static final double VISIBILITY_TOLERANCE = 0.95;

	private static Point[] anglesToFeature(String spatialcontext, String viewpoint, Angle[] angles, Database db) throws ClientException, SQLException, Exception {
		ResultSet result = db.getViewpoint(spatialcontext, viewpoint);
		if (!result.next()) {
			throw new ClientException("viewpoint " + viewpoint + " in spatialcontext " + spatialcontext + " does not exist", 404);
		}
		String filename = Config.getProperty("pointcloudpath") + result.getString("filename");
		Transformation ptrans = new Transformation(result.getString("pointcloud_trans"));
		Transformation vtrans = new Transformation(result.getString("viewpoint_trans"));
		if (result.next()) {
			Config.warn("more than one viewpoint in spatialcontext " + spatialcontext + " with name " + viewpoint + " in the database");
		}
		Point[] points = null;
		try (Pointcloud pc = new Pointcloud(filename, ptrans)) {
			points = pc.getPoints(angles);
			pc.close();
		} catch (Exception e) {
			throw e;
		}
		for (Point point : points) {
			point.transform(vtrans);
		}
		return points;
	}

	private static HashMap<String, Double> checkVisibility(String spatialcontext, GeoFeature feature, Database db, String viewpoint) throws SQLException, ClassNotFoundException, IOException {
		double tolerance;
		if (viewpoint == null || viewpoint.isEmpty()) {
			tolerance = POINTCLOUD_TOLERANCE;
		} else {
			ResultSet startViewpoint = db.getViewpoint(spatialcontext, viewpoint);
			if (startViewpoint.next()) {
				String filename = Config.getProperty("pointcloudpath") + startViewpoint.getString("filename");
				Transformation t = new Transformation(startViewpoint.getString("pointcloud_trans"));
				t.transform(new Transformation(startViewpoint.getString("viewpoint_trans")));
				feature.transform(t.inverse());
				tolerance = feature.getPointTol(filename);
				feature.transform(t);
				if (tolerance < POINTCLOUD_TOLERANCE) {
					tolerance = POINTCLOUD_TOLERANCE;
				}
			} else {
				tolerance = POINTCLOUD_TOLERANCE;
			}

		}
		//result.last();
		//int size = result.getRow();
		//result.beforeFirst();
		ResultSet result = db.getViewpoints(spatialcontext);
		HashMap<String, Double> visibility = new HashMap<>();
		while (result.next()) {
			//user.setStatus(result.getRow() * 90 / size);
			String filename = Config.getProperty("pointcloudpath") + result.getString("filename");
			Transformation t = new Transformation(result.getString("pointcloud_trans"));
			t.transform(new Transformation(result.getString("viewpoint_trans")));
			feature.transform(t.inverse());
			visibility.put(result.getString("name"), feature.visible(tolerance, filename));
			feature.transform(t);
		}

		return visibility;
	}

	public static void insertImage(String img, int fid, int width, int height) throws FileNotFoundException, IOException {
		if (img != null && !img.isEmpty()) {
			img = img.replace(' ', '+');
			String encodingPrefix = "base64,";
			int contentStartIndex = img.indexOf(encodingPrefix) + encodingPrefix.length();
			byte[] imgData = Base64.decodeBase64(img.substring(contentStartIndex).getBytes());
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgData));
			if (width == 0 || height == 0) {
				ImageIO.write(image, "png", new File(Config.getProperty("screenshotpath") + fid + ".png"));
			} else {
				BufferedImage resizedImage = resizeclip(image, width, height);
				ImageIO.write(resizedImage, "png", new File(Config.getProperty("screenshotpath") + fid + ".png"));
			}

		}
	}

	/**
	 * returns the resized and cliped img.
	 *
	 * @param img
	 * @param newW
	 * @param newH
	 * @return
	 */
	private static BufferedImage resizeclip(BufferedImage img, int newW, int newH) {

		int startx, width, starty, height;
		int w = img.getWidth();
		int h = img.getHeight();
		double sourceVer = 1.0 * h / w;
		double targetVer = 1.0 * newH / newW;
		if (sourceVer < targetVer) {
			starty = 0;
			height = h;
			startx = (int) (((1.0 * newH / h * w - newW) / 2) * 1.0 * h / newH);
			width = startx + (int) (newW * 1.0 * h / newH);
		} else {
			startx = 0;
			width = w;
			starty = (int) (((1.0 * newW / w * h - newH) / 2) * 1.0 * w / newW);
			height = starty + (int) (newH * 1.0 * w / newW);
		}
		BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newW, newH, startx, starty, width, height, null);
		g.dispose();
		return dimg;
	}

	/**
	 * returns the resized img with black bars.
	 *
	 * @param img
	 * @param newW
	 * @param newH
	 * @return
	 */
	private static BufferedImage resize(BufferedImage img, int newW, int newH) {
		int startx, wide, starty, hight;
		int w = img.getWidth();
		int h = img.getHeight();
		double sourceVer = 1.0 * h / w;
		double targetVer = 1.0 * newH / newW;
		if (sourceVer < targetVer) {
			startx = 0;
			wide = newW;
			hight = (int) 1.0 * newW / w * h;
			starty = (int) (newH - hight) / 2;
			hight += starty;
		} else {
			starty = 0;
			hight = newH;
			wide = (int) 1.0 * newH / h * w;
			startx = (int) (newW - wide) / 2;
			wide += startx;
		}
		BufferedImage dimg = dimg = new BufferedImage(newW, newH, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		Color color = new Color(0, 0, 0);
		g.setColor(color);
		g.fillRect(0, 0, startx == 0 ? newW : startx, starty == 0 ? newH : starty);
		g.drawImage(img, startx, starty, wide, hight, 0, 0, w, h, null);
		g.fillRect(startx == 0 ? 0 : wide, starty == 0 ? 0 : hight, startx == 0 ? newW : startx, starty == 0 ? newH : starty);
		g.dispose();
		return dimg;
	}

	private static double getPrismPoint(String spatialcontext, String viewpoint, Database db, InputXML input, Point[] points) throws SQLException, ClientException, Exception {
		double prismPoint;
		if (input.getType().equals("PRISM")) {
			ResultSet result = db.getViewpoint(spatialcontext, viewpoint);
			if (!result.next()) {
				throw new ClientException("viewpoint " + viewpoint + " in spatialcontext " + spatialcontext + " does not exist", 404);
			}
			String filename = Config.getProperty("pointcloudpath") + result.getString("filename");
			Transformation trans = new Transformation(result.getString("pointcloud_trans"));
			trans.transform(new Transformation(result.getString("viewpoint_trans")));
			Transformation inv = trans.inverse();
			if (result.next()) {
				Config.warn("more than one viewpoint in spatialcontext " + spatialcontext + " with name " + viewpoint + " in the database");
			}
			PlanarPolygon tmp = new PlanarPolygon(input.getAngles(), points);
			tmp.transform(inv);

			prismPoint = PTGInteraction.getPrismaPoint(tmp, filename);
			tmp.transform(trans);
		} else {
			prismPoint = 0;
		}
		return prismPoint;
	}

	public static Response getFeature(String spatialcontext, InputXML input) {
		try (Database db = new Database()) {
			Point[] points = anglesToFeature(spatialcontext, input.getViewpoint(), input.getAngles(), db);
			GeoFeature feature = Util.pointsToFeature(points, input.getAngles(), input.getType(), false, 0);
			String message = Config.xml + "<feature>";
			message += "<geom>" + feature.toWkt() + "</geom>";
			return Config.getResult(message + "</feature>");
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	public static Response addFeature(String spatialcontext, String viewpoint, InputXML input, String img, int width, int height) {
		int fid = 0;
		try (Database db = new Database()) {
			//user.setStatus(0);
			Point[] points = anglesToFeature(spatialcontext, viewpoint, input.getAngles(), db);
			double prismPoint = getPrismPoint(spatialcontext, viewpoint, db, input, points);
			GeoFeature feature = Util.pointsToFeature(points, input.getAngles(), input.getType(), false, prismPoint);
			fid = db.addFeature(spatialcontext, feature.toWkt(), "wkt", 0);
			db.addMeasurement(fid, spatialcontext, viewpoint, input);
			/*HashMap<String, Double> visibility = checkVisibility(spatialcontext,feature,db);
			 for (String vp : visibility.keySet()) {
			 db.setVisibility(spatialcontext, fid, vp, visibility.get(vp) > VISIBILITY_TOLERANCE);
			 }*/
			db.setVisibility(spatialcontext, fid, viewpoint, true);
			db.close();
			//user.setStatus(95);
			//insertImage(img, fid, width, height);
			//user.setStatus(100);
			return FeatureXML.getFeature(spatialcontext, fid);
		} catch (ClientException e) {
			//user.setStatus(100);
			if (fid != 0) {
				deleteFeature(spatialcontext, fid);
			}
			return Config.getResult(e);
		} catch (Exception e) {
			//user.setStatus(100);
			if (fid != 0) {
				deleteFeature(spatialcontext, fid);
			}
			return Config.getResult(e);
		}
	}

	public static Response importFeatures(String spatialcontext, String data, String format, String srid, String creator, String license) {
		try (Database db = new Database()) {
			String[] split = data.split("\\r?\\n");
			for (String content : split) {
				int fid = db.addFeature(spatialcontext, content, format, Integer.parseInt(srid));
				db.addImport(fid, creator, license);				

                                // TODO: Visibility Analysis
				GeoFeature feature = db.getFeature(spatialcontext, fid);
				HashMap<String, Double> visibility = checkVisibility(spatialcontext,feature,db,null);
				for (String vp : visibility.keySet()) {
                                    db.setVisibility(spatialcontext,fid,vp,visibility.get(vp)>VISIBILITY_TOLERANCE);
				}
			}
			db.close();
                        return Config.getResult();
			//return Response.seeOther(URI.create(Config.getProperty("gv_viewer"))).header("Access-Control-Allow-Origin", "*").build();
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	public static Response editFeature(int fid, String spatialcontext, String viewpoint, InputXML input, String img, int width, int height) {
		try (Database db = new Database()) {
			Point[] points = anglesToFeature(spatialcontext, viewpoint, input.getAngles(), db);
			GeoFeature feature = Util.pointsToFeature(points, input.getAngles(), input.getType(), false, 0);
			db.setFeature(fid, spatialcontext, feature.toWkt(), "wkt", 0);
			db.addMeasurement(fid, spatialcontext, viewpoint, input);
			/*HashMap<String, Double> visibility = checkVisibility(spatialcontext, feature, db);
			 for (String vp : visibility.keySet()) {
			 db.setVisibility(spatialcontext, fid, vp, visibility.get(vp) > VISIBILITY_TOLERANCE);
			 }*/
			db.setVisibility(spatialcontext, fid, viewpoint, true);
			db.close();
			//insertImage(img, fid, width, height);
			return FeatureXML.getFeature(spatialcontext, fid);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	public static Response setScreenshot(String spatialcontext, int fid, String img, int width, int height) {
		try {
			insertImage(img, fid, width, height);
			return FeatureXML.getFeature(spatialcontext, fid);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	public static Response setVisibility(String spatialcontext, int fid, String viewpoint) {
		try (Database db = new Database()) {
			GeoFeature feature = db.getFeature(spatialcontext, fid);
			HashMap<String, Double> visibility = checkVisibility(spatialcontext, feature, db, viewpoint);
			for (String vp : visibility.keySet()) {
				db.setVisibility(spatialcontext, fid, vp, visibility.get(vp) > VISIBILITY_TOLERANCE);
			}
			db.close();
			return FeatureXML.getFeature(spatialcontext, fid);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	public static Response setVisibility(String spatialcontext, String viewpoint, int fid, boolean visible) {
		try (Database db = new Database()) {
			db.setVisibility(spatialcontext, fid, viewpoint, visible);
			db.close();
			return FeatureXML.getFeature(spatialcontext, fid);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	public static Response deleteFeature(String spatialcontext, int fid) {
		try (Database db = new Database()) {
			db.removeFeature(fid, spatialcontext);
			File file = new File(Config.getProperty("screenshotpath") + fid + ".png");
			file.delete();
			return Config.getResult();
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

}

package de.i3mainz.ibr.getfeature;

import de.i3mainz.ibr.connections.ClientException;
import de.i3mainz.ibr.connections.Config;
import de.i3mainz.ibr.connections.Identification;
import de.i3mainz.ibr.geometry.GeoFeature;
import de.i3mainz.ibr.geometry.Transformation;
import de.i3mainz.ibr.geometry.Util;
import de.i3mainz.ibr.pc.PTGInputStream;
import de.i3mainz.ibr.pc.PTGPoint;
import de.i3mainz.ibr.xml.FeatureXML;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.ResultSet;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Properties;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Export {

    public static final Properties config = new Properties();

    static {
        try {
            config.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final String viewerAdress = config.getProperty("gv_viewerjsp");

    private static ArrayList<Integer> getFeatureIds(String ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        String[] split = ids.split(",");
        ArrayList<Integer> idList = new ArrayList<Integer>();
        for (String s : split) {
            idList.add(Integer.valueOf(s));
        }
        return idList;
    }

    /**
     * Export Feature(s) Please do only modify this method if you are familiar
     * with reflection. Look at the class ExportMethods instead. They are
     * invoked.
     *
     * @param user
     * @param spatialcontext
     * @param fids
     * @param viewpoint
     * @param creator
     * @param format
     * @param srid
     * @return
     */
    public static Response getFeatures(Identification user, String spatialcontext, String fids, String viewpoint, String creator, String format, String srid, int param1, int param2) {
        try {
            ArrayList<Integer> fidList = getFeatureIds(fids);
            if (fidList != null && fidList.size() == 1 && (format == null || format.isEmpty())) { // 1 feature, no format
                String url = viewerAdress + "?furi=" + Config.url + "/" + spatialcontext + "/features/" + fidList.get(0); // TODO: many features, better more general solution
                return Response.seeOther(URI.create(url)).header("Access-Control-Allow-Origin", "*").build();
            } else if (format == null || format.isEmpty() || format.equals("xml") || format.equals("XML")) { // no format or xml
                if (fidList == null) {
                    // TODO: Filter by Creator, Viewpoint and additional lastModify
                    return FeatureXML.getFeatures(spatialcontext);
                }
                if (fidList.size() == 1) {
                    return FeatureXML.getFeature(spatialcontext, fidList.get(0));
                }
                throw new ClientException("wrong parameters, number of ids not allowed: " + fidList.size(), 400);
            } else {
                for (Method method : ExportMethods.class.getMethods()) {
                    Format annotationFormat = method.getAnnotation(Format.class);
                    SingleFeature annotationSingle = method.getAnnotation(SingleFeature.class);
                    for (String s : annotationFormat.value()) {
                        if (format.equals(s)) {
                            if (annotationSingle.value()) {
                                if (fidList == null) {
                                    throw new ClientException("wrong parameters, idList is empty", 400);
                                }
                                if (fidList.size() != 1) {
                                    throw new ClientException("number of features not allowed: " + fidList.size(), 404);
                                }
                                return (Response) method.invoke(null, user, spatialcontext, srid, fidList.get(0), param1, param2);
                            } else {
                                return (Response) method.invoke(null, user, spatialcontext, srid, fidList, viewpoint, creator, param1, param2);
                            }
                        }
                    }
                }
            }
            throw new ClientException("format " + format + " not supported", 404);
        } catch (ClientException e) {
            return Config.getResult(e);
        } catch (Exception e) {
            return Config.getResult(e);
        }
    }

}

/**
 * All Export-Format-Methods must be implemented here. Methods must have
 * Attributes: Format and SingleFeature. Methods must return Response-Object.
 * Methods should handle all occuring Exceptions.
 *
 * If SingleFeature(true), the method head must look like (Identification user,
 * String spatialcontext, int srid, int fid)
 *
 * If SingleFeature(false), the method head must look like (Identification user,
 * String spatialcontext, int srid, ArrayList<Integer> fids, String viewpoint,
 * String creator)
 */
class ExportMethods {

    @Format({"png", "PNG"})
    @SingleFeature(true)
    public static Response png(Identification user, String spatialcontext, String srid, int fid, int width, int height) {
        // TODO: create Image dependent on width and height
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(Config.getProperty("screenshotpath") + fid + ".png"))) {
            byte[] data = new byte[stream.available()];
            stream.read(data);
            stream.close();
            return Config.getResult(data);
        } catch (Exception e) {
            return Config.getResult(e);
        }
    }

    @Format({"pts", "PTS"})
    @SingleFeature(true)
    public static Response pts(Identification user, String spatialcontext, String srid, int fid, int deci, int maxPoints) {
        try (Database db = new Database()) {
            StringBuilder message = new StringBuilder();
            GeoFeature feature = db.getFeature(spatialcontext, fid);
            ResultSet viewpoints = db.getViewpoints(spatialcontext, fid);
            int numberOfPoints = 0;
            double step;
            Transformation trans_dst = null;
            if (maxPoints != 0) { // limit point count
                int expectedPoints = 0;
                while (viewpoints.next()) {
                    Transformation trans = new Transformation(viewpoints.getString("pointcloud_trans"));
                    trans.transform(new Transformation(viewpoints.getString("viewpoint_trans")));
                    Transformation inv = trans.inverse();

                    if (trans_dst == null) {
                        String dstcrs = viewpoints.getString("sc_trans_dstcrs");
                        if (srid == null || srid.equals("spatialcontext")) {
                            trans_dst = new Transformation(); // Einheitsmatrix
                        } else if (dstcrs.equals(srid) || dstcrs.equals("EPSG:" + srid)) {
                            trans_dst = (new Transformation(viewpoints.getString("spatialcontext_trans")));
                        } else if (srid.equals("pointcloud")) {
                            trans_dst = inv; // sc -> pc
                        } else {
                            trans_dst = new Transformation(); // Einheitsmatrix
                        }
                    }
                    trans.transform(trans_dst); // pc -> dst crs

                    String filename = Config.getProperty("pointcloudpath") + viewpoints.getString("filename");
                    feature.transform(inv);
                    int ptgPunkte;
                    try (PTGInputStream ptg = new PTGInputStream(filename)) {
                        ptgPunkte = ptg.getSize() * ptg.getSize() * 2;
                    }
                    expectedPoints += (int) (ptgPunkte / (2 * Math.PI * Math.PI) * feature.getsphArea());
                    feature.transform(inv.inverse());
                }
                step = expectedPoints > maxPoints ? Math.sqrt((1.0 * expectedPoints) / maxPoints) : 1;

                viewpoints.beforeFirst();
            } else {
                step = 1;
            }

            while (viewpoints.next()) {
                Transformation trans = new Transformation(viewpoints.getString("pointcloud_trans"));
                trans.transform(new Transformation(viewpoints.getString("viewpoint_trans")));
                Transformation inv = trans.inverse();

                if (trans_dst == null) {
                    String dstcrs = viewpoints.getString("sc_trans_dstcrs");
                    if (srid == null || srid.equals("spatialcontext")) {
                        trans_dst = new Transformation(); // Einheitsmatrix
                    } else if (dstcrs.equals(srid) || dstcrs.equals("EPSG:" + srid)) {
                        trans_dst = (new Transformation(viewpoints.getString("spatialcontext_trans")));
                    } else if (srid.equals("pointcloud")) {
                        trans_dst = inv; // sc -> pc
                    } else {
                        trans_dst = new Transformation(); // Einheitsmatrix
                    }
                }
                trans.transform(trans_dst); // pc -> dst crs

                String filename = Config.getProperty("pointcloudpath") + viewpoints.getString("filename");
                feature.transform(inv);
                ArrayList<PTGPoint> points = feature.getPTGPoints(filename, step);
                for (PTGPoint p : points) {
                    p.transform(trans);
                    message.append(p.toPts(deci - 1));
                    message.append("\n");
                }
                feature.transform(inv.inverse());
                numberOfPoints += points.size();
            }
            message.insert(0, String.valueOf(numberOfPoints) + "\n");
            return Config.getResult(message.toString());
        } catch (Exception e) {
            return Config.getResult(e);
        }
    }

    @Format({"xyz", "XYZ"})
    @SingleFeature(true)
    public static Response xyz(Identification user, String spatialcontext, String srid, int fid, int deci, int maxPoints) {
        try (Database db = new Database()) {
            StringBuilder message = new StringBuilder();
            GeoFeature feature = db.getFeature(spatialcontext, fid);
            ResultSet viewpoints = db.getViewpoints(spatialcontext, fid);
            double step;
            Transformation trans_dst = null;
            if (maxPoints != 0) {
                int expectedPoints = 0;
                while (viewpoints.next()) {
                    Transformation trans = new Transformation(viewpoints.getString("pointcloud_trans"));
                    trans.transform(new Transformation(viewpoints.getString("viewpoint_trans")));
                    Transformation inv = trans.inverse();
                    String filename = Config.getProperty("pointcloudpath") + viewpoints.getString("filename");
                    feature.transform(inv);

                    if (trans_dst == null) {
                        String dstcrs = viewpoints.getString("sc_trans_dstcrs");
                        if (srid == null || srid.equals("spatialcontext")) {
                            trans_dst = new Transformation(); // Einheitsmatrix
                        } else if (dstcrs.equals(srid) || dstcrs.equals("EPSG:" + srid)) {
                            trans_dst = (new Transformation(viewpoints.getString("spatialcontext_trans")));
                        } else if (srid.equals("pointcloud")) {
                            trans_dst = inv; // sc -> pc
                        } else {
                            trans_dst = new Transformation(); // Einheitsmatrix
                        }
                    }
                    trans.transform(trans_dst); // pc -> dst crs

                    int ptgPunkte;
                    try (PTGInputStream ptg = new PTGInputStream(filename)) {
                        ptgPunkte = ptg.getSize() * ptg.getSize() * 2;
                    }
                    expectedPoints += (int) (ptgPunkte / (2 * Math.PI * Math.PI) * feature.getsphArea());
                    feature.transform(inv.inverse()); // transform to spatialcontext
                    //feature.transform(trans); // transform to destination crs
                }
                step = expectedPoints > maxPoints ? Math.sqrt((1.0 * expectedPoints) / maxPoints) : 1;

                viewpoints.beforeFirst();
            } else {
                step = 1;
            }
            while (viewpoints.next()) {
                Transformation trans = new Transformation(viewpoints.getString("pointcloud_trans"));
                trans.transform(new Transformation(viewpoints.getString("viewpoint_trans"))); // pc -> sc
                Transformation inv = trans.inverse(); // sc -> pc
                String filename = Config.getProperty("pointcloudpath") + viewpoints.getString("filename");
                feature.transform(inv); // sc -> pc

                if (trans_dst == null) {
                    String dstcrs = viewpoints.getString("sc_trans_dstcrs");
                    if (srid == null || srid.equals("spatialcontext")) {
                        trans_dst = new Transformation(); // Einheitsmatrix
                    } else if (dstcrs.equals(srid) || dstcrs.equals("EPSG:" + srid)) {
                        trans_dst = (new Transformation(viewpoints.getString("spatialcontext_trans")));
                    } else if (srid.equals("pointcloud")) {
                        trans_dst = inv; // sc -> pc
                    } else {
                        trans_dst = new Transformation(); // Einheitsmatrix
                    }
                }
                trans.transform(trans_dst); // pc -> dst crs

                ArrayList<PTGPoint> points = feature.getPTGPoints(filename, step);
                for (PTGPoint p : points) {
                    p.transform(trans); // pc -> dst crs
                    message.append(p.toxyz(deci - 1));
                    message.append("\n");
                }
                feature.transform(inv.inverse()); // pc -> sc (for next loop step)
            }
            return Config.getResult(message.toString());
        } catch (Exception e) {
            return Config.getResult(e);
        }
    }

    /**
     * Returns a destination transformation for feature export.
     *
     * @param spatialcontext name of the spatialcontext
     * @param srid name of the destination crs
     * @return
     */
    private static Transformation getFeatureTrafo(String spatialcontext, String srid) {
        Transformation trans_dst = trans_dst = new Transformation();
        try (Database db = new Database()) {
            if (srid != null && !srid.equals("spatialcontext")) {
                ResultSet viewpoints = db.getViewpoints(spatialcontext);
                viewpoints.next();
                String dstcrs = viewpoints.getString("st_dst");

                if (srid.equals("pointcloud")) {
                    Transformation trans = new Transformation(viewpoints.getString("pointcloud_trans"));
                    trans.transform(new Transformation(viewpoints.getString("viewpoint_trans")));
                    trans_dst = trans.inverse();
                } else if (dstcrs.equals(srid) || dstcrs.equals("EPSG:" + srid)) {
                    trans_dst = (new Transformation(viewpoints.getString("spatialcontext_trans")));
                }
            }
            return trans_dst;
        } catch (Exception e) {
            return trans_dst;
        }
    }

    @Format({"gml", "GML"})
    @SingleFeature(false)
    public static Response gml(Identification user, String spatialcontext, String srid, ArrayList<Integer> fids, String viewpoint, String creator, int deci, int maxPoints) {
        try (Database db = new Database()) {
            // Zielkoordinatensytem festlegen
            Transformation trans_dst = getFeatureTrafo(spatialcontext, srid);

            // Abfrage formulieren -> db.getFeatures
            ResultSet result = db.getFeatures("st_asewkt(geom)", spatialcontext, viewpoint, creator);
            // ResultSet durchlaufen, Abfrage formulieren
            String query = "SELECT column1 AS id, ST_ASGML(3, column2) AS geom FROM (VALUES ";
            while (result.next()) {
                if (fids == null || fids.contains(result.getInt("id"))) {
                    // jedes Feature aus ResultSet transformieren & in Abfrage sammeln
                    GeoFeature feature = Util.wktToFeature(result.getString(1));
                    int fid = result.getInt("id");
                    feature.transform(trans_dst);
                    query += "(" + fid + ", ST_GeomFromText('" + feature.toWkt() + "')  ),";
                }
            }
            query = query.substring(0, query.length() - 1);

            query += ") AS geoms";

            // Anfrage (Ausgabe als GML) an DB
            // Ausgabe (message) zusammenbauen
            String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<gml:FeatureCollection xmlns:gml=\"http://www.opengis.net/gml\">\n";
            result = db.query(query);
            while (result.next()) {
                message += "<gml:Feature fid =\"" + result.getInt("id") + "\">\n"
                        + result.getString("geom") + "\n"
                        + "</gml:Feature>\n";
            }
            message += "</gml:FeatureCollection>";
            db.close();
            return Config.getResult(message);
        } catch (Exception e) {
            System.out.println("method gml exception");
            return Config.getResult(e);
        }
    }

    @Format({"wkt", "WKT", "ewkt", "EWKT"})
    @SingleFeature(false)
    public static Response ewkt(Identification user, String spatialcontext, String srid, ArrayList<Integer> fids, String viewpoint, String creator, int deci, int maxPoints) {
        try (Database db = new Database()) {
            Transformation trans_dst = getFeatureTrafo(spatialcontext, srid);
            ResultSet result = db.getFeatures("st_asewkt(geom)", spatialcontext, viewpoint, creator);
            String query = "SELECT column1 AS id, st_asewkt(column2) AS geom FROM (VALUES ";
            while (result.next()) {
                if (fids == null || fids.contains(result.getInt("id"))) {
                    GeoFeature feature = Util.wktToFeature(result.getString(1));
                    int fid = result.getInt("id");
                    feature.transform(trans_dst);
                    query += "(" + fid + ", ST_GeomFromText('" + feature.toWkt() + "')),";
                }
            }
            query = query.substring(0, query.length() - 1);
            query += ") AS geoms";

            String message = "";
            result = db.query(query);
            while (result.next()) {
                if (fids == null || fids.contains(result.getInt("id"))) {
                    message += result.getString("geom") + "\n";
                }
            }
            db.close();
            return Config.getResult(message);
        } catch (Exception e) {
            return Config.getResult(e);
        }
    }

    @Format({"kml", "KML"})
    @SingleFeature(false)
    public static Response kml(Identification user, String spatialcontext, String srid, ArrayList<Integer> fids, String viewpoint, String creator, int deci, int maxPoints) {
        try (Database db = new Database()) {
            ResultSet viewpoints = db.getViewpoints(spatialcontext);
            viewpoints.next();
            String dstcrs = viewpoints.getString("st_dst");
            if (!(dstcrs.equals(srid) || dstcrs.equals("EPSG:" + srid))) {
                System.out.println("Ausgabe in diesem Spatialcontext nur mit SRID=" + dstcrs);
                throw new ClientException("Ausgabe in diesem Spatialcontext nur mit SRID=" + dstcrs,400);
            }
            
            Transformation trans_dst = getFeatureTrafo(spatialcontext, srid);
            ResultSet result = db.getFeatures("st_asewkt(geom)", spatialcontext, viewpoint, creator);
            String query = "SELECT column1 AS id, ST_ASKML(column2,15) AS geom FROM (VALUES ";
            while (result.next()) {
                if (fids == null || fids.contains(result.getInt("id"))) {
                    GeoFeature feature = Util.wktToFeature(result.getString(1));
                    int fid = result.getInt("id");
                    feature.transform(trans_dst);
                    query += "(" + fid + ", ST_GeomFromText('" + feature.toWkt() + "',"+ srid +")  ),";
                }
            }
            query = query.substring(0, query.length() - 1);
            query += ") AS geoms";

            String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
                    + "<Document>";
            result = db.query(query);
            while (result.next()) {
                if (fids == null || fids.contains(result.getInt("id"))) {
                    message += "<Placemark>";
                }
                message += "<name>" + result.getString("id") + "</name>\n";
                message += editkml(result.getString("geom"));
                message += "</Placemark>";
            }
            message += "</Document>";
            message += "</kml>";
            db.close();
            return Config.getResult(message);
        } catch (ClientException e) {
            return Config.getResult(e);
        } catch (Exception e) {
            return Config.getResult(e);
        }
    }

    @Format({"x3d", "X3D"})
    @SingleFeature(false)
    public static Response x3d(Identification user, String spatialcontext, String srid, ArrayList<Integer> fids, String viewpoint, String creator, int deci, int maxPoints) {
        try (Database db = new Database()) {
            Transformation trans_dst = getFeatureTrafo(spatialcontext, srid);
            ResultSet result = db.getFeatures("st_asewkt(geom)", spatialcontext, viewpoint, creator);
            String query = "SELECT column1 AS id, st_ASX3D(column2) AS geom FROM (VALUES ";
            while (result.next()) {
                if (fids == null || fids.contains(result.getInt("id"))) {
                    GeoFeature feature = Util.wktToFeature(result.getString(1));
                    int fid = result.getInt("id");
                    feature.transform(trans_dst);
                    query += "(" + fid + ", ST_GeomFromText('" + feature.toWkt() + "')),";
                }
            }
            query = query.substring(0, query.length() - 1);
            query += ") AS geoms";

            String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.0//EN\" \"http://www.web3d.org/specifications/x3d-3.0.dtd\">\n"
                    + "<X3D profile='Immersive' version='3.0'>\n"
                    + "<Scene>\n"
                    + "<Transform>\n";
            result = db.query(query);
            while (result.next()) {
                String geom = result.getString("geom");
                if (geom.contains("IndexedFaceSet") && geom.contains("MultiGeometry")) { //PRISM
                    geom.replaceAll("<MultiGeometry>", "");
                    geom.replaceAll("</MultiGeometry>", "");
                    geom.replaceAll("<geometryMember>", "");
                    geom.replaceAll("</geometryMember>", "");
                } else if (!geom.contains("LineSet") && !geom.contains("IndexedFaceSet")) { //POINT
                    geom = "<PointSet><Coordinate point='" + geom + "'/></PointSet>";
                }

                message += "<Shape>\n"
                        + "<MetadataSet>\n"
                        + "<MetadataString name='spatialcontext' value='\"" + spatialcontext + "\"'/>"
                        + "<MetadataInteger name='id' value='" + result.getInt("id") + "'/>\n"
                        + "<MetadataString name='ersteller' value='\"" + creator + "\"'/>"
                        + "</MetadataSet>\n"
                        + geom + "\n"
                        + "<Appearance>\n"
                        + "</Appearance>\n"
                        + "</Shape>\n";

            }
            message += "</Transform>\n"
                    + "</Scene>\n"
                    + "</X3D>";
            return Config.getResult(message);
        } catch (Exception e) {
            return Config.getResult(e);
        }
    }

    @Format({"editkml"})
    @SingleFeature(false)
    public static String editkml(String kml) throws ParserConfigurationException, SAXException, IOException, Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(kml));
        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();
        //element to insert: <altitudeMode>absolute</altitudeMode>
        Element alt = doc.createElement("altitudeMode");
        alt.insertBefore(doc.createTextNode("absolute"), alt.getLastChild());
        Node root = doc.getDocumentElement();
        String rootGeomType = root.getNodeName();
        if (rootGeomType.equals("MultiGeometry")) {
            NodeList nlist = root.getChildNodes();
            for (int i = 0; i < nlist.getLength(); i++) {
                Node geom = nlist.item(i);
                Node cloneAlt = alt.cloneNode(true);
                geom.insertBefore(cloneAlt, geom.getFirstChild());
            }
        } else {
            Node cloneAlt = alt.cloneNode(true);
            root.insertBefore(cloneAlt, root.getFirstChild());
        }
        return printdoc(doc);
    }

    @Format({"printdoc"})
    @SingleFeature(false)
    private static String printdoc(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
        return out.toString();
    }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Format {

    String[] value();
}

@Retention(RetentionPolicy.RUNTIME)
@interface SingleFeature {

    boolean value();
}

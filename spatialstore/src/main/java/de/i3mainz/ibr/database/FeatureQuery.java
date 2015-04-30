package de.i3mainz.ibr.database;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
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



/*
Delete Class after KML export is fixed in new Class
*/



public class FeatureQuery extends DBInterface {
	
	public enum Format {
		XML,GML,X3D,EWKT,KML
		
	}
	
	public FeatureQuery() throws ClassNotFoundException, SQLException,DBException {
		super();
	}
	
	private int getSpatialcontextId(String spatialcontext) throws SQLException, DBException {
		ResultSet rs = null; //_getSpatialcontext(spatialcontext);
		return rs.getInt("id");
	}
	
	private HashMap<Integer,String> getGeometry(String spatialcontext, String geom, ArrayList<Integer> ids, String viewpoint, String creator) throws SQLException {
		String sql = "SELECT feature.id, "+geom+" FROM feature, spatialcontext, viewpoint, feature_viewpoint, edit WHERE feature.IDREF_sc = spatialcontext.id AND spatialcontext.name = ?";
		if (viewpoint != null)
			sql += " AND feature_viewpoint.IDREF_feature = feature.id AND feature_viewpoint.IDREF_view = viewpoint.id AND viewpoint.name = ?";
		if (creator != null)
			sql += " AND edit.IDREF_feature = feature.id AND edit.creator = ?";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,spatialcontext);
		if (viewpoint != null)
			statement.setString(2,viewpoint);
		if (creator != null)
			statement.setString(viewpoint != null ? 3 : 2,creator);
		ResultSet rs = statement.executeQuery();
		HashMap<Integer,String> result = new HashMap<>();
		while (rs.next()) {
			if (ids == null || ids.contains(rs.getInt(1)))
				result.put(rs.getInt(1),rs.getString(2));
		}
		return result;
	}
	
	public String exportGeometry(String spatialcontext, String format, int srid, ArrayList<Integer> ids, String viewpoint, String creator) {
		String geom = null;
		if (format.equals("gml") || format.equals("GML"))
			geom = "st_asgml(3,geom)";
		if (format.equals("x3d") || format.equals("X3D"))
			geom = "st_asx3d(geom)";
		if (format.equals("ewkt") || format.equals("EWKT") || format.equals("wkt") || format.equals("WKT") || format.equals("txt") || format.equals("TXT"))
			geom = "st_asewkt(geom)";
		if (format.equals("kml") || format.equals("KML") || format.equals("kmz"))
			geom = "st_askml(geom,4326)";
		return geom;
	}
	
	public String exportGeometry(int[] id, String format, int srid, String spatialcontext) throws DBException, SQLException, ClassNotFoundException, IOException, ParserConfigurationException, SAXException, Exception {
		if (format.equals("gml")||format.equals("GML"))
			return exportGML(id,srid,spatialcontext);
		else if (format.equals("x3d")||format.equals("X3D"))
			return exportX3D(id,srid,spatialcontext);
		else if (format.equals("ewkt")||(format.equals("EWKT")))
			return exportEWKT(id,srid,spatialcontext);
		else if (format.equals("kml")||(format.equals("kmz")))
			return exportKML(id,srid,spatialcontext);
		throw new DBException("format not supproted: " + format);
	}
	
	private String exportGML(int[] id,int srid, String spatialcontext) throws SQLException, DBException, ClassNotFoundException, IOException {
		String query= "";
		if (srid != 0){
			query = "SELECT id, st_asgml(3,"+transformquery(spatialcontext,srid);					
			for (int i=1; i<id.length; i++){
				query+="UNION SELECT id, st_asgml(3,"+transformquery(spatialcontext,srid);					
			}
		}		
		else {
			int spatialcontextID = getSpatialcontextId(spatialcontext);
			query = "SELECT id, st_asgml(3,geom) FROM feature WHERE idref_sc="+spatialcontextID+" AND id = ?";
			for (int i=1; i<id.length; i++){
				query+="UNION SELECT id, st_asgml(3,geom) FROM feature WHERE idref_sc="+spatialcontextID+" AND id = ?";
			}
		}
		PreparedStatement statement = connection.prepareStatement(query);
		for (int i=0; i < id.length; i++)				
			statement.setInt(i+1,id[i]);				
		ResultSet result = statement.executeQuery();
		
		String gml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<gml:FeatureCollection xmlns:gml=\"http://www.opengis.net/gml\">\n";
		
		int i=0;
		while (result.next()) {
			gml += "<gml:Feature fid =\""+result.getString(1)+"\">\n"
					+ result.getString(2) + "\n"
					+ "</gml:Feature>\n";
			i++;
		}
		gml += "</gml:FeatureCollection>";
		return gml;
	}
	private String exportKML(int[] id,int srid, String spatialcontext) throws SQLException, DBException, ClassNotFoundException, IOException, ParserConfigurationException, SAXException, Exception {
		String query= "";		
		if (srid != 0){
			query = "SELECT id, st_askml("+transformquery(spatialcontext,srid);					
			for (int i=1; i<id.length; i++){
				query+="UNION SELECT id, st_askml("+transformquery(spatialcontext,srid);					
			}
		}		
		else {
			int spatialcontextID = getSpatialcontextId(spatialcontext);
			query = "SELECT id, st_askml(geom,4326) FROM feature WHERE idref_sc="+spatialcontextID+" AND id = ?";
			for (int i=1; i<id.length; i++){
				query+="UNION SELECT id, st_askml(geom,4326) FROM feature WHERE idref_sc="+spatialcontextID+" AND id = ?";
			}
		}
		PreparedStatement statement = connection.prepareStatement(query);
		for (int i=0; i < id.length; i++)				
			statement.setInt(i+1,id[i]);				
		ResultSet result = statement.executeQuery();		
		String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
				+ "<Document>";		
		int i=0;
		while (result.next()) {
			kml += "<Placemark>";
			kml += "<name>"+result.getString(1)+"</name>\n";
			kml += editkml(result.getString(2));
			kml += "</Placemark>";					
			i++;
		}
		kml += "</Document>";
		kml += "</kml>";
		return kml;
	}
	
	private String exportX3D (int[] id,int srid,String spatialcontext) throws SQLException, DBException, ClassNotFoundException, IOException {
		String query= "";		
		if (srid != 0){
			query = "SELECT id, st_asx3d("+transformquery(spatialcontext,srid);					
			for (int i=1; i<id.length; i++){
				query+="UNION SELECT id, st_asx3d("+transformquery(spatialcontext,srid);					
			}
		}		
		else {
			int spatialcontextID = getSpatialcontextId(spatialcontext);
			query = "SELECT id, st_asx3d(geom) FROM feature WHERE idref_sc="+spatialcontextID+" AND id = ?";
			for (int i=1; i<id.length; i++){
				query+="UNION SELECT id, st_asx3d(geom) FROM feature WHERE idref_sc="+spatialcontextID+" AND id = ?";
			}
		}	
		PreparedStatement statement = connection.prepareStatement(query);
		for (int i=0; i < id.length; i++)
			statement.setInt(i+1,id[i]);
		ResultSet result = statement.executeQuery();
		String x3d = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
               +"<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.0//EN\" \"http://www.web3d.org/specifications/x3d-3.0.dtd\">\n"
               +"<X3D>\n"
               +"<Scene>\n"
               +"<Transform>\n";  
		int i=0;
		while (result.next()) {
			x3d += "<Shape id =\""+result.getString(1)+"\">\n"
					+ result.getString(2) + "\n"
					+"<Appearance>\n"
					+"<Material emissiveColor='0 0 1'/> \n" 
                    +"</Appearance>\n"
                    +"</Shape>\n";					
			i++;
		}
        x3d+= "</Transform>\n"
			 +"</Scene>\n"
			 +"</X3D>";		
		return x3d;
	}	
	
	private String exportEWKT (int[] id,int srid, String spatialcontext)throws SQLException, ClassNotFoundException, DBException, IOException{
		String query= "";		
		if (srid != 0){
			query = "SELECT st_asewkt("+transformquery(spatialcontext, srid);					
			for (int i=1; i<id.length; i++){
				query+="UNION SELECT st_asewkt("+transformquery(spatialcontext, srid);					
			}
		}		
		else {
			int spatialcontextID = getSpatialcontextId(spatialcontext);
			query = "SELECT st_asewkt(geom) FROM feature WHERE idref_sc="+spatialcontextID+" AND id = ?";
			for (int i=1; i<id.length; i++){
				query+="UNION SELECT st_asewkt(geom) FROM feature WHERE idref_sc="+spatialcontextID+" AND id = ?";;
			}
		}		
		PreparedStatement statement = connection.prepareStatement(query);
		for (int i=0; i < id.length; i++)
			statement.setInt(i+1,id[i]);
		ResultSet result = statement.executeQuery();
		
		String ewkt= "";
		while (result.next()) {
			ewkt += result.getString(1) + "\n";
		}
		return ewkt;
	}
	
	private int getsrid (String spatialcontext)throws SQLException, DBException{
		return 0; //_getSpatialcontext(spatialcontext).getInt("srid");	
	}
	
	private int getdtscrs (String spatialcontext) throws SQLException, DBException {
		ResultSet result = null;//_getSpatialcontext(spatialcontext);
		String value = result.getString("dstcrs");
		StringTokenizer st = new StringTokenizer(value,"EPSG:"); //string EPSG:31467
		return Integer.parseInt(st.nextToken());
	}
	
	private String transformquery (String spatialcontext, int srid) throws SQLException, DBException, ClassNotFoundException, IOException{
		String query="";
//		int spatialcontextSRID = getsrid(spatialcontext); //trasform from (spatial context coordinate system) 
//		int transformToSRID = srid;  //transform to
//		int defaultDTSCRS = getdtscrs(spatialcontext); // stored in the database transformation from local to global
//		int spatialcontextID = getSpatialcontextId(spatialcontext);
//		TransformPC db = new TransformPC();
//		Transformation trans = db.getSpatialcontextTransformation(spatialcontext);
//		String defaultTransParam = trans.postgisaffine(); //user defined global destination params
//		db.close();
//		if (spatialcontextSRID==0){ //  0 = local Coordinate System	
//			query = "ST_TRANSFORM(georef,"+transformToSRID+"))As done \n" +
//					"FROM (SELECT id, ST_SetSRID(geom,"+defaultDTSCRS+")AS georef \n" +
//					"FROM (SELECT id, st_asewkt(st_affine(geom,"+defaultTransParam+"))AS geom FROM feature WHERE idref_sc = "+spatialcontextID+" and id = ?) AS undefined) As undefined ";
//			}				
//		else { //  if spatial context is referenced to global coordinate system
//			query =	"ST_TRANSFORM(georef,"+transformToSRID+"))As done \n" +
//					"FROM (SELECT id, ST_SetSRID(geom,"+spatialcontextSRID+")AS georef \n" +
//					"FROM (SELECT id, ST_AsEWKT(geom) As geom From feature WHERE idref_sc = "+spatialcontextID+" and id = ?) AS undefined)As undefined ";
//		}		
		return query;
	}
	
	private String editkml (String kml) throws ParserConfigurationException, SAXException, IOException, Exception{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();        
		InputSource is = new InputSource(new StringReader(kml));
		Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();		
		//element to insert: <altitudeMode>absolute</altitudeMode>
		Element alt = doc.createElement("altitudeMode");
		alt.insertBefore(doc.createTextNode("absolute"),alt.getLastChild());
		Node root = doc.getDocumentElement();
		String rootGeomType = root.getNodeName();
		if (rootGeomType.equals("MultiGeometry")){			
			NodeList nlist= root.getChildNodes();			
			for (int i = 0;i<nlist.getLength();i++){
				Node geom = nlist.item(i);
				Node cloneAlt = alt.cloneNode(true);
				geom.insertBefore(cloneAlt,geom.getFirstChild());
			}
		}
		else{
		Node cloneAlt = alt.cloneNode(true);
		root.insertBefore(cloneAlt,root.getFirstChild());
		}
		return printdoc(doc);
	}
	private String printdoc(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
		tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
		Writer out = new StringWriter();
		tf.transform(new DOMSource(xml), new StreamResult(out));		
        return out.toString();
    }
}

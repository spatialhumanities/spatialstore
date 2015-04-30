package de.i3mainz.ibr.project;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Arno Heidelberg
 */
public class ProjectXML {

	public static String parseXMLandPutInDatabase(String _xmlProject) throws JDOMException, IOException {
		String StringOut;

		try (Database db = new Database()) {

			_xmlProject = ProjectXMLValidation.validation(_xmlProject);

			//XML Parsen
			SAXBuilder builder = new SAXBuilder();
			Reader in = new StringReader(_xmlProject);

			//Database Serials
			int SC_ID = -1;
			int MED_ID = -1;
			int VP_ID = -1;
			int PC_ID = -1;
			int PAN_ID = -1;
			int PANIMG_ID = -1;

			Document document = (Document) builder.build(in);
			Element genericviewer = document.getRootElement();

			//element <spatialcontext>
			Element spatialcontext = genericviewer.getChild("spatialcontext");

			// element <resources>
			Element resources = spatialcontext.getChild("resources");
			List resource_list = resources.getChildren("resource"); //Liste der resource-Elemente [3] --> FEST!

			// element <resource id="metadata"> --> fester Bestandteil <spatialcontext> und <transformation>
			Element resource_metadata = (Element) resource_list.get(0);
				//Input Spatialcontext [1] in Database
			//required (name, place, date, trans_src, trans_dst, trans_param), result serialID oder -1, wenn nicht vorhanden
			SC_ID = db.inputSpatialcontextInDB(spatialcontext.getAttributeValue("id"),
					resource_metadata.getChild("spatialcontext").getChildText("place"),
					resource_metadata.getChild("spatialcontext").getChildText("date"),
					resource_metadata.getChild("transformation").getChildText("srccrs"),
					resource_metadata.getChild("transformation").getChildText("dstcrs"),
					resource_metadata.getChild("transformation").getChildText("params"),
					resource_metadata.getChild("spatialcontext").getChildText("srid"));

			// element <resource id="media"> --> mehrere Bestandteile <media>
			Element resource_media = (Element) resource_list.get(1);
			List media_list = resource_media.getChildren("media");
			for (Object media : media_list) {
				Element media_list_element = (Element) media;
				//Input Mediafile [i] in Database
				//required (SC_ID, type, filename, trans_src, trans_dst, trans_param), result serialID oder -1, wenn nicht vorhanden
				MED_ID = db.inputMediaInDB(SC_ID,
						media_list_element.getChild("data").getChildText("mediatype"),
						media_list_element.getChild("data").getChildText("filename"),
						media_list_element.getChild("data").getChildText("description"),
						media_list_element.getChild("transformation").getChildText("srccrs"),
						media_list_element.getChild("transformation").getChildText("dstcrs"),
						media_list_element.getChild("transformation").getChildText("params"));
			}

			// element <resource id="viewpoint"> --> mehrere Bestandteile <viewpoint>
			Element resource_viewpoint = (Element) resource_list.get(2);
			List viewpoint_list = resource_viewpoint.getChildren("viewpoint");
			for (Object viewpoint : viewpoint_list) {
				Element viewpoint_list_element = (Element) viewpoint;
				//Input Viewpoint [i] in Database
				//required (SC_ID, name, place, trans_src, trans_dst, trans_param), result serialID oder -1, wenn nicht vorhanden
				VP_ID = db.inputViewpointInDB(SC_ID,
						viewpoint_list_element.getAttributeValue("name"),
						viewpoint_list_element.getChild("metadata").getChildText("viewpointname"),
						viewpoint_list_element.getChild("metadata").getChild("transformation").getChildText("srccrs"),
						viewpoint_list_element.getChild("metadata").getChild("transformation").getChildText("dstcrs"),
						viewpoint_list_element.getChild("metadata").getChild("transformation").getChildText("params"));
				// element <pointclouds> --> mehrere Bestandteile <pointcloud>
				Element pointclouds = viewpoint_list_element.getChild("pointclouds");
				List pointcloud_list = pointclouds.getChildren("pointcloud");
				for (Object pointcloud : pointcloud_list) {
					Element pointcloud_list_element = (Element) pointcloud;
					//Input Pointcloud [ii] in Database
					//required (VP_ID, filename, type, bbox_local, bbox_global, bbox_polar, remissionrange, rows, cols, trans_src,
					//          trans_dst, trans_param), result serialID oder -1, wenn nicht vorhanden
					PC_ID = db.inputPointcloudInDB(VP_ID,
							pointcloud_list_element.getChild("metadata").getChildText("filename"),
							pointcloud_list_element.getChild("metadata").getChildText("type"),
							pointcloud_list_element.getChild("metadata").getChildText("bbox_local"),
							pointcloud_list_element.getChild("metadata").getChildText("bbox_global"),
							pointcloud_list_element.getChild("metadata").getChildText("bbox_polar"),
							pointcloud_list_element.getChild("metadata").getChildText("remissionrange"),
							pointcloud_list_element.getChild("metadata").getChildText("rows"),
							pointcloud_list_element.getChild("metadata").getChildText("cols"),
							pointcloud_list_element.getChild("transformation").getChildText("srccrs"),
							pointcloud_list_element.getChild("transformation").getChildText("dstcrs"),
							pointcloud_list_element.getChild("transformation").getChildText("params"));
				}
				// element <panoramas> --> mehrere Bestandteile <panorama>
				Element panoramas = viewpoint_list_element.getChild("panoramas");
				List panorama_list = panoramas.getChildren("panorama");
				for (Object panorama : panorama_list) {
					Element panorama_list_element = (Element) panorama;
					//Input Panorama [ii] in Database
					//required (VP_ID, structtype, kindof, trans_src, trans_dst, trans_param), result serialID oder -1, wenn nicht vorhanden
					PAN_ID = db.inputPanoramaInDB(VP_ID,
							panorama_list_element.getChildText("structuraltype"),
							panorama_list_element.getChildText("kindof"),
							panorama_list_element.getChild("transformation").getChildText("srccrs"),
							panorama_list_element.getChild("transformation").getChildText("dstcrs"),
							panorama_list_element.getChild("transformation").getChildText("params"));
					// element <panoramas> --> mehrere Bestandteile <panorama>
					Element images = panorama_list_element.getChild("images");
					List img_list = images.getChildren("img");
					for (int iii = 0; iii < img_list.size(); iii++) {
						Element img_list_element = (Element) img_list.get(iii);
						//Input PanoramaImage [iii] in Database
						//required (PANO_ID, imgpath, order), result serialID oder -1, wenn nicht vorhanden
						PANIMG_ID = db.inputPanoramaImgInDB(PAN_ID, img_list_element.getText(), iii);
					}
				}
			}

			StringOut = db.StringOut;
		} catch (Exception e) {
			StringOut = "<error>" + e.toString() + "</error>";
		}
		return StringOut;
	}

}

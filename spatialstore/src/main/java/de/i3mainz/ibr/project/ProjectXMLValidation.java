package de.i3mainz.ibr.project;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author s3b31293
 */
public class ProjectXMLValidation {

    public static String validation(String _xmlProject) throws JDOMException, IOException, IllegalArgumentException {

        ArrayList<String> errors = new ArrayList<>();

        SAXBuilder builder = new SAXBuilder();
        Reader in = new StringReader(_xmlProject);

        Document document = (Document) builder.build(in);
        Element genericviewer = document.getRootElement();

        //element <spatialcontext>
        Element spatialcontext = genericviewer.getChild("spatialcontext");

        // element <resources>
        Element resources = spatialcontext.getChild("resources");
        List resource_list = resources.getChildren("resource");
        // resource_list.size() ungleich 3 -> nicht valide
        if (resource_list.size() != 3) {
            throw new IllegalArgumentException("ProjectXML nicht vollstÃ¤ndig!");
        }

        Element resource_metadata = (Element) resource_list.get(0);

        //metadata Teil
        if (spatialcontext.getAttributeValue("id").isEmpty() || spatialcontext.getAttributeValue("id") == null) {
            errors.add("Kein Projektname gefunden!");
        }
        if (resource_metadata.getChild("spatialcontext").getChildText("place") == null || resource_metadata.getChild("spatialcontext").getChildText("place").isEmpty()) {
            errors.add("Keinen Projektort gefunden!");
        }
        if (resource_metadata.getChild("spatialcontext").getChildText("date") == null) {
            Element element = new Element("date");
            element.addContent("1900-01-01");
            resource_metadata.getChild("spatialcontext").addContent(element);
        }
        if (resource_metadata.getChild("transformation").getChildText("srccrs") == null) {
            Element element = new Element("srccrs");
            element.addContent("0");
            resource_metadata.getChild("transformation").addContent(element);
        }
        if (resource_metadata.getChild("transformation").getChildText("dstcrs") == null) {
            Element element = new Element("dstcrs");
            element.addContent("0");
            resource_metadata.getChild("transformation").addContent(element);
        }
        if (resource_metadata.getChild("transformation").getChildText("params") == null || resource_metadata.getChild("transformation").getChildText("params").isEmpty()) {
            errors.add("Keine Projekttransformation vorhanden");
        }
        if (resource_metadata.getChild("spatialcontext").getChildText("srid") == null) {
            Element element = new Element("srid");
            element.addContent("0");
            resource_metadata.getChild("spatialcontext").addContent(element);
        }

        //media Teil
        Element resource_media = (Element) resource_list.get(1);
        List media_list = resource_media.getChildren("media");

        if (media_list.isEmpty()) {
            errors.add("Keine Media-Daten vorhanden");
        }

        for (Object media : media_list) {
            Element media_list_element = (Element) media;

            if (media_list_element.getChild("data").getChildText("mediatype") == null) {
                Element element = new Element("mediatype");
                element.addContent("groundplan");
                media_list_element.getChild("data").addContent(element);
            }
            if (media_list_element.getChild("data").getChildText("filename") == null || media_list_element.getChild("data").getChildText("filename").isEmpty()) {
                errors.add("Kein Filename fÃ¼r Media vorhanden");
            }
            if (media_list_element.getChild("data").getChildText("description") == null) {
                Element element = new Element("description");
                element.addContent("Grundriss");
                media_list_element.getChild("data").addContent(element);
            }
            if (media_list_element.getChild("transformation").getChildText("srccrs") == null) {
                Element element = new Element("srccrs");
                element.addContent("0");
                media_list_element.getChild("transformation").addContent(element);
            }
            if (media_list_element.getChild("transformation").getChildText("dstcrs") == null) {
                Element element = new Element("dstcrs");
                element.addContent("0");
                media_list_element.getChild("transformation").addContent(element);
            }
            if (media_list_element.getChild("transformation").getChildText("params") == null || media_list_element.getChild("transformation").getChildText("params").isEmpty()) {
                errors.add("Keine Media-Transformation vorhanden (" 
                        + media_list_element.getChild("data").getChildText("description") + "). ");
            }
        }
        //viewPoint Teil
        String viewpointName = "";
        Element resource_viewpoint = (Element) resource_list.get(2);
        List viewpoint_list = resource_viewpoint.getChildren("viewpoint");
        for (Object viewpoint : viewpoint_list) {
            Element viewpoint_list_element = (Element) viewpoint;

            //meta data
            if (viewpoint_list_element.getAttributeValue("name") == null || viewpoint_list_element.getAttributeValue("name").isEmpty()) {
                errors.add("Kein Viewpoint Name vorhanden");
            } else {
                viewpointName = viewpoint_list_element.getAttributeValue("name");
            }
            if (viewpoint_list_element.getChild("metadata").getChildText("viewpointname") == null || viewpoint_list_element.getChild("metadata").getChildText("viewpointname").isEmpty()) {
                errors.add("Kein Viewpoint Anzeigename vorhanden");
            }
            if (viewpoint_list_element.getChild("metadata").getChild("transformation").getChildText("srccrs") == null) {
                Element element = new Element("srccrs");
                element.addContent("0");
                viewpoint_list_element.getChild("metadata").getChild("transformation").addContent(element);
            }
            if (viewpoint_list_element.getChild("metadata").getChild("transformation").getChildText("dstcrs") == null) {
                Element element = new Element("dstcrs");
                element.addContent("0");
                viewpoint_list_element.getChild("metadata").getChild("transformation").addContent(element);
            }
            if (viewpoint_list_element.getChild("metadata").getChild("transformation").getChildText("params") == null || viewpoint_list_element.getChild("metadata").getChild("transformation").getChildText("params").isEmpty()) {
                errors.add("Keine Viewpoint-Transformation vorhanden (Standpunkt " + viewpointName + "). ");
            }

            // element <pointclouds> --> mehrere Bestandteile <pointcloud>
            Element pointclouds = viewpoint_list_element.getChild("pointclouds");
            List pointcloud_list = pointclouds.getChildren("pointcloud");
            for (Object pointcloud : pointcloud_list) {
                Element pointcloud_list_element = (Element) pointcloud;

                //pointcloud data 
                if (pointcloud_list_element.getChild("metadata").getChildText("filename") == null || pointcloud_list_element.getChild("metadata").getChildText("filename").isEmpty()) {
                    errors.add("Kein Pointcloud-Dateiname vorhanden (Standpunkt " + viewpointName + "). ");
                }
                if (pointcloud_list_element.getChild("metadata").getChildText("type") == null) {
                    Element element = new Element("type");
                    element.addContent("");
                    pointcloud_list_element.getChild("metadata").addContent(element);
                }
                if (pointcloud_list_element.getChild("metadata").getChildText("bbox_local") == null) {
                    Element element = new Element("bbox_local");
                    element.addContent("0,0,0,0");
                    pointcloud_list_element.getChild("metadata").addContent(element);
                }
                if (pointcloud_list_element.getChild("metadata").getChildText("bbox_global") == null) {
                    Element element = new Element("bbox_global");
                    element.addContent("0,0,0,0");
                    pointcloud_list_element.getChild("metadata").addContent(element);
                }
                if (pointcloud_list_element.getChild("metadata").getChildText("bbox_polar") == null) {
                    Element element = new Element("bbox_polar");
                    element.addContent("0,0,0,0");
                    pointcloud_list_element.getChild("metadata").addContent(element);
                }
                if (pointcloud_list_element.getChild("metadata").getChildText("remissionrange") == null) {
                    Element element = new Element("remissionrange");
                    element.addContent("0,0");
                    pointcloud_list_element.getChild("metadata").addContent(element);
                }
                if (pointcloud_list_element.getChild("metadata").getChildText("rows") == null) {
                    Element element = new Element("rows");
                    element.addContent("0");
                    pointcloud_list_element.getChild("metadata").addContent(element);
                }
                if (pointcloud_list_element.getChild("metadata").getChildText("cols") == null) {
                    Element element = new Element("clos");
                    element.addContent("0");
                    pointcloud_list_element.getChild("metadata").addContent(element);
                }
                if (pointcloud_list_element.getChild("transformation").getChildText("srccrs") == null) {
                    Element element = new Element("srccrs");
                    element.addContent("0");
                    pointcloud_list_element.getChild("transformation").addContent(element);
                }
                if (pointcloud_list_element.getChild("transformation").getChildText("dstcrs") == null) {
                    Element element = new Element("dstcrs");
                    element.addContent("0");
                    pointcloud_list_element.getChild("transformation").addContent(element);
                }
                if (pointcloud_list_element.getChild("transformation").getChildText("params") == null) {
                    errors.add("Keine Pointcloud-Transformation vorhanden (Standpunkt " + viewpointName + "). ");
                }
            }
            // element <panoramas> --> mehrere Bestandteile <panorama>
            Element panoramas = viewpoint_list_element.getChild("panoramas");
            List panorama_list = panoramas.getChildren("panorama");
            for (Object panorama : panorama_list) {
                Element panorama_list_element = (Element) panorama;

                //panorama 
                if (panorama_list_element.getChildText("structuraltype") == null) {
                    Element element = new Element("structuraltype");
                    element.addContent("");
                    panorama_list_element.addContent(element);
                }
                if (panorama_list_element.getChildText("kindof") == null) {
                    Element element = new Element("kindof");
                    element.addContent("");
                    panorama_list_element.addContent(element);
                }
                if (panorama_list_element.getChild("transformation").getChildText("srccrs") == null) {
                    Element element = new Element("srccrs");
                    element.addContent("0");
                    panorama_list_element.getChild("transformation").addContent(element);
                }
                if (panorama_list_element.getChild("transformation").getChildText("dstcrs") == null) {
                    Element element = new Element("dstcrs");
                    element.addContent("0");
                    panorama_list_element.getChild("transformation").addContent(element);
                }
                if (panorama_list_element.getChild("transformation").getChildText("params") == null) {
                    
                    errors.add("Keine Panoramatransformation vorhanden (Standpunkt " + viewpointName 
                            + ", " + panorama_list_element.getChildText("kindof")+ "). ");
                }
                // element <panoramas> --> mehrere Bestandteile <panorama>
                Element images = panorama_list_element.getChild("images");
                List img_list = images.getChildren("img");
                if (img_list.size() != 6) {
                    errors.add("Nicht alle Bilder vorhanden für " + viewpointName + ". ");
                }

                int imageIndex = 0;
                for (Object img : img_list) {
                    imageIndex++;
                    Element img_list_element = (Element) img;
                    if (img_list_element.getText() == null || img_list_element.getText().isEmpty()) {
                        errors.add("Bild " + imageIndex + " nicht vorhanden (Standpunkt " + viewpointName + "). ");
                    }
                }
            }
        }
        if (!errors.isEmpty()) {
            String error = "";
            for (String s : errors) {
                error += s + "\n";
            }
            throw new IllegalArgumentException(error);
        }
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat().setEncoding("iso-8859-15"));
        return out.outputString(document);
    }
}

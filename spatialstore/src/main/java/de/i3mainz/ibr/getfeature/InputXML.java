package de.i3mainz.ibr.getfeature;

import de.i3mainz.ibr.geometry.Angle;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class InputXML {
	
	private Angle[] angles = null;
	private String type = null;
	private int panorama = 0;
	private double zoom = 0;
	private int width = 0;
	private int height = 0;
	private String creator = null;
	private String viewpoint = null;
	
	public InputXML(String xml, String creator) throws JDOMException, IOException {
		Reader in = new StringReader(xml);
		SAXBuilder builder = new SAXBuilder();
		Element root = ((Document) (builder.build(in))).getRootElement();

		List pointslist = root.getChildren("points");
		Element pointslistnode = (Element) pointslist.get(0);
		List pointlist = pointslistnode.getChildren("point");
		angles = new Angle[pointlist.size()];
		for (int i=0; i<angles.length; i++) {
			Element pointelement = (Element)pointlist.get(i);
			angles[i] = new Angle(Double.parseDouble(pointelement.getChildText("azim")),Double.parseDouble(pointelement.getChildText("elev")));
		}
		type = root.getChildText("geomtype");
		panorama = Integer.parseInt(root.getChildText("panorama"));
		zoom = Double.parseDouble(root.getChildText("zoom"));
		width = Integer.parseInt(root.getChildText("width"));
		height = Integer.parseInt(root.getChildText("height"));
		this.creator = creator;
	}
	
	public Angle[] getAngles() {
		return angles;
	}
	
	public String getType() {
		return type;
	}
	
	public int getPanorama() {
		return panorama;
	}
	
	public double getZoom() {
		return zoom;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public String getCreator() {
		return creator;
	}
	
	public String getViewpoint() {
		return viewpoint;
	}
	
}
